package com.bervan.spreadsheet.service;

import com.bervan.common.service.AuthService;
import com.bervan.common.service.BaseService;
import com.bervan.spreadsheet.model.Spreadsheet;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SpreadsheetService implements BaseService<UUID, Spreadsheet> {
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
    @PostFilter("filterObject.owner != null && filterObject.owner.getId().equals(T(com.bervan.common.service.AuthService).getLoggedUserId())")
    public Set<Spreadsheet> load() {
        return new HashSet<>(repository.findAll());
    }

    @Override
    public void delete(Spreadsheet item) {
        item.setDeleted(true);
        repository.save(item);
    }

    @PostFilter("filterObject.owner != null && filterObject.owner.getId().equals(T(com.bervan.common.service.AuthService).getLoggedUserId())")
    public Optional<Spreadsheet> loadByName(String spreadsheetName) {
        return repository.findByNameAndDeletedFalseAndOwnerId(spreadsheetName, AuthService.getLoggedUserId());
    }
}
