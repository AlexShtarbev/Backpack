package org.alexshtarbev.backpack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/backpack")
class BackpackController {
	private static final Logger logger = LoggerFactory.getLogger(BackpackController.class);

	private final ChatClient openAiChatClient;
	private final BackpackService backpackService;

	public BackpackController(@Name(BackpackConfig.OPEN_AI_CHAT_CLIENT) ChatClient openAiChatClient,
                                  BackpackService backpackService) {
		this.openAiChatClient = openAiChatClient;
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
	Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		return Map.of(
				"completion",
				openAiChatClient.prompt()
						.user(message)
						.call()
						.content());
	}
}
