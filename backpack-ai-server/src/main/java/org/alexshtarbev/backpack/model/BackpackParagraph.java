package org.alexshtarbev.backpack.model;

import java.util.List;

import org.alexshtarbev.backpack.openai.OpenAiVerboseJsonResponse;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record BackpackParagraph(String url, String text, List<OpenAiVerboseJsonResponse.Segment> segments) {}
