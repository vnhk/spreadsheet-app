package com.bervan.spreadsheet.functions;

import com.bervan.logging.JsonLogger;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.bervan.spreadsheet.functions.TotalSavingsFunction.FUNCTION_NAME;

@Component("F#" + FUNCTION_NAME)
public class TotalSavingsFunction implements SpreadsheetFunction {
    public final static String FUNCTION_NAME = "TOTAL_SAVINGS";
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "spreadsheets");

    private double calculateTotalSavings(double initialCapital, double interestRate, double years) {
        return initialCapital * Math.pow(1 + interestRate, years);
    }


    @Override
    public FunctionArgument calculate(List<FunctionArgument> functionArguments) {
        if (functionArguments.size() != 3) {
            throw new RuntimeException("Incorrect Params for " + FUNCTION_NAME + " function");
        }

        return new ConstantArgument(String.valueOf(calculateTotalSavings(functionArguments.get(0).asDouble(), functionArguments.get(1).asDouble(), functionArguments.get(2).asDouble())));
    }

    @Override
    public String getInfo() {
        return """
                Examples: <br>
                Total savings after 10 years with initial capital 10000 and 5% interest rate <br>
                    (a) =TOTAL_SAVINGS(10000,0.05,10) <br>
                    (b) =TOTAL_SAVINGS(10000,C1,D2) <br>
                    (c) =TOTAL_SAVINGS(C1,B2,G10)
                """;
    }

    @Override
    public String getName() {
        return FUNCTION_NAME;
    }
}
