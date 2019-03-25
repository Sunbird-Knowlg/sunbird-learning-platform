package org.ekstep.jobs.samza.reader;

public class NullableValue<T> {
    private T value;

    public NullableValue(T value) {
        this.value = value;
    }

    public T value() {
        return value;
    }

    public boolean isNull() {
        return value == null;
    }

    public T valueOrDefault(T defaultValue) {
        if (isNull())
            return defaultValue;

        return value();
    }

    @Override
    public String toString() {
        return "NullableValue{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NullableValue<?> that = (NullableValue<?>) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
