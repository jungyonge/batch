package com.batch.controller.named.baseball;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Odds {

    private List<OddsItem> domesticWinLoseOdds;
    private List<OddsItem> domesticUnderOverOdds;
    private List<OddsItem> domesticHandicapOdds;
    private List<International> internationalWinLoseOdds;
    private List<International> internationalUnderOverOdds;
    private List<International> internationalHandicapOdds;
    // 각 필드에 대한 getter와 setter 필요
}
