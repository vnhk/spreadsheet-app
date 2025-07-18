package com.bervan.spreadsheet.service;

import com.bervan.common.search.SearchService;
import com.bervan.spreadsheet.functions.DefaultCellResolver;
import com.bervan.spreadsheet.functions.FormulaParser;
import com.bervan.spreadsheet.functions.FunctionRegistry;
import com.bervan.spreadsheet.functions.SumFunction;
import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

class SpreadsheetServiceTest {
    @Mock
    SpreadsheetRepository spreadsheetRepository;
    SpreadsheetService spreadsheetService = new SpreadsheetService(spreadsheetRepository, new SearchService(),
            new FormulaParser(new FunctionRegistry(
                    Map.of(
                            SumFunction.SUM_FUNCTION_NAME, new SumFunction()
                    ))
                    , new DefaultCellResolver()
            )
    );


    @Test
    void evaluateAllFormulas() {
        List<SpreadsheetRow> rows1SimpleFunction = getRows_1_simple_function();
        spreadsheetService.evaluateAllFormulas(rows1SimpleFunction);

        // A1 = =+(1, 2) → 3
        assertEquals(3.0, rows1SimpleFunction.get(0).getCell(0).getValue());

        // B2 = =+(A1, 3) → 3 + 3 = 6
        assertEquals(6.0, rows1SimpleFunction.get(1).getCell(1).getValue());

        // C3 = 42 → should stay 42
        assertEquals(42, rows1SimpleFunction.get(2).getCell(2).getValue());

        // D4 = =+(A1, B2) → 3 + 6 = 9
        assertEquals(9.0, rows1SimpleFunction.get(3).getCell(3).getValue());

        assertEquals("Test 12", rows1SimpleFunction.get(0).getCell(1).getValue());
        assertEquals("Test 25", rows1SimpleFunction.get(1).getCell(4).getValue());
    }

    private List<SpreadsheetRow> getRows_1_simple_function() {
        List<SpreadsheetRow> rows = new ArrayList<>();

        for (int rowIndex = 1; rowIndex <= 5; rowIndex++) {
            SpreadsheetRow row = new SpreadsheetRow();
            row.rowNumber = rowIndex;

            for (int colIndex = 1; colIndex <= 5; colIndex++) {
                Object value;

                // Sample formulas and values
                if (rowIndex == 1 && colIndex == 1) {
                    value = "=+(1, 2)";
                } else if (rowIndex == 2 && colIndex == 2) {
                    value = "=+(A1, 3)";
                } else if (rowIndex == 3 && colIndex == 3) {
                    value = 42;
                } else if (rowIndex == 4 && colIndex == 4) {
                    value = "=+(A1, B2)";
                } else {
                    value = "Test " + rowIndex + colIndex;
                }

                SpreadsheetCell cell = new SpreadsheetCell(rowIndex, colIndex, value);
                row.cells.add(cell);
            }

            rows.add(row);
        }

        return rows;
    }

    @Test
    void evaluateAllFormulas_reversed() {
        List<SpreadsheetRow> rows1SimpleFunction = getRows_1_simple_reversed_function();
        spreadsheetService.evaluateAllFormulas(rows1SimpleFunction);

        // D4 = =+(1, 2) → 3
        assertEquals(3.0, rows1SimpleFunction.get(3).getCell(3).getValue());

        // C3 = =+(A1, 3) → 3 + 3 = 6
        assertEquals(6.0, rows1SimpleFunction.get(2).getCell(2).getValue());

        // B2 = 42 → should stay 42
        assertEquals(42, rows1SimpleFunction.get(1).getCell(1).getValue());

        // A1 = =+(A1, B2) → 3 + 6 = 9
        assertEquals(9.0, rows1SimpleFunction.get(0).getCell(0).getValue());

        // Other text values
        assertEquals("Test 12", rows1SimpleFunction.get(0).getCell(1).getValue());
        assertEquals("Test 25", rows1SimpleFunction.get(1).getCell(4).getValue());
    }

    private List<SpreadsheetRow> getRows_1_simple_reversed_function() {
        List<SpreadsheetRow> rows = new ArrayList<>();

        for (int rowIndex = 1; rowIndex <= 5; rowIndex++) {
            SpreadsheetRow row = new SpreadsheetRow();
            row.rowNumber = rowIndex;

            for (int colIndex = 1; colIndex <= 5; colIndex++) {
                Object value;

                // Reversed formulas and values:
                // D4 = =+(1, 2) → 3
                // C3 = =+(A1, 3) → 3 + 3 = 6
                // B2 = 42 → should stay 42
                // A1 = =+(A1, B2) → 3 + 6 = 9
                if (rowIndex == 4 && colIndex == 4) {
                    value = "=+(1, 2)";
                } else if (rowIndex == 3 && colIndex == 3) {
                    value = "=+(A1, 3)";
                } else if (rowIndex == 2 && colIndex == 2) {
                    value = 42;
                } else if (rowIndex == 1 && colIndex == 1) {
                    value = "=+(A1, B2)";
                } else {
                    value = "Test " + rowIndex + colIndex;
                }

                SpreadsheetCell cell = new SpreadsheetCell(rowIndex, colIndex, value);
                row.cells.add(cell);
            }

            rows.add(row);
        }

        return rows;
    }

}