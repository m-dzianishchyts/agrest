package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.agrest.reflect.Types.typeForName;

/**
 * @since 5.0
 */
public class JsonValueConvertersProvider implements Provider<JsonValueConverters> {

    private static final JsonValueConverter<JsonNode> DO_NOTHING_CONVERTER = node -> node;

    private final Map<String, JsonValueConverter<?>> injectedConverters;

    public JsonValueConvertersProvider(@Inject Map<String, JsonValueConverter<?>> injectedConverters) {
        this.injectedConverters = injectedConverters;
    }

    @Override
    public JsonValueConverters get() throws DIRuntimeException {
        Map<Class<?>, JsonValueConverter<?>> converters = new HashMap<>();
        appendKnownConverters(converters);
        appendInjectedConverters(converters);
        return new JsonValueConverters(converters, defaultConverter());
    }

    protected JsonValueConverter<?> defaultConverter() {
        return GenericConverter.converter();
    }

    protected void appendKnownConverters(Map<Class<?>, JsonValueConverter<?>> converters) {

        converters.put(Object.class, GenericConverter.converter());

        converters.put(byte[].class, Base64Converter.converter());

        converters.put(BigDecimal.class, BigDecimalConverter.converter());

        converters.put(Float.class, FloatConverter.converter());
        converters.put(float.class, FloatConverter.converter());

        converters.put(Double.class, DoubleConverter.converter());
        converters.put(double.class, DoubleConverter.converter());

        converters.put(Long.class, LongConverter.converter());
        converters.put(long.class, LongConverter.converter());

        converters.put(Date.class, UtcDateConverter.converter());
        converters.put(java.sql.Date.class, UtcDateConverter.converter(java.sql.Date.class));
        converters.put(java.sql.Time.class, UtcDateConverter.converter(java.sql.Time.class));
        converters.put(java.sql.Timestamp.class, UtcDateConverter.converter(java.sql.Timestamp.class));
        converters.put(LocalDate.class, ISOLocalDateConverter.converter());
        converters.put(LocalTime.class, ISOLocalTimeConverter.converter());
        converters.put(LocalDateTime.class, ISOLocalDateTimeConverter.converter());
        converters.put(OffsetDateTime.class, ISOOffsetDateTimeConverter.converter());

        converters.put(JsonNode.class, DO_NOTHING_CONVERTER);
    }

    protected void appendInjectedConverters(Map<Class<?>, JsonValueConverter<?>> converters) {
        injectedConverters.forEach((k, v) -> converters.put(typeForName(k), v));
    }
}