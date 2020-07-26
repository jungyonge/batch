package com.batch.job.task.processor;

import com.batch.model.BaseballModel;
import com.batch.util.NamedUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.Math.round;

@Component
@Slf4j
@StepScope
public class NamedBaseballNextMatchProcessor implements ItemProcessor<String, List<BaseballModel>> {

    private String baseBall_Url = "https://sports-api.named.com/v1.0/sports/baseball/games?date=";

    @Value("#{jobParameters[mode]}")
    private String mode;

    @Autowired
    private NamedUtil namedUtil;

    @Override
    public List<BaseballModel> process(String s) throws Exception {

        return updateBaseballMatch();
    }

    public List<BaseballModel> updateBaseballMatch() throws Exception {

        Calendar nextDate = Calendar.getInstance();
        nextDate.setTime(new Date());
        nextDate.add(Calendar.DATE, 1);

        List<BaseballModel> baseballModelList = new ArrayList<>();


        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        String matchDate = df.format(nextDate.getTime());

        try {

            String json = namedUtil.getNewApiResponse(baseBall_Url + matchDate + "&status=ALL", "");

            //JSON데이터를 넣어 JSON Object 로 만들어 준다.
//                JSONObject jsonObject = new JSONObject(json);

            JSONArray jsonArray = new JSONArray(json);
            //books의 배열을 추출
//                JSONArray matchArr = jsonObject.getJSONArray("response");

            for (int i = 0; i < jsonArray.length(); i++) {
                BaseballModel aTeamModel = new BaseballModel();
                BaseballModel bTeamModel = new BaseballModel();

                JSONObject matchObject = jsonArray.getJSONObject(i);

                aTeamModel.setGameId(String.valueOf(matchObject.getInt("id")));
                bTeamModel.setGameId(String.valueOf(matchObject.getInt("id")));
                aTeamModel.setLeague(matchObject.getJSONObject("league").getString("shortName"));
                bTeamModel.setLeague(matchObject.getJSONObject("league").getString("shortName"));
                aTeamModel.setStadium(matchObject.getString("venueName"));
                bTeamModel.setStadium(matchObject.getString("venueName"));

                if (!aTeamModel.getLeague().equals("KBO") && !aTeamModel.getLeague().equals("NPB") && !aTeamModel.getLeague().equals("MLB") && !aTeamModel.getLeague().equals("퓨처스")) {
                    continue;
                }
                //기본 데이터 파싱
                parseBaseData(matchObject, aTeamModel, bTeamModel);

                //선발투수, 정역배, 핸디, 언오버, 3,4,5이닝 기준점 설정
                parsePitcherAndOdd(matchObject, aTeamModel, bTeamModel);

                if (validationData(aTeamModel)) {
                    continue;
                }

                baseballModelList.add(aTeamModel);
                baseballModelList.add(bTeamModel);

            }
        } catch (Exception e) {
            e.printStackTrace();
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

        JSONObject homeTeam = matchObject.getJSONObject("teams").getJSONObject("home");
        JSONObject awayTeam = matchObject.getJSONObject("teams").getJSONObject("away");

        aTeamModel.setATeam(homeTeam.getString("name"));
        aTeamModel.setBTeam(awayTeam.getString("name"));

        bTeamModel.setATeam(aTeamModel.getBTeam());
        bTeamModel.setBTeam(aTeamModel.getATeam());

        //        JSONArray groundArr = matchObject.getJSONArray("gameTeams");

//                    if (groundArr.getJSONObject(0).getString("locationType").equals("AWAY")){
//                        homeTeam = (JSONObject) matchObject.getJSONArray("gameTeams").get(1);
//                        awayTeam = (JSONObject) matchObject.getJSONArray("gameTeams").get(0);
//                    }else {
//                        homeTeam = (JSONObject) matchObject.getJSONArray("gameTeams").get(0);
//                        awayTeam = (JSONObject) matchObject.getJSONArray("gameTeams").get(1);
//                    }
    }

    private void parsePitcherAndOdd(JSONObject matchObject, BaseballModel aTeamModel, BaseballModel bTeamModel) throws JSONException {

        JSONObject homeTeam = matchObject.getJSONObject("teams").getJSONObject("home");
        JSONObject awayTeam = matchObject.getJSONObject("teams").getJSONObject("away");

        boolean checkHomeNull = homeTeam.isNull("startPitcher");
        boolean checkAwayNull = awayTeam.isNull("startPitcher");

        if (!checkHomeNull) {
            aTeamModel.setATeamPitcher(homeTeam.getJSONObject("startPitcher").getString("name"));
            bTeamModel.setBTeamPitcher(homeTeam.getJSONObject("startPitcher").getString("name"));
        } else {
            aTeamModel.setATeamPitcher("NONE");
            bTeamModel.setBTeamPitcher("NONE");
        }

        if (!checkAwayNull) {
            aTeamModel.setBTeamPitcher(awayTeam.getJSONObject("startPitcher").getString("name"));
            bTeamModel.setATeamPitcher(awayTeam.getJSONObject("startPitcher").getString("name"));
        } else {
            aTeamModel.setBTeamPitcher("NONE");
            bTeamModel.setATeamPitcher("NONE");
        }
        double handi = 0.0;
        double unOver = 0.0;

        JSONArray internationalHandicapOdds = matchObject.getJSONObject("odds").getJSONArray("internationalHandicapOdds");
        JSONArray internationalUnderOverOdds = matchObject.getJSONObject("odds").getJSONArray("internationalUnderOverOdds");
        JSONArray domesticHandicapOdds = matchObject.getJSONObject("odds").getJSONArray("domesticHandicapOdds");
        JSONArray domesticUnderOverOdds = matchObject.getJSONObject("odds").getJSONArray("domesticUnderOverOdds");

        if (internationalHandicapOdds.length() > 0) {
            handi = internationalHandicapOdds.getJSONObject(0).getDouble("optionValue");
        } else if (domesticHandicapOdds.length() > 0) {
            handi = domesticHandicapOdds.getJSONObject(0).getDouble("optionValue");
        }

        if (internationalUnderOverOdds.length() > 0) {
            unOver = internationalUnderOverOdds.getJSONObject(0).getDouble("optionValue");
        } else if (domesticUnderOverOdds.length() > 0) {
            unOver = domesticUnderOverOdds.getJSONObject(0).getDouble("optionValue");
        }

        aTeamModel.setHandiCap(handi);
        bTeamModel.setHandiCap(handi * -1);

        aTeamModel.setPointLine(unOver);
        bTeamModel.setPointLine(unOver);

        if (aTeamModel.getHandiCap() > 0) {
            aTeamModel.setOdd("역배");
            bTeamModel.setOdd("정배");


        } else if (aTeamModel.getHandiCap() < 0) {
            aTeamModel.setOdd("정배");
            bTeamModel.setOdd("역배");

        } else {
            aTeamModel.setOdd("NONE");
            bTeamModel.setOdd("NONE");
        }

    }

    private boolean validationData(BaseballModel item) {

        if (item.getATeam().isEmpty()) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }

    }

}
