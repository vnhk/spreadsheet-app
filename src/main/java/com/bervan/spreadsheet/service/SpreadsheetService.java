package com.bervan.spreadsheet.service;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.spreadsheet.functions.CellReferenceArgument;
import com.bervan.spreadsheet.functions.FormulaParser;
import com.bervan.spreadsheet.functions.FunctionArgument;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.bervan.spreadsheet.utils.SpreadsheetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class SpreadsheetService extends BaseService<UUID, Spreadsheet> {
    private final FormulaParser formulaParser;

    public SpreadsheetService(SpreadsheetRepository repository, SearchService searchService, FormulaParser formulaParser) {
        super(repository, searchService);
        this.formulaParser = formulaParser;
    }

    public static SpreadsheetCell findCellById(List<SpreadsheetRow> rows, String cellId) {
        for (SpreadsheetRow row : rows) {
            for (SpreadsheetCell cell : row.getCells()) {
                if (cell.getCellId().equals(cellId)) {
                    return cell;
                }
            }
        }

        return null;
    }

    @Override
    public void save(List<Spreadsheet> data) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Spreadsheet save(Spreadsheet data) {
        return repository.save(data);
    }

    @Override
    public void delete(Spreadsheet item) {
        item.setDeleted(true);
        repository.save(item);
    }

    public void evaluateAllFormulas(List<SpreadsheetRow> rows) {
        List<SpreadsheetCell> formulas = rows
                .stream()
                .map(SpreadsheetRow::getCells)
                .flatMap(Collection::parallelStream)
                .filter(SpreadsheetCell::hasFormula).toList();

        Set<String> correctlyCalculatedFormulas = new HashSet<>();
        int maxIterations = 10000; // Avoid infinite loops in case of circular dependencies

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            boolean anyChanged = false;
            for (SpreadsheetCell cell : formulas) {

                String cellId = cell.getCellId();
                if (!correctlyCalculatedFormulas.contains(cellId)) {
                    Object currentValue = cell.getValue();
                    try {
                        FunctionArgument result = formulaParser.evaluate(cell.getFormula(), rows);
                        Object newValue = result.asObject();

                        // Only mark changed if value has actually changed
                        if (!newValue.equals(currentValue)) {
                            cell.updateValue(newValue);
                            anyChanged = true;
                            correctlyCalculatedFormulas.add(cellId);
                        }
                    } catch (Exception e) {
                        // Only log error in final iteration
                        if (iteration == maxIterations - 1) {
                            log.warn("Failed to evaluate formula {} in cell {}: {}", cell.getFormula(), cellId, e.getMessage());
                            cell.setNewValueAndCellRelatedFields("#ERR");
                        }
                    }
                }
            }

            if (!anyChanged) {
                break; // No changes, we can stop
            }
        }
    }

    public void addColumnLeft(List<SpreadsheetRow> rows, List<Object> values, int refColumnNumber) {
        addColumnOnLeftOrRight(rows, values, refColumnNumber, false);
    }

    public void addColumnRight(List<SpreadsheetRow> rows, List<Object> values, int refColumnNumber) {
        addColumnOnLeftOrRight(rows, values, refColumnNumber, true);
    }

    private void addColumnOnLeftOrRight(List<SpreadsheetRow> rows, List<Object> values, int refColumnNumber, boolean addColumnOnRight) {
        if (values != null && !values.isEmpty() && values.size() != rows.size()) {
            throw new IllegalArgumentException("Size of values does not match amount of rows.");
        }
        // values can be null or empty and then cells value will be empty

        //first update formulas
        for (SpreadsheetRow row : rows) {
            for (SpreadsheetCell cell : row.getCells()) {
                if (cell.hasFormula()) {
                    String formula = cell.getFormula();
                    for (FunctionArgument argument : formulaParser.extractFunctionArguments(formula, rows)) {
                        if (argument instanceof CellReferenceArgument) {
                            SpreadsheetCell cellInFormula = ((CellReferenceArgument) argument).getCell();
                            String oldCellId = cellInFormula.getCellId();
                            int columnNumber = cellInFormula.getColumnNumber();
                            int rowNumber = cellInFormula.getRowNumber();

                            if (addColumnOnRight) {
                                if (columnNumber > refColumnNumber) {
                                    columnNumber++;
                                }
                            } else {
                                if (columnNumber >= refColumnNumber) {
                                    columnNumber++;
                                }
                            }
                            String newCellId = SpreadsheetUtils.getColumnHeader(columnNumber) + rowNumber;
                            formula = formula.replaceAll(oldCellId, newCellId);
                        }
                    }
                    cell.setNewValueAndCellRelatedFields(formula);
                }
            }
        }

        //then update columnNumbers
        for (SpreadsheetRow row : rows) {
            for (SpreadsheetCell cell : row.getCells()) {

                if (addColumnOnRight) {
                    if (cell.getColumnNumber() > refColumnNumber) {
                        cell.updateColumnNumber(cell.getColumnNumber() + 1);
                    }
                } else {
                    if (cell.getColumnNumber() >= refColumnNumber) {
                        cell.updateColumnNumber(cell.getColumnNumber() + 1);
                    }
                }
            }
        }

        for (int i = 0; i < rows.size(); i++) {
            if (addColumnOnRight) {
                if (values == null || values.isEmpty()) {
                    rows.get(i).getCells().add(refColumnNumber, new SpreadsheetCell(rows.get(i).rowNumber, refColumnNumber + 1, ""));
                } else {
                    rows.get(i).getCells().add(refColumnNumber, new SpreadsheetCell(rows.get(i).rowNumber, refColumnNumber + 1, values.get(i)));
                }
            } else {
                if (values == null || values.isEmpty()) {
                    rows.get(i).getCells().add(refColumnNumber - 1, new SpreadsheetCell(rows.get(i).rowNumber, refColumnNumber, ""));
                } else {
                    rows.get(i).getCells().add(refColumnNumber - 1, new SpreadsheetCell(rows.get(i).rowNumber, refColumnNumber, values.get(i)));
                }
            }

        }
    }
}
