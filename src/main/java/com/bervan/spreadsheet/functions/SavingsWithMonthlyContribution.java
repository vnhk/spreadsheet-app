package com.bervan.spreadsheet.functions;

import org.springframework.stereotype.Component;

import java.util.List;

import static com.bervan.spreadsheet.functions.SavingsWithMonthlyContribution.FUNCTION_NAME;

@Component(FUNCTION_NAME)
public class SavingsWithMonthlyContribution implements SpreadsheetFunction {
    public final static String FUNCTION_NAME = "SAVINGS_M_CONTR";

    private double calculateFutureValueWithMonthlyContributions(double initialCapital, double monthlyContribution, double annualInterestRate, double years) {
        double monthlyRate = annualInterestRate / 12;
        double totalMonths = years * 12;
        double futureValue = initialCapital * Math.pow(1 + monthlyRate, totalMonths);

        for (int i = 1; i <= totalMonths; i++) {
            futureValue += monthlyContribution * Math.pow(1 + monthlyRate, totalMonths - i);
        }

        return futureValue;
    }

    @Override
    public FunctionArgument calculate(List<FunctionArgument> functionArguments) {
        if (functionArguments.size() != 4) {
            throw new RuntimeException("Incorrect Params for " + FUNCTION_NAME + " function");
        }

        return new ConstantArgument(
                String.valueOf(calculateFutureValueWithMonthlyContributions(functionArguments.get(0).asDouble(),
                        functionArguments.get(1).asDouble(),
                        functionArguments.get(2).asDouble(),
                        functionArguments.get(3).asDouble()))
        );
    }

    @Override
    public String getInfo() {
        return """
                Examples: <br>
                Total savings after 10 years with initial capital 10000 and 5% interest rate and 500 monthly contribution <br>
                    (a) =SAVINGS_M_CONTR(10000,0.05,500,10) <br>
                    (b) =SAVINGS_M_CONTR(10000,C1,500,D2) <br>
                    (c) =SAVINGS_M_CONTR(C1,B1,A1,G10)
                """;
    }

    @Override
    public String getName() {
        return FUNCTION_NAME;
    }
}
