package com.batch.job.task.processor;

import com.batch.model.BaseballModel;
import com.batch.util.NamedUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class namedBaseballPitcherProcessor implements ItemProcessor<String, List<BaseballModel>> {
    private final static String BASEBALL_MATCH_URL = "https://sports-api.picksmatch.com/named/v1/sports/baseball/games/";
    private final static String BASEBALL_PITCHER_URL = "https://sports-api.picksmatch.com/named/v1/sports/games/";

    private final static String BASEBALL_PARAM = "?broadcast=true&broadcastLatest=true&odds=true&scores=true&specials=true&seasonTeamStat=true&startDate=";
    private final static String PITCHER_PARAM = "?broadcast=true&odds=true&scores=true&specials=true&lineups=true&seasonTeamLeagueRankingStat=true&broadcastDesc=true&v=";


    @Autowired
    private NamedUtil namedUtil;

//20200714&endDate=20200714&v=

    @Override
    public List<BaseballModel> process(String s) throws Exception {
        return insertPitcherStat();
    }

    //     그날 경기 리스트
//    https://sports-api.picksmatch.com/named/v1/sports/baseball/games/?broadcast=true&broadcastLatest=true&odds=true&scores=true&specials=true&seasonTeamStat=true&startDate=20200714&endDate=20200714&v=1594951570374
    // 경기 상세
//    https://sports-api.picksmatch.com/named/v1/sports/games/10559594?broadcast=true&odds=true&scores=true&specials=true&lineups=true&seasonTeamLeagueRankingStat=true&broadcastDesc=true&v=1594951597
    public List<BaseballModel> insertPitcherStat() throws Exception {
//
//        List<BaseballModel> baseballModelList = new ArrayList<>();
//
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(new Date());
//
//        cal.set(2020, 6, 3);
//        DateFormat df = new SimpleDateFormat("yyyyMMdd");
//        String matchDate = df.format(cal.getTime());

        int addDate = 0;

        List<BaseballModel> baseballModelList = new ArrayList<>();

        while (true) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            cal.set(2020, 4, 04);
//            cal.set(2020, 6, 01);

            DateFormat df = new SimpleDateFormat("yyyyMMdd");

            cal.add(Calendar.DATE, addDate);
            String matchDate = df.format(cal.getTime());
            if (df.format(cal.getTime()).equals("20200718")) {
                log.info("설정한 시즌 마김 기한까지 파싱 완료 : " + "2020-07-18");
                break;
            }


            String unixTime = String.valueOf(System.currentTimeMillis() / 1000);

            String json = namedUtil.getPitcherApiResponse(BASEBALL_MATCH_URL + BASEBALL_PARAM + matchDate + "&endDate=" + matchDate + "&v=" + unixTime, "");

            JSONObject jsonObject = new JSONObject(json);
            JSONObject matchObject;
            JSONArray jsonArray = jsonObject.getJSONArray("response");
            String[] matchArr = new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++) {
                matchObject = jsonArray.getJSONObject(i);
                if (matchObject.getBoolean("isBoardShow")) {
                    String gameId = matchObject.getString("id");
                    matchArr[i] = gameId;
                }
            }

            for (String gameId : matchArr) {
                BaseballModel aTeamModel = new BaseballModel();
                BaseballModel bTeamModel = new BaseballModel();

                unixTime = String.valueOf(System.currentTimeMillis() / 1000);
                if(gameId == null){
                    continue;
                }
                json = namedUtil.getPitcherApiResponse(BASEBALL_PITCHER_URL + gameId + PITCHER_PARAM + unixTime, "");

                jsonObject = new JSONObject(json);
                matchObject = jsonObject.getJSONObject("response");

                if (!matchObject.getString("status").equals("FINAL")) {
                    continue;
                }

                aTeamModel.setGameId(String.valueOf(matchObject.getInt("id")));
                bTeamModel.setGameId(String.valueOf(matchObject.getInt("id")));
                aTeamModel.setLeague(matchObject.getJSONObject("league").getString("displayName"));
                bTeamModel.setLeague(matchObject.getJSONObject("league").getString("displayName"));
                if(!matchObject.isNull("venue")){
                    aTeamModel.setStadium(matchObject.getJSONObject("venue").getString("name"));
                    bTeamModel.setStadium(matchObject.getJSONObject("venue").getString("name"));
                }


                if (!aTeamModel.getLeague().equals("KBO") && !aTeamModel.getLeague().equals("NPB") && !aTeamModel.getLeague().equals("MLB")) {
                    continue;
                }

                //기본 데이터 파싱
                parseBaseData(matchObject, aTeamModel, bTeamModel);

                //투수 정보 파싱
                parsePitcherData(matchObject, aTeamModel, bTeamModel);

                //볼 정보 파싱
                parseBaseOnBall(matchObject, aTeamModel, bTeamModel);

                //이닝스코어 파싱
                parseInningScore(matchObject, aTeamModel, bTeamModel);

                baseballModelList.add(aTeamModel);
                baseballModelList.add(bTeamModel);
            }

            addDate++;

        }


        for (BaseballModel bb : baseballModelList) {
            int cnt = bb.getBaseOnBalls();
            if (cnt == 0) {
                continue;
            }
            String text = bb.getBaseOnBallTexts();
            if(text == null){
                log.info(text);
            }
            String[] textArr = text.split(" ");
            if (cnt != textArr.length) {
                log.info(bb.toString());
            }
        }
        return baseballModelList;
    }

    private void parseBaseData(JSONObject matchObject, BaseballModel aTeamModel, BaseballModel bTeamModel) throws JSONException, ParseException {

        Calendar cal = Calendar.getInstance();

        String startDatetime = matchObject.getString("startDatetime");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        String[] startDatetimeArr = startDatetime.split("T");

        cal = Calendar.getInstance();
        cal.setTime(format.parse(startDatetime));
        int dayNum = cal.get(Calendar.DAY_OF_WEEK);

        String date = startDatetimeArr[0];
        String dayOfWeek = namedUtil.getDayoOfWeek(dayNum);
        String time = startDatetimeArr[1].substring(0, 5);

        aTeamModel.setDate(date);
        aTeamModel.setDayOfWeek(dayOfWeek);
        aTeamModel.setTime(time);

        bTeamModel.setDate(date);
        bTeamModel.setDayOfWeek(dayOfWeek);
        bTeamModel.setTime(time);

        aTeamModel.setGround("홈");
        bTeamModel.setGround("원정");

        String locationType = matchObject.getJSONArray("gameTeams").getJSONObject(0).getString("locationType");
        JSONObject homeTeam;
        JSONObject awayTeam;
        if (locationType.equals("HOME")) {
            homeTeam = matchObject.getJSONArray("gameTeams").getJSONObject(0).getJSONObject("team");
            awayTeam = matchObject.getJSONArray("gameTeams").getJSONObject(1).getJSONObject("team");
        } else {
            homeTeam = matchObject.getJSONArray("gameTeams").getJSONObject(1).getJSONObject("team");
            awayTeam = matchObject.getJSONArray("gameTeams").getJSONObject(0).getJSONObject("team");
        }
        aTeamModel.setATeam(homeTeam.getString("name"));
        aTeamModel.setBTeam(awayTeam.getString("name"));

        bTeamModel.setATeam(aTeamModel.getBTeam());
        bTeamModel.setBTeam(aTeamModel.getATeam());


    }

    private void parsePitcherData(JSONObject matchObject, BaseballModel aTeamModel, BaseballModel bTeamModel) throws JSONException, ParseException {
        String locationType = matchObject.getJSONArray("gameTeams").getJSONObject(0).getString("locationType");
        JSONArray homeTeamPitcherList;
        JSONArray awayTeamPitcherList;
        if (locationType.equals("HOME")) {
            homeTeamPitcherList = matchObject.getJSONArray("gameTeams").getJSONObject(0).getJSONArray("pitchingPlayers");
            awayTeamPitcherList = matchObject.getJSONArray("gameTeams").getJSONObject(1).getJSONArray("pitchingPlayers");
        } else {
            homeTeamPitcherList = matchObject.getJSONArray("gameTeams").getJSONObject(1).getJSONArray("pitchingPlayers");
            awayTeamPitcherList = matchObject.getJSONArray("gameTeams").getJSONObject(0).getJSONArray("pitchingPlayers");
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

    private void parseBaseOnBall(JSONObject matchObject, BaseballModel aTeamModel, BaseballModel bTeamModel) throws JSONException, ParseException {
        JSONArray broadCasts = matchObject.getJSONArray("broadcasts");
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
                for (int j = 1 ; j < 5 ; j++){
                    broadCastObject = broadCasts.getJSONObject(i + j);
                    playText = broadCastObject.getString("playText");
                    if(!playText.contains("볼")){
                        checkSB = false;
                    }
                }

                if(checkSB){
                    homeBaseOnBallTexts.append(currentInning).append("(S),");
                }else {
                    homeBaseOnBallTexts.append(currentInning).append(",");
                }

            }

            if (playText.contains("고의4구")) {
                homeBaseOnBallTexts.append(currentInning).append("(I),");
            }

            if(playText.contains("몸에 맞는 볼") && aTeamModel.getLeague().equals("NPB")){
                int tempBaseOnBall = aTeamModel.getBaseOnBalls() - 1;
                aTeamModel.setBaseOnBalls(tempBaseOnBall);
            }

        }


        for (int i = broadCasts.length() - 1; i > -1; i--) {

            JSONObject broadCastObject = broadCasts.getJSONObject(i);
            playText = broadCastObject.getString("playText");
            currentInning = broadCastObject.getInt("period");
            teamLocationType = broadCastObject.getString("teamLocationType");

            if ( playText.contains("경기종료")) {
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
                for (int j = 1 ; j < 5 ; j++){
                    broadCastObject = broadCasts.getJSONObject(i + j);
                    playText = broadCastObject.getString("playText");
                    if(!playText.contains("볼")){
                        checkSB = false;
                    }
                }

                if(checkSB){
                    awayBaseOnBallTexts.append(currentInning).append("(S),");
                }else {
                    awayBaseOnBallTexts.append(currentInning).append(",");
                }

            }
            if (playText.contains("고의4구")) {
                awayBaseOnBallTexts.append(currentInning).append("(I),");
            }

            if(playText.contains("몸에 맞는 볼") && bTeamModel.getLeague().equals("NPB")){
                int tempBaseOnBall = bTeamModel.getBaseOnBalls() - 1;
                bTeamModel.setBaseOnBalls(tempBaseOnBall);
            }

        }

    }

    private void parseInningScore(JSONObject matchObject, BaseballModel aTeamModel, BaseballModel bTeamModel) throws JSONException, ParseException {

        String locationType = matchObject.getJSONArray("gameTeams").getJSONObject(0).getString("locationType");
        JSONArray homeTeamScore;
        JSONArray awayTeamScore;
        int homeFourthRun = 0;
        int awayFourthRun = 0;

        if (locationType.equals("HOME")) {
            homeTeamScore = matchObject.getJSONArray("gameTeams").getJSONObject(0).getJSONArray("scores");
            awayTeamScore = matchObject.getJSONArray("gameTeams").getJSONObject(1).getJSONArray("scores");
        } else {
            homeTeamScore = matchObject.getJSONArray("gameTeams").getJSONObject(1).getJSONArray("scores");
            awayTeamScore = matchObject.getJSONArray("gameTeams").getJSONObject(0).getJSONArray("scores");
        }

        aTeamModel.setFirstInningRun(homeTeamScore.getJSONObject(0).getInt("score"));
        bTeamModel.setFirstInningRun(awayTeamScore.getJSONObject(0).getInt("score"));

        for (int i = 0; i < 4; i++) {
            homeFourthRun = homeFourthRun + homeTeamScore.getJSONObject(i).getInt("score");
            awayFourthRun = awayFourthRun + awayTeamScore.getJSONObject(i).getInt("score");
        }

        aTeamModel.setFourthInningRun(homeFourthRun);
        bTeamModel.setFourthInningRun(awayFourthRun);

    }
}
