package com.bervan.spreadsheet.model;

import com.bervan.history.model.AbstractBaseHistoryEntity;
import com.bervan.history.model.HistoryField;
import com.bervan.history.model.HistoryOwnerEntity;
import com.bervan.history.model.HistorySupported;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@HistorySupported
public class HistorySpreadsheet implements AbstractBaseHistoryEntity<UUID> {
    @Id
    private UUID id;
    @HistoryField
    private String name;
    @HistoryField
    private String description;
    @Column(columnDefinition = "LONGTEXT")
    @HistoryField
    private String body;
    @Column(columnDefinition = "LONGTEXT")
    @HistoryField
    private String columnsConfig;
    @HistoryField
    private Integer columnCount;

    private LocalDateTime modificationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @HistoryOwnerEntity
    private Spreadsheet historyOwner;

    public HistorySpreadsheet() {

    }

    public String getName() {
        return name;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
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

    @Override
    public LocalDateTime getUpdateDate() {
        return modificationDate;
    }

    @Override
    public void setUpdateDate(LocalDateTime updateDate) {
        this.modificationDate = updateDate;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }

    public Spreadsheet getHistoryOwner() {
        return historyOwner;
    }

    public void setHistoryOwner(Spreadsheet historyOwner) {
        this.historyOwner = historyOwner;
    }

    public String getColumnsConfig() {
        return columnsConfig;
    }

    public void setColumnsConfig(String columnConfig) {
        this.columnsConfig = columnConfig;
    }
}