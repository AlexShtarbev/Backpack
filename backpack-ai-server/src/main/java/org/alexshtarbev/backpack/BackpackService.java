package org.alexshtarbev.backpack;

import static org.alexshtarbev.backpack.conifg.BackpackConfig.OPEN_AI_TEXT_EMBEDDING_3_TEXT_SMALL;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.alexshtarbev.backpack.model.BackpackParagraph;
import org.alexshtarbev.bacpack.tables.daos.ContentDao;
import org.alexshtarbev.bacpack.tables.daos.EmbeddingDao;
import org.alexshtarbev.bacpack.tables.pojos.Content;
import org.alexshtarbev.bacpack.tables.pojos.Embedding;
import org.alexshtarbev.bacpack.tables.records.ContentRecord;
import org.alexshtarbev.bacpack.tables.records.EmbeddingRecord;
import org.backpack.jooq.pgvector.Vector;
import org.jooq.DSLContext;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BackpackService {

  private final ObjectMapper objectMapper;
  private final ContentDao contentDao;
  private final EmbeddingDao embeddingDao;
  private final EmbeddingModel openAiEmbeddingModel;

  public BackpackService(
          ObjectMapper objectMapper,
          ContentDao contentDao,
          EmbeddingDao embeddingDao,
          @Name(OPEN_AI_TEXT_EMBEDDING_3_TEXT_SMALL) EmbeddingModel openAiEmbeddingModel) {
    this.objectMapper = objectMapper;
      this.contentDao = contentDao;
      this.openAiEmbeddingModel = openAiEmbeddingModel;
    this.embeddingDao = embeddingDao;
  }

  public EmbeddingResponse getEmbeddingsForBackpackParagraph() {
    List<BackpackParagraph> paragraphResponse = readFileIntoParagraphRecord();
    BackpackParagraph firstParagraph = paragraphResponse.get(0);
    return getEmbeddingResponse(firstParagraph);
  }

  public EmbeddingResponse getEmbeddingResponse(BackpackParagraph firstParagraph) {
    return openAiEmbeddingModel.embedForResponse(List.of(firstParagraph.content()));
  }

  public List<BackpackParagraph> readFileIntoParagraphRecord() {
    InputStream hospitalityJson =
        getClass().getClassLoader().getResourceAsStream("test_files/hospitality.json");

    try {
      return objectMapper.readValue(hospitalityJson, new TypeReference<>() {});
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse");
    }
  }

  public void upsertEmbeddingRecords(EmbeddingResponse embeddingResponse, List<BackpackParagraph> paragraphs) {
    for (int i = 0; i < paragraphs.size(); i++) {
      var paragraph = paragraphs.get(i);
      var embedding = embeddingResponse.getResults().get(i);
      var contentId = UUID.fromString("98c30b8f-1ca5-445b-9093-ea3532414bcd"); // UUID.randomUUID(); // FIXME

      var contentDbObject = new Content(contentId, paragraph.content(), paragraph.summary(), paragraph.context());
      var embeddingDbObject = new Embedding(contentId, new Vector(embedding.getOutput()));

//      contentDao.merge(contentDbObject);
      embeddingDao.merge(embeddingDbObject);
    }
  }
}
