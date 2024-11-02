package com.bervan.spreadsheet.model;

import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinTableColumn;
import com.bervan.history.model.AbstractBaseEntity;
import com.bervan.history.model.HistoryCollection;
import com.bervan.history.model.HistorySupported;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;


@Entity
@HistorySupported
public class Spreadsheet implements AbstractBaseEntity<UUID>, PersistableTableData {
    @Id
    @GeneratedValue
    private UUID id;
    @VaadinTableColumn(internalName = "name", displayName = "Name")
    private String name;
    @VaadinTableColumn(internalName = "description", displayName = "Description")
    private String description;
    @Column(columnDefinition = "LONGTEXT")
    private String body;
    @Column(columnDefinition = "LONGTEXT")
    private String columnsConfig;
    @Transient
    private List<SpreadsheetRow> rows = new ArrayList<>();
    @Transient
    private List<ColumnConfig> configs = new ArrayList<>();
    private Integer columnCount = 10;
    private LocalDateTime modificationDate;
    private Boolean deleted = false;

    @OneToMany(fetch = FetchType.EAGER)
    @HistoryCollection(historyClass = HistorySpreadsheet.class)
    private Set<HistorySpreadsheet> history = new HashSet<>();

    public Spreadsheet() {

    }

    public Spreadsheet(String name) {
        this.name = name;
        for (int i = 0; i < 5; i++) {
            addRow();
        }
    }

    public List<SpreadsheetRow> getRows() {
        return rows;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void addRow() {
        SpreadsheetRow newRow = new SpreadsheetRow(columnCount);
        rows.add(newRow);
        updateRowsAndCellsNumber();
    }

    public void updateRowsAndCellsNumber() {
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).number = i;
            for (int i1 = 0; i1 < rows.get(i).getCells().size(); i1++) {
                String columnHeader = getColumnHeader(i1);
                rows.get(i).getCells().get(i1).cellId = columnHeader + i;
            }
        }
    }

    private String getColumnHeader(int columnIndex) {
        StringBuilder label = new StringBuilder();
        while (columnIndex >= 0) {
            label.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = columnIndex / 26 - 1;
        }
        return label.toString();
    }

    public void addColumn() {
        columnCount++;
        for (SpreadsheetRow row : rows) {
            row.addCell(columnCount + 1);
        }
    }

    public void duplicateRow(SpreadsheetRow row) {
        int oldRowIndex = 0;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).rowId.equals(row.rowId)) {
                oldRowIndex = i;
            }
        }
        SpreadsheetRow duplicatedRow = new SpreadsheetRow(row);
        rows.add(oldRowIndex + 1, duplicatedRow);
        updateRowsAndCellsNumber();
    }

    public void removeRow(SpreadsheetRow row) {
        rows.remove(row);
        updateRowsAndCellsNumber();
    }

    public void duplicateColumn(int columnIndex) {
        columnCount++;
        for (SpreadsheetRow row : rows) {
            row.addCell(columnIndex + 1, row.getCell(columnIndex));  // Duplicate the cell value
        }
        updateRowsAndCellsNumber();
    }

    public void removeColumn(int columnIndex) {
        if (columnCount > 1) {
            columnCount--;
            for (SpreadsheetRow row : rows) {
                row.removeCell(columnIndex);
            }
        }
        updateRowsAndCellsNumber();
    }

    public String getName() {
        return name;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String getTableFilterableColumnValue() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public LocalDateTime getModificationDate() {
        return this.modificationDate;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Set<HistorySpreadsheet> getHistory() {
        return history;
    }

    public void setHistory(Set<HistorySpreadsheet> history) {
        this.history = history;
    }

    public void setRows(List<SpreadsheetRow> rows) {
        this.rows = rows;
    }

    public String getColumnsConfig() {
        return columnsConfig;
    }

    public void setColumnsConfig(String columnConfig) {
        this.columnsConfig = columnConfig;
    }

    public List<ColumnConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(List<ColumnConfig> configs) {
        this.configs = configs;
    }
}