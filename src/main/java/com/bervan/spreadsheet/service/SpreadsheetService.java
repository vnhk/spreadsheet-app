package com.bervan.spreadsheet.service;

import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.service.BaseService;
import com.bervan.spreadsheet.functions.CellReferenceArgument;
import com.bervan.spreadsheet.functions.FormulaParser;
import com.bervan.spreadsheet.functions.FunctionArgument;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.bervan.spreadsheet.utils.SpreadsheetUtils;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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

    private List<FunctionArgument> getFunctionArgumentsForColonSeparator(String[] splitColon, List<FunctionArgument> functionArguments) {
        String arg1 = splitColon[0].replace("=", "").replace("+", "").replace("(", "").trim();
        String arg2 = splitColon[1].replace(")", "").trim();
        FunctionArgument argument1 = functionArguments.stream().filter(e -> Objects.equals(((CellReferenceArgument) e).getCell().getCellId(), arg1)).findFirst().get();
        Optional<FunctionArgument> arg2Optional = functionArguments.stream().filter(e -> Objects.equals(((CellReferenceArgument) e).getCell().getCellId(), arg2)).findFirst();
        //second parameter can be bigger than amount of rows ex: (B2:B100)
        functionArguments = arg2Optional.map(functionArgument -> List.of(argument1, functionArgument)).orElseGet(() -> List.of(argument1));
        return functionArguments;
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

    public void duplicateColumn(List<SpreadsheetRow> rows, int refColumnNumber) {
        //addColumnRight and copy values
        addColumnRight(rows, null, refColumnNumber);
        for (SpreadsheetRow row : rows) {
            SpreadsheetCell newCell = row.getCell(refColumnNumber);
            SpreadsheetCell oldCell = row.getCell(refColumnNumber - 1);
            if (oldCell.hasFormula()) {
                newCell.setNewValueAndCellRelatedFields(oldCell.getFormula());
            } else {
                newCell.setNewValueAndCellRelatedFields(oldCell.getValue());
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
                    List<FunctionArgument> functionArguments = getFunctionArguments(rows, formula);

                    String[] splitColon = formula.split(":");
                    if (splitColon.length == 2) {
                        functionArguments = getFunctionArgumentsForColonSeparator(splitColon, functionArguments);
                    }

                    for (FunctionArgument argument : functionArguments) {
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
                            formula = updateFormula(formula, oldCellId, columnNumber, rowNumber);
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

    /* deprecated */
    public void moveRowsInFormulasToEnsureBackwardCompatibility(List<SpreadsheetRow> rows) { //old row = 0 -> new row = 1
        //first update formulas
        for (SpreadsheetRow row : rows) {
            for (SpreadsheetCell cell : row.getCells()) {
                if (cell.hasFormula()) {
                    String formula = cell.getFormula();
                    List<FunctionArgument> functionArguments = getFunctionArguments(rows, formula);

                    String[] splitColon = formula.split(":");
                    if (splitColon.length == 2) {
                        functionArguments = getFunctionArgumentsForColonSeparator(splitColon, functionArguments);
                    }
                    for (FunctionArgument argument : functionArguments) {
                        formula = updateFormula(formula, argument);
                    }
                    cell.setNewValueAndCellRelatedFields(formula);
                }
            }
        }
    }

    private String updateFormula(String formula, FunctionArgument argument) {
        if (argument instanceof CellReferenceArgument) {
            SpreadsheetCell cellInFormula = ((CellReferenceArgument) argument).getCell();
            String oldCellId = cellInFormula.getCellId();
            int columnNumber = cellInFormula.getColumnNumber();
            int rowNumber = cellInFormula.getRowNumber();
            rowNumber++;
            formula = updateFormula(formula, oldCellId, columnNumber, rowNumber);
        }
        return formula;
    }

    @NotNull
    private String updateFormula(String formula, String oldCellId, int columnNumber, int rowNumber) {
        String newCellId = SpreadsheetUtils.getColumnHeader(columnNumber) + rowNumber;
        formula = formula.replaceAll(oldCellId + ",", newCellId + ","); //to prevent replacing in (B11,B1) -> B1 to B2
        formula = formula.replaceAll(oldCellId + "\\)", newCellId + ")");
        formula = formula.replaceAll(oldCellId + " ", newCellId + " ");
        formula = formula.replaceAll(oldCellId + ":", newCellId + ":");
        return formula;
    }

    public void deleteColumn(List<SpreadsheetRow> rows, int refColumnNumber) {
        //first update formulas
        for (SpreadsheetRow row : rows) {
            for (SpreadsheetCell cell : row.getCells()) {
                if (cell.hasFormula()) {
                    String formula = cell.getFormula();
                    for (FunctionArgument argument : getFunctionArguments(rows, formula)) {
                        if (argument instanceof CellReferenceArgument) {
                            SpreadsheetCell cellInFormula = ((CellReferenceArgument) argument).getCell();
                            String oldCellId = cellInFormula.getCellId();
                            int columnNumber = cellInFormula.getColumnNumber();
                            int rowNumber = cellInFormula.getRowNumber();

                            if (columnNumber > refColumnNumber) {
                                columnNumber--;
                            }
                            formula = updateFormula(formula, oldCellId, columnNumber, rowNumber);
                        }
                    }
                    cell.setNewValueAndCellRelatedFields(formula);
                }
            }
        }

        //delete column with given ref column number
        for (SpreadsheetRow row : rows) {
            SpreadsheetCell toBeRemoved = null;
            for (SpreadsheetCell cell : row.getCells()) {
                if (cell.getColumnNumber() == refColumnNumber) {
                    toBeRemoved = cell;
                    break;
                }
            }
            row.getCells().remove(toBeRemoved);
        }

        //then update columnNumbers
        for (SpreadsheetRow row : rows) {
            for (SpreadsheetCell cell : row.getCells()) {

                if (cell.getColumnNumber() >= refColumnNumber) {
                    cell.updateColumnNumber(cell.getColumnNumber() - 1);
                }
            }
        }
    }

    public List<FunctionArgument> getFunctionArguments(List<SpreadsheetRow> rows, String formula) {
        return formulaParser.extractFunctionArguments(formula, rows);
    }

    public Optional<Spreadsheet> loadByName(String spreadsheetName) {
        SearchRequest request = new SearchRequest();
        request.addCriterion("BY_NAME", Operator.AND_OPERATOR, Spreadsheet.class, "name", SearchOperation.EQUALS_OPERATION, spreadsheetName);
        Set<Spreadsheet> load = load(request, Pageable.ofSize(1));
        return load.stream().findFirst();
    }
}
