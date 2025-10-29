package org.leo.dictionary.helper;


import java.io.*;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerializeUtils {
    private final static Logger LOGGER = Logger.getLogger(SerializeUtils.class.getName());

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
        return deserializeBytes(Base64.getDecoder().decode(string));
    }

    public static Object deserializeBytes(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }
}
