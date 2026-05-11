package com.bervan.spreadsheet.utils;

import com.bervan.common.model.UtilsMessage;
import com.bervan.logging.JsonLogger;
import com.bervan.spreadsheet.model.SpreadsheetRow;

import java.util.List;

public class SpreadsheetUtils {
    private static final JsonLogger log = JsonLogger.getLogger(SpreadsheetUtils.class, "spreadsheets");

    //does sort columns make sense? What if we sort cells that are used somewhere in a formula? should we update it or not?

    public static UtilsMessage sortColumns(List<SpreadsheetRow> spreadsheetRows, String sortColumn, String order, String columnsToBeSorted, String rows) {
        log.error(" sortColumns not implemented");
        return new UtilsMessage();
    }

    public static int getRowNumberFromColumn(String input) {
        String numberPart = input.replaceAll("[^0-9]", "");
        return Integer.parseInt(numberPart);
    }

    public static int getColumnNumber(String columnSymbol) {
        int columnNumber = 0;
        for (int i = 0; i < columnSymbol.length(); i++) {
            columnNumber = columnNumber * 26 + (columnSymbol.charAt(i) - 'A' + 1);
        }
        return columnNumber;
    }

    public static String getColumnHeader(String columnLabel) {
        return columnLabel.replaceAll("[^A-Z]", "");
    }

    public static String getColumnHeader(int columnNumber) {
        StringBuilder label = new StringBuilder();
        while (columnNumber > 0) {
            label.insert(0, (char) ('A' + (columnNumber - 1 % 26)));
            columnNumber = columnNumber / 26 - 1;
        }
        return label.toString();
    }
}
