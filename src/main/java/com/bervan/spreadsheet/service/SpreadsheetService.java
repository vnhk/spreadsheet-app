package com.bervan.spreadsheet.service;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.spreadsheet.functions.FormulaParser;
import com.bervan.spreadsheet.functions.FunctionArgument;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SpreadsheetService extends BaseService<UUID, Spreadsheet> {
    private final FormulaParser formulaParser;

    public SpreadsheetService(SpreadsheetRepository repository, SearchService searchService, FormulaParser formulaParser) {
        super(repository, searchService);
        this.formulaParser = formulaParser;
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
        for (SpreadsheetRow row : rows) {
            for (SpreadsheetCell cell : row.getCells()) {
                if (cell.hasFormula()) {
                    try {
                        FunctionArgument result = formulaParser.evaluate(cell.getFormula(), rows);
                        cell.setValue(result.asObject());
                    } catch (Exception e) {
                        log.warn("Failed to evaluate formula {} in cell {}: {}", cell.getFormula(), cell.getCellId(), e.getMessage());
                        cell.setValue("#ERR");
                    }
                }
            }
        }
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
}
