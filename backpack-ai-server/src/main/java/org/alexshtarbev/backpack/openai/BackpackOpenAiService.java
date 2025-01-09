package org.alexshtarbev.backpack.openai;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.alexshtarbev.backpack.conifg.BackpackConfig;
import org.apache.commons.io.FileUtils;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class BackpackOpenAiService {

  public static final OpenAiAudioApi.TranscriptResponseFormat OPEN_AI_TRANSCRIPTION_FORMAT =
      OpenAiAudioApi.TranscriptResponseFormat.VTT;

  public static final String OPEN_AI_TRANSCRIPTION_LANGUAGE = "en";

  private final ChatClient openAiChatClient;
  private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

  public BackpackOpenAiService(
      @Name(BackpackConfig.OPEN_AI_CHAT_CLIENT) ChatClient openAiChatClient,
      @Name(BackpackConfig.OPEN_AI_CHAT_TRANSCRIPTION_MODEL)
          OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel) {

    this.openAiChatClient = openAiChatClient;
    this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
  }

  public void transcribeAndStore(String inputFilePath, String destinationFilePath) {
    FileSystemResource audioFile = new FileSystemResource(inputFilePath);
    AudioTranscriptionResponse response = getTranscription(audioFile);
    AudioTranscription audioTranscription = response.getResult();

    try {
      FileUtils.writeStringToFile(
          new File(destinationFilePath), audioTranscription.getOutput(), StandardCharsets.UTF_8);

    } catch (IOException ex) {
      throw new RuntimeException(
          String.format(
              "Failed to write to destination transcription file %s", destinationFilePath),
          ex);
    }
  }

  public AudioTranscriptionResponse getTranscription(FileSystemResource audioFile) {
    OpenAiAudioTranscriptionOptions transcriptionOptions =
        OpenAiAudioTranscriptionOptions.builder()
            .language(OPEN_AI_TRANSCRIPTION_LANGUAGE)
            //                .withPrompt("Ask not this, but ask that")
            .temperature(0f)
            .responseFormat(OPEN_AI_TRANSCRIPTION_FORMAT)
            .build();

    AudioTranscriptionPrompt transcriptionRequest =
        new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
    return openAiAudioTranscriptionModel.call(transcriptionRequest);
  }
}
