package com.bervan.spreadsheet.service;

import com.bervan.history.model.BaseRepository;
import com.bervan.spreadsheet.model.Spreadsheet;

import java.util.Optional;
import java.util.UUID;

public interface SpreadsheetRepository extends BaseRepository<Spreadsheet, UUID> {
    Optional<Spreadsheet> findByNameAndDeletedFalseAndOwnerId(String name, UUID ownerId);
}
