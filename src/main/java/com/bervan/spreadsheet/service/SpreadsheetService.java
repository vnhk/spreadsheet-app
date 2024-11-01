package com.bervan.spreadsheet.service;

import com.bervan.common.service.BaseService;
import com.bervan.spreadsheet.model.Spreadsheet;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SpreadsheetService implements BaseService<Spreadsheet> {
    private final SpreadsheetRepository repository;

    public SpreadsheetService(SpreadsheetRepository repository) {
        this.repository = repository;
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
    public Set<Spreadsheet> load() {
        return new HashSet<>(repository.findAll());
    }

    @Override
    public void delete(Spreadsheet item) {
        item.setDeleted(true);
        repository.save(item);
    }

    public Optional<Spreadsheet> loadByName(String spreadsheetName) {
        return repository.findByName(spreadsheetName);
    }
}
