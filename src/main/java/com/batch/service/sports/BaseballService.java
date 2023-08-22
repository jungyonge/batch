package com.batch.service.sports;

import static java.lang.Math.round;

import com.batch.controller.named.baseball.FirstEvent;
import com.batch.controller.named.baseball.International;
import com.batch.controller.named.baseball.NamedBaseballResponse;
import com.batch.controller.named.baseball.OddsItem;
import com.batch.controller.named.baseball.Team;
import com.batch.controller.named.baseball.gamehistory.Away;
import com.batch.controller.named.baseball.gamehistory.Home;
import com.batch.controller.named.baseball.gamehistory.NamedGameHistoryResponse;
import com.batch.mapper.BaseballMapper;
import com.batch.model.BaseballModel;
import com.batch.service.NamedService;
import com.batch.util.NamedUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BaseballService {

    private final NamedUtil namedUtil;

    private final NamedService namedService;

    private final ModelMapper modelMapper = new ModelMapper();

    private final BaseballMapper baseballMapper;


    @Transactional
    public List<BaseballModel> convertBaseballModel(List<NamedBaseballResponse> baseballList) {
        return null;
    }

    public List<BaseballModel> convertBaseballModel()
        throws ParseException {

        NamedBaseballResponse[] responses = namedService.getBaseball("2022-06-21");
        for(NamedBaseballResponse baseball : responses){
            BaseballModel homeModel = new BaseballModel();
            BaseballModel awayModel = new BaseballModel();
            List<BaseballModel> reuslt = new ArrayList<>();

            homeModel.setGameId(String.valueOf(baseball.getId()));
            awayModel.setGameId(String.valueOf(baseball.getId()));
            homeModel.setLeague(baseball.getLeague().getName());
            awayModel.setLeague(baseball.getLeague().getName());
            homeModel.setStadium(baseball.getVenueName());
            awayModel.setStadium(baseball.getVenueName());

            if(!homeModel.getLeague().equals("KBO") &&
                !homeModel.getLeague().equals("NPB") &&
                !homeModel.getLeague().equals("MLB") &&
                !homeModel.getLeague().equals("퓨처스") ){
                continue;
            }

            parseBaseData(baseball, homeModel, awayModel);
            parsePitcherData(String.valueOf(baseball.getId()), homeModel, awayModel);
            parseInningScore(baseball, homeModel, awayModel);
            parsePitcherAndOdd(baseball, homeModel, awayModel);
//            parseBaseOnBall(baseball, homeModel, awayModel);
            parseInningHandiUnover(baseball, homeModel, awayModel);
            parseSpecial(baseball, homeModel, awayModel);
            reuslt.add(homeModel);
            reuslt.add(awayModel);

            for(BaseballModel model : reuslt){
                baseballMapper.insertBaseballMatch(model);
                baseballMapper.updateBaseballStat(model);

            }
        }

        return null;
    }

    private void parseBaseData(NamedBaseballResponse parseTarget, BaseballModel homeModel, BaseballModel awayModel)
        throws ParseException {

        Calendar cal = Calendar.getInstance();

        String startDatetime = parseTarget.getStartDatetime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        String[] startDatetimeArr = startDatetime.split("T");

        cal = Calendar.getInstance();
        cal.setTime(format.parse(startDatetime));
        int dayNum = cal.get(Calendar.DAY_OF_WEEK);

        String date = startDatetimeArr[0];
        String dayOfWeek = namedUtil.getDayoOfWeek(dayNum);
        String time = startDatetimeArr[1].substring(0, 5);

        homeModel.setDate(date);
        homeModel.setDayOfWeek(dayOfWeek);
        homeModel.setTime(time);

        awayModel.setDate(date);
        awayModel.setDayOfWeek(dayOfWeek);
        awayModel.setTime(time);

        homeModel.setGround("홈");
        awayModel.setGround("원정");

        homeModel.setATeam(parseTarget.getTeams().getHome().getName());
        homeModel.setBTeam(parseTarget.getTeams().getAway().getName());

        awayModel.setATeam(homeModel.getBTeam());
        awayModel.setBTeam(homeModel.getATeam());

    }

    private void parsePitcherData(String gameId, BaseballModel homeModel, BaseballModel awayModel) {

        NamedGameHistoryResponse historyResponse = namedService.getPitcher(gameId);

        Home homeHistory = historyResponse.getHome();
        Away awayHistory = historyResponse.getAway();


        homeModel.setATeamPitcher(homeHistory.getPitchings().get(0).getPlayer().getDisplayName());
        homeModel.setBTeamPitcher(awayHistory.getPitchings().get(0).getPlayer().getDisplayName());

        homeModel.setPitchCount(homeHistory.getPitchings().get(0).getPitchCount());
        homeModel.setSeasonWins(homeHistory.getPitchings().get(0).getWins());
        homeModel.setSeasonLosses(homeHistory.getPitchings().get(0).getLosses());
        homeModel.setInningPitched(
            Double.valueOf(homeHistory.getPitchings().get(0).getInningPitched()));
        homeModel.setHit(homeHistory.getPitchings().get(0).getHit());
        homeModel.setHomeRun(homeHistory.getPitchings().get(0).getHomeRun());
        homeModel.setBaseOnBalls(homeHistory.getPitchings().get(0).getBaseOnBalls());
        homeModel.setStrikeOuts(homeHistory.getPitchings().get(0).getStrikeOuts());
        homeModel.setRun(homeHistory.getPitchings().get(0).getRun());
        homeModel.setEarnedRun(homeHistory.getPitchings().get(0).getEarnedRun());
        homeModel.setTodayEarnedRunAverage(
            Double.valueOf(homeHistory.getPitchings().get(0).getTodayEarnedRunAverage()));
        homeModel.setSeasonEarnedRunAverage(
            Double.valueOf(homeHistory.getPitchings().get(0).getEarnedRunAverage()));

        awayModel.setATeamPitcher(awayHistory.getPitchings().get(0).getPlayer().getDisplayName());
        awayModel.setBTeamPitcher(homeHistory .getPitchings().get(0).getPlayer().getDisplayName());

        awayModel.setPitchCount(awayHistory.getPitchings().get(0).getPitchCount());
        awayModel.setSeasonWins(awayHistory.getPitchings().get(0).getWins());
        awayModel.setSeasonLosses(awayHistory.getPitchings().get(0).getLosses());
        awayModel.setInningPitched(
            Double.valueOf(awayHistory.getPitchings().get(0).getInningPitched()));
        awayModel.setHit(awayHistory.getPitchings().get(0).getHit());
        awayModel.setHomeRun(awayHistory.getPitchings().get(0).getHomeRun());
        awayModel.setBaseOnBalls(awayHistory.getPitchings().get(0).getBaseOnBalls());
        awayModel.setStrikeOuts(awayHistory.getPitchings().get(0).getStrikeOuts());
        awayModel.setRun(awayHistory.getPitchings().get(0).getRun());
        awayModel.setEarnedRun(awayHistory.getPitchings().get(0).getEarnedRun());
        awayModel.setTodayEarnedRunAverage(
            Double.valueOf(awayHistory.getPitchings().get(0).getTodayEarnedRunAverage()));
        awayModel.setSeasonEarnedRunAverage(
            Double.valueOf(awayHistory.getPitchings().get(0).getEarnedRunAverage()));

    }

    private void parseInningScore(NamedBaseballResponse parseTarget, BaseballModel homeModel, BaseballModel awayModel)  {

        Team home = parseTarget.getTeams().getHome();
        Team away = parseTarget.getTeams().getAway();

        int homeScore = 0;
        int awayScore = 0;
        for (int i = 0; i < 9; i++) {
            if(i == 2) {
                homeModel.setATeamThirdPoint(homeScore);
                awayModel.setATeamThirdPoint(awayScore);

                homeModel.setBTeamThirdPoint(awayScore);
                awayModel.setBTeamThirdPoint(homeScore);

            }

            if (i == 3) {
                homeModel.setATeamFourthPoint(homeScore);
                awayModel.setATeamFourthPoint(awayScore);

                homeModel.setBTeamFourthPoint(awayScore);
                awayModel.setBTeamFourthPoint(homeScore);

            }

            if (i == 4) {
                homeModel.setATeamFifthPoint(homeScore);
                awayModel.setATeamFifthPoint(awayScore);

                homeModel.setBTeamFifthPoint(awayScore);
                awayModel.setBTeamFifthPoint(homeScore);
            }

            homeScore = homeScore + home.getPeriodData().get(i).getScore();
            awayScore = awayScore + away.getPeriodData().get(i).getScore();
        }

        homeModel.setFirstScore(home.getPeriodData().get(0).getScore());
        awayModel.setFirstScore(away.getPeriodData().get(0).getScore());

        homeModel.setSecondScore(home.getPeriodData().get(1).getScore());
        awayModel.setSecondScore(away.getPeriodData().get(1).getScore());

        homeModel.setThirdScore(home.getPeriodData().get(2).getScore());
        awayModel.setThirdScore(away.getPeriodData().get(2).getScore());

        homeModel.setFourthScore(home.getPeriodData().get(3).getScore());
        awayModel.setFourthScore(away.getPeriodData().get(3).getScore());

        homeModel.setFifthScore(home.getPeriodData().get(4).getScore());
        awayModel.setFifthScore(away.getPeriodData().get(4).getScore());

        homeModel.setSixthScore(home.getPeriodData().get(5).getScore());
        awayModel.setSixthScore(away.getPeriodData().get(5).getScore());

        homeModel.setSeventhScore(home.getPeriodData().get(6).getScore());
        awayModel.setSeventhScore(away.getPeriodData().get(6).getScore());

        homeModel.setEighthScore(home.getPeriodData().get(7).getScore());
        awayModel.setEighthScore(away.getPeriodData().get(7).getScore());

        homeModel.setNinthScore(home.getPeriodData().get(8).getScore());
        awayModel.setNinthScore(away.getPeriodData().get(8).getScore());

        homeModel.setATeamTotalPoint(homeScore);
        awayModel.setATeamTotalPoint(awayScore);

        homeModel.setBTeamTotalPoint(awayScore);
        awayModel.setBTeamTotalPoint(homeScore);
    }

    private void parsePitcherAndOdd(NamedBaseballResponse matchObject , BaseballModel homeModel , BaseballModel awayModel) {

        Team home = matchObject.getTeams().getHome();
        Team away = matchObject.getTeams().getAway();

        homeModel.setATeamPitcher(home.getStartPitcher().getName());
        awayModel.setBTeamPitcher(home.getStartPitcher().getName());

        homeModel.setBTeamPitcher(away.getStartPitcher().getName());
        awayModel.setATeamPitcher(away.getStartPitcher().getName());

        double handi = 0.0;
        double unOver = 0.0;
        double firstInningPointLine = 0.0;

        List<OddsItem> domesticHandicapOdds = matchObject.getOdds().getDomesticHandicapOdds();
        List<OddsItem> domesticUnderOverOdds = matchObject.getOdds().getDomesticUnderOverOdds();

        List<International> internationalHandicapOdds = matchObject.getOdds()
            .getInternationalHandicapOdds();
        List<International> internationalUnderOverOdds = matchObject.getOdds()
            .getInternationalUnderOverOdds();

        if(!internationalHandicapOdds.isEmpty() ){
            handi = internationalHandicapOdds.get(0).getOptionValue();
        }else if (!domesticHandicapOdds.isEmpty()) {
            handi = domesticHandicapOdds.get(0).getOptionValue();
        }

        if(internationalUnderOverOdds.size() > 0 ){
            unOver = internationalUnderOverOdds.get(0).getOptionValue();
            firstInningPointLine = unOver / 9;
        }else if (domesticUnderOverOdds.size() > 0  ){
            unOver = domesticUnderOverOdds.get(0).getOptionValue();
            firstInningPointLine = unOver / 9;
        }

        homeModel.setHandiCap(handi);
        awayModel.setHandiCap(handi * -1);

        homeModel.setPointLine(unOver);
        awayModel.setPointLine(unOver);

        if (homeModel.getHandiCap() > 0) {
            homeModel.setOdd("역배");
            awayModel.setOdd("정배");

            homeModel.setThirdHandiCap(0.5);
            awayModel.setThirdHandiCap(-0.5);

            homeModel.setFourthHandiCap(0.5);
            awayModel.setFourthHandiCap(-0.5);

            homeModel.setFifthHandiCap(0.5);
            awayModel.setFifthHandiCap(-0.5);

        } else if (homeModel.getHandiCap() < 0) {
            homeModel.setOdd("정배");
            awayModel.setOdd("역배");

            homeModel.setThirdHandiCap(-0.5);
            awayModel.setThirdHandiCap(0.5);

            homeModel.setFourthHandiCap(-0.5);
            awayModel.setFourthHandiCap(0.5);

            homeModel.setFifthHandiCap(-0.5);
            awayModel.setFifthHandiCap(0.5);
        } else {
            homeModel.setOdd("없음");
            awayModel.setOdd("없음");

            homeModel.setThirdHandiCap(0.0);
            awayModel.setThirdHandiCap(0.0);

            homeModel.setFourthHandiCap(0.0);
            awayModel.setFourthHandiCap(0.0);

            homeModel.setFifthHandiCap(0.0);
            awayModel.setFifthHandiCap(0.0);
        }

        if(unOver == 0){

        }
        double thirdPointLine = firstInningPointLine * 4;
        int thirdPointLineInt = (int) thirdPointLine;
        double pointLine = thirdPointLine - thirdPointLineInt;

        if ((pointLine <= 0.333 && pointLine >= 0.001)) {
            homeModel.setThirdPointLine((double) thirdPointLineInt - 0.5);
            awayModel.setThirdPointLine((double) thirdPointLineInt - 0.5);

        } else if ((pointLine <= 0.999 && pointLine >= 0.666)) {
            homeModel.setThirdPointLine((double) (thirdPointLineInt + 1) - 0.5);
            awayModel.setThirdPointLine((double) (thirdPointLineInt + 1) - 0.5);

        } else if ((pointLine <= 0.665 && pointLine >= 0.334)) {
            homeModel.setThirdPointLine((double) thirdPointLineInt);
            awayModel.setThirdPointLine((double) thirdPointLineInt);

        } else {
            homeModel.setThirdPointLine((double) round(thirdPointLineInt));
            awayModel.setThirdPointLine((double) round(thirdPointLineInt));

        }

        double forthPointLine = firstInningPointLine * 4;
        int forthPointLineInt = (int) forthPointLine;
        pointLine = forthPointLine - forthPointLineInt;

        if ((pointLine <= 0.333 && pointLine >= 0.001)) {
            homeModel.setFourthPointLine((double) forthPointLineInt);
            awayModel.setFourthPointLine((double) forthPointLineInt);

        } else if ((pointLine <= 0.999 && pointLine >= 0.666)) {
            homeModel.setFourthPointLine((double) (forthPointLineInt + 1));
            awayModel.setFourthPointLine((double) (forthPointLineInt + 1));

        } else if ((pointLine <= 0.665 && pointLine >= 0.334)) {
            homeModel.setFourthPointLine(forthPointLineInt + 0.5);
            awayModel.setFourthPointLine(forthPointLineInt + 0.5);

        } else {
            homeModel.setFourthPointLine((double) round(forthPointLine));
            awayModel.setFourthPointLine((double) round(forthPointLine));

        }

        double fifthPointLine = firstInningPointLine * 5;
        int fifthPointLineInt = (int) fifthPointLine;
        pointLine = fifthPointLine - fifthPointLineInt;

        if ((pointLine <= 0.333 && pointLine >= 0.001)) {
            homeModel.setFifthPointLine((double) fifthPointLineInt);
            awayModel.setFifthPointLine((double) fifthPointLineInt);

        } else if ((pointLine <= 0.999 && pointLine >= 0.666)) {
            homeModel.setFifthPointLine((double) (fifthPointLineInt + 1));
            awayModel.setFifthPointLine((double) (fifthPointLineInt + 1));

        } else if ((pointLine <= 0.665 && pointLine >= 0.334)) {
            homeModel.setFifthPointLine(fifthPointLineInt + 0.5);
            awayModel.setFifthPointLine(fifthPointLineInt + 0.5);

        } else {
            homeModel.setFifthPointLine((double) round(fifthPointLine));
            awayModel.setFifthPointLine((double) round(fifthPointLine));

        }

    }

    private void parseInningHandiUnover( NamedBaseballResponse matchObject , BaseballModel homeModel , BaseballModel awayMoel) {
        if (homeModel.getHandiCap() == 0) {
            homeModel.setHandiCapResult("적특");
            awayMoel.setHandiCapResult("적특");

        } else {
            if ((homeModel.getATeamTotalPoint() + homeModel.getHandiCap()) > homeModel.getBTeamTotalPoint()) {
                homeModel.setHandiCapResult("승리");
                awayMoel.setHandiCapResult("패배");

            } else if ((homeModel.getATeamTotalPoint() + homeModel.getHandiCap()) < homeModel.getBTeamTotalPoint()) {
                homeModel.setHandiCapResult("패배");
                awayMoel.setHandiCapResult("승리");

            } else {
                homeModel.setHandiCapResult("적특");
                awayMoel.setHandiCapResult("적특");
            }
        }

        if (homeModel.getThirdHandiCap() == 0) {
            homeModel.setThirdHandiCapResult("적특");
            awayMoel.setThirdHandiCapResult("적특");

        } else {
            if ((homeModel.getATeamThirdPoint() + homeModel.getThirdHandiCap()) > homeModel.getBTeamThirdPoint()) {
                homeModel.setThirdHandiCapResult("승리");
                awayMoel.setThirdHandiCapResult("패배");

            } else if ((homeModel.getATeamThirdPoint() + homeModel.getThirdHandiCap()) < homeModel.getBTeamThirdPoint()) {
                homeModel.setThirdHandiCapResult("패배");
                awayMoel.setThirdHandiCapResult("승리");

            } else {
                homeModel.setThirdHandiCapResult("적특");
                awayMoel.setThirdHandiCapResult("적특");
            }
        }

        if (homeModel.getFourthHandiCap() == 0) {
            homeModel.setFourthHandiCapResult("적특");
            awayMoel.setFourthHandiCapResult("적특");

        } else {
            if ((homeModel.getATeamFourthPoint() + homeModel.getFourthHandiCap()) > homeModel.getBTeamFourthPoint()) {
                homeModel.setFourthHandiCapResult("승리");
                awayMoel.setFourthHandiCapResult("패배");

            } else if ((homeModel.getATeamFourthPoint() + homeModel.getFourthHandiCap()) < homeModel.getBTeamFourthPoint()) {
                homeModel.setFourthHandiCapResult("패배");
                awayMoel.setFourthHandiCapResult("승리");

            } else {
                homeModel.setFourthHandiCapResult("적특");
                awayMoel.setFourthHandiCapResult("적특");
            }
        }

        if (homeModel.getFifthHandiCap() == 0) {
            homeModel.setFifthHandiCapResult("적특");
            awayMoel.setFifthHandiCapResult("적특");

        } else {
            if ((homeModel.getATeamFifthPoint() + homeModel.getFifthHandiCap()) > homeModel.getBTeamFifthPoint()) {
                homeModel.setFifthHandiCapResult("승리");
                awayMoel.setFifthHandiCapResult("패배");

            } else if ((homeModel.getATeamFifthPoint() + homeModel.getFifthHandiCap()) < homeModel.getBTeamFifthPoint()) {
                homeModel.setFifthHandiCapResult("패배");
                awayMoel.setFifthHandiCapResult("승리");

            } else {
                homeModel.setFifthHandiCapResult("적특");
                awayMoel.setFifthHandiCapResult("적특");
            }
        }

        if (homeModel.getPointLine() == 0) {
            homeModel.setPointLineResult("적특");
            awayMoel.setPointLineResult("적특");

        } else {
            if ((homeModel.getATeamTotalPoint() + homeModel.getBTeamTotalPoint()) > homeModel.getPointLine()) {
                homeModel.setPointLineResult("오버");
                awayMoel.setPointLineResult("오버");

            } else if ((homeModel.getATeamTotalPoint() + homeModel.getBTeamTotalPoint()) < homeModel.getPointLine()) {
                homeModel.setPointLineResult("언더");
                awayMoel.setPointLineResult("언더");

            } else {
                homeModel.setPointLineResult("적특");
                awayMoel.setPointLineResult("적특");
            }
        }

        if (homeModel.getThirdPointLine() == 0) {
            homeModel.setThirdPointLineResult("적특");
            awayMoel.setThirdPointLineResult("적특");

        } else {
            if ((homeModel.getATeamThirdPoint() + homeModel.getBTeamThirdPoint()) > homeModel.getThirdPointLine()) {
                homeModel.setThirdPointLineResult("오버");
                awayMoel.setThirdPointLineResult("오버");

            } else if ((homeModel.getATeamThirdPoint() + homeModel.getBTeamThirdPoint()) < homeModel.getThirdPointLine()) {
                homeModel.setThirdPointLineResult("언더");
                awayMoel.setThirdPointLineResult("언더");

            } else {
                homeModel.setThirdPointLineResult("적특");
                awayMoel.setThirdPointLineResult("적특");
            }
        }


        if (homeModel.getFourthPointLine() == 0) {
            homeModel.setFourthPointLineResult("적특");
            awayMoel.setFourthPointLineResult("적특");

        } else {
            if ((homeModel.getATeamFourthPoint() + homeModel.getBTeamFourthPoint()) > homeModel.getFourthPointLine()) {
                homeModel.setFourthPointLineResult("오버");
                awayMoel.setFourthPointLineResult("오버");

            } else if ((homeModel.getATeamFourthPoint() + homeModel.getBTeamFourthPoint()) < homeModel.getFourthPointLine()) {
                homeModel.setFourthPointLineResult("언더");
                awayMoel.setFourthPointLineResult("언더");

            } else {
                homeModel.setFourthPointLineResult("적특");
                awayMoel.setFourthPointLineResult("적특");
            }
        }

        if (homeModel.getFifthPointLine() == 0) {
            homeModel.setFifthPointLineResult("적특");
            awayMoel.setFifthPointLineResult("적특");

        } else {
            if ((homeModel.getATeamFifthPoint() + homeModel.getBTeamFifthPoint()) > homeModel.getFifthPointLine()) {
                homeModel.setFifthPointLineResult("오버");
                awayMoel.setFifthPointLineResult("오버");

            } else if ((homeModel.getATeamFifthPoint() + homeModel.getBTeamFifthPoint()) < homeModel.getFifthPointLine()) {
                homeModel.setFifthPointLineResult("언더");
                awayMoel.setFifthPointLineResult("언더");

            } else {
                homeModel.setFifthPointLineResult("적특");
                awayMoel.setFifthPointLineResult("적특");
            }
        }
    }

    private void parseSpecial( NamedBaseballResponse matchObject , BaseballModel homeModel , BaseballModel awayMoel)  {
        FirstEvent firstBaseOnBall = matchObject.getSpecial().getFirstBaseOnBall();
        FirstEvent firstHomerun = matchObject.getSpecial().getFirstHomerun();
        FirstEvent firstStrikeOut = matchObject.getSpecial().getFirstStrikeOut();


        if(firstBaseOnBall == null) {
            homeModel.setFirstBaseOnBall("적특");
            awayMoel.setFirstBaseOnBall("적특");
        } else {
            if ("HOME".equals(firstBaseOnBall.getLocation())) {
                homeModel.setFirstBaseOnBall("승리");
                awayMoel.setFirstBaseOnBall("패배");
            } else if("AWAY".equals(firstBaseOnBall.getLocation())) {
                homeModel.setFirstBaseOnBall("패배");
                awayMoel.setFirstBaseOnBall("승리");
            } else {
                homeModel.setFirstBaseOnBall("적특");
                awayMoel.setFirstBaseOnBall("적특");
            }
        }

        if(firstHomerun == null) {
            homeModel.setFirstHomerun("적특");
            awayMoel.setFirstHomerun("적특");
        } else {
            if ("HOME".equals(firstHomerun.getLocation())) {
                homeModel.setFirstHomerun("승리");
                awayMoel.setFirstHomerun("패배");
            } else if("AWAY".equals(firstHomerun.getLocation())) {
                homeModel.setFirstHomerun("패배");
                awayMoel.setFirstHomerun("승리");
            } else {
                homeModel.setFirstHomerun("적특");
                awayMoel.setFirstHomerun("적특");
            }
        }

        if(firstStrikeOut == null) {
            homeModel.setFirstStrikeOut("적특");
            awayMoel.setFirstStrikeOut("적특");
        } else {
            if ("HOME".equals(firstStrikeOut.getLocation())) {
                homeModel.setFirstStrikeOut("승리");
                awayMoel.setFirstStrikeOut("패배");
            } else if("AWAY".equals(firstStrikeOut.getLocation())){
                homeModel.setFirstStrikeOut("패배");
                awayMoel.setFirstStrikeOut("승리");
            } else {
                homeModel.setFirstStrikeOut("적특");
                awayMoel.setFirstStrikeOut("적특");
            }
        }

    }

    
    
//    private void parseBaseOnBall(NamedBaseballResponse parseTarget, BaseballModel homeModel, BaseballModel awayModel) {
//        JSONArray broadCasts = parseTarget.getJSONArray("broadcasts");
//        int currentInning = 0;
//        String playText;
//        StringBuilder homeBaseOnBallTexts = new StringBuilder();
//        StringBuilder awayBaseOnBallTexts = new StringBuilder();
//
//        String teamLocationType = "";
//
//        //홈 투수 먼저 볼 체크 진행
//        for (int i = broadCasts.length() - 1; i > -1; i--) {
//
//            JSONObject broadCastObject = broadCasts.getJSONObject(i);
//            playText = broadCastObject.getString("playText");
//            currentInning = broadCastObject.getInt("period");
//            teamLocationType = broadCastObject.getString("teamLocationType");
//
//            if (playText.contains("경기종료")) {
//                homeModel.setBaseOnBallTexts(homeBaseOnBallTexts.toString());
//                break;
//            }
//
//            if (teamLocationType.equals("HOME")) {
//                continue;
//            }
//
//            if ((playText.contains("투수") && playText.contains("교체"))) {
//                homeModel.setBaseOnBallTexts(homeBaseOnBallTexts.toString());
//                break;
//            }
//
//
//            if (playText.contains("볼넷")) {
//                boolean checkSB = true;
//                for (int j = 1; j < 5; j++) {
//                    broadCastObject = broadCasts.getJSONObject(i + j);
//                    playText = broadCastObject.getString("playText");
//                    if (!playText.contains("볼")) {
//                        checkSB = false;
//                    }
//                }
//
//                if (checkSB) {
//                    homeBaseOnBallTexts.append(currentInning).append("(S),");
//                } else {
//                    homeBaseOnBallTexts.append(currentInning).append(",");
//                }
//
//            }
//
//            if (playText.contains("고의4구")) {
//                homeBaseOnBallTexts.append(currentInning).append("(I),");
//            }
//
//            if (playText.contains("몸에 맞는 볼") && homeModel.getLeague().equals("NPB")) {
//                int tempBaseOnBall = homeModel.getBaseOnBalls() - 1;
//                homeModel.setBaseOnBalls(tempBaseOnBall);
//            }
//
//        }
//
//
//        for (int i = broadCasts.length() - 1; i > -1; i--) {
//
//            JSONObject broadCastObject = broadCasts.getJSONObject(i);
//            playText = broadCastObject.getString("playText");
//            currentInning = broadCastObject.getInt("period");
//            teamLocationType = broadCastObject.getString("teamLocationType");
//
//            if (playText.contains("경기종료")) {
//                awayModel.setBaseOnBallTexts(awayBaseOnBallTexts.toString());
//                break;
//            }
//
//            if (teamLocationType.equals("AWAY")) {
//                continue;
//            }
//
//            if ((playText.contains("투수") && playText.contains("교체"))) {
//                awayModel.setBaseOnBallTexts(awayBaseOnBallTexts.toString());
//                break;
//            }
//
//            if (playText.contains("볼넷")) {
//                boolean checkSB = true;
//                for (int j = 1; j < 5; j++) {
//                    broadCastObject = broadCasts.getJSONObject(i + j);
//                    playText = broadCastObject.getString("playText");
//                    if (!playText.contains("볼")) {
//                        checkSB = false;
//                    }
//                }
//
//                if (checkSB) {
//                    awayBaseOnBallTexts.append(currentInning).append("(S),");
//                } else {
//                    awayBaseOnBallTexts.append(currentInning).append(",");
//                }
//
//            }
//            if (playText.contains("고의4구")) {
//                awayBaseOnBallTexts.append(currentInning).append("(I),");
//            }
//
//            if (playText.contains("몸에 맞는 볼") && awayModel.getLeague().equals("NPB")) {
//                int tempBaseOnBall = awayModel.getBaseOnBalls() - 1;
//                awayModel.setBaseOnBalls(tempBaseOnBall);
//            }
//
//        }
//
//    }


}
