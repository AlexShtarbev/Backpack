package org.alexshtarbev.backpack;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/backpack")
class BackpackController {
	private final ChatClient openAiChatClient;

	public BackpackController(@Name(BackpackConfig.OPEN_AI_CHAT_CLIENT) ChatClient openAiChatClient) {
		this.openAiChatClient = openAiChatClient;
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
