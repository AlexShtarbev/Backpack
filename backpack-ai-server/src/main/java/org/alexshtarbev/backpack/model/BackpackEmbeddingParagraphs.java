package org.alexshtarbev.backpack.model;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record BackpackEmbeddingParagraphs(List<String> paragraphs) { }
