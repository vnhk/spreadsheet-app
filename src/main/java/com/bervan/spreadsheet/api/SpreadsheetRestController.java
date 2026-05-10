package com.bervan.spreadsheet.api;

import com.bervan.common.service.AuthService;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.bervan.spreadsheet.service.SpreadsheetRepository;
import com.bervan.spreadsheet.service.SpreadsheetRowConverter;
import com.bervan.spreadsheet.service.SpreadsheetService;
import com.google.common.reflect.TypeToken;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/spreadsheet")
@RolesAllowed("USER")
public class SpreadsheetRestController {

    private final SpreadsheetService spreadsheetService;
    private final SpreadsheetRepository spreadsheetRepository;
    private static final Gson gson = new GsonBuilder().create();

    public SpreadsheetRestController(SpreadsheetService spreadsheetService,
                                     SpreadsheetRepository spreadsheetRepository) {
        this.spreadsheetService = spreadsheetService;
        this.spreadsheetRepository = spreadsheetRepository;
    }

    private Optional<Spreadsheet> findOwned(UUID id) {
        UUID userId = AuthService.getLoggedUserId();
        return spreadsheetRepository.findByDeletedFalseAndOwnersId(userId).stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    private List<SpreadsheetRow> parseAndEvaluate(String body) {
        List<SpreadsheetRow> rows = SpreadsheetRowConverter.deserializeSpreadsheetBody(body);
        if (rows != null) {
            spreadsheetService.evaluateAllFormulas(rows);
        }
        return rows != null ? rows : new ArrayList<>();
    }

    private Map<Integer, Integer> parseColumnWidths(String columnWidthsBody) {
        if (columnWidthsBody == null || columnWidthsBody.isBlank()) return new HashMap<>();
        try {
            TypeToken<Map<Integer, Integer>> tt = new TypeToken<>() {};
            return gson.fromJson(columnWidthsBody, tt.getType());
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }

    @GetMapping
    public ResponseEntity<List<SpreadsheetDto>> list() {
        UUID userId = AuthService.getLoggedUserId();
        List<SpreadsheetDto> dtos = spreadsheetRepository.findByDeletedFalseAndOwnersId(userId).stream()
                .map(s -> new SpreadsheetDto(s.getId(), s.getName(), s.getDescription(), s.getModificationDate()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<SpreadsheetDto> create(@RequestBody CreateSpreadsheetRequest req) {
        Spreadsheet s = new Spreadsheet(req.getName());
        s.setId(UUID.randomUUID());
        s.setDescription(req.getDescription());
        List<SpreadsheetRow> rows = new ArrayList<>();
        for (int r = 1; r <= 10; r++) {
            SpreadsheetRow row = new SpreadsheetRow(r);
            for (int c = 1; c <= 5; c++) {
                row.addCell(new SpreadsheetCell(r, c, ""));
            }
            rows.add(row);
        }
        s.setBody(SpreadsheetRowConverter.serializeSpreadsheetBody(rows));
        s.setModificationDate(LocalDateTime.now());
        Spreadsheet saved = spreadsheetService.save(s);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SpreadsheetDto(saved.getId(), saved.getName(), saved.getDescription(), saved.getModificationDate()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return findOwned(id)
                .map(s -> { spreadsheetService.delete(s); return ResponseEntity.<Void>noContent().build(); })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpreadsheetDto> updateMeta(@PathVariable UUID id,
                                                      @RequestBody CreateSpreadsheetRequest req) {
        return findOwned(id).map(s -> {
            if (req.getName() != null) s.setName(req.getName());
            if (req.getDescription() != null) s.setDescription(req.getDescription());
            s.setModificationDate(LocalDateTime.now());
            Spreadsheet saved = spreadsheetService.save(s);
            return ResponseEntity.ok(new SpreadsheetDto(saved.getId(), saved.getName(), saved.getDescription(), saved.getModificationDate()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/data")
    public ResponseEntity<SpreadsheetDataDto> getData(@PathVariable UUID id) {
        return findOwned(id).map(s -> {
            List<SpreadsheetRow> rows = new ArrayList<>();
            if (s.getBody() != null && !s.getBody().isBlank()) {
                List<SpreadsheetRow> parsed = SpreadsheetRowConverter.deserializeSpreadsheetBody(s.getBody());
                if (parsed != null) {
                    rows = parsed;
                    spreadsheetService.evaluateAllFormulas(rows);
                }
            }
            Map<Integer, Integer> widths = parseColumnWidths(s.getColumnsWidthsBody());
            return ResponseEntity.ok(new SpreadsheetDataDto(s.getId(), s.getName(), s.getDescription(), rows, widths));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/data")
    public ResponseEntity<SpreadsheetDataDto> saveData(@PathVariable UUID id,
                                                        @RequestBody SaveDataRequest req) {
        return findOwned(id).map(s -> {
            if (req.getBody() != null) s.setBody(req.getBody());
            if (req.getColumnWidthsBody() != null) s.setColumnsWidthsBody(req.getColumnWidthsBody());
            s.setModificationDate(LocalDateTime.now());
            spreadsheetService.save(s);
            List<SpreadsheetRow> rows = parseAndEvaluate(s.getBody());
            Map<Integer, Integer> widths = parseColumnWidths(req.getColumnWidthsBody());
            return ResponseEntity.ok(new SpreadsheetDataDto(s.getId(), s.getName(), s.getDescription(), rows, widths));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/evaluate")
    public ResponseEntity<List<SpreadsheetRow>> evaluate(@PathVariable UUID id,
                                                          @RequestBody EvaluateRequest req) {
        if (findOwned(id).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(parseAndEvaluate(req.getBody()));
    }

    @PostMapping("/{id}/row")
    public ResponseEntity<List<SpreadsheetRow>> rowOperation(@PathVariable UUID id,
                                                               @RequestBody RowOperationRequest req) {
        if (findOwned(id).isEmpty()) return ResponseEntity.notFound().build();
        List<SpreadsheetRow> rows = SpreadsheetRowConverter.deserializeSpreadsheetBody(req.getBody());
        if (rows == null) return ResponseEntity.badRequest().build();

        switch (req.getAction()) {
            case "ADD_ABOVE" -> spreadsheetService.addRowAbove(rows, null, req.getRowNumber());
            case "ADD_BELOW" -> spreadsheetService.addRowBelow(rows, null, req.getRowNumber());
            case "DUPLICATE" -> spreadsheetService.duplicateRow(rows, req.getRowNumber());
            case "DELETE"    -> spreadsheetService.deleteRow(rows, req.getRowNumber());
        }
        spreadsheetService.evaluateAllFormulas(rows);
        return ResponseEntity.ok(rows);
    }

    @PostMapping("/{id}/column")
    public ResponseEntity<List<SpreadsheetRow>> columnOperation(@PathVariable UUID id,
                                                                  @RequestBody ColumnOperationRequest req) {
        if (findOwned(id).isEmpty()) return ResponseEntity.notFound().build();
        List<SpreadsheetRow> rows = SpreadsheetRowConverter.deserializeSpreadsheetBody(req.getBody());
        if (rows == null) return ResponseEntity.badRequest().build();

        switch (req.getAction()) {
            case "ADD_LEFT"  -> spreadsheetService.addColumnLeft(rows, null, req.getColumnNumber());
            case "ADD_RIGHT" -> spreadsheetService.addColumnRight(rows, null, req.getColumnNumber());
            case "DUPLICATE" -> spreadsheetService.duplicateColumn(rows, req.getColumnNumber());
            case "DELETE"    -> spreadsheetService.deleteColumn(rows, req.getColumnNumber());
        }
        spreadsheetService.evaluateAllFormulas(rows);
        return ResponseEntity.ok(rows);
    }
}
