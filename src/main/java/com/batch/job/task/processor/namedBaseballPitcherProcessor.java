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
    private final static String BASEBALL_MATCH_URL   = "https://sports-api.picksmatch.com/named/v1/sports/baseball/games/";
    private final static String BASEBALL_PITCHER_URL = "https://sports-api.picksmatch.com/named/v1/sports/games/";

    private final static String BASEBALL_PARAM= "?broadcast=true&broadcastLatest=true&odds=true&scores=true&specials=true&seasonTeamStat=true&startDate=";
    private final static String PITCHER_PARAM= "?broadcast=true&odds=true&scores=true&specials=true&lineups=true&seasonTeamLeagueRankingStat=true&broadcastDesc=true&v=";


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

        List<BaseballModel> baseballModelList = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        cal.set(2020, 6, 3);
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String matchDate = df.format(cal.getTime());
        String unixTime = String.valueOf(System.currentTimeMillis() / 1000);

        String json = namedUtil.getPitcherApiResponse(BASEBALL_MATCH_URL + BASEBALL_PARAM + matchDate + "&endDate=" + matchDate + "&v=" + unixTime,"");

        JSONObject jsonObject = new JSONObject(json);
        JSONObject matchObject;
        JSONArray jsonArray = jsonObject.getJSONArray("response");
        String[] matchArr = new String[jsonArray.length()];

        for(int i = 0 ; i < jsonArray.length() ; i++){
            matchObject = jsonArray.getJSONObject(i);
            String gameId = matchObject.getString("id");
            matchArr[i] = gameId;
        }

        for (String gameId : matchArr){
            BaseballModel aTeamModel = new BaseballModel();
            BaseballModel bTeamModel = new BaseballModel();

            unixTime = String.valueOf(System.currentTimeMillis() / 1000);
            json = namedUtil.getPitcherApiResponse(BASEBALL_PITCHER_URL + gameId + PITCHER_PARAM + unixTime,"");

            jsonObject = new JSONObject(json);
            matchObject = jsonObject.getJSONObject("response");

            if(!matchObject.getString("status").equals("FINAL")){
                continue;
            }

            aTeamModel.setGameId(String.valueOf(matchObject.getInt("id")));
            bTeamModel.setGameId(String.valueOf(matchObject.getInt("id")));
            aTeamModel.setLeague(matchObject.getJSONObject("league").getString("displayName"));
            bTeamModel.setLeague(matchObject.getJSONObject("league").getString("displayName"));
            aTeamModel.setStadium(matchObject.getJSONObject("venue").getString("name"));
            bTeamModel.setStadium(matchObject.getJSONObject("venue").getString("name"));

            if(!aTeamModel.getLeague().equals("KBO") && !aTeamModel.getLeague().equals("NPB") && !aTeamModel.getLeague().equals("MLB") ){
                continue;
            }
            //기본 데이터 파싱
            parseBaseData(matchObject,aTeamModel,bTeamModel);

            //투수 정보 파싱
           parsePitcherData(matchObject,aTeamModel,bTeamModel);

            baseballModelList.add(aTeamModel);
            baseballModelList.add(bTeamModel);
        }

        return baseballModelList;
    }

    private void parseBaseData( JSONObject matchObject , BaseballModel aTeamModel , BaseballModel bTeamModel) throws JSONException, ParseException {

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
        if(locationType.equals("HOME")){
            homeTeam = matchObject.getJSONArray("gameTeams").getJSONObject(0).getJSONObject("team");
            awayTeam = matchObject.getJSONArray("gameTeams").getJSONObject(1).getJSONObject("team");
        }else {
            homeTeam = matchObject.getJSONArray("gameTeams").getJSONObject(1).getJSONObject("team");
            awayTeam = matchObject.getJSONArray("gameTeams").getJSONObject(0).getJSONObject("team");
        }
        aTeamModel.setATeam(homeTeam.getString("name"));
        aTeamModel.setBTeam(awayTeam.getString("name"));

        bTeamModel.setATeam(aTeamModel.getBTeam());
        bTeamModel.setBTeam(aTeamModel.getATeam());


    }

    private void parsePitcherData( JSONObject matchObject , BaseballModel aTeamModel , BaseballModel bTeamModel) throws JSONException, ParseException{
        String locationType = matchObject.getJSONArray("gameTeams").getJSONObject(0).getString("locationType");
        JSONArray homeTeamPitcherList;
        JSONArray awayTeamPitcherList;
        if(locationType.equals("HOME")){
            homeTeamPitcherList = matchObject.getJSONArray("gameTeams").getJSONObject(0).getJSONArray("pitchingPlayers");
            awayTeamPitcherList = matchObject.getJSONArray("gameTeams").getJSONObject(1).getJSONArray("pitchingPlayers");
        }else {
            homeTeamPitcherList = matchObject.getJSONArray("gameTeams").getJSONObject(1).getJSONArray("pitchingPlayers");
            awayTeamPitcherList = matchObject.getJSONArray("gameTeams").getJSONObject(0).getJSONArray("pitchingPlayers");
        }

        JSONObject homeTeamStartPitcher = new JSONObject();
        JSONObject awayTeamStartPitcher = new JSONObject();

        for(int i = 0 ; i < homeTeamPitcherList.length() ; i++){
            homeTeamStartPitcher = homeTeamPitcherList.getJSONObject(i);
            if(homeTeamStartPitcher.getBoolean("startingMember")){
                break;
            }
        }

        for(int i = 0 ; i < awayTeamPitcherList.length() ; i++){
            awayTeamStartPitcher = awayTeamPitcherList.getJSONObject(i);
            if(awayTeamStartPitcher.getBoolean("startingMember")){
                break;
            }
        }

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
}
