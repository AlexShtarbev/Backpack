package org.alexshtarbev.backpack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.alexshtarbev.backpack.conifg.BackpackConfig;
import org.alexshtarbev.backpack.download.BackpackYoutubeAudioDownloader;
import org.alexshtarbev.backpack.model.BackpackParagraph;
import org.alexshtarbev.backpack.model.ContentEmbeddingResponse;
import org.alexshtarbev.backpack.openai.BackpackOpenAiService;
import org.alexshtarbev.backpack.openai.BackpackTranscribeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backpack")
class BackpackController {
  private static final Logger logger = LoggerFactory.getLogger(BackpackController.class);

  private final ChatClient openAiChatClient;
  private final BackpackService backpackService;
  private final BackpackOpenAiService openAiService;
  private final BackpackYoutubeAudioDownloader backpackYoutubeAudioDownloader;

  public BackpackController(
          BackpackOpenAiService openAiService,
          BackpackService backpackService,
          BackpackYoutubeAudioDownloader backpackYoutubeAudioDownloader,
          @Name(BackpackConfig.OPEN_AI_CHAT_CLIENT) ChatClient openAiChatClient) {
    this.openAiChatClient = openAiChatClient;
    this.openAiService = openAiService;
    this.backpackService = backpackService;
      this.backpackYoutubeAudioDownloader = backpackYoutubeAudioDownloader;
  }

  @GetMapping("/embed/file/query")
  EmbeddingResponse getEmbedding() {
    return backpackService.getEmbeddingsForBackpackParagraph();
  }

  @GetMapping("/download")
  void log(@RequestParam(value = "url") String url, @RequestParam(value = "fileName") String fileName) {
    backpackYoutubeAudioDownloader.downloadYoutubeVideoAsAudioMp3(url, fileName);
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

  @PostMapping("/embed/store")
  void embedStore(@RequestBody BackpackTranscribeRequest request) {
    List<BackpackParagraph> paragraphResponse = backpackService.readFileIntoParagraphRecord();
    BackpackParagraph firstParagraph = paragraphResponse.get(0);
    var embeddingResponse = backpackService.getEmbeddingResponse(firstParagraph);
    backpackService.upsertEmbeddingRecords(embeddingResponse, List.of(firstParagraph));
  }

  @GetMapping("/embed/query")
  ContentEmbeddingResponse getContentEmbedding(@RequestParam(value = "contentId") String contentId) {
    return backpackService.getContentEmbedding(UUID.fromString(contentId));
  }

  @GetMapping("/search/query")
  List<ContentEmbeddingResponse> searchByQuery(@RequestParam(value = "query") String query) {
    return backpackService.fetchNearestVectorByCosineDistance(query,5);
  }
}
