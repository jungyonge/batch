package com.batch.job.task.processor;


import com.batch.model.HockeyModel;
import com.batch.util.NamedUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
public class NamedHockeyAllMatchProcessor implements ItemProcessor<String, List<HockeyModel>> {

    private String initSeasonDate;
    private String finishSeasonDate = "2021-05-10";
    private String baseBall_Url = "https://www.livescore.co.kr/sports/score_board/hockey/view.php?date=";

    @Autowired
    private NamedUtil namedUtil;

    @Value("#{jobParameters[mode]}")
    private String mode;

    @Override
    public List<HockeyModel> process(String s) throws Exception {
        return allHockeyMatch();
    }

    public List<HockeyModel> allHockeyMatch() throws IOException, ParseException, JSONException {

        List<HockeyModel> hockeyModelList = new ArrayList<>();
        String rootHtml = "";
        
        int addDate = 0;

        while (true){
            Calendar startDate = Calendar.getInstance();
            startDate.setTime(new Date());
            if(mode.equals("all")) {
                startDate.set(2021, 0, 10);
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
                    HockeyModel aTeamStat = new HockeyModel();
                    HockeyModel bTeamStat = new HockeyModel();

                    int i = 0;

                    String league = element.select("thead tr th.reague").text();

                    if (league.equals("NHL")) {

                        String gameId = element.select("div.score_tbl_individual").attr("id");

                        aTeamStat.setGameId(gameId);
                        bTeamStat.setGameId(gameId);
                        aTeamStat.setDayOfWeek(dayOfWeek);
                        bTeamStat.setDayOfWeek(dayOfWeek);

                        aTeamStat.setTime(element.select("thead tr th.ptime").text().replaceAll("오전 ", "").replaceAll("오후 ", ""));
                        bTeamStat.setTime(element.select("thead tr th.ptime").text().replaceAll("오전 ", "").replaceAll("오후 ", ""));

                        aTeamStat.setDate(df.format(startDate.getTime()));
                        bTeamStat.setDate(df.format(startDate.getTime()));

                        aTeamStat.setGround("홈");
                        bTeamStat.setGround("원정");

                        aTeamStat.setBTeam(element.select("tbody tr > td.teaminfo.visitor strong").text());
                        aTeamStat.setATeam(element.select("tbody tr > td.teaminfo.hometeam strong").text());
                        bTeamStat.setATeam(element.select("tbody tr > td.teaminfo.visitor strong").text());
                        bTeamStat.setBTeam(element.select("tbody tr > td.teaminfo.hometeam strong").text());
                        aTeamStat.setLeague(league);
                        bTeamStat.setLeague(league);

                        if(aTeamStat.getATeam().contains("디비전")){
                            continue;
                        }

                    }
                    if(aTeamStat.getGameId() == null|| bTeamStat.getGameId() == null){
                        continue;
                    } else {
                        hockeyModelList.add(aTeamStat);
                        hockeyModelList.add(bTeamStat);
                    }
                }
            } catch (Exception e) {
                throw e;
            }
            addDate++;

        }
        return hockeyModelList;
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
        NamedHockeyAllMatchProcessor namedHockeyAllMatchProcessor = new NamedHockeyAllMatchProcessor();
        namedHockeyAllMatchProcessor.allHockeyMatch();
    }
}

