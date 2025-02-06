package org.alexshtarbev.backpack.model;

import java.util.List;
import java.util.UUID;

import org.alexshtarbev.backpack.openai.OpenAiVerboseJsonResponse;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record BackpakParagrpahEmbeddingResponse(
    UUID contentId,
    String text,
    List<OpenAiVerboseJsonResponse.Segment> segments,
    float[] vectors
) { }
