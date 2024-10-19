package org.alexshtarbev.backpack.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record BackpackParagraph(
        String paragraph,
        String summary,
        String context
) {
}
