package org.alexshtarbev.backpack.openai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiVerboseJsonResponse(
        String language,
        Float duration,
        String text,
        List<Word> words,
        List<Segment> segments) {
    public record Segment(
            // @formatter:off
            Integer id,
            Integer seek,
            Float start,
            Float end,
            String text,
            List<Integer> tokens,
            Float temperature,
            Float avgLogprob,
            Float compressionRatio,
            Float noSpeechProb) { }

    public record Word(
            String word,
            Float start,
            Float end) {
    }
}

