package com.batch.controller.named.baseball;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class League {

    private int id;
    private String name;
    private String shortName;
    private String abbreviation;
    private String color;
    private Nation nation;
    private boolean important;
    // 각 필드에 대한 getter와 setter 필요
}
