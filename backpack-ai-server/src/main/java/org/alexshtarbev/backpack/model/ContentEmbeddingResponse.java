package org.alexshtarbev.backpack.model;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record ContentEmbeddingResponse(
    UUID contentId,
    String content,
    String summary,
    String context,
    float[] vectors
) { }
