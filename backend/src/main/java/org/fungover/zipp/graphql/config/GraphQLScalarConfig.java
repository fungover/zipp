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
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class GraphQLScalarConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder.scalar(dateTimeScalar()).scalar(dateScalar()).scalar(jsonScalar());
    }

    /**
     * DateTime scalar - handles Instant/ISO-8601 timestamps
     */
    private GraphQLScalarType dateTimeScalar() {
        return GraphQLScalarType.newScalar().name("DateTime").description("ISO-8601 DateTime scalar")
                .coercing(new Coercing<Instant, String>() {

                    @Override
                    public String serialize(Object dataFetcherResult) {
                        if (dataFetcherResult == null) {
                            return null;
                        }
                        if (dataFetcherResult instanceof Instant instant) {
                            return instant.toString();
                        }
                        throw new CoercingSerializeException(
                            "Expected Instant but got " + dataFetcherResult.getClass().getSimpleName()
                        );
                    }

                    @Override
                    public Instant parseValue(Object input) {
                        if (input instanceof String s) {
                            try {
                                return Instant.parse(s);
                            } catch (DateTimeParseException e) {
                                throw new CoercingParseValueException("Invalid DateTime format: " + s, e);
                            }
                        }
                        throw new CoercingParseValueException(
                            "Expected String but got " + input.getClass().getSimpleName()
                        );
                    }

                    @Override
                    public Instant parseLiteral(Object input) {
                        if (input instanceof StringValue stringValue) {
                            try {
                                return Instant.parse(stringValue.getValue());
                            } catch (DateTimeParseException e) {
                                throw new CoercingParseLiteralException("Invalid DateTime format", e);
                            }
                        }
                        throw new CoercingParseLiteralException(
                            "Expected StringValue but got " + input.getClass().getSimpleName()
                        );
                    }
                })
            .build();
    }

    /**
     * Date scalar - handles LocalDate (YYYY-MM-DD)
     */
    private GraphQLScalarType dateScalar() {
        return GraphQLScalarType.newScalar().name("Date").description("ISO-8601 Date scalar (YYYY-MM-DD)")
                .coercing(new Coercing<LocalDate, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) {
                        if (dataFetcherResult == null) {
                            return null;
                        }
                        if (dataFetcherResult instanceof LocalDate date) {
                            return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                        }
                        throw new CoercingSerializeException(
                            "Expected LocalDate but got " + dataFetcherResult.getClass().getSimpleName()
                        );
                    }

                    @Override
                    public LocalDate parseValue(Object input) {
                        if (input instanceof String s) {
                            try {
                                return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
                            } catch (DateTimeParseException e) {
                                throw new CoercingParseValueException("Invalid Date format: " + s, e);
                            }
                        }
                        throw new CoercingParseValueException(
                            "Expected String but got " + input.getClass().getSimpleName()
                        );
                    }

                    @Override
                    public LocalDate parseLiteral(Object input) {
                        if (input instanceof StringValue stringValue) {
                            try {
                                return LocalDate.parse(
                                    stringValue.getValue(),
                                    DateTimeFormatter.ISO_LOCAL_DATE
                                );
                            } catch (DateTimeParseException e) {
                                throw new CoercingParseLiteralException("Invalid Date format", e);
                            }
                        }
                        throw new CoercingParseLiteralException(
                            "Expected StringValue but got " + input.getClass().getSimpleName()
                        );
                    }
                })
            .build();
    }


    /**
     * JSON scalar - handles arbitrary JSON data (Map, List, etc.) Without Jackson
     * dependency - parses GraphQL literals directly
     */
    private GraphQLScalarType jsonScalar() {
        return GraphQLScalarType.newScalar().name("JSON").description("Arbitrary JSON data")
                .coercing(new Coercing<Object, Object>() {

                    @Override
                    public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        return dataFetcherResult;
                    }

                    @Override
                    public Object parseValue(Object input) throws CoercingParseValueException {
                        // Input from variables - already parsed by Spring/Jackson
                        return input;
                    }

                    @Override
                    public Object parseLiteral(Object input) {
                        if (input instanceof Value<?> value) {
                            return parseLiteralValue(value);
                        }
                        throw new CoercingParseLiteralException(
                            "Unsupported literal type: " + input.getClass().getSimpleName()
                        );
                    }

                    private Object parseLiteralValue(Value<?> value) {
                        if (value instanceof StringValue v) {
                            return v.getValue();
                        }
                        if (value instanceof IntValue v) {
                            return v.getValue().longValue();
                        }
                        if (value instanceof FloatValue v) {
                            return v.getValue().doubleValue();
                        }
                        if (value instanceof BooleanValue v) {
                            return v.isValue();
                        }
                        if (value instanceof ObjectValue v) {
                            Map<String, Object> map = new LinkedHashMap<>();
                            v.getObjectFields().forEach(
                                field -> map.put(
                                    field.getName(),
                                    parseLiteralValue(field.getValue())
                                )
                            );
                            return map;
                        }
                        if (value instanceof ArrayValue v) {
                            return v.getValues()
                                .stream()
                                .map(this::parseLiteralValue)
                                .collect(Collectors.toList());
                        }

                        throw new CoercingParseLiteralException(
                            "Unsupported JSON literal: " + value.getClass().getSimpleName()
                        );
                    }
                })
            .build();
    }
}
