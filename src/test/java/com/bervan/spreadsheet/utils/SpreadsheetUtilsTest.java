package com.bervan.spreadsheet.utils;

import com.bervan.common.model.UtilsMessage;
import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

class SpreadsheetUtilsTest {

    @Test
    void uuid_test() {
        Assertions.assertEquals(UUID.nameUUIDFromBytes("123456".getBytes()).toString(), "e10adc39-49ba-39ab-be56-e057f20f883e");
    }

    @Test
    void sortColumns_Ascending() {
        List<SpreadsheetRow> cells = initializeTestCells();

        String sortColumn = "B";
        String columnsToBeSorted = "A,B";
        String rows = "1:13";
        String order = "Ascending";

        UtilsMessage utilsMessage = SpreadsheetUtils.sortColumns(cells, sortColumn, order, columnsToBeSorted, rows);

        Assertions.assertTrue(utilsMessage.isSuccess);

        // Verify sorted order
        Assertions.assertEquals("Bonds", cells.get(1).getCell(0).getValue());
        Assertions.assertEquals("0", cells.get(1).getCell(1).getValue());

        Assertions.assertEquals("Investing", cells.get(2).getCell(0).getValue());
        Assertions.assertEquals("0", cells.get(2).getCell(1).getValue());

        Assertions.assertEquals("Internet", cells.get(3).getCell(0).getValue());
        Assertions.assertEquals("80", cells.get(3).getCell(1).getValue());

        Assertions.assertEquals("Netflix", cells.get(4).getCell(0).getValue());
        Assertions.assertEquals("90", cells.get(4).getCell(1).getValue());

        Assertions.assertEquals("GPT", cells.get(5).getCell(0).getValue());
        Assertions.assertEquals("100", cells.get(5).getCell(1).getValue());

        Assertions.assertEquals("Electricity", cells.get(6).getCell(0).getValue());
        Assertions.assertEquals("180", cells.get(6).getCell(1).getValue());

        Assertions.assertEquals("English", cells.get(7).getCell(0).getValue());
        Assertions.assertEquals("400", cells.get(7).getCell(1).getValue());

        Assertions.assertEquals("English", cells.get(8).getCell(0).getValue());
        Assertions.assertEquals("400", cells.get(8).getCell(1).getValue());

        Assertions.assertEquals("Fuel", cells.get(9).getCell(0).getValue());
        Assertions.assertEquals("500", cells.get(9).getCell(1).getValue());

        Assertions.assertEquals("Food Ordering", cells.get(10).getCell(0).getValue());
        Assertions.assertEquals("600", cells.get(10).getCell(1).getValue());

        Assertions.assertEquals("G-Bills", cells.get(11).getCell(0).getValue());
        Assertions.assertEquals("630", cells.get(11).getCell(1).getValue());

        Assertions.assertEquals("Credit", cells.get(12).getCell(0).getValue());
        Assertions.assertEquals("1700", cells.get(12).getCell(1).getValue());

        Assertions.assertEquals("Household Shopping", cells.get(13).getCell(0).getValue());
        Assertions.assertEquals("2000", cells.get(13).getCell(1).getValue());

        for (int i = 0; i < cells.size(); i++) {
            SpreadsheetRow row = cells.get(i);

            Assertions.assertEquals(String.valueOf(i + 1), row.getCell(2).getValue(), "Index mismatch at row " + i);
        }
    }

    @Test
    void sortColumns_Descending() {
        List<SpreadsheetRow> cells = initializeTestCells();

        String sortColumn = "B";
        String columnsToBeSorted = "A,B";
        String rows = "1:13";
        String order = "Descending";

        UtilsMessage utilsMessage = SpreadsheetUtils.sortColumns(cells, sortColumn, order, columnsToBeSorted, rows);

        Assertions.assertTrue(utilsMessage.isSuccess);

        // Verify sorted order
        Assertions.assertEquals("Household Shopping", cells.get(1).getCell(0).getValue());
        Assertions.assertEquals("2000", cells.get(1).getCell(1).getValue());

        Assertions.assertEquals("Credit", cells.get(2).getCell(0).getValue());
        Assertions.assertEquals("1700", cells.get(2).getCell(1).getValue());

        Assertions.assertEquals("G-Bills", cells.get(3).getCell(0).getValue());
        Assertions.assertEquals("630", cells.get(3).getCell(1).getValue());

        Assertions.assertEquals("Food Ordering", cells.get(4).getCell(0).getValue());
        Assertions.assertEquals("600", cells.get(4).getCell(1).getValue());

        Assertions.assertEquals("Fuel", cells.get(5).getCell(0).getValue());
        Assertions.assertEquals("500", cells.get(5).getCell(1).getValue());

        Assertions.assertEquals("English", cells.get(6).getCell(0).getValue());
        Assertions.assertEquals("400", cells.get(6).getCell(1).getValue());

        Assertions.assertEquals("English", cells.get(7).getCell(0).getValue());
        Assertions.assertEquals("400", cells.get(7).getCell(1).getValue());

        Assertions.assertEquals("Electricity", cells.get(8).getCell(0).getValue());
        Assertions.assertEquals("180", cells.get(8).getCell(1).getValue());

        Assertions.assertEquals("GPT", cells.get(9).getCell(0).getValue());
        Assertions.assertEquals("100", cells.get(9).getCell(1).getValue());

        Assertions.assertEquals("Netflix", cells.get(10).getCell(0).getValue());
        Assertions.assertEquals("90", cells.get(10).getCell(1).getValue());

        Assertions.assertEquals("Internet", cells.get(11).getCell(0).getValue());
        Assertions.assertEquals("80", cells.get(11).getCell(1).getValue());

        Assertions.assertEquals("Bonds", cells.get(12).getCell(0).getValue());
        Assertions.assertEquals("0", cells.get(12).getCell(1).getValue());

        Assertions.assertEquals("Investing", cells.get(13).getCell(0).getValue());
        Assertions.assertEquals("0", cells.get(13).getCell(1).getValue());

        for (int i = 0; i < cells.size(); i++) {
            SpreadsheetRow row = cells.get(i);

            Assertions.assertEquals(String.valueOf(i + 1), row.getCell(2).getValue(), "Index mismatch at row " + i);
        }

    }

    private List<SpreadsheetRow> initializeTestCells() {
        SpreadsheetCell[][] cells = new SpreadsheetCell[14][3];

        cells[0][0] = new SpreadsheetCell("Expense:", 0, 0);
        cells[0][1] = new SpreadsheetCell("Cost:", 1, 0);
        cells[0][2] = new SpreadsheetCell("Index:", 2, 0);

        cells[1][0] = new SpreadsheetCell("Household Shopping", 0, 1);
        cells[1][1] = new SpreadsheetCell("2000", 1, 1);

        cells[2][0] = new SpreadsheetCell("Credit", 0, 2);
        cells[2][1] = new SpreadsheetCell("1700", 1, 2);

        cells[3][0] = new SpreadsheetCell("G-Bills", 0, 3);
        cells[3][1] = new SpreadsheetCell("630", 1, 3);

        cells[4][0] = new SpreadsheetCell("Food Ordering", 0, 4);
        cells[4][1] = new SpreadsheetCell("600", 1, 4);

        cells[5][0] = new SpreadsheetCell("Bonds", 0, 5);
        cells[5][1] = new SpreadsheetCell("0", 1, 5);

        cells[6][0] = new SpreadsheetCell("Investing", 0, 6);
        cells[6][1] = new SpreadsheetCell("0", 1, 6);

        cells[7][0] = new SpreadsheetCell("Fuel", 0, 7);
        cells[7][1] = new SpreadsheetCell("500", 1, 7);

        cells[8][0] = new SpreadsheetCell("English", 0, 8);
        cells[8][1] = new SpreadsheetCell("400", 1, 8);

        cells[9][0] = new SpreadsheetCell("Electricity", 0, 9);
        cells[9][1] = new SpreadsheetCell("180", 1, 9);

        cells[10][0] = new SpreadsheetCell("GPT", 0, 10);
        cells[10][1] = new SpreadsheetCell("100", 1, 10);

        cells[11][0] = new SpreadsheetCell("Netflix", 0, 11);
        cells[11][1] = new SpreadsheetCell("90", 1, 11);

        cells[12][0] = new SpreadsheetCell("Internet", 0, 12);
        cells[12][1] = new SpreadsheetCell("80", 1, 12);

        cells[13][0] = new SpreadsheetCell("English", 0, 13);
        cells[13][1] = new SpreadsheetCell("400", 1, 13);

        for (int i = 0; i < cells.length; i++) {
            cells[i][2] = new SpreadsheetCell(String.valueOf(i + 1), 2, i);
        }

        List<SpreadsheetRow> result = new ArrayList<>();

        for (SpreadsheetCell[] rowCells : cells) {
            SpreadsheetRow row = new SpreadsheetRow();
            row.setCells(Arrays.stream(rowCells).toList());
            result.add(row);
        }

        return result;
    }
}