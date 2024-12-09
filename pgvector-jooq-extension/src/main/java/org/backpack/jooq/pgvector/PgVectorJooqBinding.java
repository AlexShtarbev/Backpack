package org.backpack.jooq.pgvector;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

public class PgVectorJooqBinding implements Binding<Object, Vector> {

    public static final String VECTOR_CAST = "::vector";

    @Override
    public Converter<Object, Vector> converter() {
        return new Converter<>() {
            @Override
            public Vector from(Object t) {
                if (t == null) {
                    return new Vector(new float[]{});
                }
                String s = t.toString();
                var floats = s.substring(1, s.length() - 1).split(",");
                var result = new float[floats.length];
                for (int i = 0; i < floats.length; i++) {
                    result[i] = Float.parseFloat(floats[i]);
                }
                return new Vector(result);
            }

            @Override
            public Object to(Vector vector) {
                return String.format("[%s]", toString(vector));
            }

            private static String toString(Vector vector) {
                if (vector == null || vector.vectors().length == 0) {
                    return "";
                }

                return IntStream.range(0, vector.vectors().length)
                        .mapToObj(i -> String.valueOf(vector.vectors()[i]))
                        .collect(Collectors.joining(","));
            }

            @Override
            public Class<Object> fromType() {
                return Object.class;
            }

            @Override
            public Class<Vector> toType() {
                return Vector.class;
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<Vector> ctx) throws SQLException {
        if (ctx.render().paramType() == ParamType.INLINED)
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql(VECTOR_CAST);
        else
            ctx.render().sql(ctx.variable()).sql(VECTOR_CAST);
    }

    @Override
    public void register(BindingRegisterContext<Vector> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.OTHER);
    }

    @Override
    public void set(BindingSetStatementContext<Vector> ctx) throws SQLException {
        var value = ctx.convert(converter()).value();
        ctx.statement().setObject(ctx.index(), value);
    }

    @Override
    public void set(BindingSetSQLOutputContext<Vector> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetResultSetContext<Vector> ctx) throws SQLException {
        var resultSet = ctx.resultSet();
        var vector = (Vector) resultSet.getObject(ctx.index());
        ctx.value(new Vector(vector.vectors()));
    }

    @Override
    public void get(BindingGetStatementContext<Vector> ctx) throws SQLException {
        var statement = ctx.statement();
        var vectorAsString = statement.getString(ctx.index());
        ctx.value(converter().from(vectorAsString));
    }

    @Override
    public void get(BindingGetSQLInputContext<Vector> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}