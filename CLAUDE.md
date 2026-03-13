# Spreadsheet App - Project Notes

> **IMPORTANT**: Keep this file updated when making significant changes to the codebase. This file serves as persistent memory between Claude Code sessions.

## Overview
Browser-based spreadsheet editor with formula support, row/column operations, and history tracking. Built with Spring Boot + Vaadin. Data stored as JSON in LONGTEXT columns.

## Key Architecture

### Entities

#### Spreadsheet
- `id: UUID`, `name: String` (3-255), `description: String` (3-5000)
- `body: LONGTEXT` — JSON-serialized rows
- `columnsWidthsBody: LONGTEXT` — JSON-serialized column widths
- `rows: List<SpreadsheetRow>` (transient), `columnWidths: Map<Integer,Integer>` (transient)
- `modificationDate`, `deleted: Boolean`, `history: Set<HistorySpreadsheet>`

#### SpreadsheetRow
- `rowNumber: int`, `rowId: UUID`, `cells: List<SpreadsheetCell>`

#### SpreadsheetCell
- `cellId: String` (e.g., "A1"), `rowNumber: int`, `columnNumber: int`
- `value: Object`, `formula: String`
- `cellType: CellType` — AUTO-DETECTED: FORMULA (starts with `=`), NUMBER, TEXT, DATE, BOOLEAN, ERROR, EMPTY

### Services

#### SpreadsheetService
**Formula Evaluation:**
- `evaluateAllFormulas(rows)` — iterative with circular dependency protection (max 10,000 iterations)
- Uses `FormulaParser` to parse and execute formulas

**Row/Column Operations:**
- `addRowAbove/Below(rows, values, refRowNumber)`
- `deleteRow/Column(rows, refRowNumber)` — validates no dependent formulas first
- `duplicateRow/Column(rows, refRowNumber)`
- `addColumnLeft/Right(rows, values, refColumnNumber)`
- Automatically updates cell references in formulas when rows/columns shift
- Uses UUID-based temp placeholders to prevent cascading replacements

### Formula System

#### FormulaParser
- Syntax: `=FunctionName(arg1,arg2,...)`
- Supports cell references (`A1`), ranges (`B2:B10`), constants

#### FunctionRegistry
- Keys: `"F#" + FUNCTION_NAME.toUpperCase()`
- Pluggable via `register(SpreadsheetFunction)`

#### Built-in Functions (7)
| Function | Symbol | Description |
|----------|--------|-------------|
| SumFunction | `+` | Sum of args or range |
| MultiplyFunction | `*` | Product |
| DivisionFunction | `/` | Division |
| SubtractFunction | `-` | Subtraction |
| CurrencyFunction | - | Currency formatting |
| SavingsWithMonthlyContribution | `SAVINGS_M_CONTR` | Future value: `(initialCapital, monthlyContrib, annualRate, years)` |
| TotalSavingsFunction | - | Related savings calculation |

### Views

#### AbstractSpreadsheetsView
- Route: `/spreadsheet-app/spreadsheets`
- List/management grid of all spreadsheets

#### AbstractSpreadsheetView
- Route: `/spreadsheet-app/spreadsheets/{spreadsheetName}`
- `@ClientCallable` methods: `onCellEdit(cellId, value)`, `addRowAbove/Below`, `addColumnLeft/Right`, `deleteRow/Column`, `duplicateRow/Column`, `onColumnResize(columnNumber, width)`
- Toolbar sections: Edit, Data, File
- Find & Replace dialog, Import/Export as JSON

#### HtmlSpreadsheet (Custom Vaadin Component)
- Renders HTML table with column headers (A, B, C…), row numbers
- Editable cells with right-click context menu (`spreadsheet-context-menu.js`)
- Column resize support, cell info overlay

## Utilities

#### SpreadsheetUtils
- `getColumnNumber(String)` — "A" → 1, "Z" → 26, "AA" → 27
- `getColumnHeader(int)` — reverse conversion
- `getRowNumberFromColumn(String)` — "A5" → 5

## Configuration
- `src/main/resources/autoconfig/Spreadsheet.yml` — name (3-255) and description (3-5000) fields

## Important Notes
1. Circular dependency protection: max 10,000 formula evaluation iterations
2. Formula references auto-update when rows/columns are added/deleted
3. Delete operations validate no dependent formulas before proceeding
4. Data stored as JSON in LONGTEXT; backward compatibility for old formats
5. `sortColumns` is not implemented (commented out)
6. History tracking on all spreadsheet changes
7. Soft deletes; multi-tenancy via `BervanOwnedBaseEntity`
8. Tests: `SpreadsheetServiceTest`, `CellTest`, `SpreadsheetUtilsTest`
