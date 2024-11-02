package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetRow;

import java.util.List;

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
}
