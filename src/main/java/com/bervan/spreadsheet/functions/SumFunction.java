package com.bervan.spreadsheet.functions;

import org.springframework.stereotype.Component;

import java.util.List;

import static com.bervan.spreadsheet.functions.SumFunction.FUNCTION_NAME;


@Component("F#" + FUNCTION_NAME)
public class SumFunction implements SpreadsheetFunction {
    public final static String FUNCTION_NAME = "+";

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
                    (d) =+(1,2,-3.15)
                """;
    }

    @Override
    public String getName() {
        return FUNCTION_NAME;
    }
}
