package org.alexshtarbev.backpack.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.alexshtarbev.backpack.model.BackpackEmbeddingParagraphs;
import org.alexshtarbev.backpack.openai.OpenAiVerboseJsonResponse;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TranscriptionStitchService {
    private static final String TRANSCRIPTION_FILE_PATH = "test_files/anger_transcribe_formatted.json";
    private static final String PARAGRAPHS_FILE_PATH = "test_files/anger_text_split_for_embedding.json";

    private final ObjectMapper objectMapper;

    public TranscriptionStitchService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<SegmentAndParagraph> stitch() {
        var transcriptionResponse = readJsonFile(TRANSCRIPTION_FILE_PATH, new TypeReference<OpenAiVerboseJsonResponse>() {});
        var paragraphs = readJsonFile(PARAGRAPHS_FILE_PATH, new TypeReference<BackpackEmbeddingParagraphs>() {});

        int paragraphsIndex = 0;
        int paragraphOffset = 0;
        int segmentsIndex = 0;
        List<SegmentAndParagraph> segmentsPerParagraph = new ArrayList<>();

        while (paragraphsIndex < paragraphs.paragraphs().size()) {
            var paragraph = paragraphs.paragraphs().get(paragraphsIndex);
            var paragraphTextCharArray = paragraph.trim().toCharArray();
            var segmentsList = new ArrayList<OpenAiVerboseJsonResponse.Segment>();
            int segmentTextOffset = 0;

            while (segmentsIndex < transcriptionResponse.segments().size()) {
                var segment = transcriptionResponse.segments().get(segmentsIndex);
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

            segmentsPerParagraph.add(new SegmentAndParagraph(segmentsList, paragraph));
        }

        return segmentsPerParagraph;
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
}
