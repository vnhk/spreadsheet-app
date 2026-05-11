package com.bervan.spreadsheet.api;

import com.bervan.core.model.BaseDTO;
import com.bervan.core.model.BaseModel;
import com.bervan.spreadsheet.model.Spreadsheet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSpreadsheetRequest implements BaseDTO<UUID> {
    private UUID id;
    private String name;
    private String description;

    @Override
    public Class<? extends BaseModel<UUID>> dtoTarget() {
        return Spreadsheet.class;
    }
}
