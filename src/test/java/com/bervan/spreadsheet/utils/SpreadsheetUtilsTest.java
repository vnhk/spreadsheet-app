package com.bervan.spreadsheet.utils;

import com.bervan.common.model.UtilsMessage;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


class SpreadsheetUtilsTest {

    @Test
    void sortColumns_1() {
        Spreadsheet spreadsheet = new Spreadsheet();
        String sortColumn = "B";
        String columnsToBeSorted = "A,B";
        String rows = "1:13";
        Grid<SpreadsheetRow> grid = new Grid<>(SpreadsheetRow.class);

        List<SpreadsheetRow> gridRows = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            SpreadsheetRow row = new SpreadsheetRow(2);
            row.number = i;
            gridRows.add(row);
        }

        gridRows.get(0).setCell(0, "Expenses:");
        gridRows.get(0).setCell(1, "Cost:");

        gridRows.get(1).setCell(0, "Household Shopping");
        gridRows.get(1).setCell(1, "2000");

        gridRows.get(2).setCell(0, "Credit");
        gridRows.get(2).setCell(1, "1700");

        gridRows.get(3).setCell(0, "G-Bills");
        gridRows.get(3).setCell(1, "630");

        gridRows.get(4).setCell(0, "Food Ordering");
        gridRows.get(4).setCell(1, "600");

        gridRows.get(5).setCell(0, "Bonds");
        gridRows.get(5).setCell(1, "0");

        gridRows.get(6).setCell(0, "Investing");
        gridRows.get(6).setCell(1, "0");

        gridRows.get(7).setCell(0, "Fuel");
        gridRows.get(7).setCell(1, "500");

        gridRows.get(8).setCell(0, "English");
        gridRows.get(8).setCell(1, "400");

        gridRows.get(9).setCell(0, "Electricity");
        gridRows.get(9).setCell(1, "180");

        gridRows.get(10).setCell(0, "GPT");
        gridRows.get(10).setCell(1, "100");

        gridRows.get(11).setCell(0, "Netflix");
        gridRows.get(11).setCell(1, "90");

        gridRows.get(12).setCell(0, "Internet");
        gridRows.get(12).setCell(1, "80");

        gridRows.get(13).setCell(0, "English");
        gridRows.get(13).setCell(1, "400");

        grid.setItems(gridRows);
        spreadsheet.setRows(gridRows);

        String order = "Ascending";
        UtilsMessage utilsMessage = SpreadsheetUtils.sortColumns(spreadsheet, sortColumn, order, columnsToBeSorted, rows, grid);

        Assertions.assertTrue(utilsMessage.isSuccess);

        Assertions.assertEquals("Expenses:", gridRows.get(0).getCell(0).value);
        Assertions.assertEquals("Cost:", gridRows.get(0).getCell(1).value);

        Assertions.assertEquals("Bonds", gridRows.get(1).getCell(0).value);
        Assertions.assertEquals("0", gridRows.get(1).getCell(1).value);

        Assertions.assertEquals("Investing", gridRows.get(2).getCell(0).value);
        Assertions.assertEquals("0", gridRows.get(2).getCell(1).value);

        Assertions.assertEquals("Internet", gridRows.get(3).getCell(0).value);
        Assertions.assertEquals("80", gridRows.get(3).getCell(1).value);

        Assertions.assertEquals("Netflix", gridRows.get(4).getCell(0).value);
        Assertions.assertEquals("90", gridRows.get(4).getCell(1).value);

        Assertions.assertEquals("GPT", gridRows.get(5).getCell(0).value);
        Assertions.assertEquals("100", gridRows.get(5).getCell(1).value);

        Assertions.assertEquals("Electricity", gridRows.get(6).getCell(0).value);
        Assertions.assertEquals("180", gridRows.get(6).getCell(1).value);

        Assertions.assertEquals("English", gridRows.get(7).getCell(0).value);
        Assertions.assertEquals("400", gridRows.get(7).getCell(1).value);

        Assertions.assertEquals("English", gridRows.get(8).getCell(0).value);
        Assertions.assertEquals("400", gridRows.get(8).getCell(1).value);

        Assertions.assertEquals("Fuel", gridRows.get(9).getCell(0).value);
        Assertions.assertEquals("500", gridRows.get(9).getCell(1).value);

        Assertions.assertEquals("Food Ordering", gridRows.get(10).getCell(0).value);
        Assertions.assertEquals("600", gridRows.get(10).getCell(1).value);

        Assertions.assertEquals("G-Bills", gridRows.get(11).getCell(0).value);
        Assertions.assertEquals("630", gridRows.get(11).getCell(1).value);

        Assertions.assertEquals("Credit", gridRows.get(12).getCell(0).value);
        Assertions.assertEquals("1700", gridRows.get(12).getCell(1).value);

        Assertions.assertEquals("Household Shopping", gridRows.get(13).getCell(0).value);
        Assertions.assertEquals("2000", gridRows.get(13).getCell(1).value);
    }

    @Test
    void sortColumns_2() {
        Spreadsheet spreadsheet = new Spreadsheet();
        String sortColumn = "B";
        String columnsToBeSorted = "A,B";
        String rows = "1:13";
        Grid<SpreadsheetRow> grid = new Grid<>(SpreadsheetRow.class);

        List<SpreadsheetRow> gridRows = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            SpreadsheetRow row = new SpreadsheetRow(2);
            row.number = i;
            gridRows.add(row);
        }

        gridRows.get(0).setCell(0, "Expenses:");
        gridRows.get(0).setCell(1, "Cost:");

        gridRows.get(1).setCell(0, "Household Shopping");
        gridRows.get(1).setCell(1, "2000");

        gridRows.get(2).setCell(0, "Credit");
        gridRows.get(2).setCell(1, "1700");

        gridRows.get(3).setCell(0, "G-Bills");
        gridRows.get(3).setCell(1, "630");

        gridRows.get(4).setCell(0, "Food Ordering");
        gridRows.get(4).setCell(1, "600");

        gridRows.get(5).setCell(0, "Bonds");
        gridRows.get(5).setCell(1, "0");

        gridRows.get(6).setCell(0, "Investing");
        gridRows.get(6).setCell(1, "0");

        gridRows.get(7).setCell(0, "Fuel");
        gridRows.get(7).setCell(1, "500");

        gridRows.get(8).setCell(0, "English");
        gridRows.get(8).setCell(1, "400");

        gridRows.get(9).setCell(0, "Electricity");
        gridRows.get(9).setCell(1, "180");

        gridRows.get(10).setCell(0, "GPT");
        gridRows.get(10).setCell(1, "100");

        gridRows.get(11).setCell(0, "Netflix");
        gridRows.get(11).setCell(1, "90");

        gridRows.get(12).setCell(0, "Internet");
        gridRows.get(12).setCell(1, "80");

        gridRows.get(13).setCell(0, "English");
        gridRows.get(13).setCell(1, "400");

        grid.setItems(gridRows);
        spreadsheet.setRows(gridRows);

        String order = "Descending";
        UtilsMessage utilsMessage = SpreadsheetUtils.sortColumns(spreadsheet, sortColumn, order, columnsToBeSorted, rows, grid);

        Assertions.assertTrue(utilsMessage.isSuccess);

        Assertions.assertEquals("Expenses:", gridRows.get(0).getCell(0).value);
        Assertions.assertEquals("Cost:", gridRows.get(0).getCell(1).value);

        Assertions.assertEquals("Household Shopping", gridRows.get(1).getCell(0).value);
        Assertions.assertEquals("2000", gridRows.get(1).getCell(1).value);

        Assertions.assertEquals("Credit", gridRows.get(2).getCell(0).value);
        Assertions.assertEquals("1700", gridRows.get(2).getCell(1).value);

        Assertions.assertEquals("G-Bills", gridRows.get(3).getCell(0).value);
        Assertions.assertEquals("630", gridRows.get(3).getCell(1).value);

        Assertions.assertEquals("Food Ordering", gridRows.get(4).getCell(0).value);
        Assertions.assertEquals("600", gridRows.get(4).getCell(1).value);

        Assertions.assertEquals("Fuel", gridRows.get(5).getCell(0).value);
        Assertions.assertEquals("500", gridRows.get(5).getCell(1).value);

        Assertions.assertEquals("English", gridRows.get(6).getCell(0).value);
        Assertions.assertEquals("400", gridRows.get(6).getCell(1).value);

        Assertions.assertEquals("English", gridRows.get(7).getCell(0).value);
        Assertions.assertEquals("400", gridRows.get(7).getCell(1).value);

        Assertions.assertEquals("Electricity", gridRows.get(8).getCell(0).value);
        Assertions.assertEquals("180", gridRows.get(8).getCell(1).value);

        Assertions.assertEquals("GPT", gridRows.get(9).getCell(0).value);
        Assertions.assertEquals("100", gridRows.get(9).getCell(1).value);

        Assertions.assertEquals("Netflix", gridRows.get(10).getCell(0).value);
        Assertions.assertEquals("90", gridRows.get(10).getCell(1).value);

        Assertions.assertEquals("Internet", gridRows.get(11).getCell(0).value);
        Assertions.assertEquals("80", gridRows.get(11).getCell(1).value);

        Assertions.assertEquals("Bonds", gridRows.get(12).getCell(0).value);
        Assertions.assertEquals("0", gridRows.get(12).getCell(1).value);

        Assertions.assertEquals("Investing", gridRows.get(13).getCell(0).value);
        Assertions.assertEquals("0", gridRows.get(13).getCell(1).value);
    }

    @Test
    void sortColumns_WithNonNumericValuesAtBottom_ascending() {
        Spreadsheet spreadsheet = new Spreadsheet();
        String sortColumn = "B";
        String columnsToBeSorted = "A,B";
        String rows = "1:13";
        Grid<SpreadsheetRow> grid = new Grid<>(SpreadsheetRow.class);

        List<SpreadsheetRow> gridRows = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            SpreadsheetRow row = new SpreadsheetRow(2);
            row.number = i;
            gridRows.add(row);
        }

        gridRows.get(0).setCell(0, "Expense:");
        gridRows.get(0).setCell(1, "Cost:");

        gridRows.get(1).setCell(0, "Household Shopping");
        gridRows.get(1).setCell(1, "2000");

        gridRows.get(2).setCell(0, "Credit");
        gridRows.get(2).setCell(1, "1700");

        gridRows.get(3).setCell(0, "G-Bills");
        gridRows.get(3).setCell(1, "630");

        gridRows.get(4).setCell(0, "Food Ordering");
        gridRows.get(4).setCell(1, "600");

        gridRows.get(5).setCell(0, "Bonds");
        gridRows.get(5).setCell(1, "abc"); // Non-numeric value

        gridRows.get(6).setCell(0, "Investing");
        gridRows.get(6).setCell(1, null); // Null value

        gridRows.get(7).setCell(0, "Fuel");
        gridRows.get(7).setCell(1, "500");

        gridRows.get(8).setCell(0, "English");
        gridRows.get(8).setCell(1, "400");

        gridRows.get(9).setCell(0, "Electricity");
        gridRows.get(9).setCell(1, "180");

        gridRows.get(10).setCell(0, "GPT");
        gridRows.get(10).setCell(1, "100");

        gridRows.get(11).setCell(0, "Netflix");
        gridRows.get(11).setCell(1, "90");

        gridRows.get(12).setCell(0, "Internet");
        gridRows.get(12).setCell(1, "80");

        gridRows.get(13).setCell(0, "English");
        gridRows.get(13).setCell(1, "400");

        grid.setItems(gridRows);
        spreadsheet.setRows(gridRows);

        String order = "Ascending";
        UtilsMessage utilsMessage = SpreadsheetUtils.sortColumns(spreadsheet, sortColumn, order, columnsToBeSorted, rows, grid);

        Assertions.assertTrue(utilsMessage.isSuccess);

        // Assertions for sorted order with non-numeric values at the bottom
        Assertions.assertEquals("Expense:", gridRows.get(0).getCell(0).value);
        Assertions.assertEquals("Cost:", gridRows.get(0).getCell(1).value);

        Assertions.assertEquals("Internet", gridRows.get(1).getCell(0).value);
        Assertions.assertEquals("80", gridRows.get(1).getCell(1).value);

        Assertions.assertEquals("Netflix", gridRows.get(2).getCell(0).value);
        Assertions.assertEquals("90", gridRows.get(2).getCell(1).value);

        Assertions.assertEquals("GPT", gridRows.get(3).getCell(0).value);
        Assertions.assertEquals("100", gridRows.get(3).getCell(1).value);

        Assertions.assertEquals("Electricity", gridRows.get(4).getCell(0).value);
        Assertions.assertEquals("180", gridRows.get(4).getCell(1).value);

        Assertions.assertEquals("English", gridRows.get(5).getCell(0).value);
        Assertions.assertEquals("400", gridRows.get(5).getCell(1).value);

        Assertions.assertEquals("English", gridRows.get(6).getCell(0).value);
        Assertions.assertEquals("400", gridRows.get(6).getCell(1).value);

        Assertions.assertEquals("Fuel", gridRows.get(7).getCell(0).value);
        Assertions.assertEquals("500", gridRows.get(7).getCell(1).value);

        Assertions.assertEquals("Food Ordering", gridRows.get(8).getCell(0).value);
        Assertions.assertEquals("600", gridRows.get(8).getCell(1).value);

        Assertions.assertEquals("G-Bills", gridRows.get(9).getCell(0).value);
        Assertions.assertEquals("630", gridRows.get(9).getCell(1).value);

        Assertions.assertEquals("Credit", gridRows.get(10).getCell(0).value);
        Assertions.assertEquals("1700", gridRows.get(10).getCell(1).value);

        Assertions.assertEquals("Household Shopping", gridRows.get(11).getCell(0).value);
        Assertions.assertEquals("2000", gridRows.get(11).getCell(1).value);

        // Non-numeric and empty values should be at the bottom
        Assertions.assertEquals("Bonds", gridRows.get(12).getCell(0).value);
        Assertions.assertEquals("abc", gridRows.get(12).getCell(1).value);

        Assertions.assertEquals("Investing", gridRows.get(13).getCell(0).value);
        Assertions.assertEquals("", gridRows.get(13).getCell(1).value);
    }

    @Test
    void sortColumns_WithNonNumericValuesAtBottom_descending() {
        Spreadsheet spreadsheet = new Spreadsheet();
        String sortColumn = "B";
        String columnsToBeSorted = "A,B";
        String rows = "1:13";
        Grid<SpreadsheetRow> grid = new Grid<>(SpreadsheetRow.class);

        List<SpreadsheetRow> gridRows = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            SpreadsheetRow row = new SpreadsheetRow(2);
            row.number = i;
            gridRows.add(row);
        }

        gridRows.get(0).setCell(0, "Expense:");
        gridRows.get(0).setCell(1, "Cost:");

        gridRows.get(1).setCell(0, "Household Shopping");
        gridRows.get(1).setCell(1, "2000");

        gridRows.get(2).setCell(0, "Credit");
        gridRows.get(2).setCell(1, "1700");

        gridRows.get(3).setCell(0, "G-Bills");
        gridRows.get(3).setCell(1, "630");

        gridRows.get(4).setCell(0, "Food Ordering");
        gridRows.get(4).setCell(1, "600");

        gridRows.get(5).setCell(0, "Bonds");
        gridRows.get(5).setCell(1, "abc"); // Non-numeric value

        gridRows.get(6).setCell(0, "Investing");
        gridRows.get(6).setCell(1, null); // Null value

        gridRows.get(7).setCell(0, "Fuel");
        gridRows.get(7).setCell(1, "500");

        gridRows.get(8).setCell(0, "English");
        gridRows.get(8).setCell(1, "400");

        gridRows.get(9).setCell(0, "Electricity");
        gridRows.get(9).setCell(1, "180");

        gridRows.get(10).setCell(0, "GPT");
        gridRows.get(10).setCell(1, "100");

        gridRows.get(11).setCell(0, "Netflix");
        gridRows.get(11).setCell(1, "90");

        gridRows.get(12).setCell(0, "Internet");
        gridRows.get(12).setCell(1, "80");

        gridRows.get(13).setCell(0, "English");
        gridRows.get(13).setCell(1, "400");

        grid.setItems(gridRows);
        spreadsheet.setRows(gridRows);

        String order = "Descending";
        UtilsMessage utilsMessage = SpreadsheetUtils.sortColumns(spreadsheet, sortColumn, order, columnsToBeSorted, rows, grid);

        Assertions.assertTrue(utilsMessage.isSuccess);

        // Assertions for sorted order with non-numeric values at the bottom (Descending)
        Assertions.assertEquals("Expense:", gridRows.get(0).getCell(0).value);
        Assertions.assertEquals("Cost:", gridRows.get(0).getCell(1).value);

        Assertions.assertEquals("Household Shopping", gridRows.get(1).getCell(0).value);
        Assertions.assertEquals("2000", gridRows.get(1).getCell(1).value);

        Assertions.assertEquals("Credit", gridRows.get(2).getCell(0).value);
        Assertions.assertEquals("1700", gridRows.get(2).getCell(1).value);

        Assertions.assertEquals("G-Bills", gridRows.get(3).getCell(0).value);
        Assertions.assertEquals("630", gridRows.get(3).getCell(1).value);

        Assertions.assertEquals("Food Ordering", gridRows.get(4).getCell(0).value);
        Assertions.assertEquals("600", gridRows.get(4).getCell(1).value);

        Assertions.assertEquals("Fuel", gridRows.get(5).getCell(0).value);
        Assertions.assertEquals("500", gridRows.get(5).getCell(1).value);

        Assertions.assertEquals("English", gridRows.get(6).getCell(0).value);
        Assertions.assertEquals("400", gridRows.get(6).getCell(1).value);

        Assertions.assertEquals("English", gridRows.get(7).getCell(0).value);
        Assertions.assertEquals("400", gridRows.get(7).getCell(1).value);

        Assertions.assertEquals("Electricity", gridRows.get(8).getCell(0).value);
        Assertions.assertEquals("180", gridRows.get(8).getCell(1).value);

        Assertions.assertEquals("GPT", gridRows.get(9).getCell(0).value);
        Assertions.assertEquals("100", gridRows.get(9).getCell(1).value);

        Assertions.assertEquals("Netflix", gridRows.get(10).getCell(0).value);
        Assertions.assertEquals("90", gridRows.get(10).getCell(1).value);

        Assertions.assertEquals("Internet", gridRows.get(11).getCell(0).value);
        Assertions.assertEquals("80", gridRows.get(11).getCell(1).value);

        // Non-numeric and empty values should be at the bottom
        Assertions.assertEquals("Bonds", gridRows.get(12).getCell(0).value);
        Assertions.assertEquals("abc", gridRows.get(12).getCell(1).value);

        Assertions.assertEquals("Investing", gridRows.get(13).getCell(0).value);
        Assertions.assertEquals("", gridRows.get(13).getCell(1).value);
    }

    @Test
    void extractRowIndex() {
    }

    @Test
    void getColumnIndex() {
    }

    @Test
    void testGetColumnIndex() {
    }

    @Test
    void getColumnHeader() {
    }
}