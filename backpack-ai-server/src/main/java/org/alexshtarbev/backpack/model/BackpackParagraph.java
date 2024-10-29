package org.alexshtarbev.backpack.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record BackpackParagraph(String content, String summary, String context) {}
