package org.alexshtarbev.backpack.service;

import java.util.List;

import org.alexshtarbev.backpack.openai.OpenAiVerboseJsonResponse;

public record SegmentAndParagraph(List<OpenAiVerboseJsonResponse.Segment> segment, String paragraph){}
