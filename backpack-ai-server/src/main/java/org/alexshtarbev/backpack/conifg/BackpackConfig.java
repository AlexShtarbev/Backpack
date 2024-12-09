package org.alexshtarbev.backpack.conifg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.alexshtarbev.bacpack.tables.daos.ContentDao;
import org.alexshtarbev.bacpack.tables.daos.EmbeddingDao;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackpackConfig {

  public static final String OPEN_AI_CHAT_CLIENT = "OPEN_AI_CHAT_CLIENT";
  public static final String OPEN_AI_CHAT_TRANSCRIPTION_MODEL = "OPEN_AI_CHAT_TRANSCRIPTION_MODEL";
  public static final String OPEN_AI_TEXT_EMBEDDING_3_TEXT_SMALL = "text-embedding-3-small";
  public static final String HIKARI_DSL_CONTEXT = "HIKARI_DSL_CONTEXT";

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

  @Bean(OPEN_AI_TEXT_EMBEDDING_3_TEXT_SMALL)
  public EmbeddingModel getOpenAiEmbeddingModel(OpenAiApi openAiApi) {
    return new OpenAiEmbeddingModel(
        openAiApi,
        MetadataMode.EMBED,
        OpenAiEmbeddingOptions.builder()
            .withModel(OpenAiApi.EmbeddingModel.TEXT_EMBEDDING_3_SMALL.value)
            .build());
  }

  @Bean(HIKARI_DSL_CONTEXT)
  public DSLContext getHikariDslContext(BackpackApplicationConfigRecord applicationConfigRecord)
      throws SQLException {
    return DSL.using(getHikariDataSource(applicationConfigRecord), SQLDialect.POSTGRES);
  }

  @Bean
  public DataSource getGlobalDataSource(BackpackApplicationConfigRecord applicationConfigRecord) {
    return getHikariDataSource(applicationConfigRecord);
  }

  @Bean
  @LiquibaseDataSource
  public DataSource getLiquibaseDataSource(
      BackpackApplicationConfigRecord applicationConfigRecord) {
    return getHikariDataSource(applicationConfigRecord);
  }

  @Bean
  public JdbcConnectionDetails jdbcConnectionDetails(
      BackpackApplicationConfigRecord backpackApplicationConfigRecord) {
    return new JdbcConnectionDetails() {
      @Override
      public String getJdbcUrl() {
        return backpackApplicationConfigRecord.datasource().url();
      }

      @Override
      public String getUsername() {
        return backpackApplicationConfigRecord.datasource().username();
      }

      @Override
      public String getPassword() {
        return backpackApplicationConfigRecord.datasource().password();
      }
    };
  }

  private HikariDataSource getHikariDataSource(
      BackpackApplicationConfigRecord applicationConfigRecord) {
    return new HikariDataSource(getHikariConfig(applicationConfigRecord));
  }

  public HikariConfig getHikariConfig(BackpackApplicationConfigRecord applicationConfigRecord) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(applicationConfigRecord.datasource().url());
    config.setUsername(applicationConfigRecord.datasource().username());
    config.setPassword(applicationConfigRecord.datasource().password());
    config.setMaximumPoolSize(applicationConfigRecord.datasource().hikari().maximumPoolSize());
    config.setConnectionTimeout(applicationConfigRecord.datasource().hikari().maxLifetime());
    config.setPoolName(applicationConfigRecord.datasource().hikari().poolName());
    config.setDriverClassName(org.postgresql.Driver.class.getName());
    return config;
  }

  @Bean
  public ObjectMapper getObjectMapper() {
    return new ObjectMapper();
  }

  @Bean
  @Autowired
  public ContentDao getContentDao(DSLContext dslContext) {
    return new ContentDao(dslContext.configuration());
  }

  @Bean
  @Autowired
  public EmbeddingDao getEmbeddingDao(DSLContext dslContext) {
    return new EmbeddingDao(dslContext.configuration());
  }
}
