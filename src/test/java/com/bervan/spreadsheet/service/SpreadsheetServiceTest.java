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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SpreadsheetServiceTest {
    @Mock
    SpreadsheetRepository spreadsheetRepository;
    SpreadsheetService spreadsheetService = new SpreadsheetService(spreadsheetRepository, new SearchService(),
            new FormulaParser(new FunctionRegistry(
                    Map.of(
                            "F#" + SumFunction.FUNCTION_NAME, new SumFunction()
                    ))
                    , new DefaultCellResolver()
            )
    );

    public static void printSpreadsheet(List<SpreadsheetRow> rows) {
        if (rows == null || rows.isEmpty()) {
            System.out.println("Spreadsheet is empty");
            return;
        }

        int maxCols = rows.stream()
                .mapToInt(r -> r.getCells().size())
                .max()
                .orElse(0);

        System.out.print("Row\t");
        for (int col = 0; col < maxCols; col++) {
            System.out.print(getColumnLetter(col) + "\t");
        }
        System.out.println();

        for (SpreadsheetRow row : rows) {
            System.out.print(row.rowNumber + "\t");
            for (SpreadsheetCell cell : row.getCells()) {
                String display = cell.getValue() != null ? cell.getValue().toString() : "";
                System.out.print(display + "\t");
            }
            System.out.println();
        }
    }

    private static String getColumnLetter(int columnIndex) {
        StringBuilder column = new StringBuilder();
        while (columnIndex >= 0) {
            column.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = columnIndex / 26 - 1;
        }
        return column.toString();
    }

    @Test
    void colonSeparatedSumFunction_1row() {
        SpreadsheetRow row = new SpreadsheetRow(1);
        row.addCell(new SpreadsheetCell(1, 1, "1"));
        row.addCell(new SpreadsheetCell(1, 2, "2"));
        row.addCell(new SpreadsheetCell(1, 3, "3"));
        row.addCell(new SpreadsheetCell(1, 4, "4"));
        row.addCell(new SpreadsheetCell(1, 5, "5"));
        SpreadsheetCell formulaCell = new SpreadsheetCell(1, 6, "=+(A1:E1)");
        row.addCell(formulaCell);

        spreadsheetService.evaluateAllFormulas(Collections.singletonList(row));

        Object value = formulaCell.getValue();
        assertEquals(15.0, value);
    }

    @Test
    void colonSeparatedSumFunction_1col() {
        SpreadsheetRow row1 = new SpreadsheetRow(1);
        row1.addCell(new SpreadsheetCell(1, 1, "1"));
        SpreadsheetRow row2 = new SpreadsheetRow(2);
        row2.addCell(new SpreadsheetCell(2, 1, "2"));
        SpreadsheetRow row3 = new SpreadsheetRow(3);
        row3.addCell(new SpreadsheetCell(3, 1, "3"));
        SpreadsheetRow row4 = new SpreadsheetRow(4);
        row4.addCell(new SpreadsheetCell(4, 1, "4"));
        SpreadsheetRow row5 = new SpreadsheetRow(5);
        row5.addCell(new SpreadsheetCell(5, 1, "5"));
        SpreadsheetRow row6 = new SpreadsheetRow(6);
        SpreadsheetCell formulaCell = new SpreadsheetCell(6, 1, "=+(A1:A5)");
        row6.addCell(formulaCell);

        spreadsheetService.evaluateAllFormulas(List.of(row1, row2, row3, row4, row5, row6));

        Object value = formulaCell.getValue();
        assertEquals(15.0, value);
    }

    @Test
    void colonSeparatedSumFunction_1col_moreRowsInFormulaThanRows() {
        SpreadsheetRow row1 = new SpreadsheetRow(1);
        SpreadsheetCell formulaCell = new SpreadsheetCell(6, 1, "=+(A2:A50)");

        row1.addCell(formulaCell);
        SpreadsheetRow row2 = new SpreadsheetRow(2);
        row2.addCell(new SpreadsheetCell(2, 1, "2"));
        SpreadsheetRow row3 = new SpreadsheetRow(3);
        row3.addCell(new SpreadsheetCell(3, 1, "3"));
        SpreadsheetRow row4 = new SpreadsheetRow(4);
        row4.addCell(new SpreadsheetCell(4, 1, "4"));
        SpreadsheetRow row5 = new SpreadsheetRow(5);
        row5.addCell(new SpreadsheetCell(5, 1, "5"));

        spreadsheetService.evaluateAllFormulas(List.of(row1, row2, row3, row4, row5));

        Object value = formulaCell.getValue();
        assertEquals(14.0, value);
    }

    @Test
    void commaSeparatedSumFunction_moreRowsInFormulaThanRows() {
        SpreadsheetRow row1 = new SpreadsheetRow(1);
        SpreadsheetCell formulaCell = new SpreadsheetCell(6, 1, "=+(A2,A7)");

        row1.addCell(formulaCell);
        SpreadsheetRow row2 = new SpreadsheetRow(2);
        row2.addCell(new SpreadsheetCell(2, 1, "2"));
        SpreadsheetRow row3 = new SpreadsheetRow(3);
        row3.addCell(new SpreadsheetCell(3, 1, "3"));
        SpreadsheetRow row4 = new SpreadsheetRow(4);
        row4.addCell(new SpreadsheetCell(4, 1, "4"));
        SpreadsheetRow row5 = new SpreadsheetRow(5);
        row5.addCell(new SpreadsheetCell(5, 1, "5"));

        spreadsheetService.evaluateAllFormulas(List.of(row1, row2, row3, row4, row5));

        Object value = formulaCell.getValue();
        assertEquals(2.0, value); //not existing cell val + 2 -> "" (0) + 2 = 2
    }

    @Test
    void addColumnLeft() {
        int refColumnNumber = 2;
        List<Object> values = new ArrayList<>();
        List<SpreadsheetRow> rows = new ArrayList<>();
        SpreadsheetRow row = new SpreadsheetRow(1);
        SpreadsheetCell cell1 = new SpreadsheetCell(1, 1, "=+(F1,B1)"); // A1
        // here we add new empty column
        SpreadsheetCell cell2 = new SpreadsheetCell(1, 2, "=+(A1,C1)"); // B1
        SpreadsheetCell cell3 = new SpreadsheetCell(1, 3, "=+(A1,5)");  // C1
        SpreadsheetCell cell4 = new SpreadsheetCell(1, 4, "=+(C1,5)");  // D1
        SpreadsheetCell cell5 = new SpreadsheetCell(1, 5, "=+(1,5)");   // E1
        SpreadsheetCell cell6 = new SpreadsheetCell(1, 6, "=+(A1,5)");  // F1
        row.addCell(cell1, cell2, cell3, cell4, cell5, cell6);
        rows.add(row);
        spreadsheetService.addColumnLeft(rows, values, refColumnNumber);

        assertEquals(row.getCells().get(0).getFormula(), "=+(G1,C1)");  // F1 -> G1, B1 -> C1
        assertNull(row.getCells().get(1).getFormula());                        // Newly inserted column (empty cell)
        assertEquals(row.getCells().get(1).getValue(), "");             // Should be empty string
        assertEquals(row.getCells().get(2).getFormula(), "=+(A1,D1)");  // C1 -> D1
        assertEquals(row.getCells().get(3).getFormula(), "=+(A1,5)");   // Unaffected
        assertEquals(row.getCells().get(4).getFormula(), "=+(D1,5)");   // C1 -> D1
        assertEquals(row.getCells().get(5).getFormula(), "=+(1,5)");    // No cell reference, stays the same
        assertEquals(row.getCells().get(6).getFormula(), "=+(A1,5)");   // Unaffected

        assertEquals(row.getCells().get(0).getCellId(), "A1");
        assertEquals(row.getCells().get(1).getCellId(), "B1");
        assertEquals(row.getCells().get(2).getCellId(), "C1");
        assertEquals(row.getCells().get(3).getCellId(), "D1");
        assertEquals(row.getCells().get(4).getCellId(), "E1");
        assertEquals(row.getCells().get(5).getCellId(), "F1");
        assertEquals(row.getCells().get(6).getCellId(), "G1");
    }

    @Test
    void addRowBelow() {
        int refRowNumber = 2;
        List<Object> values = new ArrayList<>();
        List<SpreadsheetRow> rows = new ArrayList<>();

        // row 1
        SpreadsheetRow row1 = new SpreadsheetRow(1);
        SpreadsheetCell cell1 = new SpreadsheetCell(1, 1, "=+(A2,B3)"); // A1
        SpreadsheetCell cell2 = new SpreadsheetCell(1, 2, "=+(C3,5)");  // B1
        row1.addCell(cell1, cell2);
        rows.add(row1);

        // row 2
        SpreadsheetRow row2 = new SpreadsheetRow(2);
        SpreadsheetCell cell3 = new SpreadsheetCell(2, 1, "=+(A1,B2)"); // A2
        SpreadsheetCell cell4 = new SpreadsheetCell(2, 2, "=+(C3,5)");  // B2
        row2.addCell(cell3, cell4);
        rows.add(row2);

        spreadsheetService.addRowBelow(rows, values, refRowNumber);

        // row1 formulas updated: only refs >2 are shifted
        assertEquals("=+(A2,B4)", row1.getCells().get(0).getFormula()); // B3->B4
        assertEquals("=+(C4,5)", row1.getCells().get(1).getFormula());  // C3->C4

        // row2 stays with same references (because it's refRowNumber)
        assertEquals("=+(A1,B2)", row2.getCells().get(0).getFormula());
        assertEquals("=+(C4,5)", row2.getCells().get(1).getFormula());

        // new row 3 should be empty
        SpreadsheetRow newRow3 = rows.get(2);
        assertNull(newRow3.getCells().get(0).getFormula());
        assertEquals("", newRow3.getCells().get(0).getValue());

        // old row3 becomes row4, references updated

        // check IDs
        assertEquals("A1", row1.getCells().get(0).getCellId());
        assertEquals("B1", row1.getCells().get(1).getCellId());

        assertEquals("A2", row2.getCells().get(0).getCellId());
        assertEquals("B2", row2.getCells().get(1).getCellId());

        assertEquals("A3", newRow3.getCells().get(0).getCellId());
        assertEquals("B3", newRow3.getCells().get(1).getCellId());
    }

    @Test
    void addRowAbove() {
        int refRowNumber = 2;
        List<Object> values = new ArrayList<>();
        List<SpreadsheetRow> rows = new ArrayList<>();

        // row 1
        SpreadsheetRow row1 = new SpreadsheetRow(1);
        SpreadsheetCell cell1 = new SpreadsheetCell(1, 1, "=+(A2,B3)"); // A1
        SpreadsheetCell cell2 = new SpreadsheetCell(1, 2, "=+(C2,5)");  // B1
        row1.addCell(cell1, cell2);
        rows.add(row1);

        // row 2
        SpreadsheetRow row2 = new SpreadsheetRow(2);
        SpreadsheetCell cell3 = new SpreadsheetCell(2, 1, "=+(A1,B2)"); // A2
        SpreadsheetCell cell4 = new SpreadsheetCell(2, 2, "=+(C3,5)");  // B2
        row2.addCell(cell3, cell4);
        rows.add(row2);

        spreadsheetService.addRowAbove(rows, values, refRowNumber);

        // check row 1 formulas updated
        assertEquals("=+(A3,B4)", row1.getCells().get(0).getFormula()); // A2->A3, B3->B4
        assertEquals("=+(C3,5)", row1.getCells().get(1).getFormula());  // C2->C3

        // new row 2 should be empty
        SpreadsheetRow newRow2 = rows.get(1);
        assertNull(newRow2.getCells().get(0).getFormula());
        assertEquals("", newRow2.getCells().get(0).getValue());

        // old row2 becomes row3, references updated
        SpreadsheetRow shiftedRow = rows.get(2);
        assertEquals("=+(A1,B3)", shiftedRow.getCells().get(0).getFormula()); // B2->B3
        assertEquals("=+(C4,5)", shiftedRow.getCells().get(1).getFormula());  // C3->C4

        // check IDs
        assertEquals("A1", row1.getCells().get(0).getCellId());
        assertEquals("B1", row1.getCells().get(1).getCellId());

        assertEquals("A2", newRow2.getCells().get(0).getCellId());
        assertEquals("B2", newRow2.getCells().get(1).getCellId());

        assertEquals("A3", shiftedRow.getCells().get(0).getCellId());
        assertEquals("B3", shiftedRow.getCells().get(1).getCellId());
    }

    @Test
    void duplicateRowAndDelete() {
        int refRowNumber = 2;
        List<SpreadsheetRow> rows = new ArrayList<>();

        // row 1
        SpreadsheetRow row1 = new SpreadsheetRow(1);
        SpreadsheetCell cell1 = new SpreadsheetCell(1, 1, "=+(A2,B2)"); // A1
        SpreadsheetCell cell2 = new SpreadsheetCell(1, 2, "=+(C2,5)");  // B1
        row1.addCell(cell1, cell2);
        rows.add(row1);

        // row 2
        SpreadsheetRow row2 = new SpreadsheetRow(2);
        SpreadsheetCell cell3 = new SpreadsheetCell(2, 1, "=+(A1,B2)"); // A2
        SpreadsheetCell cell4 = new SpreadsheetCell(2, 2, "=+(C3,5)");  // B2
        row2.addCell(cell3, cell4);
        rows.add(row2);

        // row 3
        SpreadsheetRow row3 = new SpreadsheetRow(3);
        SpreadsheetCell cell5 = new SpreadsheetCell(3, 1, "=+(A2,B3)"); // A3
        SpreadsheetCell cell6 = new SpreadsheetCell(3, 2, "=+(C2,5)");  // B3
        row3.addCell(cell5, cell6);
        rows.add(row3);

        printSpreadsheet(rows);
        spreadsheetService.duplicateRow(rows, refRowNumber);
        printSpreadsheet(rows);

        // row1 updated (refs to > 2 shift down)
        assertEquals("=+(A2,B2)", row1.getCells().get(0).getFormula());
        assertEquals("=+(C2,5)", row1.getCells().get(1).getFormula());

        // duplicated row (row2 copy becomes new row3)
        SpreadsheetRow duplicatedRow = rows.get(2);
        assertEquals("A3", duplicatedRow.getCells().get(0).getCellId());
        assertEquals("=+(A1,B2)", duplicatedRow.getCells().get(0).getFormula());
        assertEquals("B3", duplicatedRow.getCells().get(1).getCellId());
        assertEquals("=+(C4,5)", duplicatedRow.getCells().get(1).getFormula());

        // old row3 shifted to row4
        SpreadsheetRow shiftedRow = rows.get(3);
        assertEquals("A4", shiftedRow.getCells().get(0).getCellId());
        assertEquals("=+(A2,B4)", shiftedRow.getCells().get(0).getFormula());
        assertEquals("B4", shiftedRow.getCells().get(1).getCellId());
        assertEquals("=+(C2,5)", shiftedRow.getCells().get(1).getFormula());

        // now delete the duplicated row
        spreadsheetService.deleteRow(rows, refRowNumber);

        // after delete we should be back to original
        assertEquals("=+(A2,B2)", row1.getCells().get(0).getFormula());
        assertEquals("=+(C2,5)", row1.getCells().get(1).getFormula());

        SpreadsheetRow restoredRow2 = rows.get(1);
        assertEquals("A2", restoredRow2.getCells().get(0).getCellId());
        assertEquals("=+(A1,B2)", restoredRow2.getCells().get(0).getFormula());
        assertEquals("B2", restoredRow2.getCells().get(1).getCellId());
        assertEquals("=+(C3,5)", restoredRow2.getCells().get(1).getFormula());

        SpreadsheetRow restoredRow3 = rows.get(2);
        assertEquals("A3", restoredRow3.getCells().get(0).getCellId());
        assertEquals("=+(A2,B3)", restoredRow3.getCells().get(0).getFormula());
        assertEquals("B3", restoredRow3.getCells().get(1).getCellId());
        assertEquals("=+(C2,5)", restoredRow3.getCells().get(1).getFormula());
    }

    @Test
    void duplicateColumnAndDelete() {
        int refColumnNumber = 2;
        List<SpreadsheetRow> rows = new ArrayList<>();
        SpreadsheetRow row = new SpreadsheetRow(1);
        SpreadsheetCell cell1 = new SpreadsheetCell(1, 1, "=+(F1,B1)"); // A1
        // here we add new duplicated column
        SpreadsheetCell cell2 = new SpreadsheetCell(1, 2, "=+(A1,C1)"); // B1
        SpreadsheetCell cell3 = new SpreadsheetCell(1, 3, "=+(A1,5)");  // C1
        SpreadsheetCell cell4 = new SpreadsheetCell(1, 4, "=+(C1,5)");  // D1
        SpreadsheetCell cell5 = new SpreadsheetCell(1, 5, "=+(1,5)");   // E1
        SpreadsheetCell cell6 = new SpreadsheetCell(1, 6, "=+(A1,5)");  // F1
        row.addCell(cell1, cell2, cell3, cell4, cell5, cell6);
        rows.add(row);
        spreadsheetService.duplicateColumn(rows, refColumnNumber);

        assertEquals(row.getCells().get(0).getCellId(), "A1");
        assertEquals(row.getCells().get(0).getFormula(), "=+(G1,B1)");

        assertEquals(row.getCells().get(1).getCellId(), "B1");
        assertEquals(row.getCells().get(1).getFormula(), "=+(A1,D1)");

        assertEquals(row.getCells().get(2).getCellId(), "C1");
        assertEquals(row.getCells().get(2).getFormula(), "=+(A1,D1)");

        assertEquals(row.getCells().get(3).getCellId(), "D1");
        assertEquals(row.getCells().get(3).getFormula(), "=+(A1,5)");

        assertEquals(row.getCells().get(4).getCellId(), "E1");
        assertEquals(row.getCells().get(4).getFormula(), "=+(D1,5)");

        assertEquals(row.getCells().get(5).getCellId(), "F1");
        assertEquals(row.getCells().get(5).getFormula(), "=+(1,5)");

        assertEquals(row.getCells().get(6).getCellId(), "G1");
        assertEquals(row.getCells().get(6).getFormula(), "=+(A1,5)");

        spreadsheetService.deleteColumn(rows, refColumnNumber);

        assertEquals(row.getCells().get(0).getCellId(), "A1");
        assertEquals(row.getCells().get(0).getFormula(), "=+(F1,B1)");

        assertEquals(row.getCells().get(1).getCellId(), "B1");
        assertEquals(row.getCells().get(1).getFormula(), "=+(A1,C1)");

        assertEquals(row.getCells().get(2).getCellId(), "C1");
        assertEquals(row.getCells().get(2).getFormula(), "=+(A1,5)");

        assertEquals(row.getCells().get(3).getCellId(), "D1");
        assertEquals(row.getCells().get(3).getFormula(), "=+(C1,5)");

        assertEquals(row.getCells().get(4).getCellId(), "E1");
        assertEquals(row.getCells().get(4).getFormula(), "=+(1,5)");

        assertEquals(row.getCells().get(5).getCellId(), "F1");
        assertEquals(row.getCells().get(5).getFormula(), "=+(A1,5)");

    }

    @Test
    void addColumnRight() {
        int refColumnNumber = 2;
        List<Object> values = new ArrayList<>();
        List<SpreadsheetRow> rows = new ArrayList<>();
        SpreadsheetRow row = new SpreadsheetRow(1);
        SpreadsheetCell cell1 = new SpreadsheetCell(1, 1, "=+(F1,B1)"); // A1
        SpreadsheetCell cell2 = new SpreadsheetCell(1, 2, "=+(A1,C1)"); // B1
        // here we add new empty column

        SpreadsheetCell cell3 = new SpreadsheetCell(1, 3, "=+(A1,5)");  // C1
        SpreadsheetCell cell4 = new SpreadsheetCell(1, 4, "=+(C1,5)");  // D1
        SpreadsheetCell cell5 = new SpreadsheetCell(1, 5, "=+(1,5)");   // E1
        SpreadsheetCell cell6 = new SpreadsheetCell(1, 6, "=+(A1,5)");  // F1
        row.addCell(cell1, cell2, cell3, cell4, cell5, cell6);
        rows.add(row);
        spreadsheetService.addColumnRight(rows, values, refColumnNumber);

        assertEquals(row.getCells().get(0).getCellId(), "A1");
        assertEquals(row.getCells().get(0).getFormula(), "=+(G1,B1)");

        assertEquals(row.getCells().get(1).getCellId(), "B1");
        assertEquals(row.getCells().get(1).getFormula(), "=+(A1,D1)");

        assertEquals(row.getCells().get(2).getCellId(), "C1");
        assertEquals(row.getCells().get(2).getFormula(), null);
        assertEquals(row.getCells().get(2).getValue(), "");

        assertEquals(row.getCells().get(3).getCellId(), "D1");
        assertEquals(row.getCells().get(3).getFormula(), "=+(A1,5)");

        assertEquals(row.getCells().get(4).getCellId(), "E1");
        assertEquals(row.getCells().get(4).getFormula(), "=+(D1,5)");

        assertEquals(row.getCells().get(5).getCellId(), "F1");
        assertEquals(row.getCells().get(5).getFormula(), "=+(1,5)");

        assertEquals(row.getCells().get(6).getCellId(), "G1");
        assertEquals(row.getCells().get(6).getFormula(), "=+(A1,5)");
    }

    @Test
    void evaluateAllFormulas() {
        List<SpreadsheetRow> rows1SimpleFunction = getRows_1_simple_function();
        spreadsheetService.evaluateAllFormulas(rows1SimpleFunction);

        // A1 = =+(1, 2)  ->  3
        assertEquals(3.0, rows1SimpleFunction.get(0).getCell(0).getValue());

        // B2 = =+(A1, 3)  ->  3 + 3 = 6
        assertEquals(6.0, rows1SimpleFunction.get(1).getCell(1).getValue());

        // C3 = 42  ->  should stay 42
        assertEquals(42, rows1SimpleFunction.get(2).getCell(2).getValue());

        // D4 = =+(A1, B2)  ->  3 + 6 = 9
        assertEquals(9.0, rows1SimpleFunction.get(3).getCell(3).getValue());

        assertEquals("Test 12", rows1SimpleFunction.get(0).getCell(1).getValue());
        assertEquals("Test 25", rows1SimpleFunction.get(1).getCell(4).getValue());
    }

    @Test
    void evaluateAllFormulas_reversed() {
        List<SpreadsheetRow> rows1SimpleFunction = getRows_1_simple_reversed_function();
        spreadsheetService.evaluateAllFormulas(rows1SimpleFunction);

        // D4 = =+(1, 2)  ->  3
        assertEquals(3.0, rows1SimpleFunction.get(3).getCell(3).getValue());

        // C3 = =+(A1, 3)  ->  45 + 3 = 48
        assertEquals(48.0, rows1SimpleFunction.get(2).getCell(2).getValue());

        // B2 = 42  ->  should stay 42
        assertEquals(42, rows1SimpleFunction.get(1).getCell(1).getValue());

        // A1 = =+(D4, B2)  ->  3 + 42 = 45
        assertEquals(45.0, rows1SimpleFunction.get(0).getCell(0).getValue());

        // Other text values
        assertEquals("Test 12", rows1SimpleFunction.get(0).getCell(1).getValue());
        assertEquals("Test 25", rows1SimpleFunction.get(1).getCell(4).getValue());
    }

    @Test
    void evaluateAllFormulas_loop_dependency() {
        List<SpreadsheetRow> rows = new ArrayList<>();
        SpreadsheetRow row = new SpreadsheetRow(1);
        SpreadsheetCell cell1 = new SpreadsheetCell(1, 1, "=+(A2,1)");
        SpreadsheetCell cell2 = new SpreadsheetCell(1, 2, "=+(A1,1)");
        row.addCell(cell1, cell2);
        rows.add(row);

        spreadsheetService.evaluateAllFormulas(rows);

        assertEquals("=+(A2,1)", cell1.getValue());
        assertEquals("=+(A1,1)", cell2.getValue());
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
                    // D4 = =+(1, 2)  ->  3
                } else if (rowIndex == 3 && colIndex == 3) {
                    value = "=+(A1,3)";
                    // C3 = =+(A1, 3)  ->  45 + 3 = 48
                } else if (rowIndex == 2 && colIndex == 2) {
                    value = 42;
                    // B2 = 42  ->  should stay 42
                } else if (rowIndex == 1 && colIndex == 1) {
                    value = "=+(D4,B2)";
                    // A1 = =+(D4, B2)  ->  3 + 42 = 45
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