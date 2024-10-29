package org.alexshtarbev.backpack;

import java.util.Map;
import org.alexshtarbev.backpack.conifg.BackpackConfig;
import org.alexshtarbev.backpack.openai.BackpackOpenAiService;
import org.alexshtarbev.backpack.openai.BackpackTranscribeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/backpack")
class BackpackController {
  private static final Logger logger = LoggerFactory.getLogger(BackpackController.class);

  private final ChatClient openAiChatClient;
  private final BackpackService backpackService;

  private final BackpackOpenAiService openAiService;

  public BackpackController(
      BackpackOpenAiService openAiService,
      BackpackService backpackService,
      @Name(BackpackConfig.OPEN_AI_CHAT_CLIENT) ChatClient openAiChatClient) {
    this.openAiChatClient = openAiChatClient;
    this.openAiService = openAiService;
    this.backpackService = backpackService;
  }

  @GetMapping("/embed")
  EmbeddingResponse getEmbedding() {
    return backpackService.getEmbeddingsForBackpackParagraph();
  }

  @GetMapping("/log")
  void log(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
    logger.info("{}", message);
  }

  @GetMapping("/message")
  Map<String, String> completion(
      @RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
    return Map.of("completion", openAiChatClient.prompt().user(message).call().content());
  }

  @PostMapping("/transcribe")
  void transcribe(@RequestBody BackpackTranscribeRequest request) {
    openAiService.transcribeAndStore(request.inputFilePath(), request.destinationFilePath());
  }
}
