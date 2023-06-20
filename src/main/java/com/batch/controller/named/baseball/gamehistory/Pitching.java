package com.batch.controller.named.baseball.gamehistory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pitching {

    public int sequence;
    public boolean entered;
    public boolean currentPlaying;
    public boolean startingMember;
    public String inningPitched;
    public int pitchCount;
    public int strike;
    public int ball;
    public int run;
    public int earnedRun;
    public int hit;
    public int homeRun;
    public int baseOnBalls;
    public int strikeOuts;
    public String todayEarnedRunAverage;
    public int seasonGameCount;
    public String seasonInningPitched;
    public int seasonStrikeOuts;
    public Player player;
    public String earnedRunAverage;
    public int wins;
    public int losses;
    public int saves;
    public int holds;
}
