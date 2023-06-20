package com.batch.controller.named.baseball;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OddsItem {

    private int id;
    private String type;
    private String result;
    private double optionValue;
    private double odds;
    private Double preOptionValue;
    private Double preOdds;
    private int slot;
    private int sequence;
    private String choiceValue;
    private String updateType;
    private String createAt;
    private String roundGameType;
    private boolean availableFlag;
    private boolean latestFlag;
    // 각 필드에 대한 getter와 setter 필요
}
