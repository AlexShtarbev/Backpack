package org.backpack.jooq.pgvector;

import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.TableDefinition;

public class JavaGeneratorWithVectorSupport extends JavaGenerator {

    private static final String EMBEDDING_KEYWORD = "embedding";

    public static final String FETCH_COSINE_DISTANCE_WITH_LIMIT = """
    public /* non-final */ List<?> fetchNearestVectorByCosineDistance(
            org.jooq.Field<?> field, Vector value, int limit) {
        return ctx()
            .selectFrom(getTable())
            .orderBy(org.jooq.impl.DSL.condition(String.format("%%s<->'[%%s]'::vector", field.getName(), toString(value))))
            .limit(limit)
            .fetch(mapper());
    }

    private static String toString(Vector vector) {
        if (vector == null || vector.vectors().length == 0) {
            return "";
        }

        return java.util.stream.IntStream.range(0, vector.vectors().length)
                .mapToObj(i -> String.valueOf(vector.vectors()[i]))
                .collect(java.util.stream.Collectors.joining(","));
    }
            """;

    @Override
    protected void generateDaoClassFooter(TableDefinition table, JavaWriter out) {
        if (table.getTable().getName().contains(EMBEDDING_KEYWORD)) {
            out.println(String.format(FETCH_COSINE_DISTANCE_WITH_LIMIT));
        }
    }

}
