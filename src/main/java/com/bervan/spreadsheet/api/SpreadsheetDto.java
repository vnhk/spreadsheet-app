package com.bervan.spreadsheet.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpreadsheetDto {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime modificationDate;
}
