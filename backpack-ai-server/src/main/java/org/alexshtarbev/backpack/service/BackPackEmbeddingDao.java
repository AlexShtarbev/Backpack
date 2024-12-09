package org.alexshtarbev.backpack.service;

import org.alexshtarbev.bacpack.Tables;
import org.alexshtarbev.bacpack.tables.records.EmbeddingRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

@Component
public class BackPackEmbeddingDao {
    private final DSLContext dslContext;

    public BackPackEmbeddingDao(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public void upsert(EmbeddingRecord embeddingRecord) {
        dslContext.mergeInto(Tables.EMBEDDING)
                .values(
                        embeddingRecord.get(Tables.EMBEDDING.CONTENT_ID),
                        embeddingRecord.get(Tables.EMBEDDING.EMBEDDING_))
                .execute();
    }
}
