package com.bervan.spreadsheet.functions;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class FunctionRegistry {
    private final Map<String, SpreadsheetFunction> functionMap;

    public FunctionRegistry(Map<String, SpreadsheetFunction> functionMap) {
        this.functionMap = functionMap;
    }

    public void register(SpreadsheetFunction function) {
        functionMap.put(function.getName().toUpperCase(), function);
    }

    public Optional<SpreadsheetFunction> getFunction(String name) {
        return Optional.ofNullable(functionMap.get("F#" + name.toUpperCase()));
    }
}