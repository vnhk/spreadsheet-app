package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetRow;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TotalSavingsFunction implements SpreadsheetFunction {
    @Override
    public String calculate(List<String> allParams, List<SpreadsheetRow> rows) {
        try {
            List<Object> paramsCareAboutOrder = getParams_careAboutOrder(allParams, rows);

            if (paramsCareAboutOrder.size() != 3) {
                throw new RuntimeException("Incorrect Params");
            }

            double initialCapital = getDouble(paramsCareAboutOrder, 0);
            double interestRate = getDouble(paramsCareAboutOrder, 1);
            double years = getDouble(paramsCareAboutOrder, 2);

            return String.valueOf(calculateTotalSavings(initialCapital, interestRate, years));
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR!";
        }
    }

    private double calculateTotalSavings(double initialCapital, double interestRate, double years) {
        return initialCapital * Math.pow(1 + interestRate, years);
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
        return "TOTAL_SAVINGS";
    }
}
