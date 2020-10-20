package com.batch.job.task.processor;


import com.batch.model.BaseballModel;
import com.batch.model.VolleyballModel;
import com.batch.util.NamedUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@StepScope
public class NamedVolleyballAllMatchProcessor implements ItemProcessor<String, List<VolleyballModel>> {

    private String initSeasonDate;
    private String finishSeasonDate = "2021-04-15";
    private String baseBall_Url = "https://sports-api.named.com/v1.0/sports/volleyball/games?date=";

    @Autowired
    private NamedUtil namedUtil;

    @Value("#{jobParameters[mode]}")
    private String mode;

    @Override
    public List<VolleyballModel> process(String s) throws Exception {
        return allVolleyballMatch();
    }

    public List<VolleyballModel> allVolleyballMatch() throws IOException, ParseException, JSONException {

        List<VolleyballModel> volleeyModelList = new ArrayList<>();
        int addDate = 0;
        NamedUtil namedUtil = new NamedUtil();

        while (true){
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            cal.set(2020, 9, 15);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            cal.add(Calendar.DATE, addDate);
            String matchDate = df.format(cal.getTime());
            if(df.format(cal.getTime()).equals(finishSeasonDate)){
                log.info("설정한 시즌 마김 기한까지 파싱 완료 : " + finishSeasonDate);
                break;
            }

            try {
                String json = namedUtil.getNewApiResponse(baseBall_Url + matchDate + "&status=ALL","");

                //JSON데이터를 넣어 JSON Object 로 만들어 준다.
//                JSONObject jsonObject = new JSONObject(json);

                JSONArray jsonArray = new JSONArray(json);
                //books의 배열을 추출
//                JSONArray matchArr = jsonObject.getJSONArray("response");

                for (int i = 0; i < jsonArray.length(); i++) {
                    VolleyballModel aTeamModel = new VolleyballModel();
                    VolleyballModel bTeamModel = new VolleyballModel();

                    JSONObject matchObject = jsonArray.getJSONObject(i);

                    aTeamModel.setGameId(String.valueOf(matchObject.getInt("id")));
                    bTeamModel.setGameId(String.valueOf(matchObject.getInt("id")));
                    aTeamModel.setLeague(matchObject.getJSONObject("league").getString("shortName"));
                    bTeamModel.setLeague(matchObject.getJSONObject("league").getString("shortName"));

                    if(!aTeamModel.getLeague().equals("Vleague(여)") && !aTeamModel.getLeague().equals("Vleague(남)")){
                        continue;
                    }

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
//                    if (groundArr.getJSONObject(0).getString("locationType").equals("AWAY")){
//                        homeTeam = (JSONObject) matchObject.getJSONArray("gameTeams").get(1);
//                        awayTeam = (JSONObject) matchObject.getJSONArray("gameTeams").get(0);
//                    }else {
//                        homeTeam = (JSONObject) matchObject.getJSONArray("gameTeams").get(0);
//                        awayTeam = (JSONObject) matchObject.getJSONArray("gameTeams").get(1);
//                    }

                    aTeamModel.setATeam(homeTeam.getString("name"));
                    aTeamModel.setBTeam(awayTeam.getString("name"));
                    bTeamModel.setATeam(aTeamModel.getBTeam());
                    bTeamModel.setBTeam(aTeamModel.getATeam());
//                    if(aTeamModel.getLeague().contains("퓨처스") || aTeamModel.getLeague().contains("KBO")){
//                        aTeamModel.setATeam(homeTeam.getJSONObject("team").getString("nickname"));
//                        aTeamModel.setBTeam(awayTeam.getJSONObject("team").getString("nickname"));
//                    } else {
//                        aTeamModel.setATeam(homeTeam.getJSONObject("team").getString("name"));
//                        aTeamModel.setBTeam(awayTeam.getJSONObject("team").getString("name"));
//                    }

                    if (aTeamModel.getATeam().equals("")){
                        continue;
                    }

                    volleeyModelList.add(aTeamModel);
                    volleeyModelList.add(bTeamModel);

                }
            } catch (Exception e) {
                throw e;
            }
            addDate++;

        }

        return volleeyModelList;
    }

    public static void main(String[] args) throws ParseException, JSONException, IOException {
        NamedVolleyballAllMatchProcessor namedVolleyballAllMatchProcessor = new NamedVolleyballAllMatchProcessor();
        namedVolleyballAllMatchProcessor.allVolleyballMatch();
    }
}

