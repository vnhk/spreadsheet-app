package com.bervan.spreadsheet.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CellTest {

    @Test
    void testCreation_function_colon_separated() {
        String functionValue = "=+(A0:A1)";
        Cell cell = new Cell(functionValue, 1, 1);
        Assertions.assertEquals(functionValue, cell.getFunctionValue());
    }


    @Test
    void testCreation_function_colon_separated_2() {
        String functionValue = "=+(A0:A2)";
        Cell cell = new Cell(functionValue, 1, 1);
        Assertions.assertEquals(functionValue, cell.getFunctionValue());
    }


    @Test
    void testCreation_function_comma_separated() {
        String functionValue = "=+(A0,A1)";
        Cell cell = new Cell(functionValue, 1, 1);
        Assertions.assertEquals(functionValue, cell.getFunctionValue());
    }

    @Test
    void testCreation_function_comma_separated_2() {
        String functionValue = "=+(A0,A1,A2)";
        Cell cell = new Cell(functionValue, 1, 1);
        Assertions.assertEquals(functionValue, cell.getFunctionValue());
    }

    @Test
    void testCreation_function_comma_separated_3() {
        String functionValue = "=+(0,A0,A1,A2,A3,5,'Test')";
        Cell cell = new Cell(functionValue, 1, 1);
        Assertions.assertEquals(functionValue, cell.getFunctionValue());
    }
}