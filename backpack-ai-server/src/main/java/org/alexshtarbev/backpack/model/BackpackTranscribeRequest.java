package org.alexshtarbev.backpack.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record BackpackTranscribeRequest(
        String url,
        boolean shouldDownload,
        String audioFileName,
        String transcriptionFileName) {}
