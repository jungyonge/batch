package com.batch.service.sports;

import com.batch.controller.named.baseball.NamedBaseballResponse;
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
                return null;
            }

            parseBaseData(baseball, homeModel, awayModel);
            parsePitcherData(String.valueOf(baseball.getId()), homeModel, awayModel);
            parseInningScore(baseball, homeModel, awayModel);
            reuslt.add(homeModel);
            reuslt.add(awayModel);

            for(BaseballModel model : reuslt){
                baseballMapper.insertBaseballMatch(model);
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
        homeModel.setFirstInningRun(home.getPeriodData().get(0).getScore());
        awayModel.setFirstInningRun(away.getPeriodData().get(0).getScore());

        int homeFourthRun = 0;
        int awayFourthRun = 0;
        for (int i = 0; i < 4; i++) {
            homeFourthRun = homeFourthRun + home.getPeriodData().get(i).getScore();
            awayFourthRun = awayFourthRun + away.getPeriodData().get(i).getScore();
        }

        homeModel.setFourthInningRun(awayFourthRun);
        awayModel.setFourthInningRun(homeFourthRun);

        if (homeModel.getFirstInningRun() > awayFourthRun) {
            log.info(homeModel.toString());
        }

        if (awayModel.getFirstInningRun() > homeFourthRun) {
            log.info(awayModel.toString());
        }

    }

//    private void parsePitcherAndOdd(NamedBaseballResponse matchObject , BaseballModel homeModel , BaseballModel awayModel) throws JSONException {
//
//        Team home = matchObject.getTeams().getHome();
//        Team away = matchObject.getTeams().getAway();
//
//
//
//        homeModel.setATeamPitcher(home.getStartPitcher().getName());
//        awayModel.setBTeamPitcher(home.getStartPitcher().getName());
//
//        homeModel.setBTeamPitcher(away.getStartPitcher().getName());
//        awayModel.setATeamPitcher(away.getStartPitcher().getName());
//
//        double handi = 0.0;
//        double unOver = 0.0;
//        double firstInningPointLine = 0.0;
//
//        JSONArray internationalHandicapOdds = matchObject.getJSONObject("odds").getJSONArray("internationalHandicapOdds");
//        JSONArray internationalUnderOverOdds = matchObject.getJSONObject("odds").getJSONArray("internationalUnderOverOdds");
//        JSONArray domesticHandicapOdds = matchObject.getJSONObject("odds").getJSONArray("domesticHandicapOdds");
//        JSONArray domesticUnderOverOdds = matchObject.getJSONObject("odds").getJSONArray("domesticUnderOverOdds");
//
//        if(internationalHandicapOdds.length() > 0 ){
//            handi = internationalHandicapOdds.getJSONObject(0).getDouble("optionValue");
//        }else if (domesticHandicapOdds.length() > 0) {
//            handi = domesticHandicapOdds.getJSONObject(0).getDouble("optionValue");
//        }
//
//        if(internationalUnderOverOdds.length() > 0 ){
//            unOver = internationalUnderOverOdds.getJSONObject(0).getDouble("optionValue");
//            firstInningPointLine = unOver / 9;
//        }else if (domesticUnderOverOdds.length() > 0  ){
//            unOver = domesticUnderOverOdds.getJSONObject(0).getDouble("optionValue");
//            firstInningPointLine = unOver / 9;
//        }
//
//        aTeamModel.setHandiCap(handi);
//        bTeamModel.setHandiCap(handi * -1);
//
//        aTeamModel.setPointLine(unOver);
//        bTeamModel.setPointLine(unOver);
//
//        if (aTeamModel.getHandiCap() > 0) {
//            aTeamModel.setOdd("역배");
//            bTeamModel.setOdd("정배");
//
//            aTeamModel.setThirdHandiCap(0.5);
//            bTeamModel.setThirdHandiCap(-0.5);
//
//            aTeamModel.setFourthHandiCap(0.5);
//            bTeamModel.setFourthHandiCap(-0.5);
//
//            aTeamModel.setFifthHandiCap(0.5);
//            bTeamModel.setFifthHandiCap(-0.5);
//
//        } else if (aTeamModel.getHandiCap() < 0) {
//            aTeamModel.setOdd("정배");
//            bTeamModel.setOdd("역배");
//
//            aTeamModel.setThirdHandiCap(-0.5);
//            bTeamModel.setThirdHandiCap(0.5);
//
//            aTeamModel.setFourthHandiCap(-0.5);
//            bTeamModel.setFourthHandiCap(0.5);
//
//            aTeamModel.setFifthHandiCap(-0.5);
//            bTeamModel.setFifthHandiCap(0.5);
//        } else {
//            aTeamModel.setOdd("없음");
//            bTeamModel.setOdd("없음");
//
//            aTeamModel.setThirdHandiCap(0.0);
//            bTeamModel.setThirdHandiCap(0.0);
//
//            aTeamModel.setFourthHandiCap(0.0);
//            bTeamModel.setFourthHandiCap(0.0);
//
//            aTeamModel.setFifthHandiCap(0.0);
//            bTeamModel.setFifthHandiCap(0.0);
//        }
//
//        if(unOver == 0){
//
//        }
//        double thirdPointLine = firstInningPointLine * 4;
//        int thirdPointLineInt = (int) thirdPointLine;
//        double pointLine = thirdPointLine - thirdPointLineInt;
//
//        if ((pointLine <= 0.333 && pointLine >= 0.001)) {
//            aTeamModel.setThirdPointLine((double) thirdPointLineInt - 0.5);
//            bTeamModel.setThirdPointLine((double) thirdPointLineInt - 0.5);
//
//        } else if ((pointLine <= 0.999 && pointLine >= 0.666)) {
//            aTeamModel.setThirdPointLine((double) (thirdPointLineInt + 1) - 0.5);
//            bTeamModel.setThirdPointLine((double) (thirdPointLineInt + 1) - 0.5);
//
//        } else if ((pointLine <= 0.665 && pointLine >= 0.334)) {
//            aTeamModel.setThirdPointLine((double) thirdPointLineInt);
//            bTeamModel.setThirdPointLine((double) thirdPointLineInt);
//
//        } else {
//            aTeamModel.setThirdPointLine((double) round(thirdPointLineInt));
//            bTeamModel.setThirdPointLine((double) round(thirdPointLineInt));
//
//        }
//
//        double forthPointLine = firstInningPointLine * 4;
//        int forthPointLineInt = (int) forthPointLine;
//        pointLine = forthPointLine - forthPointLineInt;
//
//        if ((pointLine <= 0.333 && pointLine >= 0.001)) {
//            aTeamModel.setFourthPointLine((double) forthPointLineInt);
//            bTeamModel.setFourthPointLine((double) forthPointLineInt);
//
//        } else if ((pointLine <= 0.999 && pointLine >= 0.666)) {
//            aTeamModel.setFourthPointLine((double) (forthPointLineInt + 1));
//            bTeamModel.setFourthPointLine((double) (forthPointLineInt + 1));
//
//        } else if ((pointLine <= 0.665 && pointLine >= 0.334)) {
//            aTeamModel.setFourthPointLine(forthPointLineInt + 0.5);
//            bTeamModel.setFourthPointLine(forthPointLineInt + 0.5);
//
//        } else {
//            aTeamModel.setFourthPointLine((double) round(forthPointLine));
//            bTeamModel.setFourthPointLine((double) round(forthPointLine));
//
//        }
//
//        double fifthPointLine = firstInningPointLine * 5;
//        int fifthPointLineInt = (int) fifthPointLine;
//        pointLine = fifthPointLine - fifthPointLineInt;
//
//        if ((pointLine <= 0.333 && pointLine >= 0.001)) {
//            aTeamModel.setFifthPointLine((double) fifthPointLineInt);
//            bTeamModel.setFifthPointLine((double) fifthPointLineInt);
//
//        } else if ((pointLine <= 0.999 && pointLine >= 0.666)) {
//            aTeamModel.setFifthPointLine((double) (fifthPointLineInt + 1));
//            bTeamModel.setFifthPointLine((double) (fifthPointLineInt + 1));
//
//        } else if ((pointLine <= 0.665 && pointLine >= 0.334)) {
//            aTeamModel.setFifthPointLine(fifthPointLineInt + 0.5);
//            bTeamModel.setFifthPointLine(fifthPointLineInt + 0.5);
//
//        } else {
//            aTeamModel.setFifthPointLine((double) round(fifthPointLine));
//            bTeamModel.setFifthPointLine((double) round(fifthPointLine));
//
//        }
//
//
//
//    }

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
