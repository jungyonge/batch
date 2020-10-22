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
    private String baseBall_Url = "https://livescore.co.kr/sports/score_board/volley/view.php?date=";

    @Autowired
    private NamedUtil namedUtil;

    @Value("#{jobParameters[mode]}")
    private String mode;

    @Override
    public List<VolleyballModel> process(String s) throws Exception {
        return allVolleyballMatch();
    }

    public List<VolleyballModel> allVolleyballMatch() throws IOException, ParseException, JSONException {

        List<VolleyballModel> volleyballModelList = new ArrayList<>();
        String rootHtml = "";
        
        int addDate = 0;

        while (true){
            Calendar startDate = Calendar.getInstance();
            startDate.setTime(new Date());
            if(mode.equals("all")) {
                startDate.set(2020, 9, 15);
            }else {
                startDate.add(Calendar.DATE, -5);
            }

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            startDate.add(Calendar.DATE, addDate);
            if(df.format(startDate.getTime()).equals(finishSeasonDate)){
                log.info("설정한 시즌 마감 기한까지 파싱 완료 : " + finishSeasonDate);
                break;
            }

            try {
                int dayNum = startDate.get(Calendar.DAY_OF_WEEK);
                String dayOfWeek = namedUtil.getDayoOfWeek(dayNum);
                rootHtml = namedUtil.liveScoreUrlToString(baseBall_Url + df.format(startDate.getTime()));
                Document rootDoc = Jsoup.parse(rootHtml);

                for (Element element : rootDoc.select("div#score_board div.score_tbl_individual")) {
                    VolleyballModel aTeamModel = new VolleyballModel();
                    VolleyballModel bTeamModel = new VolleyballModel();

                    String league = element.select("thead tr th.reague").text();
    
                    if (league.equals("V-리그")) {
    
                        String gameId = element.select("div.score_tbl_individual").attr("id");
    
                        aTeamModel.setGameId(gameId);
                        bTeamModel.setGameId(gameId);
                        aTeamModel.setDayOfWeek(dayOfWeek);
                        bTeamModel.setDayOfWeek(dayOfWeek);

                        aTeamModel.setTime(element.select("thead tr th.ptime").text().replaceAll("오전 ", "").replaceAll("오후 ", ""));
                        bTeamModel.setTime(element.select("thead tr th.ptime").text().replaceAll("오전 ", "").replaceAll("오후 ", ""));
    
                        aTeamModel.setDate(df.format(startDate.getTime()));
                        bTeamModel.setDate(df.format(startDate.getTime()));
    
                        aTeamModel.setGround("홈");
                        bTeamModel.setGround("원정");
    
                        aTeamModel.setBTeam(element.select("tbody tr > td.teaminfo.visitor strong").text());
                        aTeamModel.setATeam(element.select("tbody tr > td.teaminfo.hometeam strong").text());
                        bTeamModel.setATeam(element.select("tbody tr > td.teaminfo.visitor strong").text());
                        bTeamModel.setBTeam(element.select("tbody tr > td.teaminfo.hometeam strong").text());
                        aTeamModel.setLeague(getDivision(aTeamModel.getBTeam()));
                        bTeamModel.setLeague(getDivision(aTeamModel.getBTeam()));

                        if (!aTeamModel.getATeam().equals("") && !aTeamModel.getGameId().equals("")){
                            volleyballModelList.add(aTeamModel);
                            volleyballModelList.add(bTeamModel);
                        }
                    }

                }
            } catch (Exception e) {
                throw e;
            }
            addDate++;

        }

        return volleyballModelList;
    }

    private String getDivision (String team){
        String result = "";
        if(team.contains("현대건설") || team.contains("흥국생명") || team.contains("GS칼텍스") || team.contains("KGC인삼공사") || team.contains("한국도로공사") || team.contains("IBK기업은행")){
            result = "여자배구";
        } else {
            result = "남자배구";
        }
        return result;
    }

    public static void main(String[] args) throws ParseException, JSONException, IOException {
        NamedVolleyballAllMatchProcessor namedVolleyballAllMatchProcessor = new NamedVolleyballAllMatchProcessor();
        namedVolleyballAllMatchProcessor.allVolleyballMatch();
    }
}

