package com.batch.service.sports;

import com.batch.controller.named.baseball.NamedBaseballResponse;
import com.batch.model.BaseballModel;
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

    @Transactional
    public List<BaseballModel> convertBaseballModel(List<NamedBaseballResponse> baseballList) {
        return null;
    }

    private List<BaseballModel> convertBaseballModel(NamedBaseballResponse baseball) {
        BaseballModel homeModel = new BaseballModel();
        BaseballModel awayModel = new BaseballModel();
        List<BaseballModel> reuslt = new ArrayList<>();

        parseBaseData(baseball, homeModel, awayModel);
        parsePitcherData(baseball, homeModel, awayModel);

        reuslt.add(homeModel);
        reuslt.add(awayModel);
        return reuslt;
    }



    private void parseBaseData(NamedBaseballResponse parseTarget, BaseballModel homeModel, BaseballModel awayModel){

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

    private void parsePitcherData(JSONObject parseTarget, BaseballModel aTeamModel, BaseballModel bTeamModel) throws JSONException, ParseException {
        String locationType = parseTarget.getJSONArray("gameTeams").getJSONObject(0).getString("locationType");
        JSONArray homeTeamPitcherList;
        JSONArray awayTeamPitcherList;
        if (locationType.equals("HOME")) {
            homeTeamPitcherList = parseTarget.getJSONArray("gameTeams").getJSONObject(0).getJSONArray("pitchingPlayers");
            awayTeamPitcherList = parseTarget.getJSONArray("gameTeams").getJSONObject(1).getJSONArray("pitchingPlayers");
        } else {
            homeTeamPitcherList = parseTarget.getJSONArray("gameTeams").getJSONObject(1).getJSONArray("pitchingPlayers");
            awayTeamPitcherList = parseTarget.getJSONArray("gameTeams").getJSONObject(0).getJSONArray("pitchingPlayers");
        }

        JSONObject homeTeamStartPitcher = new JSONObject();
        JSONObject awayTeamStartPitcher = new JSONObject();

        for (int i = 0; i < homeTeamPitcherList.length(); i++) {
            homeTeamStartPitcher = homeTeamPitcherList.getJSONObject(i);
            if (homeTeamStartPitcher.getBoolean("startingMember")) {
                break;
            }
        }

        for (int i = 0; i < awayTeamPitcherList.length(); i++) {
            awayTeamStartPitcher = awayTeamPitcherList.getJSONObject(i);
            if (awayTeamStartPitcher.getBoolean("startingMember")) {
                break;
            }
        }

        aTeamModel.setATeamPitcher(homeTeamStartPitcher.getJSONObject("player").getString("displayName"));
        aTeamModel.setPitchCount(homeTeamStartPitcher.getInt("pitchCount"));
        aTeamModel.setSeasonWins(homeTeamStartPitcher.getInt("seasonWins"));
        aTeamModel.setSeasonLosses(homeTeamStartPitcher.getInt("seasonLosses"));
        aTeamModel.setInningPitched(homeTeamStartPitcher.getDouble("inningPitched"));
        aTeamModel.setHit(homeTeamStartPitcher.getInt("hit"));
        aTeamModel.setHomeRun(homeTeamStartPitcher.getInt("homeRun"));
        aTeamModel.setBaseOnBalls(homeTeamStartPitcher.getInt("baseOnBalls"));
        aTeamModel.setStrikeOuts(homeTeamStartPitcher.getInt("strikeOuts"));
        aTeamModel.setRun(homeTeamStartPitcher.getInt("run"));
        aTeamModel.setEarnedRun(homeTeamStartPitcher.getInt("earnedRun"));
        aTeamModel.setTodayEarnedRunAverage(homeTeamStartPitcher.getDouble("todayEarnedRunAverage"));
        aTeamModel.setSeasonEarnedRunAverage(homeTeamStartPitcher.getDouble("seasonEarnedRunAverage"));


        bTeamModel.setATeamPitcher(awayTeamStartPitcher.getJSONObject("player").getString("displayName"));
        bTeamModel.setPitchCount(awayTeamStartPitcher.getInt("pitchCount"));
        bTeamModel.setSeasonWins(awayTeamStartPitcher.getInt("seasonWins"));
        bTeamModel.setSeasonLosses(awayTeamStartPitcher.getInt("seasonLosses"));
        bTeamModel.setInningPitched(awayTeamStartPitcher.getDouble("inningPitched"));
        bTeamModel.setHit(awayTeamStartPitcher.getInt("hit"));
        bTeamModel.setHomeRun(awayTeamStartPitcher.getInt("homeRun"));
        bTeamModel.setBaseOnBalls(awayTeamStartPitcher.getInt("baseOnBalls"));
        bTeamModel.setStrikeOuts(awayTeamStartPitcher.getInt("strikeOuts"));
        bTeamModel.setRun(awayTeamStartPitcher.getInt("run"));
        bTeamModel.setEarnedRun(awayTeamStartPitcher.getInt("earnedRun"));
        bTeamModel.setTodayEarnedRunAverage(awayTeamStartPitcher.getDouble("todayEarnedRunAverage"));
        bTeamModel.setSeasonEarnedRunAverage(awayTeamStartPitcher.getDouble("seasonEarnedRunAverage"));

    }

    private void parseBaseOnBall(JSONObject parseTarget, BaseballModel aTeamModel, BaseballModel bTeamModel) throws JSONException, ParseException {
        JSONArray broadCasts = parseTarget.getJSONArray("broadcasts");
        int currentInning = 0;
        String playText;
        StringBuilder homeBaseOnBallTexts = new StringBuilder();
        StringBuilder awayBaseOnBallTexts = new StringBuilder();

        String teamLocationType = "";

        //홈 투수 먼저 볼 체크 진행
        for (int i = broadCasts.length() - 1; i > -1; i--) {

            JSONObject broadCastObject = broadCasts.getJSONObject(i);
            playText = broadCastObject.getString("playText");
            currentInning = broadCastObject.getInt("period");
            teamLocationType = broadCastObject.getString("teamLocationType");

            if (playText.contains("경기종료")) {
                aTeamModel.setBaseOnBallTexts(homeBaseOnBallTexts.toString());
                break;
            }

            if (teamLocationType.equals("HOME")) {
                continue;
            }

            if ((playText.contains("투수") && playText.contains("교체"))) {
                aTeamModel.setBaseOnBallTexts(homeBaseOnBallTexts.toString());
                break;
            }


            if (playText.contains("볼넷")) {
                boolean checkSB = true;
                for (int j = 1; j < 5; j++) {
                    broadCastObject = broadCasts.getJSONObject(i + j);
                    playText = broadCastObject.getString("playText");
                    if (!playText.contains("볼")) {
                        checkSB = false;
                    }
                }

                if (checkSB) {
                    homeBaseOnBallTexts.append(currentInning).append("(S),");
                } else {
                    homeBaseOnBallTexts.append(currentInning).append(",");
                }

            }

            if (playText.contains("고의4구")) {
                homeBaseOnBallTexts.append(currentInning).append("(I),");
            }

            if (playText.contains("몸에 맞는 볼") && aTeamModel.getLeague().equals("NPB")) {
                int tempBaseOnBall = aTeamModel.getBaseOnBalls() - 1;
                aTeamModel.setBaseOnBalls(tempBaseOnBall);
            }

        }


        for (int i = broadCasts.length() - 1; i > -1; i--) {

            JSONObject broadCastObject = broadCasts.getJSONObject(i);
            playText = broadCastObject.getString("playText");
            currentInning = broadCastObject.getInt("period");
            teamLocationType = broadCastObject.getString("teamLocationType");

            if (playText.contains("경기종료")) {
                bTeamModel.setBaseOnBallTexts(awayBaseOnBallTexts.toString());
                break;
            }

            if (teamLocationType.equals("AWAY")) {
                continue;
            }

            if ((playText.contains("투수") && playText.contains("교체"))) {
                bTeamModel.setBaseOnBallTexts(awayBaseOnBallTexts.toString());
                break;
            }

            if (playText.contains("볼넷")) {
                boolean checkSB = true;
                for (int j = 1; j < 5; j++) {
                    broadCastObject = broadCasts.getJSONObject(i + j);
                    playText = broadCastObject.getString("playText");
                    if (!playText.contains("볼")) {
                        checkSB = false;
                    }
                }

                if (checkSB) {
                    awayBaseOnBallTexts.append(currentInning).append("(S),");
                } else {
                    awayBaseOnBallTexts.append(currentInning).append(",");
                }

            }
            if (playText.contains("고의4구")) {
                awayBaseOnBallTexts.append(currentInning).append("(I),");
            }

            if (playText.contains("몸에 맞는 볼") && bTeamModel.getLeague().equals("NPB")) {
                int tempBaseOnBall = bTeamModel.getBaseOnBalls() - 1;
                bTeamModel.setBaseOnBalls(tempBaseOnBall);
            }

        }

    }

    private void parseInningScore(JSONObject parseTarget, BaseballModel aTeamModel, BaseballModel bTeamModel) throws JSONException, ParseException {

        String locationType = parseTarget.getJSONArray("gameTeams").getJSONObject(0).getString("locationType");
        JSONArray homeTeamScore;
        JSONArray awayTeamScore;
        int homeFourthRun = 0;
        int awayFourthRun = 0;

        if (locationType.equals("HOME")) {
            homeTeamScore = parseTarget.getJSONArray("gameTeams").getJSONObject(0).getJSONArray("scores");
            awayTeamScore = parseTarget.getJSONArray("gameTeams").getJSONObject(1).getJSONArray("scores");
        } else {
            homeTeamScore = parseTarget.getJSONArray("gameTeams").getJSONObject(1).getJSONArray("scores");
            awayTeamScore = parseTarget.getJSONArray("gameTeams").getJSONObject(0).getJSONArray("scores");
        }

        aTeamModel.setFirstInningRun(awayTeamScore.getJSONObject(0).getInt("score"));
        bTeamModel.setFirstInningRun(homeTeamScore.getJSONObject(0).getInt("score"));

        for (int i = 0; i < 4; i++) {
            homeFourthRun = homeFourthRun + homeTeamScore.getJSONObject(i).getInt("score");
            awayFourthRun = awayFourthRun + awayTeamScore.getJSONObject(i).getInt("score");
        }

        aTeamModel.setFourthInningRun(awayFourthRun);
        bTeamModel.setFourthInningRun(homeFourthRun);

        if (aTeamModel.getFirstInningRun() > awayFourthRun) {
            log.info(aTeamModel.toString());
        }

        if (bTeamModel.getFirstInningRun() > homeFourthRun) {
            log.info(bTeamModel.toString());
        }

    }

}
