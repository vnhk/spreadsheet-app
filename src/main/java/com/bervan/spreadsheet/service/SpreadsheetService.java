package com.bervan.spreadsheet.service;

import com.bervan.common.service.AuthService;
import com.bervan.common.service.BaseService;
import com.bervan.ieentities.ExcelIEEntity;
import com.bervan.spreadsheet.model.Spreadsheet;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public Set<Spreadsheet> load() {
        return new HashSet<>(repository.findAll());
    }

    @Override
    public void delete(Spreadsheet item) {
        item.setDeleted(true);
        repository.save(item);
    }

    @Override
    public void saveIfValid(List<? extends ExcelIEEntity> objects) {
        throw new RuntimeException("Not supported yet!");
    }

    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public List<Spreadsheet> loadByName(String spreadsheetName) {
        return repository.findByNameAndDeletedFalseAndOwnersId(spreadsheetName, AuthService.getLoggedUserId());
    }
}
