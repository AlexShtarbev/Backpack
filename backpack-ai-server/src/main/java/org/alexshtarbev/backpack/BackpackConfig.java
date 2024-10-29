package org.alexshtarbev.backpack;

import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackpackConfig {

  public static final String OPEN_AI_CHAT_CLIENT = "OPEN_AI_CHAT_CLIENT";
  public static final String OPEN_AI_CHAT_TRANSCRIPTION_MODEL = "OPEN_AI_CHAT_TRANSCRIPTION_MODEL";

  @Bean
  public OpenAiApi getOpenAiApi(OpenAiConnectionProperties openAiConnectionProperties) {
    return new OpenAiApi(openAiConnectionProperties.getApiKey());
  }

  @Bean
  public OpenAiAudioApi getOpenAiAudioApi(OpenAiConnectionProperties openAiConnectionProperties) {
    return new OpenAiAudioApi(openAiConnectionProperties.getApiKey());
  }

  @Bean
  public OpenAiChatModel getOpenAiChatModel(OpenAiApi openAiApi) {
    return new OpenAiChatModel(openAiApi);
  }

  @Bean(OPEN_AI_CHAT_CLIENT)
  public ChatClient getChatClient(OpenAiChatModel openAiChatModel) {
    return ChatClient.create(openAiChatModel);
  }

  @Bean(OPEN_AI_CHAT_TRANSCRIPTION_MODEL)
  public OpenAiAudioTranscriptionModel getOpenAiAudioTranscriptionModel(
      OpenAiAudioApi openAiAudioApi) {
    return new OpenAiAudioTranscriptionModel(openAiAudioApi);
  }
}
