package com.batch.controller.named.baseball;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player {

    private int id;
    private String name;
    private String shortName;
    private String stat;
    // 각 필드에 대한 getter와 setter 필요
}
