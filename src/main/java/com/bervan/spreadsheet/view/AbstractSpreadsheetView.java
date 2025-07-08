package com.bervan.spreadsheet.view;

import com.bervan.common.AbstractPageView;
import com.bervan.common.BervanButton;
import com.bervan.common.service.AuthService;
import com.bervan.core.model.BervanLogger;
import com.bervan.spreadsheet.functions.SpreadsheetFunction;
import com.bervan.spreadsheet.model.Cell;
import com.bervan.spreadsheet.model.HistorySpreadsheet;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.service.HistorySpreadsheetRepository;
import com.bervan.spreadsheet.service.SpreadsheetRepository;
import com.bervan.spreadsheet.service.SpreadsheetService;
import com.bervan.spreadsheet.utils.SpreadsheetUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractSpreadsheetView extends AbstractPageView implements HasUrlParameter<String> {
    public static final String ROUTE_NAME = "/spreadsheet-app/spreadsheets/";
    private static final int MAX_RECURSION_DEPTH = 100;
    private final SpreadsheetService spreadsheetService;
    private final BervanLogger logger;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Div historyHtml = new Div();
    private final Set<Cell> selectedCells = new HashSet<>();
    private final List<SpreadsheetFunction> spreadsheetFunctions;
    // Map for quick access to cells by their ID
    private final Map<String, Cell> cellMap = new HashMap<>();
    @Autowired
    private SpreadsheetRepository spreadsheetRepository;
    @Autowired
    private HistorySpreadsheetRepository historySpreadsheetRepository;
    private Spreadsheet spreadsheetEntity;
    private Div tableHtml;
    private int rows;
    private int columns;
    private Cell[][] cells;
    private boolean historyShow = false;
    private List<HistorySpreadsheet> sorted = new ArrayList<>();
    private Button clearSelectionButton;
    private Cell focusedCell; // Keep track of the currently focused cell

    public AbstractSpreadsheetView(SpreadsheetService service, BervanLogger logger, List<? extends SpreadsheetFunction> spreadsheetFunctions) {
        this.spreadsheetService = service;
        this.logger = logger;
        this.spreadsheetFunctions = (List<SpreadsheetFunction>) spreadsheetFunctions;
    }

    @Override
    public void setParameter(BeforeEvent event, String s) {
        String spreadsheetName = event.getRouteParameters().get("___url_parameter").orElse(UUID.randomUUID().toString());
        init(spreadsheetName);
    }

    private void init(String name) {

        // Load or create Spreadsheet
        List<Spreadsheet> optionalEntity = spreadsheetRepository.findByNameAndDeletedFalseAndOwnersId(name, AuthService.getLoggedUserId());

        if (optionalEntity.size() > 0) {
            spreadsheetEntity = optionalEntity.get(0);
            String body = spreadsheetEntity.getBody();

            // Deserialize body to cells
            try {
                for (Field declaredField : Cell.class.getDeclaredFields()) {
                    declaredField.setAccessible(true);
                }

                cells = objectMapper.readValue(body, Cell[][].class);
                rows = cells.length;
                columns = cells[0].length;

                for (Field declaredField : Cell.class.getDeclaredFields()) {
                    declaredField.setAccessible(false);
                }

                // Rebuild cell map
                rebuildCellMap();

            } catch (Exception e) {
                logger.error(e);
                // Initialize default values on error
                rows = 2;
                columns = 10;
                initializeCells();
            }
        } else {
            spreadsheetEntity = new Spreadsheet(name);
            rows = 2;
            columns = 10;
            initializeCells();
        }

        NewSpreadsheet newSpreadsheet = new NewSpreadsheet(cells);
        this.add(newSpreadsheet);
    }

    private void save() {
        try {
            String body = objectMapper.writeValueAsString(cells);
            spreadsheetEntity.setBody(body);
            spreadsheetRepository.save(spreadsheetEntity);
            reloadHistory();
            showSuccessNotification("Table saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorNotification("Failed to save table.");
        }
    }


    private void reloadHistory() {
        List<HistorySpreadsheet> history = historySpreadsheetRepository.findAllByHistoryOwnerId(spreadsheetEntity.getId());
        sorted = history.stream().sorted(Comparator.comparing(HistorySpreadsheet::getUpdateDate).reversed()).collect(Collectors.toList());
    }

    private void initializeCells() {
        cells = new Cell[rows][columns];
        cellMap.clear();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Cell cell = new Cell("", j, i);
                cell.setHtmlContent("");
                cells[i][j] = cell;
                cellMap.put(cell.getCellId(), cell);
            }
        }
    }

    private void rebuildCellMap() {
        cellMap.clear();
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                Cell cell = cells[i][j];
                // Update cell IDs to start row numbering from 0
                cell.setColumnNumber(j);
                cell.setRowNumber(i);
                cell.setColumnSymbol(getColumnName(j));
                cell.setCellId(cell.getColumnSymbol() + cell.getRowNumber());
                if (cell.getHtmlContent() == null) {
                    cell.setHtmlContent(cell.getValue() != null ? cell.getValue() : "");
                }
                cellMap.put(cell.getCellId(), cell);
            }
        }
    }

    // Method to generate Excel-like column labels
    private String getColumnName(int columnIndex) {
        return SpreadsheetUtils.getColumnHeader(columnIndex);
    }

    @ClientCallable
    public void showSuccessNotification(String message) {
        super.showSuccessNotification(message);
    }

    @ClientCallable
    public void showErrorNotification(String message) {
        super.showErrorNotification(message);
    }
}