package com.bervan.spreadsheet.service;

import com.bervan.history.model.BaseRepository;
import com.bervan.spreadsheet.model.HistorySpreadsheet;

import java.util.List;
import java.util.UUID;

public interface HistorySpreadsheetRepository extends BaseRepository<HistorySpreadsheet, UUID> {
    List<HistorySpreadsheet> findAllByHistoryOwnerId(UUID id);
}
