package org.leo.dictionary.config.entity;

import java.util.*;

public class ConfigParent {

    protected Map<Object, Object> properties;

    public <V> V getOrDefault(String key, V defaultValue) {
        String fullKey = getClass().getName() + "." + key;

        Object v = properties.get(fullKey);
        try {
            if (v != null && defaultValue != null) {
                Class<?> defaultClass = defaultValue.getClass();
                if (defaultClass.isAssignableFrom(v.getClass()) ||
                        (Collection.class.isAssignableFrom(defaultClass) && v instanceof Collection)) {
                    return (V) v;
                }
                V result;
                String value = String.valueOf(v);
                if (Collection.class.isAssignableFrom(defaultClass)) {
                    ArrayList<Object> values = new ArrayList<>();
                    if (!value.isEmpty()) {
                        Collections.addAll(values, value.split(";"));
                    }
                    if (List.class.isAssignableFrom(defaultClass)) {
                        result = (V) values;
                    } else {
                        result = (V) new HashSet<>(values);
                    }
                } else if (defaultValue instanceof Boolean) {
                    result = (V) Boolean.valueOf(value);
                } else if (defaultValue instanceof Integer) {
                    result = (V) Integer.valueOf(value);
                } else if (defaultValue instanceof Long) {
                    result = (V) Long.valueOf(value);
                } else if (defaultValue instanceof Double) {
                    result = (V) Double.valueOf(value);
                } else if (defaultValue instanceof Float) {
                    result = (V) Float.valueOf(value);
                } else if (defaultValue instanceof Character) {
                    result = value.isEmpty() ? defaultValue : (V) Character.valueOf(value.charAt(0));
                } else if (defaultValue instanceof Byte) {
                    result = (V) Byte.valueOf(value);
                } else {
                    result = (V) v;
                }
                defaultValue = result;
            }
        } catch (NumberFormatException e) {
            if (defaultValue instanceof Integer) defaultValue = (V) Integer.valueOf(0);
            else if (defaultValue instanceof Long) defaultValue = (V) Long.valueOf(0L);
            else if (defaultValue instanceof Double) defaultValue = (V) Double.valueOf(0.0);
            else if (defaultValue instanceof Float) defaultValue = (V) Float.valueOf(0.0f);
            else if (defaultValue instanceof Byte) defaultValue = (V) Byte.valueOf((byte) 0);
        }
        properties.put(fullKey, defaultValue);
        return defaultValue;
    }

    public <V> void put(String key, V value) {
        String fullKey = getClass().getName() + "." + key;
        properties.put(fullKey, value);
    }

    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
    }
}
