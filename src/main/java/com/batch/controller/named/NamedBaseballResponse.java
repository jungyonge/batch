package com.batch.controller.named;


import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NamedBaseballResponse {

    private int id;
    private String sportsType;
    private League league;
    private Season season;
    private String startDatetime;
    private String realStartDateTime;
    private String venueName;
    private String gameStatus;
    private String result;
    private Broadcast broadcast;
    private int period;
    private String displayTime;
    private String videoLink;
    private boolean hasLineup;
    private boolean oddsFlag;
    private boolean boardFlag;
    private Teams teams;
    private String inningDivision;
    private boolean firstBaseOccupied;
    private boolean secondBaseOccupied;
    private boolean thirdBaseOccupied;
    private int strike;
    private int out;
    private int ball;
    private Player currentPitcher;
    private Player currentBatter;
    private Special special;
    private RepresentativeOdds representativeOdds;
    private Odds odds;
    private String betbleUri;
    private boolean betbleLiveDisableFlag;
    private boolean hasManualRelay;
    private int cheerCount;
    private int viewCount;
    private int analysisCount;
    private boolean majorHot;
    private boolean hot;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class League {

    private int id;
    private String name;
    private String shortName;
    private String abbreviation;
    private String color;
    private Nation nation;
    private boolean important;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class Nation {

    private int id;
    private String name;
    private String imgPath;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class Season {

    private int id;
    private String name;
    private String shortName;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class Broadcast {

    private Score score;
    private String playText;
    private String locationType;
    private String eventType;
    private String displayTime;
    private int period;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class Score {

    private int home;
    private int away;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class Teams {

    private Team home;
    private Team away;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class Team {

    private int id;
    private String name;
    private String shortName;
    private String imgPath;
    private List<PeriodData> periodData;
    private int hitCount;
    private int errorCount;
    private int baseOnBallCount;
    private Player startPitcher;
    private SeasonRecord seasonRecord;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class PeriodData {

    private int period;
    private int score;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class Player {

    private int id;
    private String name;
    private String shortName;
    private String stat;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class SeasonRecord {

    private int ranking;
    private int gameCount;
    private int winCount;
    private int defeatCount;
    private int drawCount;
    private String winPercentage;
    private String streak;
    private String performance;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class Special {

    private FirstEvent firstHomerun;
    private FirstEvent firstStrikeOut;
    private FirstEvent firstBaseOnBall;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class FirstEvent {

    private int period;
    private String location;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class RepresentativeOdds {

    private Domestic domestic;
    private International international;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class Domestic {

    private String roundGameType;
    private List<OddsItem> odds;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class International {
    // setter와 getter가 필요합니다.
}

@Getter
@Setter
class Odds {

    private List<OddsItem> domesticWinLoseOdds;
    private List<OddsItem> domesticUnderOverOdds;
    private List<OddsItem> domesticHandicapOdds;
    private List<International> internationalWinLoseOdds;
    private List<International> internationalUnderOverOdds;
    private List<International> internationalHandicapOdds;
    // 각 필드에 대한 getter와 setter 필요
}

@Getter
@Setter
class OddsItem {

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


