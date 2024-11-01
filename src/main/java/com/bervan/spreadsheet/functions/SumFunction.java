package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetRow;

import java.util.List;

public class SumFunction implements SpreadsheetFunction {
    @Override
    public String calculate(List<String> allParams, List<SpreadsheetRow> rows) {
        try {
            List<Object> params = getParams(allParams, rows);

            double res = 0;
            for (int i = 0; i < params.size(); i++) {
                res += getDouble(params, i);
            }

            return String.valueOf(res);
        } catch (Exception e) {
            return "ERROR!";
        }
    }
}
