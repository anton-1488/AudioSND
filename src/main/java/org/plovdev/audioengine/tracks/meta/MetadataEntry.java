package org.plovdev.audioengine.tracks.meta;

public class MetadataEntry {
    private Object value;
    private Class<?> type;

    public MetadataEntry() {}
    public MetadataEntry(Object value, Class<?> type) {
        this.value = value;
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}