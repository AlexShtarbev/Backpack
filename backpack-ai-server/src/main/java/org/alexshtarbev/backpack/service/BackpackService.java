package org.alexshtarbev.backpack.service;

import static org.alexshtarbev.backpack.conifg.BackpackConfig.OPEN_AI_TEXT_EMBEDDING_3_TEXT_SMALL;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.alexshtarbev.backpack.conifg.BackpackApplicationConfig;
import org.alexshtarbev.backpack.conifg.BackpackApplicationDownloadsConfig;
import org.alexshtarbev.backpack.model.BackpackEmbeddingParagraphs;
import org.alexshtarbev.backpack.model.BackpackParagraph;
import org.alexshtarbev.backpack.model.BackpackTranscribeRequest;
import org.alexshtarbev.backpack.openai.BackpackOpenAiService;
import org.alexshtarbev.backpack.openai.OpenAiVerboseJsonResponse;
import org.alexshtarbev.bacpack.tables.daos.TranscriptionDao;
import org.alexshtarbev.bacpack.tables.daos.TranscriptionParagraphDao;
import org.alexshtarbev.bacpack.tables.daos.TranscriptionParagraphEmbeddingDao;
import org.alexshtarbev.bacpack.tables.pojos.Transcription;
import org.alexshtarbev.bacpack.tables.pojos.TranscriptionParagraph;
import org.alexshtarbev.bacpack.tables.pojos.TranscriptionParagraphEmbedding;
import org.backpack.jooq.pgvector.Vector;
import org.jooq.JSONB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BackpackService {
  private static final Logger LOGGER = LoggerFactory.getLogger(BackpackService.class);

  private static final String EMBEDDING_PARAGRAPHS_PROMPT= """
    I need you to help me with splitting text into paragraphs so that I can embed it for RAG. 
    I am going to present the text and then ask you to split it and return the result as JSON. 
    The JSON should be formatted as such:
    {
            "paragraphs: []
    }
    and nothing else should be in the response.
    and the text is: %s
  """;

  private final BackpackApplicationDownloadsConfig downloadsConfig;
  private final BackpackYoutubeAudioDownloader backpackYoutubeAudioDownloader;
  private final BackpackOpenAiService backpackOpenAiService;
  private final ObjectMapper objectMapper;

  private final TranscriptionDao transcriptionDao;
  private final TranscriptionParagraphDao paragraphDao;
  private final TranscriptionParagraphEmbeddingDao embeddingDao;
  private final EmbeddingModel openAiEmbeddingModel;

  public BackpackService(
          BackpackApplicationConfig configRecord,
          BackpackYoutubeAudioDownloader backpackYoutubeAudioDownloader,
          BackpackOpenAiService backpackOpenAiService,
          ObjectMapper objectMapper,
          TranscriptionDao transcriptionDao,
          TranscriptionParagraphDao paragraphDao,
          TranscriptionParagraphEmbeddingDao embeddingDao,
          @Name(OPEN_AI_TEXT_EMBEDDING_3_TEXT_SMALL) EmbeddingModel openAiEmbeddingModel) {

    this.downloadsConfig = configRecord.download();
    this.backpackYoutubeAudioDownloader = backpackYoutubeAudioDownloader;
    this.backpackOpenAiService = backpackOpenAiService;
    this.objectMapper = objectMapper;
    this.transcriptionDao = transcriptionDao;
    this.paragraphDao = paragraphDao;
    this.embeddingDao = embeddingDao;
    this.openAiEmbeddingModel = openAiEmbeddingModel;
  }

  public List<BackpackParagraph> downloadAndTranscribe(BackpackTranscribeRequest request) {
    String audioFilePath = getFilePath(request.audioFileName());
    if (request.shouldDownload() || !(new File(audioFilePath).exists())) {
      backpackYoutubeAudioDownloader.downloadYoutubeVideoAsAudioMp3(
              request.url(), audioFilePath, downloadsConfig.cookiesFilePath());
    }

    var transcription = transcriptionDao.fetchOneByUrl(request.url());
    if (transcription == null) {
      return getTranscriptionAndEmbed(request, audioFilePath);
    } else {
      return getParagraphsFromDatabase(request);
    }
  }

  private List<BackpackParagraph> getTranscriptionAndEmbed(BackpackTranscribeRequest request, String audioFilePath) {
    OpenAiVerboseJsonResponse response = backpackOpenAiService.getTranscription(new FileSystemResource(audioFilePath));
    transcriptionDao.merge(new Transcription(request.url(), response.text()));
//    var response = readJsonFile("test_files/anger_transcribe_formatted.json", new TypeReference<OpenAiVerboseJsonResponse>() {});
    List<OpenAiVerboseJsonResponse.Segment> transcriptionSegments = response.segments();

    // TODO - handle large text
    String paragraphsForEmbedding =
            backpackOpenAiService.promptAndGetText(String.format(EMBEDDING_PARAGRAPHS_PROMPT, response.text()));

    BackpackEmbeddingParagraphs backpackEmbeddingParagraphs;
    try {
      paragraphsForEmbedding = paragraphsForEmbedding.replace("```json", "")
              .replace("```", "");
      backpackEmbeddingParagraphs = objectMapper.readValue(paragraphsForEmbedding, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      throw new RuntimeException("Failed to parse ChatGPT embedding paragraph output", ex);
    }
    var backpackParagraphs = mergeParagraphsWithSegments(request.url(), transcriptionSegments, backpackEmbeddingParagraphs);
    var transcriptionParagraphs =  upsertParagraphRecords(backpackParagraphs);

    var embeddingResponse = getEmbeddingResponse(backpackParagraphs);
    var embeddings = embeddingResponse.getResults();

    var transcriptionParagraphEmbeddings = new ArrayList<TranscriptionParagraphEmbedding>();
    for (int i = 0; i < embeddings.size(); i++) {
      var transcriptionParagraph = transcriptionParagraphs.get(i);
      var embedding = embeddings.get(i);
      transcriptionParagraphEmbeddings.add(
              new TranscriptionParagraphEmbedding(transcriptionParagraph.getId(), new Vector(embedding.getOutput())));
    }
    embeddingDao.merge(transcriptionParagraphEmbeddings);

    return backpackParagraphs;
  }

  public <T> T readJsonFile(String filePath, TypeReference<T> typeReference) {
    InputStream hospitalityJson =
            getClass().getClassLoader().getResourceAsStream(filePath);

    try {
      return objectMapper.readValue(hospitalityJson, typeReference);
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse");
    }
  }

  private List<BackpackParagraph> getParagraphsFromDatabase(BackpackTranscribeRequest request) {
    List<TranscriptionParagraph> paragraphs = paragraphDao.fetchByUrl(request.url());
    return paragraphs.stream().map(t -> {
      try {
        return new BackpackParagraph(
                t.getUrl(),
                t.getText(),
                objectMapper.readValue(t.getSegments().data(), new TypeReference<>() {
                }));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }).toList();
  }

  private String getFilePath(String fileName) {
    return Paths.get(downloadsConfig.downloadsDirectory(), fileName).toString();
  }

  public EmbeddingResponse getEmbeddingResponse(BackpackParagraph firstParagraph) {
    return openAiEmbeddingModel.embedForResponse(List.of(firstParagraph.text()));
  }

  public EmbeddingResponse getEmbeddingResponse(List<BackpackParagraph> paragraphs) {
    List<String> paragraphTextList = paragraphs.stream().map(BackpackParagraph::text).toList();
    return openAiEmbeddingModel.embedForResponse(paragraphTextList);
  }

  public ArrayList<TranscriptionParagraph> upsertParagraphRecords(List<BackpackParagraph> paragraphs) {
    var paragraphsToUpsert = new ArrayList<TranscriptionParagraph>();

    for (BackpackParagraph paragraph : paragraphs) {
      TranscriptionParagraph paragraphRecord;
        try {
            paragraphRecord = new TranscriptionParagraph(
                    UUID.randomUUID(),
                    paragraph.url(),
                    paragraph.text(),
                    JSONB.valueOf(objectMapper.writeValueAsString(paragraph.segments())));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to map paragraph to JSONB object", ex);
        }
        paragraphsToUpsert.add(paragraphRecord);
    }

    paragraphDao.merge(paragraphsToUpsert);

    return paragraphsToUpsert;
  }


//  public List<BackpakParagrpahEmbeddingResponse> fetchNearestVectorByCosineDistance(String query, int limit) {
//    var response = openAiEmbeddingModel.embed(query);
//    var result = embeddingDao.fetchNearestVectorByCosineDistance(
//            Tables.TRANSCRIPTION_PARAGRAPH_EMBEDDING.EMBEDDINGS, new Vector(response), limit);
//
//    if (result.isEmpty()) {
//      return List.of();
//    }
//
//    return result.stream()
//            .map(this::convertToParagraphEmbeddingResponse)
//            .filter(Optional::isPresent)
//            .map(Optional::get)
//            .toList();
//  }
//
//  public Optional<BackpakParagrpahEmbeddingResponse> convertToParagraphEmbeddingResponse(Object o) {
//      var embedding = (TranscriptionParagraphEmbedding) o;
//      var paragraph = Optional.ofNullable(paragraphDao.fetchOneByUrl(embedding.getUrl()));
//
//      if (paragraph.isEmpty()) {
//          LOGGER.warn("Embedding with Id {} has no equivalent paragraph", embedding.getUrl());
//          return Optional.empty();
//      }
//
//      List<OpenAiVerboseJsonResponse.Segment> segments = getSegmentsFromParagraph(paragraph.get().getSegments());
//      return Optional.of(new BackpakParagrpahEmbeddingResponse(paragraph.get().getId(),
//                                                               paragraph.get().getText(),
//                                                               segments,
//                                                               embedding.getEmbeddings().vectors()));
//  }

  private List<OpenAiVerboseJsonResponse.Segment> getSegmentsFromParagraph(JSONB segmentsJsonb) {
    List<OpenAiVerboseJsonResponse.Segment> segments;
    try {
        segments = objectMapper.readValue(segmentsJsonb.data(), new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
        throw new RuntimeException("Failed to convert segments for paragraph", ex);
    }
    return segments;
  }

  public List<BackpackParagraph> mergeParagraphsWithSegments(String url,
          List<OpenAiVerboseJsonResponse.Segment> segments, BackpackEmbeddingParagraphs paragraphs) {

    int paragraphsIndex = 0;
    int paragraphOffset = 0;
    int segmentsIndex = 0;
    var segmentsPerParagraph = new ArrayList<BackpackParagraph>();

    while (paragraphsIndex < paragraphs.paragraphs().size()) {
      var paragraph = paragraphs.paragraphs().get(paragraphsIndex);
      var paragraphTextCharArray = paragraph.trim().toCharArray();
      var segmentsList = new ArrayList<OpenAiVerboseJsonResponse.Segment>();
      int segmentTextOffset = 0;

      while (segmentsIndex < segments.size()) {
        var segment = segments.get(segmentsIndex);
        var segmentText = segment.text().trim();
        var segmentTextCharArray = segmentText.toCharArray();

        while(segmentTextOffset < segmentTextCharArray.length
              && paragraphOffset < paragraphTextCharArray.length
              && segmentTextCharArray[segmentTextOffset] == paragraphTextCharArray[paragraphOffset]) {

          segmentTextOffset++;
          paragraphOffset++;
        }

        if (segmentTextOffset == segmentTextCharArray.length) {
          segmentsList.add(segment);
          segmentTextOffset = 0;
          segmentsIndex++;
        }

        if (paragraphOffset == paragraphTextCharArray.length) {
          if (segmentTextOffset > 0) {
            segmentsList.add(segment);
          }
          paragraphOffset = 0;
          paragraphsIndex++;
          break;
        } else {
          if (paragraphTextCharArray[paragraphOffset] == ' ') {
            paragraphOffset++;
          }
        }
      }

      segmentsPerParagraph.add(new BackpackParagraph(url, paragraph, segmentsList));
    }

    return segmentsPerParagraph;
  }
}
