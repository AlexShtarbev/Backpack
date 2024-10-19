package org.alexshtarbev.backpack;

import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class BackpackConfig {

    public static final String OPEN_AI_CHAT_CLIENT = "OPEN_AI_CHAT_CLIENT";
    public static final String OPEN_AI_TEXT_EMBEDDING_3_TEXT_SMALL = "text-embedding-3-small";

    @Bean
    public OpenAiApi getOpenAiApi(OpenAiConnectionProperties openAiConnectionProperties) {
        return new OpenAiApi(openAiConnectionProperties.getApiKey());
    }

    @Bean
    public OpenAiChatModel getOpenAiChatModel(OpenAiApi openAiApi) {
        return new OpenAiChatModel(openAiApi);
    }

    @Bean(OPEN_AI_CHAT_CLIENT)
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.create(openAiChatModel);
    }

    @Bean(OPEN_AI_TEXT_EMBEDDING_3_TEXT_SMALL)
    public EmbeddingModel getOpenAiEmbeddingModel(OpenAiApi openAiApi) {
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, OpenAiEmbeddingOptions.builder()
                .withModel(OpenAiApi.EmbeddingModel.TEXT_EMBEDDING_3_SMALL.value)
                .build());
    }

}
