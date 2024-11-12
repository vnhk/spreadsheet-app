package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetRow;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SavingsWithMonthlyContribution implements SpreadsheetFunction {
    @Override
    public String calculate(List<String> allParams, List<SpreadsheetRow> rows) {
        try {
            List<Object> paramsCareAboutOrder = getParams_careAboutOrder(allParams, rows);

            if (paramsCareAboutOrder.size() != 4) {
                throw new RuntimeException("Incorrect Params");
            }

            double initialCapital = getDouble(paramsCareAboutOrder, 0);
            double interestRate = getDouble(paramsCareAboutOrder, 1);
            double monthlyContribution = getDouble(paramsCareAboutOrder, 2);
            double years = getDouble(paramsCareAboutOrder, 3);

            return String.valueOf(calculateFutureValueWithMonthlyContributions(initialCapital, monthlyContribution, interestRate, years));
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR!";
        }
    }

    private  double calculateFutureValueWithMonthlyContributions(double initialCapital, double monthlyContribution, double annualInterestRate, double years) {
        double monthlyRate = annualInterestRate / 12;
        double totalMonths = years * 12;
        double futureValue = initialCapital * Math.pow(1 + monthlyRate, totalMonths);

        for (int i = 1; i <= totalMonths; i++) {
            futureValue += monthlyContribution * Math.pow(1 + monthlyRate, totalMonths - i);
        }

        return futureValue;
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
        return "SAVINGS_M_CONTR";
    }
}
