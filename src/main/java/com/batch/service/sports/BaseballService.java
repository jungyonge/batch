package com.batch.service.sports;

import com.batch.controller.named.baseball.NamedBaseballResponse;
import com.batch.controller.named.baseball.Team;
import com.batch.controller.named.baseball.gamehistory.Away;
import com.batch.controller.named.baseball.gamehistory.Home;
import com.batch.controller.named.baseball.gamehistory.NamedGameHistoryResponse;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BaseballService {

    private final NamedUtil namedUtil;

    private final NamedService namedService;

    @Transactional
    public List<BaseballModel> convertBaseballModel(List<NamedBaseballResponse> baseballList) {
        return null;
    }

    private List<BaseballModel> convertBaseballModel(NamedBaseballResponse baseball)
        throws ParseException {
        BaseballModel homeModel = new BaseballModel();
        BaseballModel awayModel = new BaseballModel();
        List<BaseballModel> reuslt = new ArrayList<>();

        parseBaseData(baseball, homeModel, awayModel);
        parsePitcherData(String.valueOf(baseball.getId()), homeModel, awayModel);
//        parseInningScore(baseball, homeModel, awayModel);
        reuslt.add(homeModel);
        reuslt.add(awayModel);
        return reuslt;
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

    private void parsePitcherData(String gameId, BaseballModel aTeamModel, BaseballModel bTeamModel) {

        NamedGameHistoryResponse historyResponse = namedService.getPitcher(gameId);

        Home homeHistory = historyResponse.getHome();
        Away awayHistory = historyResponse.getAway();


        aTeamModel.setATeamPitcher(homeHistory.getPitchings().get(0).getPlayer().getDisplayName());
        aTeamModel.setPitchCount(homeHistory.getPitchings().get(0).getPitchCount());
        aTeamModel.setSeasonWins(homeHistory.getPitchings().get(0).getWins());
        aTeamModel.setSeasonLosses(homeHistory.getPitchings().get(0).getLosses());
        aTeamModel.setInningPitched(
            Double.valueOf(homeHistory.getPitchings().get(0).getInningPitched()));
        aTeamModel.setHit(homeHistory.getPitchings().get(0).getHit());
        aTeamModel.setHomeRun(homeHistory.getPitchings().get(0).getHomeRun());
        aTeamModel.setBaseOnBalls(homeHistory.getPitchings().get(0).getBaseOnBalls());
        aTeamModel.setStrikeOuts(homeHistory.getPitchings().get(0).getStrikeOuts());
        aTeamModel.setRun(homeHistory.getPitchings().get(0).getRun());
        aTeamModel.setEarnedRun(homeHistory.getPitchings().get(0).getEarnedRun());
        aTeamModel.setTodayEarnedRunAverage(
            Double.valueOf(homeHistory.getPitchings().get(0).getTodayEarnedRunAverage()));
        aTeamModel.setSeasonEarnedRunAverage(
            Double.valueOf(homeHistory.getPitchings().get(0).getEarnedRunAverage()));

        bTeamModel.setATeamPitcher(awayHistory.getPitchings().get(0).getPlayer().getDisplayName());
        bTeamModel.setPitchCount(awayHistory.getPitchings().get(0).getPitchCount());
        bTeamModel.setSeasonWins(awayHistory.getPitchings().get(0).getWins());
        bTeamModel.setSeasonLosses(awayHistory.getPitchings().get(0).getLosses());
        bTeamModel.setInningPitched(
            Double.valueOf(awayHistory.getPitchings().get(0).getInningPitched()));
        bTeamModel.setHit(awayHistory.getPitchings().get(0).getHit());
        bTeamModel.setHomeRun(awayHistory.getPitchings().get(0).getHomeRun());
        bTeamModel.setBaseOnBalls(awayHistory.getPitchings().get(0).getBaseOnBalls());
        bTeamModel.setStrikeOuts(awayHistory.getPitchings().get(0).getStrikeOuts());
        bTeamModel.setRun(awayHistory.getPitchings().get(0).getRun());
        bTeamModel.setEarnedRun(awayHistory.getPitchings().get(0).getEarnedRun());
        bTeamModel.setTodayEarnedRunAverage(
            Double.valueOf(awayHistory.getPitchings().get(0).getTodayEarnedRunAverage()));
        bTeamModel.setSeasonEarnedRunAverage(
            Double.valueOf(awayHistory.getPitchings().get(0).getEarnedRunAverage()));

    }

//    private void parseInningScore(NamedBaseballResponse parseTarget, BaseballModel aTeamModel, BaseballModel bTeamModel) throws JSONException {
//
//        Team home = parseTarget.getTeams().getHome();
//        Team away = parseTarget.getTeams().getAway();
//        aTeamModel.setFirstInningRun(awayTeamScore.getJSONObject(0).getInt("score"));
//        bTeamModel.setFirstInningRun(homeTeamScore.getJSONObject(0).getInt("score"));
//
//        for (int i = 0; i < 4; i++) {
//            homeFourthRun = homeFourthRun + homeTeamScore.getJSONObject(i).getInt("score");
//            awayFourthRun = awayFourthRun + awayTeamScore.getJSONObject(i).getInt("score");
//        }
//
//        aTeamModel.setFourthInningRun(awayFourthRun);
//        bTeamModel.setFourthInningRun(homeFourthRun);
//
//        if (aTeamModel.getFirstInningRun() > awayFourthRun) {
//            log.info(aTeamModel.toString());
//        }
//
//        if (bTeamModel.getFirstInningRun() > homeFourthRun) {
//            log.info(bTeamModel.toString());
//        }
//
//    }

//    private void parseBaseOnBall(NamedBaseballResponse parseTarget, BaseballModel aTeamModel, BaseballModel bTeamModel) {
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
//                aTeamModel.setBaseOnBallTexts(homeBaseOnBallTexts.toString());
//                break;
//            }
//
//            if (teamLocationType.equals("HOME")) {
//                continue;
//            }
//
//            if ((playText.contains("투수") && playText.contains("교체"))) {
//                aTeamModel.setBaseOnBallTexts(homeBaseOnBallTexts.toString());
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
//            if (playText.contains("몸에 맞는 볼") && aTeamModel.getLeague().equals("NPB")) {
//                int tempBaseOnBall = aTeamModel.getBaseOnBalls() - 1;
//                aTeamModel.setBaseOnBalls(tempBaseOnBall);
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
//                bTeamModel.setBaseOnBallTexts(awayBaseOnBallTexts.toString());
//                break;
//            }
//
//            if (teamLocationType.equals("AWAY")) {
//                continue;
//            }
//
//            if ((playText.contains("투수") && playText.contains("교체"))) {
//                bTeamModel.setBaseOnBallTexts(awayBaseOnBallTexts.toString());
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
//            if (playText.contains("몸에 맞는 볼") && bTeamModel.getLeague().equals("NPB")) {
//                int tempBaseOnBall = bTeamModel.getBaseOnBalls() - 1;
//                bTeamModel.setBaseOnBalls(tempBaseOnBall);
//            }
//
//        }
//
//    }


}
