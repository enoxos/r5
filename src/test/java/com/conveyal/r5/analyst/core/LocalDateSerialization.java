package com.conveyal.r5.analyst.core;

import com.conveyal.r5.common.JsonUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

/**
 * Tests correct LocalDate Serialization
 */
public class LocalDateSerialization {

    @Test
    public void testSerializationDeserialization() throws Exception {
        LocalDate date = LocalDate.now();

        String jsonDate = JsonUtilities.objectMapper.writeValueAsString(date);

        LocalDate deserializedDate = JsonUtilities.objectMapper.readValue(jsonDate, LocalDate.class);
        Assertions.assertEquals(date, deserializedDate);


    }
}
