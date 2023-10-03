package org.example;

public class Measure {
    private final double value;
    private final long timestamp;

    public Measure(double value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Si es necesario, puedes agregar métodos de comparación aquí

    @Override
    public String toString() {
        return "Value: " + value + ", Timestamp: " + timestamp;
    }
}
