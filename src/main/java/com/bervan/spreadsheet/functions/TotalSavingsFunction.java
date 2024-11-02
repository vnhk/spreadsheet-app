package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetRow;

import java.util.List;

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
}
