package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetRow;
import org.apache.commons.math3.exception.NotANumberException;

import java.util.List;

public class MultiplyFunction implements SpreadsheetFunction {
    @Override
    public String calculate(List<String> allParams, List<SpreadsheetRow> rows) {
        try {
            List<Object> params = getParams(allParams, rows);

            double res = 1;
            for (int i = 0; i < params.size(); i++) {
                try {
                    res *= getDouble(params, i);
                } catch (NotANumberException e) {
                    //we ignore it, because we want to make this function working for empty values
                }
            }
            return String.valueOf(res);
        } catch (Exception e) {
            return "ERROR!";
        }
    }
}
