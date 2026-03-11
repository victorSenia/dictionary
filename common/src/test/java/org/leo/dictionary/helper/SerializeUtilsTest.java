package org.leo.dictionary.helper;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SerializeUtilsTest {

    @Test
    void serializeAndDeserializeRoundTripForMapPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("langFrom", "de");
        payload.put("playTranslationFor", List.of("en", "es"));
        payload.put("repeatTimes", 2);

        String encoded = SerializeUtils.serialize(payload);
        Object decoded = SerializeUtils.deserialize(encoded);

        assertInstanceOf(Map.class, decoded);
        Map<?, ?> decodedMap = (Map<?, ?>) decoded;
        assertEquals("de", decodedMap.get("langFrom"));
        assertEquals(List.of("en", "es"), decodedMap.get("playTranslationFor"));
        assertEquals(2, decodedMap.get("repeatTimes"));
    }

    @Test
    void serializeReturnsEmptyStringForNonSerializableObject() {
        assertEquals("", SerializeUtils.serialize(new Object()));
    }

    @Test
    void deserializeReturnsNullForInvalidBase64() {
        assertNull(SerializeUtils.deserialize("%%%"));
    }

    @Test
    void deserializeBytesReturnsNullForNullOrEmptyInput() {
        assertNull(SerializeUtils.deserializeBytes(null));
        assertNull(SerializeUtils.deserializeBytes(new byte[0]));
    }
}
