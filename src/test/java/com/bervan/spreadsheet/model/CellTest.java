package com.bervan.spreadsheet.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpreadsheetCellTest {

    @Test
    void testCreation_function_colon_separated() {
        String functionValue = "=+(A0:A1)";
        SpreadsheetCell SpreadsheetCell = new SpreadsheetCell(functionValue, 1, 1);
        Assertions.assertEquals(functionValue, SpreadsheetCell.getValue());
    }

    @Test
    void testCreation_multiFunction() {
        String functionValue = "=+(1,=+(1,2))";
        SpreadsheetCell SpreadsheetCell = new SpreadsheetCell(functionValue, 1, 1);
        Assertions.assertEquals(functionValue, SpreadsheetCell.getValue());
    }

    @Test
    void testCreation_simple() {
        String functionValue = "=+(1,5)";
        SpreadsheetCell SpreadsheetCell = new SpreadsheetCell(functionValue, 1, 1);
        Assertions.assertEquals(functionValue, SpreadsheetCell.getValue());
    }


    @Test
    void testCreation_function_colon_separated_2() {
        String functionValue = "=+(A0:A2)";
        SpreadsheetCell SpreadsheetCell = new SpreadsheetCell(functionValue, 1, 1);
        Assertions.assertEquals(functionValue, SpreadsheetCell.getValue());
    }


    @Test
    void testCreation_function_comma_separated() {
        String functionValue = "=+(A0,A1)";
        SpreadsheetCell SpreadsheetCell = new SpreadsheetCell(functionValue, 1, 1);
        Assertions.assertEquals(functionValue, SpreadsheetCell.getValue());
    }

    @Test
    void testCreation_function_comma_separated_2() {
        String functionValue = "=+(A0,A1,A2)";
        SpreadsheetCell SpreadsheetCell = new SpreadsheetCell(functionValue, 1, 1);
        Assertions.assertEquals(functionValue, SpreadsheetCell.getValue());
    }

    @Test
    void testCreation_function_comma_separated_3() {
        String functionValue = "=+(0,A0,A1,A2,A3,5,'Test')";
        SpreadsheetCell cell = new SpreadsheetCell(functionValue, 1, 1);
        Assertions.assertEquals(functionValue, cell.getValue());
    }
}