package com.batch.model;

import lombok.Data;

@Data
public class BasketballModel {
    String gameId;
    String league;
    String ground;

    String date;
    String dayOfWeek;
    String time;

    Integer aTeamRestDay;
    String aTeam;
    Integer aTeamTotalPoint;
    Integer bTeamTotalPoint;
    String bTeam;
    Integer bTeamRestDay;

    Double handiCap;
    String handiCapResult;
    String odd;


    Double pointLine;
    String pointLineResult;

    Integer aTeamFirstQPoint;
    Integer bTeamFirstQPoint;

    Double firstQHandiCap;
    String firstQHandiCapResult;
    Double firstQPointLine;
    String firstQPointLineResult;

    Integer aTeamSecondQPoint;
    Integer bTeamSecondQPoint;
    String secondQHandiCapResult;
    String secondQPointLineResult;

    Integer aTeamThirdQPoint;
    Integer bTeamThirdQPoint;
    String thirdQHandiCapResult;
    String thirdQPointLineResult;

    Integer aTeamFourthQPoint;
    Integer bTeamFourthQPoint;
    String fourthQHandiCapResult;
    String fourthQPointLineResult;

    Integer aTeamExtendQPoint;
    Integer bTeamExtendQPoint;

    Integer aTeamFirstHalfPoint;
    Integer bTeamFirstHalfPoint;
    Double halfHandiCap;
    String firstHalfHandiCapResult;
    Double halfPointLine;
    String firstHalfPointLineResult;
    Integer aTeamSecondHalfPoint;
    Integer bTeamSecondHalfPoint;
    String secondHalfHandiCapResult;
    String secondHalfPointLineResult;

    String firstQFirstPoint;
    String firstQFirstFreeTwo;
    String firstQFirstTwoPoint;
    String firstQFirstThreePoint;

    String secondQFirstPoint;
    String secondQFirstFreeTwo;
    String secondQFirstTwoPoint;
    String secondQFirstThreePoint;

    String thirdQFirstPoint;
    String thirdQFirstFreeTwo;
    String thirdQFirstTwoPoint;
    String thirdQFirstThreePoint;

    String fourthQFirstPoint;
    String fourthQFirstFreeTwo;
    String fourthQFirstTwoPoint;
    String fourthQFirstThreePoint;

    Integer firstQTotalPoint;
    Integer secondQTotalPoint;
    Integer thirdQTotalPoint;
    Integer fourthQTotalPoint;
    Integer extendQTotalPoint;

//    Integer aTeamFirstQPoint;
//    Integer aTeamSecondQPoint;
//    Integer aTeamThirdQPoint;
//    Integer aTeamFourthQPoint;
//    Integer aTeamExtendQPoint;
//
//    Integer bTeamFirstQPoint;
//    Integer bTeamSecondQPoint;
//    Integer bTeamThirdQPoint;
//    Integer bTeamFourthQPoint;
//    Integer bTeamExtendQPoint;

}
