package org.fungover.zipp.graphql.config;

import graphql.language.StringValue;
import graphql.language.ObjectValue;
import graphql.language.ArrayValue;
import graphql.language.IntValue;
import graphql.language.FloatValue;
import graphql.language.BooleanValue;
import graphql.language.Value;
import graphql.schema.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class GraphQLScalarConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
            .scalar(dateTimeScalar())
            .scalar(dateScalar())
            .scalar(jsonScalar());
    }

    /**
     * DateTime scalar - handles Instant/ISO-8601 timestamps
     */
    private GraphQLScalarType dateTimeScalar() {
        return GraphQLScalarType.newScalar()
            .name("DateTime")
            .description("ISO-8601 DateTime scalar")
            .coercing(new Coercing<Instant, String>() {
                @Override
                public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                    if (dataFetcherResult instanceof Instant instant) {
                        return instant.toString();
                    }
                    throw new CoercingSerializeException("Expected Instant but got: " + dataFetcherResult.getClass());
                }

                @Override
                public Instant parseValue(Object input) throws CoercingParseValueException {
                    try {
                        if (input instanceof String s) {
                            return Instant.parse(s);
                        }
                        throw new CoercingParseValueException("Expected String but got: " + input.getClass());
                    } catch (DateTimeParseException e) {
                        throw new CoercingParseValueException("Invalid DateTime format: " + input, e);
                    }
                }

                @Override
                public Instant parseLiteral(Object input) throws CoercingParseLiteralException {
                    if (input instanceof StringValue stringValue) {
                        try {
                            return Instant.parse(stringValue.getValue());
                        } catch (DateTimeParseException e) {
                            throw new CoercingParseLiteralException("Invalid DateTime format", e);
                        }
                    }
                    throw new CoercingParseLiteralException("Expected StringValue but got: " + input.getClass());
                }
            })
            .build();
    }

    /**
     * Date scalar - handles LocalDate (YYYY-MM-DD)
     */
    private GraphQLScalarType dateScalar() {
        return GraphQLScalarType.newScalar()
            .name("Date")
            .description("ISO-8601 Date scalar (YYYY-MM-DD)")
            .coercing(new Coercing<LocalDate, String>() {
                @Override
                public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                    if (dataFetcherResult instanceof LocalDate date) {
                        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    }
                    throw new CoercingSerializeException("Expected LocalDate but got: " + dataFetcherResult.getClass());
                }

                @Override
                public LocalDate parseValue(Object input) throws CoercingParseValueException {
                    try {
                        if (input instanceof String s) {
                            return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
                        }
                        throw new CoercingParseValueException("Expected String but got: " + input.getClass());
                    } catch (DateTimeParseException e) {
                        throw new CoercingParseValueException("Invalid Date format: " + input, e);
                    }
                }

                @Override
                public LocalDate parseLiteral(Object input) throws CoercingParseLiteralException {
                    if (input instanceof StringValue stringValue) {
                        try {
                            return LocalDate.parse(stringValue.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);
                        } catch (DateTimeParseException e) {
                            throw new CoercingParseLiteralException("Invalid Date format", e);
                        }
                    }
                    throw new CoercingParseLiteralException("Expected StringValue but got: " + input.getClass());
                }
            })
            .build();
    }

    /**
     * JSON scalar - handles arbitrary JSON data (Map, List, etc.)
     * Without Jackson dependency - parses GraphQL literals directly
     */
    private GraphQLScalarType jsonScalar() {
        return GraphQLScalarType.newScalar()
            .name("JSON")
            .description("Arbitrary JSON data")
            .coercing(new Coercing<Object, Object>() {
                @Override
                public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
                    return dataFetcherResult;
                }

                @Override
                public Object parseValue(Object input) throws CoercingParseValueException {
                    // Input from variables - allready parsad by Spring/Jackson
                    return input;
                }

                @Override
                public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                    if (input instanceof Value<?> value) {
                        return parseLiteralValue(value);
                    }
                    return input;
                }

                private Object parseLiteralValue(Value<?> value) {
                    if (value instanceof StringValue stringValue) {
                        return stringValue.getValue();
                    }
                    if (value instanceof IntValue intValue) {
                        return intValue.getValue().longValue();
                    }
                    if (value instanceof FloatValue floatValue) {
                        return floatValue.getValue().doubleValue();
                    }
                    if (value instanceof BooleanValue booleanValue) {
                        return booleanValue.isValue();
                    }
                    if (value instanceof ObjectValue objectValue) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        objectValue.getObjectFields().forEach(field ->
                            map.put(field.getName(), parseLiteralValue(field.getValue()))
                        );
                        return map;
                    }
                    if (value instanceof ArrayValue arrayValue) {
                        return arrayValue.getValues().stream()
                            .map(this::parseLiteralValue)
                            .collect(Collectors.toList());
                    }
                    return null;
                }
            })
            .build();
    }
}
