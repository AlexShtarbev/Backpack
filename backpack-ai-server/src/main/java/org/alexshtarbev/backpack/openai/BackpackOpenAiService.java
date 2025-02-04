package org.alexshtarbev.backpack.openai;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BackpackOpenAiService {

  private static final RetryTemplate RETRY_TEMPLATE = RetryUtils.DEFAULT_RETRY_TEMPLATE;
  public static final String OPEN_AI_TRANSCRIPTION_LANGUAGE = "en";

  private final ObjectMapper objectMapper;
  private final OpenAiAudioApi openAiAudioApi;

  public BackpackOpenAiService(
          ObjectMapper objectMapper,
          OpenAiAudioApi openAiAudioApi) {
      this.objectMapper = objectMapper;
      this.openAiAudioApi = openAiAudioApi;
  }

  public void transcribeAndStore(String inputFilePath, String destinationFilePath) {
    FileSystemResource audioFile = new FileSystemResource(inputFilePath);
    OpenAiVerboseJsonResponse response = getTranscription(audioFile);

    try {
      FileUtils.writeStringToFile(
          new File(destinationFilePath), objectMapper.writeValueAsString(response), StandardCharsets.UTF_8);

    } catch (IOException ex) {
      throw new RuntimeException(
          String.format(
              "Failed to write to destination transcription file %s", destinationFilePath),
          ex);
    }
  }

  public OpenAiVerboseJsonResponse getTranscription(FileSystemResource audioFile) {
    OpenAiAudioApi.TranscriptionRequest request = getVerboseJsonWithSegmentsTranscriptionRequest(audioFile);

    ResponseEntity<OpenAiVerboseJsonResponse> transcriptionEntity;
    transcriptionEntity = RETRY_TEMPLATE.execute(
            (ctx) -> openAiAudioApi.createTranscription(request, OpenAiVerboseJsonResponse.class));

    if (!transcriptionEntity.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException("Failed to transcribe");
    }

    return transcriptionEntity.getBody();
  }

  private OpenAiAudioApi.TranscriptionRequest getVerboseJsonWithSegmentsTranscriptionRequest(FileSystemResource audioFile) {
    return OpenAiAudioApi.TranscriptionRequest.builder()
            .withFile(this.toBytes(audioFile))
            .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.VERBOSE_JSON)
            .withTemperature(0F)
            .withLanguage(OPEN_AI_TRANSCRIPTION_LANGUAGE)
            .withModel(OpenAiAudioApi.WhisperModel.WHISPER_1.getValue())
            .withGranularityType(OpenAiAudioApi.TranscriptionRequest.GranularityType.SEGMENT)
            .build();
  }

  private byte[] toBytes(Resource resource) {
    try {
      return resource.getInputStream().readAllBytes();
    } catch (Exception var3) {
      throw new IllegalArgumentException("Failed to read resource: " + resource, var3);
    }
  }
}
