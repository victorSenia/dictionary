package org.leo.dictionary.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


public class ConfigParser {
    public static <T> T createConfig(Map<?, ?> properties, T obj) {
        try {
            Class<?> aClass = obj.getClass();
            String prefix = aClass.getCanonicalName() + ".";
            for (Map.Entry<?, ?> entry : properties.entrySet()) {
                if (entry.getKey().toString().startsWith(prefix)) {
                    String parameter = entry.getKey().toString().substring(prefix.length());
                    Field declaredField = getDeclaredField(aClass, parameter);
                    Method method = aClass.getMethod("set" + parameter.substring(0, 1).toUpperCase(Locale.ROOT) + parameter.substring(1), declaredField.getType());
                    method.invoke(obj, getObject(entry.getValue().toString(), declaredField.getType()));
                }
            }
            return obj;
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException |
                 NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field getDeclaredField(Class<?> aClass, String parameter) throws NoSuchFieldException {
        try {
            return aClass.getDeclaredField(parameter);
        } catch (NoSuchFieldException e) {
            if (aClass.getSuperclass() != null) {
                return getDeclaredField(aClass.getSuperclass(), parameter);
            }
            throw e;
        }
    }

    private static Object getObject(String value, Class<?> fieldType) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (fieldType.isArray()) {
            ArrayList<Object> values = new ArrayList<>();
            for (String elementValue : value.split(";")) {
                values.add(getObject(elementValue, fieldType.getComponentType()));
            }
            return values.toArray();//TODO wrong type
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            ArrayList<Object> values = new ArrayList<>();
            for (String elementValue : value.split(";")) {
                values.add(getObject(elementValue, String.class));//TODO supports only strings
            }
            if (List.class.isAssignableFrom(fieldType)) {
                return values;
            } else {
                return new HashSet<>(values);
            }
        } else if (fieldType.isAssignableFrom(boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (fieldType.isAssignableFrom(int.class)) {
            return Integer.parseInt(value);
        } else if (fieldType.isAssignableFrom(long.class)) {
            return Long.parseLong(value);
        } else if (fieldType.isAssignableFrom(double.class)) {
            return Double.parseDouble(value);
        } else if (fieldType.isAssignableFrom(float.class)) {
            return Float.parseFloat(value);
        } else if (fieldType.isAssignableFrom(char.class)) {
            return value.charAt(0);
        } else if (fieldType.isAssignableFrom(byte.class)) {
            return Byte.parseByte(value);
        } else {
            return fieldType.getConstructor(String.class).newInstance(value);
        }
    }

    public static <T> void writeConfigInProperties(Map<Object, Object> properties, T config) {
        Class<?> aClass = config.getClass();
        String prefix = aClass.getCanonicalName() + ".";
        writeForClass(properties, config, aClass, prefix);
        Class<?> superClass = aClass;
        while ((superClass = superClass.getSuperclass()) != null) {
            writeForClass(properties, config, superClass, prefix);
        }
    }

    private static <T> void writeForClass(Map<Object, Object> properties, T config, Class<?> aClass, String prefix) {
        for (Method method : aClass.getMethods()) {
            String name = method.getName().startsWith("get") ? method.getName().substring(3, 4).toLowerCase(Locale.ROOT) + method.getName().substring(4) :
                    method.getName().startsWith("is") ? method.getName().substring(2, 3).toLowerCase(Locale.ROOT) + method.getName().substring(3) : null;
            try {
                if (name != null) {
                    Field declaredField = aClass.getDeclaredField(name);
                    if (method.getReturnType().isArray()) {
                        properties.put(prefix + name, Arrays.stream((Object[]) method.invoke(config)).map(Object::toString).collect(Collectors.joining(";")));
                    } else if (Collection.class.isAssignableFrom(method.getReturnType())) {
                        properties.put(prefix + name, ((Collection<?>) (method.invoke(config))).stream().map(Object::toString).collect(Collectors.joining(";")));
                    } else {
                        properties.put(prefix + name, method.invoke(config).toString());
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
                //ignore
            }
        }
    }
}
