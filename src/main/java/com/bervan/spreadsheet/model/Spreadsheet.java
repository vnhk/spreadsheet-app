package com.bervan.spreadsheet.model;

import com.bervan.common.model.BervanOwnedBaseEntity;
import com.bervan.common.model.PersistableTableOwnedData;
import com.bervan.history.model.HistoryCollection;
import com.bervan.history.model.HistorySupported;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@HistorySupported
public class Spreadsheet extends BervanOwnedBaseEntity<UUID> implements PersistableTableOwnedData<UUID> {
    @Id
    private UUID id;
    private String name;
    private String description;
    @Column(columnDefinition = "LONGTEXT")
    private String body;
    @Column(columnDefinition = "LONGTEXT")
    private String columnsWidthsBody;
    @Transient
    private List<SpreadsheetRow> rows = new ArrayList<>();
    @Transient
    private Map<Integer, Integer> columnWidths = new HashMap<>();
    private LocalDateTime modificationDate;
    private Boolean deleted = false;

    @OneToMany(fetch = FetchType.EAGER)
    @HistoryCollection(historyClass = HistorySpreadsheet.class)
    private Set<HistorySpreadsheet> history = new HashSet<>();

    public Spreadsheet() {

    }

    public Spreadsheet(String name) {
        this.name = name;
    }

    public List<SpreadsheetRow> getRows() {
        return rows;
    }

    public void setRows(List<SpreadsheetRow> rows) {
        this.rows = rows;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getTableFilterableColumnValue() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public Boolean isDeleted() {
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

    public Map<Integer, Integer> getColumnWidths() {
        return columnWidths;
    }

    public void setColumnWidths(Map<Integer, Integer> columnWidths) {
        this.columnWidths = columnWidths;
    }

    public String getColumnsWidthsBody() {
        return columnsWidthsBody;
    }

    public void setColumnsWidthsBody(String columnConfig) {
        this.columnsWidthsBody = columnConfig;
    }
}