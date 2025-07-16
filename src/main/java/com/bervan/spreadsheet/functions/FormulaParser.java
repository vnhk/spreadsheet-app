package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetRow;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FormulaParser {
    private final CellResolver cellResolver;
    private final FunctionRegistry functionRegistry;

    public FormulaParser(FunctionRegistry functionRegistry, CellResolver cellResolver) {
        this.functionRegistry = functionRegistry;
        this.cellResolver = cellResolver;
    }

    public FunctionArgument evaluate(String formula, List<SpreadsheetRow> rows) {
        if (!formula.startsWith("=")) {
            throw new IllegalArgumentException("Not a formula: " + formula);
        }

        // Remove '=' and extract function name and arguments
        String content = formula.substring(1).trim(); // e.g. SUM(1, 2)
        int parenIndex = content.indexOf('(');
        if (parenIndex == -1 || !content.endsWith(")")) {
            throw new IllegalArgumentException("Invalid formula syntax: " + formula);
        }

        String functionName = content.substring(0, parenIndex).trim();
        String argsString = content.substring(parenIndex + 1, content.length() - 1); // between (...)

        List<FunctionArgument> args = parseArguments(argsString, rows);
        SpreadsheetFunction function = functionRegistry.getFunction(functionName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown function: " + functionName));

        return function.calculate(args);
    }

    private List<FunctionArgument> parseArguments(String argsString, List<SpreadsheetRow> rows) {
        if (argsString.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(argsString.split(","))
                .map(String::trim)
                .map(e -> toArgument(e, rows))
                .collect(Collectors.toList());
    }

    private FunctionArgument toArgument(String raw, List<SpreadsheetRow> rows) {
        if (raw.matches("[A-Z]+\\d+")) {
            return cellResolver.resolve(raw, rows); // ex. "A1"
        }

        try {
            return new ConstantArgument(Double.parseDouble(raw));
        } catch (NumberFormatException e) {
            return new ConstantArgument(raw); // string
        }
    }
}
