package org.leo.dictionary.config.entity;

import java.util.*;

public class ConfigParent {

    protected Map<Object, Object> properties;

    public <V> V getOrDefault(String key, V defaultValue) {
        String fullKey = getClass().getName() + "." + key;

        Object v = properties.get(fullKey);
        if (v != null) {
            if (v.getClass().isAssignableFrom(defaultValue.getClass())) {
                return (V) v;
            }
            if (Collection.class.isAssignableFrom(defaultValue.getClass())) {
                String value = v.toString();
                ArrayList<Object> values = new ArrayList<>();
                if (!value.isEmpty()) {
                    Collections.addAll(values, value.split(";"));
                }
                if (List.class.isAssignableFrom(defaultValue.getClass())) {
                    return (V) values;
                } else {
                    return (V) new HashSet<>(values);
                }
            }
            if (defaultValue instanceof Boolean) {
                return (V) Boolean.valueOf(v.toString());
            }
            if (defaultValue instanceof Integer) {
                return (V) Integer.valueOf(v.toString());
            }
            if (defaultValue instanceof Long) {
                return (V) Long.valueOf(v.toString());
            }
            if (defaultValue instanceof Double) {
                return (V) Double.valueOf(v.toString());
            }
            if (defaultValue instanceof Float) {
                return (V) Float.valueOf(v.toString());
            }
            if (defaultValue instanceof Character) {
                return (V) v.toString();//TODO
            }
            if (defaultValue instanceof Byte) {
                return (V) Byte.valueOf(v.toString());
            }
            return (V) v;
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
