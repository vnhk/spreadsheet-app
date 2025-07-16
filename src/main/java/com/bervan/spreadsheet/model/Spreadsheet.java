package com.bervan.spreadsheet.model;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinBervanColumn;
import com.bervan.history.model.HistoryCollection;
import com.bervan.history.model.HistorySupported;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@HistorySupported
public class Spreadsheet extends BervanBaseEntity<UUID> implements PersistableTableData<UUID> {
    @Id
    private UUID id;
    @VaadinBervanColumn(internalName = "name", displayName = "Name")
    private String name;
    @VaadinBervanColumn(internalName = "description", displayName = "Description")
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

    public Optional<SpreadsheetCell> getCell(String cellId) {
        for (SpreadsheetRow row : rows) {
            Optional<SpreadsheetCell> any = row.getCells().stream().filter(e -> e.getCellId().equals(cellId)).findAny();
            if (any.isPresent()) {
                return any;
            }
        }
        return Optional.empty();
    }
}