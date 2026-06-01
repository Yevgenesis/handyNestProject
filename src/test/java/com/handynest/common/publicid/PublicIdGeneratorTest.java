package com.handynest.common.publicid;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicIdGeneratorTest {

    @Test
    void newUlidReturnsValidNonSequentialPublicId() {
        PublicIdGenerator generator = new PublicIdGenerator(
                Clock.fixed(Instant.parse("2026-05-25T12:00:00Z"), ZoneOffset.UTC),
                new Random(42)
        );

        String publicId = generator.newUlid();

        assertEquals(26, publicId.length());
        assertTrue(generator.isValid(publicId));
        assertFalse(publicId.matches("\\d+"));
    }

    @Test
    void isValidRejectsMalformedIds() {
        PublicIdGenerator generator = PublicIdGenerator.defaultGenerator();

        assertFalse(generator.isValid(null));
        assertFalse(generator.isValid(""));
        assertFalse(generator.isValid("TASK-00001"));
        assertFalse(generator.isValid("01JZ7Y6F4C6XKQ3M6YB4F9M1AI"));
    }
}
