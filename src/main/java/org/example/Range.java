package org.example;

public class Range {
    private double min;
    private double max;

    public Range(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean isValueInRange(double value) {
        return value >= min && value <= max;
    }
}