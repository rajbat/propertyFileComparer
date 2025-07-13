package com.view;

public class ComparisonRow {
    private final String key;
    private final String value1;
    private final String value2;

    public ComparisonRow(String key, String value1, String value2) {
        this.key = key;
        this.value1 = value1;
        this.value2 = value2;
    }

    public String getKey() { return key; }
    public String getValue1() { return value1; }
    public String getValue2() { return value2; }
}