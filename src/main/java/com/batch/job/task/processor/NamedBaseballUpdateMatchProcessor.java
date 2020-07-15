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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class NamedBaseballUpdateMatchProcessor implements ItemProcessor<String, List<BaseballModel>> {

    private String initSeasonDate;
    private String finishSeasonDate;
    @Autowired
    private NamedUtil namedUtil;

    @Override
    public  List<BaseballModel> process(String s) throws Exception {
        log.info("process");

        return allBaseballMatch();
    }

    public List<BaseballModel> allBaseballMatch() throws IOException, ParseException, JSONException {

        long unixTime = System.currentTimeMillis() / 1000;

        int date1 = 0;

        List<BaseballModel> baseballModelList = new ArrayList<>();

        while (true){
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            cal.set(2020, 4, 01);
            DateFormat df = new SimpleDateFormat("yyyyMMdd");

            cal.add(Calendar.DATE, date1);
            String matchDate = df.format(cal.getTime());
            if(df.format(cal.getTime()).equals("20200714")){
                System.out.println("시즌끝");
                break;
            }


            try {

                String json = namedUtil.getOldApiResponse(matchDate,"");

                //JSON데이터를 넣어 JSON Object 로 만들어 준다.
                JSONObject jsonObject = new JSONObject(json);

                JSONArray jsonArray = jsonObject.getJSONArray("response");
                //books의 배열을 추출
//                JSONArray matchArr = jsonObject.getJSONArray("response");

                for (int i = 0; i < jsonArray.length(); i++) {
                    BaseballModel aTeamModel = new BaseballModel();
                    BaseballModel bTeamModel = new BaseballModel();

                    JSONObject matchObject = jsonArray.getJSONObject(i);

//                    String gameStatus = matchObject.getJSONArray("broadcasts").getJSONObject(0).getString("playText");
//                    if (gameStatus.contains("취소")) {
//                        continue;
//                    }

                    aTeamModel.setGameId(String.valueOf(matchObject.getInt("id")));
                    bTeamModel.setGameId(String.valueOf(matchObject.getInt("id")));
                    aTeamModel.setLeague(matchObject.getJSONObject("league").getString("displayName"));
                    bTeamModel.setLeague(matchObject.getJSONObject("league").getString("displayName"));

                    if(!aTeamModel.getLeague().equals("KBO") && !aTeamModel.getLeague().equals("NPB") && !aTeamModel.getLeague().equals("MLB") && !aTeamModel.getLeague().equals("퓨처스") ){
                        continue;
                    }

                    String startDatetime = matchObject.getString("startDatetime");
                    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");

                    String[] startDatetimeArr = startDatetime.split("T");

                    cal = Calendar.getInstance();
                    cal.setTime(format1.parse(startDatetime));
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

                    JSONArray groundArr = matchObject.getJSONArray("gameTeams");
                    JSONObject homeTeam = null;
                    JSONObject awayTeam = null;
                    if (groundArr.getJSONObject(0).getString("locationType").equals("AWAY")){
                        homeTeam = (JSONObject) matchObject.getJSONArray("gameTeams").get(1);
                        awayTeam = (JSONObject) matchObject.getJSONArray("gameTeams").get(0);
                    }else {
                        homeTeam = (JSONObject) matchObject.getJSONArray("gameTeams").get(0);
                        awayTeam = (JSONObject) matchObject.getJSONArray("gameTeams").get(1);
                    }

                    if(aTeamModel.getLeague().contains("퓨처스") || aTeamModel.getLeague().contains("KBO")){
                        aTeamModel.setATeam(homeTeam.getJSONObject("team").getString("nickname"));
                        aTeamModel.setBTeam(awayTeam.getJSONObject("team").getString("nickname"));
                    } else {
                        aTeamModel.setATeam(homeTeam.getJSONObject("team").getString("name"));
                        aTeamModel.setBTeam(awayTeam.getJSONObject("team").getString("name"));
                    }

                    if (aTeamModel.getATeam().equals("")){
                        continue;
                    }

                    bTeamModel.setATeam(aTeamModel.getBTeam());
                    bTeamModel.setBTeam(aTeamModel.getATeam());
                    baseballModelList.add(aTeamModel);
                    baseballModelList.add(bTeamModel);

                }
            } catch (Exception e) {
                throw e;
            }
            date1++;

        }
        return baseballModelList;
    }



}
