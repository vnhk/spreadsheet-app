package com.bervan.spreadsheet.functions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConstantArgument implements FunctionArgument {
    private final Object value;

    public ConstantArgument(Object value) {
        this.value = value;
    }

    @Override
    public double asDouble() {
        try {
            return Double.parseDouble(asText());
        } catch (Exception e) {
            log.error("Value cannot be parsed to double: {}", asText());
            throw e;
        }
    }

    @Override
    public String asText() {
        return String.valueOf(value);
    }

    @Override
    public Object asObject() {
        return value;
    }
}