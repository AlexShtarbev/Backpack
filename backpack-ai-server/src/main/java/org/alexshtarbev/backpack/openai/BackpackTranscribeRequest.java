package org.alexshtarbev.backpack.openai;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record BackpackTranscribeRequest(String inputFilePath, String destinationFilePath) {}
