package com.grs.core.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthYear implements Serializable {

    Integer month;
    Integer year;

    public MonthYear(Integer month, Integer year) {
        this.month = month;
        this.year = year;
    }
}
