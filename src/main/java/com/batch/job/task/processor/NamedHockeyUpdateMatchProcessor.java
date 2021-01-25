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
public class NamedHockeyUpdateMatchProcessor implements ItemProcessor<String, List<HockeyModel>> {

    private String initSeasonDate;
    private String finishSeasonDate = "2021-05-10";
    private String baseBall_Url = "https://www.livescore.co.kr/sports/score_board/hockey/view.php?date=";


    @Autowired
    private NamedUtil namedUtil;

    @Value("#{jobParameters[mode]}")
    private String mode;

    @Override
    public List<HockeyModel> process(String s) throws Exception {
        return updateHockeyMatch();
    }


    public List<HockeyModel> updateHockeyMatch() throws IOException, ParseException, JSONException, InterruptedException {


        int addDate = 0;
        Calendar curDate = Calendar.getInstance();
        curDate.setTime(new Date());
        curDate.add(Calendar.DATE, 1);
        String rootHtml = "";


        List<HockeyModel> hockeyModelList = new ArrayList<>();

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
            if(df.format(startDate.getTime()).equals(df.format(curDate.getTime()))){
                log.info("하키 Update Match 완료 : " + df.format(curDate.getTime()));
                break;
            }

            try {
                int dayNum = startDate.get(Calendar.DAY_OF_WEEK);
                String dayOfWeek = namedUtil.getDayoOfWeek(dayNum);
                rootHtml = namedUtil.liveScoreUrlToString(baseBall_Url + df.format(startDate.getTime()));
                Document rootDoc = Jsoup.parse(rootHtml);

                for (Element element : rootDoc.select("div#score_board div.score_tbl_individual")) {
                    HockeyModel aTeamModel = new HockeyModel();
                    HockeyModel bTeamModel = new HockeyModel();

                    int i = 0;

                    String league = element.select("thead tr th.reague").text();
                    String status = element.select("th.ping").text();

                    if (league.equals("NHL") && status.equals("종")) {
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
                        aTeamModel.setLeague(league);
                        bTeamModel.setLeague(league);

                        if(aTeamModel.getATeam().contains("디비전")){
                            continue;
                        }
                        //풀, 하프, 이닝별 스코어, 핸디, 언오버, 정역배 설정
                        parseScoreAndOdd(element, aTeamModel, bTeamModel);

                        parseFullHalfHandiUnover(element, aTeamModel, bTeamModel);

                        //이닝쿼별 스폐셜 결과 설정
                        parseSpecial(element, aTeamModel, bTeamModel, i);

                        setBteamStat(aTeamModel, bTeamModel);

                    }
                    if(aTeamModel.getGameId() == null|| bTeamModel.getGameId() == null){
                        continue;
                    } else {
                        hockeyModelList.add(aTeamModel);
                        hockeyModelList.add(bTeamModel);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            addDate++;

        }

        return hockeyModelList;
    }

    private void parseScoreAndOdd(Element element, HockeyModel aTeamModel, HockeyModel bTeamModel) {

        String[] arrayHandi = element.select("tbody > tr > td.line").text().split(" ");

        if (arrayHandi.length > 1) {
            aTeamModel.setPointLine(Double.valueOf(arrayHandi[0]));
            aTeamModel.setHandiCap(Double.valueOf(arrayHandi[1]));
        } else {
            aTeamModel.setPointLine(0.0);
            aTeamModel.setHandiCap(0.0);
        }

        String[] arrayTotalScore = element.select("tbody > tr > td.score").text().split(" ");

        if (arrayTotalScore.length > 1) {
            aTeamModel.setBTeamTotalPoint(Integer.valueOf(arrayTotalScore[0]));
            aTeamModel.setATeamTotalPoint(Integer.valueOf(arrayTotalScore[1]));

            bTeamModel.setATeamTotalPoint(Integer.valueOf(arrayTotalScore[0]));
            bTeamModel.setBTeamTotalPoint(Integer.valueOf(arrayTotalScore[1]));
        } else {
            aTeamModel.setBTeamTotalPoint(0);
            aTeamModel.setATeamTotalPoint(0);

            bTeamModel.setBTeamTotalPoint(0);
            bTeamModel.setATeamTotalPoint(0);
        }

        String[] arrayFirstScore = element.select("tbody > tr > td.s").text().split(" ");

        if (arrayFirstScore.length == 9) {
            bTeamModel.setFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            bTeamModel.setSecondQPoint(Integer.valueOf(arrayFirstScore[1]));
            bTeamModel.setThirdQPoint(Integer.valueOf(arrayFirstScore[2]));
            bTeamModel.setExtendQPoint(Integer.valueOf(arrayFirstScore[3]));
            bTeamModel.setShotoutQPoint(0);

            aTeamModel.setFirstQPoint(Integer.valueOf(arrayFirstScore[5]));
            aTeamModel.setSecondQPoint(Integer.valueOf(arrayFirstScore[6]));
            aTeamModel.setThirdQPoint(Integer.valueOf(arrayFirstScore[7]));
            aTeamModel.setExtendQPoint(Integer.valueOf(arrayFirstScore[8]));
            aTeamModel.setShotoutQPoint(0);

            aTeamModel.setBTeamFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            aTeamModel.setATeamFirstQPoint(Integer.valueOf(arrayFirstScore[5]));

        } else if(arrayFirstScore.length == 10){

            bTeamModel.setFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            bTeamModel.setSecondQPoint(Integer.valueOf(arrayFirstScore[1]));
            bTeamModel.setThirdQPoint(Integer.valueOf(arrayFirstScore[2]));
            bTeamModel.setExtendQPoint(Integer.valueOf(arrayFirstScore[3]));
            bTeamModel.setShotoutQPoint(Integer.valueOf(arrayFirstScore[4]));

            aTeamModel.setFirstQPoint(Integer.valueOf(arrayFirstScore[5]));
            aTeamModel.setSecondQPoint(Integer.valueOf(arrayFirstScore[6]));
            aTeamModel.setThirdQPoint(Integer.valueOf(arrayFirstScore[7]));
            aTeamModel.setExtendQPoint(Integer.valueOf(arrayFirstScore[8]));
            aTeamModel.setShotoutQPoint(Integer.valueOf(arrayFirstScore[9]));

            aTeamModel.setBTeamFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            aTeamModel.setATeamFirstQPoint(Integer.valueOf(arrayFirstScore[5]));


        } else {
            bTeamModel.setFirstQPoint(0);
            bTeamModel.setSecondQPoint(0);
            bTeamModel.setThirdQPoint(0);
            bTeamModel.setExtendQPoint(0);
            bTeamModel.setShotoutQPoint(0);

            aTeamModel.setFirstQPoint(0);
            aTeamModel.setSecondQPoint(0);
            aTeamModel.setThirdQPoint(0);
            aTeamModel.setExtendQPoint(0);
            aTeamModel.setShotoutQPoint(0);
        }

        if(aTeamModel.getHandiCap() > 0){
            aTeamModel.setOdd("역배");
            bTeamModel.setOdd("정배");
        } else if (aTeamModel.getHandiCap() < 0){
            aTeamModel.setOdd("정배");
            bTeamModel.setOdd("역배");
        } else {
            aTeamModel.setOdd("없음");
            bTeamModel.setOdd("없음");
        }
    }

    private void parseFullHalfHandiUnover(Element element, HockeyModel aTeamModel, HockeyModel bTeamModel) {

        if(aTeamModel.getHandiCap() == 0){
            aTeamModel.setHandiCapResult("적특");
        }else {
            if ((aTeamModel.getATeamTotalPoint() + aTeamModel.getHandiCap()) > aTeamModel.getBTeamTotalPoint()) {
                aTeamModel.setHandiCapResult("승리");
            } else if ((aTeamModel.getATeamTotalPoint() + aTeamModel.getHandiCap()) < aTeamModel.getBTeamTotalPoint()) {
                aTeamModel.setHandiCapResult("패배");
            } else {
                aTeamModel.setHandiCapResult("적특");
            }
        }


        if(aTeamModel.getPointLine() == 0){
            aTeamModel.setPointLineResult("적특");
            bTeamModel.setPointLineResult("적특");
        } else {
            if ((aTeamModel.getATeamTotalPoint() + aTeamModel.getBTeamTotalPoint()) > aTeamModel.getPointLine()) {
                aTeamModel.setPointLineResult("오버");
                bTeamModel.setPointLineResult("오버");
            } else if ((aTeamModel.getATeamTotalPoint() + aTeamModel.getBTeamTotalPoint()) < aTeamModel.getPointLine()) {
                aTeamModel.setPointLineResult("언더");
                bTeamModel.setPointLineResult("언더");
            } else {
                aTeamModel.setPointLineResult("적특");
                bTeamModel.setPointLineResult("적특");
            }
        }



        double handi = aTeamModel.getHandiCap() / 3;
        int handiInt = (int) handi;
        double pointHandi = handi - handiInt;

        if ((pointHandi <= 0.333 && pointHandi >= 0.001) || (pointHandi >= -0.333 && pointHandi <= -0.001)) {
            if(pointHandi < 0){
                aTeamModel.setFirstQHandiCap(Double.valueOf(Math.ceil(aTeamModel.getHandiCap() / 3)));
            }else {
                aTeamModel.setFirstQHandiCap(Double.valueOf(Math.floor(aTeamModel.getHandiCap() / 3)));
            }
        } else if((pointHandi <= 0.999 && pointHandi >= 0.666) || (pointHandi >= -0.999 && pointHandi <= -0.666)) {
            if(pointHandi < 0){
                aTeamModel.setFirstQHandiCap(Double.valueOf(Math.floor(aTeamModel.getHandiCap() / 3)));
            }else {
                aTeamModel.setFirstQHandiCap(Double.valueOf(Math.ceil(aTeamModel.getHandiCap() / 3)));
            }
        } else if((pointHandi <= 0.665 && pointHandi >= 0.334)) {
            aTeamModel.setFirstQHandiCap(handiInt + 0.5);
        }  else if((pointHandi >= -0.665 && pointHandi <= -0.334)) {
            aTeamModel.setFirstQHandiCap(handiInt - 0.5);
        } else if (aTeamModel.getHandiCap() >= 0.5 && aTeamModel.getHandiCap() <= 1.5) {
            aTeamModel.setFirstQHandiCap(0.5);
        } else if (aTeamModel.getHandiCap() <= -0.5 && aTeamModel.getHandiCap() >= -1.5) {
            aTeamModel.setFirstQHandiCap(-0.5);
        } else {
            aTeamModel.setFirstQHandiCap(0.0);
        }


        if(aTeamModel.getFirstQHandiCap() == 0){
            aTeamModel.setFirstQHandiCapResult("적특");
        }else {
            if ((aTeamModel.getFirstQPoint() + aTeamModel.getFirstQHandiCap()) > bTeamModel.getFirstQPoint()) {
                aTeamModel.setFirstQHandiCapResult("승리");
            } else if ((aTeamModel.getFirstQPoint() + aTeamModel.getFirstQHandiCap()) < bTeamModel.getFirstQPoint()) {
                aTeamModel.setFirstQHandiCapResult("패배");
            } else {
                aTeamModel.setFirstQHandiCapResult("적특");
            }
        }

        if(aTeamModel.getPointLine() >= 7){
            aTeamModel.setFirstQPointLine(2.0);
        }else if(aTeamModel.getPointLine() > 5 || aTeamModel.getPointLine() < 7) {
            aTeamModel.setFirstQPointLine(1.5);
        }else if(aTeamModel.getPointLine() <= 5){
            aTeamModel.setFirstQPointLine(1.0);
        }


        if(aTeamModel.getFirstQPointLine() == 0){
            aTeamModel.setFirstQPointLineResult("적특");
            bTeamModel.setFirstQPointLineResult("적특");
        }else {
            if ((aTeamModel.getATeamFirstQPoint() + aTeamModel.getBTeamFirstQPoint()) > aTeamModel.getFirstQPointLine()) {
                aTeamModel.setFirstQPointLineResult("오버");
                bTeamModel.setFirstQPointLineResult("오버");

            } else if ((aTeamModel.getATeamFirstQPoint() + aTeamModel.getBTeamFirstQPoint()) < aTeamModel.getFirstQPointLine()) {
                aTeamModel.setFirstQPointLineResult("언더");
                bTeamModel.setFirstQPointLineResult("언더");

            } else {
                aTeamModel.setFirstQPointLineResult("적특");
                bTeamModel.setFirstQPointLineResult("적특");
            }
        }



        String[] arrayQTotalScore = element.select("tfoot > tr > td.s").text().split(" ");

        if (arrayQTotalScore.length == 3) {
            aTeamModel.setFirstQTotalPoint(Integer.valueOf(arrayQTotalScore[0]));
            aTeamModel.setSecondQTotalPoint(Integer.valueOf(arrayQTotalScore[1]));
            aTeamModel.setThirdQTotalPoint(Integer.valueOf(arrayQTotalScore[2]));
            aTeamModel.setExtendQTotalPoint(0);
            aTeamModel.setShotoutQTotalPoint(0);

        } else if (arrayQTotalScore.length == 4) {

            aTeamModel.setFirstQTotalPoint(Integer.valueOf(arrayQTotalScore[0]));
            aTeamModel.setSecondQTotalPoint(Integer.valueOf(arrayQTotalScore[1]));
            aTeamModel.setThirdQTotalPoint(Integer.valueOf(arrayQTotalScore[2]));
            aTeamModel.setExtendQTotalPoint(Integer.valueOf(arrayQTotalScore[3]));
            aTeamModel.setShotoutQTotalPoint(0);

        } else if (arrayQTotalScore.length == 5) {
            aTeamModel.setFirstQTotalPoint(Integer.valueOf(arrayQTotalScore[0]));
            aTeamModel.setSecondQTotalPoint(Integer.valueOf(arrayQTotalScore[1]));
            aTeamModel.setThirdQTotalPoint(Integer.valueOf(arrayQTotalScore[2]));
            aTeamModel.setExtendQTotalPoint(Integer.valueOf(arrayQTotalScore[3]));
            aTeamModel.setShotoutQTotalPoint(Integer.valueOf(arrayQTotalScore[4]));
        }else  {
            aTeamModel.setFirstQTotalPoint(0);
            aTeamModel.setSecondQTotalPoint(0);
            aTeamModel.setThirdQTotalPoint(0);
            aTeamModel.setExtendQTotalPoint(0);
            aTeamModel.setShotoutQTotalPoint(0);

        }


    }


    private void parseSpecial(Element element, HockeyModel aTeamModel, HockeyModel bTeamModel,int i) {

        for (Element element1 : element.select("tbody > tr > td.f.ico_linescore")) {
            if (i == 0) {
                if (element1.select("span.ico_firstpoint").text().equals("첫득점")) {
                    aTeamModel.setFirstPoint("패배");
                } else {
                    aTeamModel.setFirstPoint("승리");
                }
            }
            i++;
        }

    }

    public void setBteamStat(HockeyModel aTeamModel , HockeyModel bTeamModel){
        bTeamModel.setLeague(aTeamModel.getLeague());


        bTeamModel.setPointLine(aTeamModel.getPointLine());
        bTeamModel.setHandiCap(-(aTeamModel.getHandiCap()));



        if (aTeamModel.getHandiCapResult().equals("패배")) {
            bTeamModel.setHandiCapResult("승리");
        } else if (aTeamModel.getHandiCapResult().equals("승리")) {
            bTeamModel.setHandiCapResult("패배");
        } else {
            bTeamModel.setHandiCapResult("적특");
        }

//        bTeamModel.setBTeamFirstQPoint(aTeamModel.getATeamFirstQPoint());
//        bTeamModel.setATeamFirstQPoint(aTeamModel.getBTeamFirstQPoint());


        bTeamModel.setFirstQHandiCap(-(aTeamModel.getFirstQHandiCap()));

        bTeamModel.setFirstQPointLine(aTeamModel.getFirstQPointLine());


        if (aTeamModel.getFirstQHandiCapResult().equals("패배")) {
            bTeamModel.setFirstQHandiCapResult("승리");
        } else if  (aTeamModel.getFirstQHandiCapResult().equals("승리")) {
            bTeamModel.setFirstQHandiCapResult("패배");
        } else {
            bTeamModel.setFirstQHandiCapResult("적특");
        }


        bTeamModel.setFirstQTotalPoint(aTeamModel.getFirstQTotalPoint());
        bTeamModel.setSecondQTotalPoint(aTeamModel.getSecondQTotalPoint());
        bTeamModel.setThirdQTotalPoint(aTeamModel.getThirdQTotalPoint());
        bTeamModel.setExtendQTotalPoint(aTeamModel.getExtendQTotalPoint());
        bTeamModel.setShotoutQTotalPoint(aTeamModel.getShotoutQTotalPoint());

        bTeamModel.setATeamFirstQPoint(aTeamModel.getBTeamFirstQPoint());
        bTeamModel.setBTeamFirstQPoint(aTeamModel.getATeamFirstQPoint());

        bTeamModel.setTime(aTeamModel.getTime());
        bTeamModel.setBTeam(aTeamModel.getATeam());
        bTeamModel.setATeam(aTeamModel.getBTeam());


        //첫득점
        if(aTeamModel.getFirstPoint().equals("승리")){
            bTeamModel.setFirstPoint("패배");
        } else {
            bTeamModel.setFirstPoint("승리");
        }

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


}

