package com.bervan.spreadsheet.service;

import com.bervan.history.model.BaseRepository;
import com.bervan.spreadsheet.model.Spreadsheet;

import java.util.List;
import java.util.UUID;

public interface SpreadsheetRepository extends BaseRepository<Spreadsheet, UUID> {
    List<Spreadsheet> findByNameAndDeletedFalseAndOwnersId(String name, UUID ownerId);
}
