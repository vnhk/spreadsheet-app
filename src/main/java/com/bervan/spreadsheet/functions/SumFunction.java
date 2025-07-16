package com.bervan.spreadsheet.functions;

import org.springframework.stereotype.Component;

import java.util.List;

@Component(value = SumFunction.SUM_FUNCTION_NAME)
public class SumFunction implements SpreadsheetFunction {
    public final static String SUM_FUNCTION_NAME = "+";

    @Override
    public FunctionArgument calculate(List<FunctionArgument> args) {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Function '" + getName() + "' requires at least one argument.");
        }

        double sum = args.stream()
                .mapToDouble(FunctionArgument::asDouble)
                .sum();
        return new ConstantArgument(sum);
    }

    @Override
    public String getInfo() {
        return """
                Examples: <br>
                    (a) =+(A1,10) <br>
                    (b) =+(C0:C10) <br>
                    (c) =+(C1,B2,G10)
                """;
    }

    @Override
    public String getName() {
        return SUM_FUNCTION_NAME;
    }
}
