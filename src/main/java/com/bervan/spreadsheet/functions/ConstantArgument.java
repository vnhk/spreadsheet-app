package com.bervan.spreadsheet.functions;

import com.bervan.logging.JsonLogger;

public class ConstantArgument implements FunctionArgument {
    private final Object value;
    private final JsonLogger log = JsonLogger.getLogger(getClass());

    public ConstantArgument(Object value) {
        this.value = value;
    }

    @Override
    public double asDouble() {
        try {
            String text = asText();
            if (text.isBlank()) {
                return 0;
            }
            return Double.parseDouble(text);
        } catch (Exception e) {
            log.error("Value cannot be parsed to double: {}", asText());
            throw e;
        }
    }

    @Override
    public String asText() {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }

    @Override
    public Object asObject() {
        return value;
    }
}