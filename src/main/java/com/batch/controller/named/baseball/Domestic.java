package com.batch.controller.named.baseball;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Domestic {

    private String roundGameType;
    private List<OddsItem> odds;
    // 각 필드에 대한 getter와 setter 필요
}
