package com.bervan.spreadsheet.utils;

import com.bervan.common.model.UtilsMessage;
import com.bervan.spreadsheet.model.Cell;
import com.bervan.spreadsheet.utils.SpreadsheetUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class SpreadsheetUtilsTest {

    @Test
    void uuid_test() {
        Assertions.assertEquals(UUID.nameUUIDFromBytes("123456".getBytes()).toString(), "e10adc39-49ba-39ab-be56-e057f20f883e");
    }

    @Test
    void sortColumns_Ascending() {
        Cell[][] cells = initializeTestCells();

        String sortColumn = "B";
        String columnsToBeSorted = "A,B";
        String rows = "1:13";
        String order = "Ascending";

        UtilsMessage utilsMessage = SpreadsheetUtils.sortColumns(cells, sortColumn, order, columnsToBeSorted, rows);

        Assertions.assertTrue(utilsMessage.isSuccess);

        // Verify sorted order
        Assertions.assertEquals("Bonds", cells[1][0].value);
        Assertions.assertEquals("0", cells[1][1].value);

        Assertions.assertEquals("Investing", cells[2][0].value);
        Assertions.assertEquals("0", cells[2][1].value);

        Assertions.assertEquals("Internet", cells[3][0].value);
        Assertions.assertEquals("80", cells[3][1].value);

        Assertions.assertEquals("Netflix", cells[4][0].value);
        Assertions.assertEquals("90", cells[4][1].value);

        Assertions.assertEquals("GPT", cells[5][0].value);
        Assertions.assertEquals("100", cells[5][1].value);

        Assertions.assertEquals("Electricity", cells[6][0].value);
        Assertions.assertEquals("180", cells[6][1].value);

        Assertions.assertEquals("English", cells[7][0].value);
        Assertions.assertEquals("400", cells[7][1].value);

        Assertions.assertEquals("English", cells[8][0].value);
        Assertions.assertEquals("400", cells[8][1].value);

        Assertions.assertEquals("Fuel", cells[9][0].value);
        Assertions.assertEquals("500", cells[9][1].value);

        Assertions.assertEquals("Food Ordering", cells[10][0].value);
        Assertions.assertEquals("600", cells[10][1].value);

        Assertions.assertEquals("G-Bills", cells[11][0].value);
        Assertions.assertEquals("630", cells[11][1].value);

        Assertions.assertEquals("Credit", cells[12][0].value);
        Assertions.assertEquals("1700", cells[12][1].value);

        Assertions.assertEquals("Household Shopping", cells[13][0].value);
        Assertions.assertEquals("2000", cells[13][1].value);
    }

    @Test
    void sortColumns_Descending() {
        Cell[][] cells = initializeTestCells();

        String sortColumn = "B";
        String columnsToBeSorted = "A,B";
        String rows = "1:13";
        String order = "Descending";

        UtilsMessage utilsMessage = SpreadsheetUtils.sortColumns(cells, sortColumn, order, columnsToBeSorted, rows);

        Assertions.assertTrue(utilsMessage.isSuccess);

        // Verify sorted order
        Assertions.assertEquals("Household Shopping", cells[1][0].value);
        Assertions.assertEquals("2000", cells[1][1].value);

        Assertions.assertEquals("Credit", cells[2][0].value);
        Assertions.assertEquals("1700", cells[2][1].value);

        Assertions.assertEquals("G-Bills", cells[3][0].value);
        Assertions.assertEquals("630", cells[3][1].value);

        Assertions.assertEquals("Food Ordering", cells[4][0].value);
        Assertions.assertEquals("600", cells[4][1].value);

        Assertions.assertEquals("Fuel", cells[5][0].value);
        Assertions.assertEquals("500", cells[5][1].value);

        Assertions.assertEquals("English", cells[6][0].value);
        Assertions.assertEquals("400", cells[6][1].value);

        Assertions.assertEquals("English", cells[7][0].value);
        Assertions.assertEquals("400", cells[7][1].value);

        Assertions.assertEquals("Electricity", cells[8][0].value);
        Assertions.assertEquals("180", cells[8][1].value);

        Assertions.assertEquals("GPT", cells[9][0].value);
        Assertions.assertEquals("100", cells[9][1].value);

        Assertions.assertEquals("Netflix", cells[10][0].value);
        Assertions.assertEquals("90", cells[10][1].value);

        Assertions.assertEquals("Internet", cells[11][0].value);
        Assertions.assertEquals("80", cells[11][1].value);

        Assertions.assertEquals("Bonds", cells[12][0].value);
        Assertions.assertEquals("0", cells[12][1].value);

        Assertions.assertEquals("Investing", cells[13][0].value);
        Assertions.assertEquals("0", cells[13][1].value);
    }

    private Cell[][] initializeTestCells() {
        Cell[][] cells = new Cell[14][2];

        cells[0][0] = new Cell("Expense:", 0, 0);
        cells[0][1] = new Cell("Cost:", 1, 0);

        cells[1][0] = new Cell("Household Shopping", 0, 1);
        cells[1][1] = new Cell("2000", 1, 1);

        cells[2][0] = new Cell("Credit", 0, 2);
        cells[2][1] = new Cell("1700", 1, 2);

        cells[3][0] = new Cell("G-Bills", 0, 3);
        cells[3][1] = new Cell("630", 1, 3);

        cells[4][0] = new Cell("Food Ordering", 0, 4);
        cells[4][1] = new Cell("600", 1, 4);

        cells[5][0] = new Cell("Bonds", 0, 5);
        cells[5][1] = new Cell("0", 1, 5);

        cells[6][0] = new Cell("Investing", 0, 6);
        cells[6][1] = new Cell("0", 1, 6);

        cells[7][0] = new Cell("Fuel", 0, 7);
        cells[7][1] = new Cell("500", 1, 7);

        cells[8][0] = new Cell("English", 0, 8);
        cells[8][1] = new Cell("400", 1, 8);

        cells[9][0] = new Cell("Electricity", 0, 9);
        cells[9][1] = new Cell("180", 1, 9);

        cells[10][0] = new Cell("GPT", 0, 10);
        cells[10][1] = new Cell("100", 1, 10);

        cells[11][0] = new Cell("Netflix", 0, 11);
        cells[11][1] = new Cell("90", 1, 11);

        cells[12][0] = new Cell("Internet", 0, 12);
        cells[12][1] = new Cell("80", 1, 12);

        cells[13][0] = new Cell("English", 0, 13);
        cells[13][1] = new Cell("400", 1, 13);

        return cells;
    }
}