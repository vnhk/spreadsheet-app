# spreadsheet-app

Browser-based spreadsheet editor with formula support, row/column operations, and revision history. Data stored as JSON in `LONGTEXT` columns.

## Features

- **Formulas**: `=FunctionName(args)` syntax with cell references (`A1`), ranges (`B2:B10`), and constants
- **Row/column ops**: Add above/below, delete, duplicate — with automatic formula reference updates
- **Circular dependency protection**: Max 10,000 evaluation iterations
- **Delete validation**: Blocks deletion of rows/columns that are referenced by formulas
- **Find & Replace**: Across the entire spreadsheet
- **Import/Export**: JSON format
- **History**: Full audit trail on all changes

## Built-in Functions

| Function | Symbol | Description |
|----------|--------|-------------|
| Sum | `+` | Sum of args or range |
| Multiply | `*` | Product |
| Division | `/` | Division |
| Subtract | `-` | Subtraction |
| Currency | — | Currency formatting |
| `SAVINGS_M_CONTR` | — | Future value with monthly contributions |

## Key Entities

| Entity | Description |
|--------|-------------|
| `Spreadsheet` | Name, description, JSON body, JSON column widths |
| `SpreadsheetRow` | Row number + list of cells |
| `SpreadsheetCell` | Cell ID (e.g., `A1`), value, formula, auto-detected type |

## Cell Types (auto-detected)

`FORMULA` (starts with `=`), `NUMBER`, `TEXT`, `DATE`, `BOOLEAN`, `ERROR`, `EMPTY`

## Routes (`/spreadsheet-app/`)

| Path | Purpose |
|------|---------|
| `spreadsheets` | List and manage spreadsheets |
| `spreadsheets/{name}` | Open and edit a spreadsheet |

## Build

```bash
mvn clean install -DskipTests
```

Part of the `my-tools` multi-module Maven project. Requires `common` to be built first.
