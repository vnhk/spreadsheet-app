package com.bervan.spreadsheet.api;

import com.bervan.common.config.EntityConfigValidator;
import com.bervan.common.controller.BaseOwnedController;
import com.bervan.common.mapper.BervanDTOMapper;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.bervan.spreadsheet.service.SpreadsheetService;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/spreadsheet")
@RolesAllowed("USER")
public class SpreadsheetRestController extends BaseOwnedController<Spreadsheet, UUID> {

    private final SpreadsheetService spreadsheetService;

    public SpreadsheetRestController(SpreadsheetService spreadsheetService,
                                     BervanDTOMapper mapper,
                                     EntityConfigValidator validator) {
        super(spreadsheetService, mapper, validator, "Spreadsheet");
        this.spreadsheetService = spreadsheetService;
    }

    @GetMapping
    public ResponseEntity<List<SpreadsheetDto>> list(
            @RequestParam MultiValueMap<String, String> allParams,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<SpreadsheetDto> result = super.search(allParams, page, size, SpreadsheetDto.class, Spreadsheet.class).getBody();
        return ResponseEntity.ok(result != null ? result.getContent() : List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpreadsheetDto> getById(@PathVariable UUID id) {
        return super.getById(id, SpreadsheetDto.class);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateSpreadsheetRequest req) {
        // initialise with default 10x5 grid before delegating to base
        List<SpreadsheetRow> rows = new ArrayList<>();
        for (int r = 1; r <= 10; r++) {
            com.bervan.spreadsheet.model.SpreadsheetRow row = new com.bervan.spreadsheet.model.SpreadsheetRow(r);
            for (int c = 1; c <= 5; c++) {
                row.addCell(new com.bervan.spreadsheet.model.SpreadsheetCell(r, c, ""));
            }
            rows.add(row);
        }
        req.setId(null); // ensure base generates a new id
        ResponseEntity<?> res = super.create(req, SpreadsheetDto.class);
        // after base saves, persist the default body and return 201
        if (res.getStatusCode().is2xxSuccessful() && res.getBody() instanceof SpreadsheetDto dto) {
            spreadsheetService.loadById(dto.getId()).ifPresent(s -> {
                s.setBody(com.bervan.spreadsheet.service.SpreadsheetRowConverter.serializeSpreadsheetBody(rows));
                spreadsheetService.save(s);
            });
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        }
        return res;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMeta(@PathVariable UUID id, @RequestBody CreateSpreadsheetRequest req) {
        req.setId(id);
        return super.update(req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        return super.delete(id);
    }

    // ── Spreadsheet-specific endpoints ──────────────────────────────────────

    @GetMapping("/{id}/data")
    public ResponseEntity<SpreadsheetDataDto> getData(@PathVariable UUID id) {
        return spreadsheetService.loadById(id).map(s -> {
            List<SpreadsheetRow> rows = new ArrayList<>();
            if (s.getBody() != null && !s.getBody().isBlank()) {
                List<SpreadsheetRow> parsed = com.bervan.spreadsheet.service.SpreadsheetRowConverter.deserializeSpreadsheetBody(s.getBody());
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
        return spreadsheetService.loadById(id).map(s -> {
            if (req.getBody() != null) s.setBody(req.getBody());
            if (req.getColumnWidthsBody() != null) s.setColumnsWidthsBody(req.getColumnWidthsBody());
            s.setModificationDate(java.time.LocalDateTime.now());
            spreadsheetService.save(s);
            List<SpreadsheetRow> rows = parseAndEvaluate(s.getBody());
            Map<Integer, Integer> widths = parseColumnWidths(req.getColumnWidthsBody());
            return ResponseEntity.ok(new SpreadsheetDataDto(s.getId(), s.getName(), s.getDescription(), rows, widths));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/evaluate")
    public ResponseEntity<List<SpreadsheetRow>> evaluate(@PathVariable UUID id,
                                                         @RequestBody EvaluateRequest req) {
        if (spreadsheetService.loadById(id).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(parseAndEvaluate(req.getBody()));
    }

    @PostMapping("/{id}/row")
    public ResponseEntity<List<SpreadsheetRow>> rowOperation(@PathVariable UUID id,
                                                             @RequestBody RowOperationRequest req) {
        if (spreadsheetService.loadById(id).isEmpty()) return ResponseEntity.notFound().build();
        List<SpreadsheetRow> rows = com.bervan.spreadsheet.service.SpreadsheetRowConverter.deserializeSpreadsheetBody(req.getBody());
        if (rows == null) return ResponseEntity.badRequest().build();
        switch (req.getAction()) {
            case "ADD_ABOVE" -> spreadsheetService.addRowAbove(rows, null, req.getRowNumber());
            case "ADD_BELOW" -> spreadsheetService.addRowBelow(rows, null, req.getRowNumber());
            case "DUPLICATE" -> spreadsheetService.duplicateRow(rows, req.getRowNumber());
            case "DELETE" -> spreadsheetService.deleteRow(rows, req.getRowNumber());
        }
        spreadsheetService.evaluateAllFormulas(rows);
        return ResponseEntity.ok(rows);
    }

    @PostMapping("/{id}/column")
    public ResponseEntity<List<SpreadsheetRow>> columnOperation(@PathVariable UUID id,
                                                                @RequestBody ColumnOperationRequest req) {
        if (spreadsheetService.loadById(id).isEmpty()) return ResponseEntity.notFound().build();
        List<SpreadsheetRow> rows = com.bervan.spreadsheet.service.SpreadsheetRowConverter.deserializeSpreadsheetBody(req.getBody());
        if (rows == null) return ResponseEntity.badRequest().build();
        switch (req.getAction()) {
            case "ADD_LEFT" -> spreadsheetService.addColumnLeft(rows, null, req.getColumnNumber());
            case "ADD_RIGHT" -> spreadsheetService.addColumnRight(rows, null, req.getColumnNumber());
            case "DUPLICATE" -> spreadsheetService.duplicateColumn(rows, req.getColumnNumber());
            case "DELETE" -> spreadsheetService.deleteColumn(rows, req.getColumnNumber());
        }
        spreadsheetService.evaluateAllFormulas(rows);
        return ResponseEntity.ok(rows);
    }

    private List<SpreadsheetRow> parseAndEvaluate(String body) {
        List<SpreadsheetRow> rows = com.bervan.spreadsheet.service.SpreadsheetRowConverter.deserializeSpreadsheetBody(body);
        if (rows != null) spreadsheetService.evaluateAllFormulas(rows);
        return rows != null ? rows : new ArrayList<>();
    }

    private Map<Integer, Integer> parseColumnWidths(String body) {
        if (body == null || body.isBlank()) return new java.util.HashMap<>();
        try {
            TypeToken<Map<Integer, Integer>> tt = new TypeToken<>() {
            };
            return new GsonBuilder().create().fromJson(body, tt.getType());
        } catch (Exception ignored) {
            return new java.util.HashMap<>();
        }
    }
}
