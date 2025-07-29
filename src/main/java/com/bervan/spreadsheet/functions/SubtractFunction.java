package com.bervan.spreadsheet.functions;

import org.springframework.stereotype.Component;

import java.util.List;

import static com.bervan.spreadsheet.functions.SubtractFunction.FUNCTION_NAME;

@Component(FUNCTION_NAME)
public class SubtractFunction implements SpreadsheetFunction {
    public final static String FUNCTION_NAME = "-";

    @Override
    public FunctionArgument calculate(List<FunctionArgument> args) {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Function '" + getName() + "' requires at least one argument.");
        }

        double result = args.get(0).asDouble();

        for (int i = 1; i < args.size(); i++) {
            result -= args.get(i).asDouble();
        }

        return new ConstantArgument(result);
    }

    @Override
    public String getInfo() {
        return """
                Examples: <br>
                    (a) =-(A1,10) <br>
                    (b) =-(C0:C10) <br>
                    (c) =-(C1,B2,G10)
                """;
    }

    @Override
    public String getName() {
        return FUNCTION_NAME;
    }
}
