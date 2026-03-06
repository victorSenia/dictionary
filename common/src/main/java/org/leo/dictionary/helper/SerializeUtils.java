package org.leo.dictionary.helper;


import java.io.*;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerializeUtils {
    private final static Logger LOGGER = Logger.getLogger(SerializeUtils.class.getName());
    private static final long MAX_ARRAY_LENGTH = 100_000;
    private static final long MAX_DEPTH = 50;
    private static final long MAX_REFERENCES = 200_000;
    private static final long MAX_STREAM_BYTES = 10_000_000;

    private static final ObjectInputFilter SAFE_DESERIALIZATION_FILTER = info -> {
        if (info.arrayLength() >= 0 && info.arrayLength() > MAX_ARRAY_LENGTH) {
            return ObjectInputFilter.Status.REJECTED;
        }
        if (info.depth() >= 0 && info.depth() > MAX_DEPTH) {
            return ObjectInputFilter.Status.REJECTED;
        }
        if (info.references() >= 0 && info.references() > MAX_REFERENCES) {
            return ObjectInputFilter.Status.REJECTED;
        }
        if (info.streamBytes() >= 0 && info.streamBytes() > MAX_STREAM_BYTES) {
            return ObjectInputFilter.Status.REJECTED;
        }

        Class<?> serialClass = info.serialClass();
        if (serialClass == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }
        while (serialClass.isArray()) {
            serialClass = serialClass.getComponentType();
        }
        if (serialClass.isPrimitive()) {
            return ObjectInputFilter.Status.ALLOWED;
        }
        String className = serialClass.getName();
        if (className.startsWith("java.lang.")
                || className.startsWith("java.util.")
                || className.startsWith("org.leo.dictionary.")) {
            return ObjectInputFilter.Status.ALLOWED;
        }
        return ObjectInputFilter.Status.REJECTED;
    };

    public static String serialize(final Object obj) {
        byte[] bytes = serializeToBytes(obj);
        if (bytes != null) {
            return Base64.getEncoder().encodeToString(bytes);
        }
        return "";
    }

    public static byte[] serializeToBytes(final Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    public static Object deserialize(String string) {
        try {
            return deserializeBytes(Base64.getDecoder().decode(string));
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    public static Object deserializeBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInput in = new ObjectInputStream(bis)) {
            ((ObjectInputStream) in).setObjectInputFilter(SAFE_DESERIALIZATION_FILTER);
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }
}
