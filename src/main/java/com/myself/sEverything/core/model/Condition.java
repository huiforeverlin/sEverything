package com.myself.sEverything.core.model;

import lombok.Data;

@Data
public class Condition {
    private String name;
    private String fileType;
    private Integer limit;
    private Boolean orderByAsc;
    private Long interval;
}
