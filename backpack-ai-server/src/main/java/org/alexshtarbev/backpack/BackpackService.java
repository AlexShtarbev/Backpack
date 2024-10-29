package org.alexshtarbev.backpack;

import static org.alexshtarbev.backpack.conifg.BackpackConfig.OPEN_AI_TEXT_EMBEDDING_3_TEXT_SMALL;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.alexshtarbev.backpack.model.BackpackParagraph;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.stereotype.Component;

@Component
public class BackpackService {

  private final ObjectMapper objectMapper;
  private final EmbeddingModel openAiEmbeddingModel;

  public BackpackService(
      ObjectMapper objectMapper,
      @Name(OPEN_AI_TEXT_EMBEDDING_3_TEXT_SMALL) EmbeddingModel openAiEmbeddingModel) {
    this.objectMapper = objectMapper;
    this.openAiEmbeddingModel = openAiEmbeddingModel;
  }

  public EmbeddingResponse getEmbeddingsForBackpackParagraph() {
    List<BackpackParagraph> paragraphResponse = readFileIntoParagraphRecord();
    BackpackParagraph firstParagraph = paragraphResponse.get(0);
    return openAiEmbeddingModel.embedForResponse(List.of(firstParagraph.content()));
  }

  private List<BackpackParagraph> readFileIntoParagraphRecord() {
    InputStream hospitalityJson =
        getClass().getClassLoader().getResourceAsStream("test_files/hospitality.json");

    try {
      return objectMapper.readValue(hospitalityJson, new TypeReference<>() {});
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse");
    }
  }
}
