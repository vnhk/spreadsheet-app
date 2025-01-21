package com.bervan.spreadsheet.service;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.spreadsheet.model.Spreadsheet;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SpreadsheetService extends BaseService<UUID, Spreadsheet> {

    public SpreadsheetService(SpreadsheetRepository repository, SearchService searchService) {
        super(repository, searchService);
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
    public void delete(Spreadsheet item) {
        item.setDeleted(true);
        repository.save(item);
    }
}
