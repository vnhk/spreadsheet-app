package com.bervan.spreadsheet.service;

import com.bervan.common.search.SearchService;
import com.bervan.spreadsheet.functions.DefaultCellResolver;
import com.bervan.spreadsheet.functions.FormulaParser;
import com.bervan.spreadsheet.functions.FunctionRegistry;
import com.bervan.spreadsheet.functions.SumFunction;
import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.bervan.spreadsheet.utils.SpreadsheetUtils;
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

    @Test
    void evaluateAllFormulas_reversed() {
        List<SpreadsheetRow> rows1SimpleFunction = getRows_1_simple_reversed_function();
        spreadsheetService.evaluateAllFormulas(rows1SimpleFunction);

        // D4 = =+(1, 2) → 3
        assertEquals(3.0, rows1SimpleFunction.get(3).getCell(3).getValue());

        // C3 = =+(A1, 3) → 45 + 3 = 48
        assertEquals(48.0, rows1SimpleFunction.get(2).getCell(2).getValue());

        // B2 = 42 → should stay 42
        assertEquals(42, rows1SimpleFunction.get(1).getCell(1).getValue());

        // A1 = =+(D4, B2) → 3 + 42 = 45
        assertEquals(45.0, rows1SimpleFunction.get(0).getCell(0).getValue());

        // Other text values
        assertEquals("Test 12", rows1SimpleFunction.get(0).getCell(1).getValue());
        assertEquals("Test 25", rows1SimpleFunction.get(1).getCell(4).getValue());
    }

    @Test
    void evaluateLargeGridPerformance() {
        int rowsCount, colsCount = 27;
        rowsCount = colsCount;
        List<SpreadsheetRow> rows = new ArrayList<>();

        // Step 1: Generate grid
        for (int rowIndex = 1; rowIndex <= rowsCount; rowIndex++) {
            SpreadsheetRow row = new SpreadsheetRow();
            row.rowNumber = rowIndex;

            for (int colIndex = 1; colIndex <= colsCount; colIndex++) {
                Object value;
                // A1 = 1, B1 = =+(A1, 1), C1 = =+(B1, 1), ...
                if (rowIndex == 1 && colIndex == 1) {
                    value = 1; // Starting value in A1
                } else {
                    String prevCol = getExcelColumnLetter(colIndex - 2);
                    value = "=+(" + prevCol + "1, 1)";
                }

                SpreadsheetCell cell = new SpreadsheetCell(rowIndex, colIndex, value);
                row.cells.add(cell);
            }

            rows.add(row);
        }

        // Step 2: Measure time
        long start = System.currentTimeMillis();

        spreadsheetService.evaluateAllFormulas(rows);

        long end = System.currentTimeMillis();
        double seconds = (end - start) / 1000.0;
        System.out.println("Execution time: " + seconds + " s");

        // Step 3: Verify the last formula (Z1 = 1 + 1 + ... + 1 = 100)
        SpreadsheetCell lastCell = rows.get(0).getCell(colsCount - 1);
        assertEquals(rowsCount, Double.valueOf(lastCell.getValue().toString()).intValue());
    }

    @Test
    void evaluateLargeGridPerformanceReversed() {
        int rowsCount, colsCount = 26;
        rowsCount = colsCount;
        List<SpreadsheetRow> rows = new ArrayList<>();

        // Step 1: Generate grid (from right to left)
        for (int rowIndex = 1; rowIndex <= rowsCount; rowIndex++) {
            SpreadsheetRow row = new SpreadsheetRow();
            row.rowNumber = rowIndex;

            for (int colIndex = colsCount; colIndex >= 1; colIndex--) {
                Object value;
                // Z1 = 1, Y1 = =+(Z1, 1), X1 = =+(Y1, 1), ...
                if (rowIndex == 1 && colIndex == colsCount) {
                    value = 1; // Starting value in the last column
                } else {
                    String nextCol = getExcelColumnLetter(colIndex); // colIndex+1 - 1 = colIndex
                    value = "=+(" + nextCol + "1, 1)";
                }

                SpreadsheetCell cell = new SpreadsheetCell(rowIndex, colIndex, value);
                row.cells.add(cell);
            }

            rows.add(row);
        }

        // Step 2: Measure time
        long start = System.currentTimeMillis();

        spreadsheetService.evaluateAllFormulas(rows);

        long end = System.currentTimeMillis();
        double seconds = (end - start) / 1000.0;
        System.out.println("Execution time (reversed): " + seconds + " s");

        // Step 3: Verify the first formula (A1 = 1 + 1 + ... + 1 = )
        SpreadsheetCell firstCell = SpreadsheetService.findCellById(rows, "A1"); // A1
        assertEquals(rowsCount, Double.valueOf(firstCell.getValue().toString()).intValue());
    }

    @Test
    void evaluateFullGridChainedDownward() {
        int rowsCount = 20;
        int colsCount = 20;
        List<SpreadsheetRow> rows = new ArrayList<>();

        // Step 1: Generate grid with chained formulas
        for (int rowIndex = 1; rowIndex <= rowsCount; rowIndex++) {
            SpreadsheetRow row = new SpreadsheetRow();
            row.rowNumber = rowIndex;

            for (int colIndex = 1; colIndex <= colsCount; colIndex++) {
                Object value;

                // If this is the last cell (Z26), set it to 1 (base case)
                if (rowIndex == rowsCount && colIndex == colsCount) {
                    value = 1;
                } else {
                    // Calculate next cell in the chain
                    int nextRow = rowIndex;
                    int nextCol = colIndex + 1;

                    // If we're at the last column, jump to the first column of the next row
                    if (nextCol > colsCount) {
                        nextRow = nextRow + 1;
                        nextCol = 1;
                    }

                    String ref = SpreadsheetUtils.getColumnHeader(nextCol) + nextRow;
                    value = "=+(" + ref + ",1)";
                }

                SpreadsheetCell cell = new SpreadsheetCell(rowIndex, colIndex, value);
                row.cells.add(cell);
            }

            rows.add(row);
        }

        // Step 2: Measure evaluation time
        long start = System.currentTimeMillis();
        spreadsheetService.evaluateAllFormulas(rows);
        long end = System.currentTimeMillis();

        double seconds = (end - start) / 1000.0;
        System.out.println("Execution time (full grid chained): " + seconds + " s");

        // Step 3: Verify the top-left cell contains the sum of all cells
        SpreadsheetCell a1 = SpreadsheetService.findCellById(rows, "A1");
        assertEquals(rowsCount * colsCount, Double.valueOf(a1.getValue().toString()).intValue());
    }

    private List<SpreadsheetRow> getRows_1_simple_reversed_function() {
        List<SpreadsheetRow> rows = new ArrayList<>();

        for (int rowIndex = 1; rowIndex <= 5; rowIndex++) {
            SpreadsheetRow row = new SpreadsheetRow();
            row.rowNumber = rowIndex;

            for (int colIndex = 1; colIndex <= 5; colIndex++) {
                Object value;

                // Reversed formulas and values:
                if (rowIndex == 4 && colIndex == 4) {
                    value = "=+(1,2)";
                    // D4 = =+(1, 2) → 3
                } else if (rowIndex == 3 && colIndex == 3) {
                    value = "=+(A1,3)";
                    // C3 = =+(A1, 3) → 45 + 3 = 48
                } else if (rowIndex == 2 && colIndex == 2) {
                    value = 42;
                    // B2 = 42 → should stay 42
                } else if (rowIndex == 1 && colIndex == 1) {
                    value = "=+(D4,B2)";
                    // A1 = =+(D4, B2) → 3 + 42 = 45
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

    private String getExcelColumnLetter(int columnIndex) {
        StringBuilder column = new StringBuilder();
        while (columnIndex >= 0) {
            column.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = columnIndex / 26 - 1;
        }
        return column.toString();
    }

}