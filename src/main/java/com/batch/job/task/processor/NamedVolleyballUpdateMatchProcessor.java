package com.batch.job.task.processor;


import com.batch.model.VolleyballModel;
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

import static java.lang.Math.round;

@Component
@Slf4j
@StepScope
public class NamedVolleyballUpdateMatchProcessor implements ItemProcessor<String, List<VolleyballModel>> {

    private String initSeasonDate;
    private String finishSeasonDate = "2021-04-15";
    private String baseBall_Url = "https://livescore.co.kr/sports/score_board/volley/view.php?date=";


    @Autowired
    private NamedUtil namedUtil;

    @Value("#{jobParameters[mode]}")
    private String mode;

    @Override
    public List<VolleyballModel> process(String s) throws Exception {
        return updateVolleyballMatch();
    }


    public List<VolleyballModel> updateVolleyballMatch() throws IOException, ParseException, JSONException, InterruptedException {


        int addDate = 0;
        Calendar curDate = Calendar.getInstance();
        curDate.setTime(new Date());
        curDate.add(Calendar.DATE, 1);
        String rootHtml = "";


        List<VolleyballModel> volleyballModelList = new ArrayList<>();

        while (true){
            Calendar startDate = Calendar.getInstance();
            startDate.setTime(new Date());
            if(mode.equals("all")){
                startDate.set(2020, 9, 15);
             }else {
                startDate.add(Calendar.DATE, -2);
             }

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            startDate.add(Calendar.DATE, addDate);
            String matchDate = df.format(startDate.getTime());
            if(df.format(startDate.getTime()).equals(df.format(curDate.getTime()))){
                log.info("배 Update Match 완료 : " + df.format(curDate.getTime()));
                break;
            }

            try {
                int dayNum = startDate.get(Calendar.DAY_OF_WEEK);
                String dayOfWeek = namedUtil.getDayoOfWeek(dayNum);
                rootHtml = namedUtil.liveScoreUrlToString(baseBall_Url + df.format(startDate.getTime()));
                Document rootDoc = Jsoup.parse(rootHtml);


                for (Element element : rootDoc.select("div#score_board div.score_tbl_individual")) {
                    int i = 0;
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

                        //풀, 하프, 이닝별 스코어, 핸디, 언오버, 정역배 설정
                        parseScoreAndOdd(element, aTeamModel, bTeamModel);

                        //풀, 하프, 핸드 언오버 결과 설정
                        parseFullHalfHandiUnover(element, aTeamModel, bTeamModel);

                        //쿼별 핸디 언오버 결과 설정
                        parseInningHandiUnover(element, aTeamModel, bTeamModel);

                        //이닝쿼별 스폐셜 결과 설정
                        parseSpecial(element, aTeamModel, bTeamModel, i);
                        
                        setBteamStat(aTeamModel, bTeamModel);

                        if (!aTeamModel.getATeam().equals("") && !aTeamModel.getGameId().equals("")){
                            volleyballModelList.add(aTeamModel);
                            volleyballModelList.add(bTeamModel);
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            addDate++;

        }

        return volleyballModelList;
    }

    private void parseScoreAndOdd(Element element, VolleyballModel aTeamModel, VolleyballModel bTeamModel) {
        String[] arrayHandi = element.select("tbody > tr > td.line").text().split(" ");

        if (arrayHandi.length > 1) {
            aTeamModel.setPointLine(Double.valueOf(arrayHandi[0]));
            aTeamModel.setHandiCap(Double.valueOf(arrayHandi[1]));
        } else {
            aTeamModel.setPointLine(0.0);
            aTeamModel.setHandiCap(0.0);
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

        String[] arrayFirstScore = element.select("tbody > tr > td.s").text().split(" ");

        if (arrayFirstScore.length == 8) {
            aTeamModel.setBTeamFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            aTeamModel.setBTeamSecondQPoint(Integer.valueOf(arrayFirstScore[1]));
            aTeamModel.setBTeamThirdQPoint(Integer.valueOf(arrayFirstScore[2]));
            aTeamModel.setBTeamFourthQPoint(0);
            aTeamModel.setBTeamFifthQPoint(0);

            aTeamModel.setATeamFirstQPoint(Integer.valueOf(arrayFirstScore[5]));
            aTeamModel.setATeamSecondQPoint(Integer.valueOf(arrayFirstScore[6]));
            aTeamModel.setATeamThirdQPoint(Integer.valueOf(arrayFirstScore[7]));
            aTeamModel.setATeamFourthQPoint(0);
            aTeamModel.setATeamFifthQPoint(0);

            bTeamModel.setATeamFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            bTeamModel.setATeamSecondQPoint(Integer.valueOf(arrayFirstScore[1]));
            bTeamModel.setATeamThirdQPoint(Integer.valueOf(arrayFirstScore[2]));
            bTeamModel.setATeamFourthQPoint(0);
            bTeamModel.setATeamFifthQPoint(0);

            bTeamModel.setBTeamFirstQPoint(Integer.valueOf(arrayFirstScore[5]));
            bTeamModel.setBTeamSecondQPoint(Integer.valueOf(arrayFirstScore[6]));
            bTeamModel.setBTeamThirdQPoint(Integer.valueOf(arrayFirstScore[7]));
            bTeamModel.setBTeamFourthQPoint(0);
            bTeamModel.setBTeamFifthQPoint(0);

            bTeamModel.setFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            bTeamModel.setSecondQPoint(Integer.valueOf(arrayFirstScore[1]));
            bTeamModel.setThirdQPoint(Integer.valueOf(arrayFirstScore[2]));
            bTeamModel.setFourthQPoint(0);
            bTeamModel.setFifthQPoint(0);

            aTeamModel.setFirstQPoint(Integer.valueOf(arrayFirstScore[5]));
            aTeamModel.setSecondQPoint(Integer.valueOf(arrayFirstScore[6]));
            aTeamModel.setThirdQPoint(Integer.valueOf(arrayFirstScore[7]));
            aTeamModel.setFourthQPoint(0);
            aTeamModel.setFifthQPoint(0);


        }
        else if(arrayFirstScore.length == 9){
            aTeamModel.setBTeamFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            aTeamModel.setBTeamSecondQPoint(Integer.valueOf(arrayFirstScore[1]));
            aTeamModel.setBTeamThirdQPoint(Integer.valueOf(arrayFirstScore[2]));
            aTeamModel.setBTeamFourthQPoint(Integer.valueOf(arrayFirstScore[3]));
            aTeamModel.setBTeamFifthQPoint(0);

            aTeamModel.setATeamFirstQPoint(Integer.valueOf(arrayFirstScore[5]));
            aTeamModel.setATeamSecondQPoint(Integer.valueOf(arrayFirstScore[6]));
            aTeamModel.setATeamThirdQPoint(Integer.valueOf(arrayFirstScore[7]));
            aTeamModel.setATeamFourthQPoint(Integer.valueOf(arrayFirstScore[8]));
            aTeamModel.setATeamFifthQPoint(0);

            bTeamModel.setATeamFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            bTeamModel.setATeamSecondQPoint(Integer.valueOf(arrayFirstScore[1]));
            bTeamModel.setATeamThirdQPoint(Integer.valueOf(arrayFirstScore[2]));
            bTeamModel.setATeamFourthQPoint(Integer.valueOf(arrayFirstScore[3]));
            bTeamModel.setATeamFifthQPoint(0);

            bTeamModel.setBTeamFirstQPoint(Integer.valueOf(arrayFirstScore[5]));
            bTeamModel.setBTeamSecondQPoint(Integer.valueOf(arrayFirstScore[6]));
            bTeamModel.setBTeamThirdQPoint(Integer.valueOf(arrayFirstScore[7]));
            bTeamModel.setBTeamFourthQPoint(Integer.valueOf(arrayFirstScore[8]));
            bTeamModel.setBTeamFifthQPoint(0);

            bTeamModel.setFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            bTeamModel.setSecondQPoint(Integer.valueOf(arrayFirstScore[1]));
            bTeamModel.setThirdQPoint(Integer.valueOf(arrayFirstScore[2]));
            bTeamModel.setFourthQPoint(Integer.valueOf(arrayFirstScore[3]));
            bTeamModel.setFifthQPoint(0);

            aTeamModel.setFirstQPoint(Integer.valueOf(arrayFirstScore[5]));
            aTeamModel.setSecondQPoint(Integer.valueOf(arrayFirstScore[6]));
            aTeamModel.setThirdQPoint(Integer.valueOf(arrayFirstScore[7]));
            aTeamModel.setFourthQPoint(Integer.valueOf(arrayFirstScore[8]));
            aTeamModel.setFifthQPoint(0);
            
        }
        else if(arrayFirstScore.length == 10){
            aTeamModel.setBTeamFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            aTeamModel.setBTeamSecondQPoint(Integer.valueOf(arrayFirstScore[1]));
            aTeamModel.setBTeamThirdQPoint(Integer.valueOf(arrayFirstScore[2]));
            aTeamModel.setBTeamFourthQPoint(Integer.valueOf(arrayFirstScore[3]));
            aTeamModel.setBTeamFifthQPoint(Integer.valueOf(arrayFirstScore[4]));

            aTeamModel.setATeamFirstQPoint(Integer.valueOf(arrayFirstScore[5]));
            aTeamModel.setATeamSecondQPoint(Integer.valueOf(arrayFirstScore[6]));
            aTeamModel.setATeamThirdQPoint(Integer.valueOf(arrayFirstScore[7]));
            aTeamModel.setATeamFourthQPoint(Integer.valueOf(arrayFirstScore[8]));
            aTeamModel.setATeamFifthQPoint(Integer.valueOf(arrayFirstScore[9]));



            bTeamModel.setATeamFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            bTeamModel.setATeamSecondQPoint(Integer.valueOf(arrayFirstScore[1]));
            bTeamModel.setATeamThirdQPoint(Integer.valueOf(arrayFirstScore[2]));
            bTeamModel.setATeamFourthQPoint(Integer.valueOf(arrayFirstScore[3]));
            bTeamModel.setATeamFifthQPoint(Integer.valueOf(arrayFirstScore[4]));

            bTeamModel.setBTeamFirstQPoint(Integer.valueOf(arrayFirstScore[5]));
            bTeamModel.setBTeamSecondQPoint(Integer.valueOf(arrayFirstScore[6]));
            bTeamModel.setBTeamThirdQPoint(Integer.valueOf(arrayFirstScore[7]));
            bTeamModel.setBTeamFourthQPoint(Integer.valueOf(arrayFirstScore[8]));
            bTeamModel.setBTeamFifthQPoint(Integer.valueOf(arrayFirstScore[9]));

            bTeamModel.setFirstQPoint(Integer.valueOf(arrayFirstScore[0]));
            bTeamModel.setSecondQPoint(Integer.valueOf(arrayFirstScore[1]));
            bTeamModel.setThirdQPoint(Integer.valueOf(arrayFirstScore[2]));
            bTeamModel.setFourthQPoint(Integer.valueOf(arrayFirstScore[3]));
            bTeamModel.setFifthQPoint(Integer.valueOf(arrayFirstScore[4]));

            aTeamModel.setFirstQPoint(Integer.valueOf(arrayFirstScore[5]));
            aTeamModel.setSecondQPoint(Integer.valueOf(arrayFirstScore[6]));
            aTeamModel.setThirdQPoint(Integer.valueOf(arrayFirstScore[7]));
            aTeamModel.setFourthQPoint(Integer.valueOf(arrayFirstScore[8]));
            aTeamModel.setFifthQPoint(Integer.valueOf(arrayFirstScore[9]));

        }
        else {
            aTeamModel.setBTeamFirstQPoint(0);
            aTeamModel.setBTeamSecondQPoint(0);
            aTeamModel.setBTeamThirdQPoint(0);
            aTeamModel.setBTeamFourthQPoint(0);
            aTeamModel.setBTeamFifthQPoint(0);

            aTeamModel.setATeamFirstQPoint(0);
            aTeamModel.setATeamSecondQPoint(0);
            aTeamModel.setATeamThirdQPoint(0);
            aTeamModel.setATeamFourthQPoint(0);
            aTeamModel.setATeamFifthQPoint(0);

            bTeamModel.setATeamFirstQPoint(0);
            bTeamModel.setATeamSecondQPoint(0);
            bTeamModel.setATeamThirdQPoint(0);
            bTeamModel.setATeamFourthQPoint(0);
            bTeamModel.setATeamFifthQPoint(0);

            bTeamModel.setBTeamFirstQPoint(0);
            bTeamModel.setBTeamSecondQPoint(0);
            bTeamModel.setBTeamThirdQPoint(0);
            bTeamModel.setBTeamFourthQPoint(0);
            bTeamModel.setBTeamFifthQPoint(0);

            bTeamModel.setFirstQPoint(0);
            bTeamModel.setSecondQPoint(0);
            bTeamModel.setThirdQPoint(0);
            bTeamModel.setFourthQPoint(0);
            bTeamModel.setFifthQPoint(0);

            aTeamModel.setFirstQPoint(0);
            aTeamModel.setSecondQPoint(0);
            aTeamModel.setThirdQPoint(0);
            aTeamModel.setFourthQPoint(0);
            aTeamModel.setFifthQPoint(0);
        }
 
    }

    private void parseFullHalfHandiUnover(Element element, VolleyballModel aTeamModel, VolleyballModel bTeamModel) {
        
        if(aTeamModel.getHandiCap() == 0){
            aTeamModel.setHandiCapResult("적특");
        }else {
            if ((aTeamModel.getTotalPoint() + aTeamModel.getHandiCap()) > bTeamModel.getTotalPoint()) {
                aTeamModel.setHandiCapResult("승리");
            } else if ((aTeamModel.getTotalPoint() + aTeamModel.getHandiCap()) < bTeamModel.getTotalPoint()) {
                aTeamModel.setHandiCapResult("패배");
            } else {
                aTeamModel.setHandiCapResult("적특");
            }
        }


        if(aTeamModel.getPointLine() == 0){
            aTeamModel.setPointLineResult("적특");
            bTeamModel.setPointLineResult("적특");
        } else {
            if ((aTeamModel.getTotalPoint() + bTeamModel.getTotalPoint()) > aTeamModel.getPointLine()) {
                aTeamModel.setPointLineResult("오버");
                bTeamModel.setPointLineResult("오버");
            } else if ((aTeamModel.getTotalPoint() + bTeamModel.getTotalPoint()) < aTeamModel.getPointLine()) {
                aTeamModel.setPointLineResult("언더");
                bTeamModel.setPointLineResult("언더");
            } else {
                aTeamModel.setPointLineResult("적특");
                bTeamModel.setPointLineResult("적특");
            }
        }

        aTeamModel.setATeamTotalPoint(aTeamModel.getTotalPoint());
        aTeamModel.setBTeamTotalPoint(bTeamModel.getTotalPoint());
        
        String[] arrayTotalScore = element.select("tbody > tr > td.score").text().split(" ");

        if (arrayTotalScore.length > 1) {
            aTeamModel.setBTeamSetScore(Integer.valueOf(arrayTotalScore[0]));
            aTeamModel.setATeamSetScore(Integer.valueOf(arrayTotalScore[1]));
        } else {
            aTeamModel.setBTeamSetScore(0);
            aTeamModel.setATeamSetScore(0);
        }
        if(aTeamModel.getHandiCap() < 0){
            aTeamModel.setSetHandiCap(-1.5);
        }else {
            aTeamModel.setSetHandiCap(1.5);
        }


        if(aTeamModel.getHandiCap() == 0){
            aTeamModel.setSetHandiCapResult("적특");
        }else {
            if ((aTeamModel.getATeamSetScore() + aTeamModel.getSetHandiCap()) > aTeamModel.getBTeamSetScore()) {
                aTeamModel.setSetHandiCapResult("승리");
            } else if ((aTeamModel.getATeamSetScore() + aTeamModel.getSetHandiCap()) < aTeamModel.getBTeamSetScore()) {
                aTeamModel.setSetHandiCapResult("패배");
            } else {
                aTeamModel.setSetHandiCapResult("적특");
            }
        }
        
    }

    private void parseInningHandiUnover(Element element, VolleyballModel aTeamModel, VolleyballModel bTeamModel) {

        String[] arrayFirstScore = element.select("tbody > tr > td.s").text().split(" ");


        double handi = aTeamModel.getHandiCap() / 4;
         int handiInt = (int) handi;
         double pointHandi = handi - handiInt;

         if ((pointHandi <= 0.333 && pointHandi >= 0.001) || (pointHandi >= -0.333 && pointHandi <= -0.001)) {
             if(pointHandi < 0){
                 aTeamModel.setFirstQHandiCap(Double.valueOf(Math.ceil(aTeamModel.getHandiCap() / 4)));
             }else {
                 aTeamModel.setFirstQHandiCap(Double.valueOf(Math.floor(aTeamModel.getHandiCap() / 4)));
             }
         } else if((pointHandi <= 0.999 && pointHandi >= 0.666) || (pointHandi >= -0.999 && pointHandi <= -0.666)) {
             if(pointHandi < 0){
                 aTeamModel.setFirstQHandiCap(Double.valueOf(Math.floor(aTeamModel.getHandiCap() / 4)));
             }else {
                 aTeamModel.setFirstQHandiCap(Double.valueOf(Math.ceil(aTeamModel.getHandiCap() / 4)));
             }
         } else if((pointHandi <= 0.665 && pointHandi >= 0.334)) {
             aTeamModel.setFirstQHandiCap(handiInt + 0.5);
         }  else if((pointHandi >= -0.665 && pointHandi <= -0.334)) {
             aTeamModel.setFirstQHandiCap(handiInt - 0.5);
         } else if (aTeamModel.getHandiCap() >= 0.5 && aTeamModel.getHandiCap() <= 1.5) {
             aTeamModel.setFirstQHandiCap(0.5);
         } else if (aTeamModel.getHandiCap() <= -0.5 && aTeamModel.getHandiCap() >= -1.5) {
             aTeamModel.setFirstQHandiCap(-0.5);
         }else {
             aTeamModel.setFirstQHandiCap(0.0);
         }


         if(aTeamModel.getFirstQHandiCap() == 0){
             aTeamModel.setFirstQHandiCapResult("적특");
             aTeamModel.setSecondQHandiCapResult("적특");
             aTeamModel.setThirdQHandiCapResult("적특");
             aTeamModel.setFourthQHandiCapResult("적특");
             aTeamModel.setFifthQHandiCapResult("적특");
         }
         else {

             if ((aTeamModel.getATeamFirstQPoint() + aTeamModel.getFirstQHandiCap()) > aTeamModel.getBTeamFirstQPoint()) {
                 aTeamModel.setFirstQHandiCapResult("승리");
             } else if ((aTeamModel.getATeamFirstQPoint() + aTeamModel.getFirstQHandiCap()) < aTeamModel.getBTeamFirstQPoint()) {
                 aTeamModel.setFirstQHandiCapResult("패배");
             } else {
                 aTeamModel.setFirstQHandiCapResult("적특");
             }

             if ((aTeamModel.getATeamSecondQPoint() + aTeamModel.getFirstQHandiCap()) > aTeamModel.getBTeamSecondQPoint()) {
                 aTeamModel.setSecondQHandiCapResult("승리");
             } else if ((aTeamModel.getATeamSecondQPoint() + aTeamModel.getFirstQHandiCap()) < aTeamModel.getBTeamSecondQPoint()) {
                 aTeamModel.setSecondQHandiCapResult("패배");
             } else {
                 aTeamModel.setSecondQHandiCapResult("적특");
             }

             if ((aTeamModel.getATeamThirdQPoint() + aTeamModel.getFirstQHandiCap()) > aTeamModel.getBTeamThirdQPoint()) {
                 aTeamModel.setThirdQHandiCapResult("승리");
             } else if ((aTeamModel.getATeamThirdQPoint() + aTeamModel.getFirstQHandiCap()) < aTeamModel.getBTeamThirdQPoint()) {
                 aTeamModel.setThirdQHandiCapResult("패배");
             } else {
                 aTeamModel.setThirdQHandiCapResult("적특");
             }

             if((arrayFirstScore.length >= 9)){
                 if ((aTeamModel.getATeamFourthQPoint() + aTeamModel.getFirstQHandiCap()) > aTeamModel.getBTeamFourthQPoint()) {
                     aTeamModel.setFourthQHandiCapResult("승리");
                 } else if ((aTeamModel.getATeamFourthQPoint() + aTeamModel.getFirstQHandiCap()) < aTeamModel.getBTeamFourthQPoint()) {
                     aTeamModel.setFourthQHandiCapResult("패배");
                 } else {
                     aTeamModel.setFourthQHandiCapResult("적특");
                 }
             } else {
                 aTeamModel.setFourthQHandiCapResult("0");

             }


             if((arrayFirstScore.length >= 10)){
                 if ((aTeamModel.getATeamFifthQPoint() + aTeamModel.getFirstQHandiCap()) > aTeamModel.getBTeamFifthQPoint()) {
                     aTeamModel.setFifthQHandiCapResult("승리");
                 } else if ((aTeamModel.getATeamFifthQPoint() + aTeamModel.getFirstQHandiCap()) < aTeamModel.getBTeamFifthQPoint()) {
                     aTeamModel.setFifthQHandiCapResult("패배");
                 } else {
                     aTeamModel.setFifthQHandiCapResult("적특");
                 }
             } else {
                 aTeamModel.setFifthQHandiCapResult("0");

             }


         }

         if (aTeamModel.getATeamFirstQPoint() > aTeamModel.getBTeamFirstQPoint()) {
             aTeamModel.setFirstQResult("승리");
             bTeamModel.setFirstQResult("패배");
         } else if (aTeamModel.getATeamFirstQPoint() < aTeamModel.getBTeamFirstQPoint()) {
             aTeamModel.setFirstQResult("패배");
             bTeamModel.setFirstQResult("승리");
         } else {
             aTeamModel.setFirstQResult("적특");
             bTeamModel.setFirstQResult("적특");

         }

         if (aTeamModel.getATeamSecondQPoint() > aTeamModel.getBTeamSecondQPoint()) {
             aTeamModel.setSecondQResult("승리");
             bTeamModel.setSecondQResult("패배");
         } else if (aTeamModel.getATeamSecondQPoint() < aTeamModel.getBTeamSecondQPoint()) {
             aTeamModel.setSecondQResult("패배");
             bTeamModel.setSecondQResult("승리");
         } else {
             aTeamModel.setSecondQResult("적특");
             bTeamModel.setSecondQResult("적특");

         }

         if (aTeamModel.getATeamThirdQPoint() > aTeamModel.getBTeamThirdQPoint()) {
             aTeamModel.setThirdQResult("승리");
             bTeamModel.setThirdQResult("패배");
         } else if (aTeamModel.getATeamThirdQPoint() < aTeamModel.getBTeamThirdQPoint()) {
             aTeamModel.setThirdQResult("패배");
             bTeamModel.setThirdQResult("승리");
         } else {
             aTeamModel.setThirdQResult("적특");
             bTeamModel.setThirdQResult("적특");

         }

         if((arrayFirstScore.length >= 9)) {
             if (aTeamModel.getATeamFourthQPoint() > aTeamModel.getBTeamFourthQPoint()) {
                 aTeamModel.setFourthQResult("승리");
                 bTeamModel.setFourthQResult("패배");
             } else if (aTeamModel.getATeamFourthQPoint() < aTeamModel.getBTeamFourthQPoint()) {
                 aTeamModel.setFourthQResult("패배");
                 bTeamModel.setFourthQResult("승리");
             } else {
                 aTeamModel.setFourthQResult("적특");
                 bTeamModel.setFourthQResult("적특");
             }
         } else {
             aTeamModel.setFourthQResult("0");
             bTeamModel.setFourthQResult("0");
         }


         if((arrayFirstScore.length >= 10)){

             if (aTeamModel.getATeamFifthQPoint() > aTeamModel.getBTeamFifthQPoint()) {
                 aTeamModel.setFifthQResult("승리");
                 bTeamModel.setFifthQResult("패배");
             } else if (aTeamModel.getATeamFifthQPoint() < aTeamModel.getBTeamFifthQPoint()) {
                 aTeamModel.setFifthQResult("패배");
                 bTeamModel.setFifthQResult("승리");
             } else {
                 aTeamModel.setFifthQResult("적특");
                 bTeamModel.setFifthQResult("적특");
             }
         } else {
         aTeamModel.setFifthQResult("0");
         bTeamModel.setFifthQResult("0");
         }





     double point = aTeamModel.getPointLine() / 4;
         int pointInt = (int) point;
         double pointLine = point - pointInt;

         if ((pointLine <= 0.333 && pointLine >= 0.001)) {
             aTeamModel.setFirstQPointLine(Double.valueOf(Math.floor(aTeamModel.getPointLine() / 4)));
         } else if((pointLine <= 0.999 && pointLine >= 0.666)) {
             aTeamModel.setFirstQPointLine(Double.valueOf(Math.ceil(aTeamModel.getPointLine() / 4)));
         } else if((pointLine <= 0.665 && pointLine >= 0.334)) {
             aTeamModel.setFirstQPointLine(pointInt + 0.5);
         } else {
             aTeamModel.setFirstQPointLine(Double.valueOf(Math.round(aTeamModel.getPointLine() / 4)));
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

             if ((aTeamModel.getATeamSecondQPoint() + aTeamModel.getBTeamSecondQPoint()) > aTeamModel.getFirstQPointLine()) {
                 aTeamModel.setSecondQPointLineResult("오버");
                 bTeamModel.setSecondQPointLineResult("오버");

             } else if ((aTeamModel.getATeamSecondQPoint() + aTeamModel.getBTeamSecondQPoint()) < aTeamModel.getFirstQPointLine()) {
                 aTeamModel.setSecondQPointLineResult("언더");
                 bTeamModel.setSecondQPointLineResult("언더");

             } else {
                 aTeamModel.setSecondQPointLineResult("적특");
                 bTeamModel.setSecondQPointLineResult("적특");
             }

             if ((aTeamModel.getATeamThirdQPoint() + aTeamModel.getBTeamThirdQPoint()) > aTeamModel.getFirstQPointLine()) {
                 aTeamModel.setThirdQPointLineResult("오버");
                 bTeamModel.setThirdQPointLineResult("오버");

             } else if ((aTeamModel.getATeamThirdQPoint() + aTeamModel.getBTeamThirdQPoint()) < aTeamModel.getFirstQPointLine()) {
                 aTeamModel.setThirdQPointLineResult("언더");
                 bTeamModel.setThirdQPointLineResult("언더");

             } else {
                 aTeamModel.setThirdQPointLineResult("적특");
                 bTeamModel.setThirdQPointLineResult("적특");
             }

             if((arrayFirstScore.length >= 9)){
                 if ((aTeamModel.getATeamFourthQPoint() + aTeamModel.getBTeamFourthQPoint()) > aTeamModel.getFirstQPointLine()) {
                     aTeamModel.setFourthQPointLineResult("오버");
                     bTeamModel.setFourthQPointLineResult("오버");

                 } else if ((aTeamModel.getATeamFourthQPoint() + aTeamModel.getBTeamFourthQPoint()) < aTeamModel.getFirstQPointLine()) {
                     aTeamModel.setFourthQPointLineResult("언더");
                     bTeamModel.setFourthQPointLineResult("언더");

                 } else {
                     aTeamModel.setFourthQPointLineResult("적특");
                     bTeamModel.setFourthQPointLineResult("적특");
                 }
             } else {
                 aTeamModel.setFourthQPointLineResult("0");
                 bTeamModel.setFourthQPointLineResult("0");
             }


         }


         String[] arrayQTotalScore = element.select("tfoot > tr > td.s").text().split(" ");

         if (arrayQTotalScore.length == 3) {
             aTeamModel.setFirstQTotalPoint(Integer.valueOf(arrayQTotalScore[0]));
             aTeamModel.setSecondQTotalPoint(Integer.valueOf(arrayQTotalScore[1]));
             aTeamModel.setThirdQTotalPoint(Integer.valueOf(arrayQTotalScore[2]));
             aTeamModel.setFourthQTotalPoint(0);
             aTeamModel.setFifthQTotalPoint(0);

         } else if (arrayQTotalScore.length == 4) {

             aTeamModel.setFirstQTotalPoint(Integer.valueOf(arrayQTotalScore[0]));
             aTeamModel.setSecondQTotalPoint(Integer.valueOf(arrayQTotalScore[1]));
             aTeamModel.setThirdQTotalPoint(Integer.valueOf(arrayQTotalScore[2]));
             aTeamModel.setFourthQTotalPoint(Integer.valueOf(arrayQTotalScore[3]));
             aTeamModel.setFifthQTotalPoint(0);

         } else if (arrayQTotalScore.length == 5) {
             aTeamModel.setFirstQTotalPoint(Integer.valueOf(arrayQTotalScore[0]));
             aTeamModel.setSecondQTotalPoint(Integer.valueOf(arrayQTotalScore[1]));
             aTeamModel.setThirdQTotalPoint(Integer.valueOf(arrayQTotalScore[2]));
             aTeamModel.setFourthQTotalPoint(Integer.valueOf(arrayQTotalScore[3]));
             aTeamModel.setFifthQTotalPoint(Integer.valueOf(arrayQTotalScore[4]));
         }else  {
             aTeamModel.setFirstQTotalPoint(0);
             aTeamModel.setSecondQTotalPoint(0);
             aTeamModel.setThirdQTotalPoint(0);
             aTeamModel.setFourthQTotalPoint(0);
             aTeamModel.setFifthQTotalPoint(0);

         }
   
    }

    private void parseSpecial(Element element, VolleyballModel aTeamModel, VolleyballModel bTeamModel,int i) {

        for (Element element1 : element.select("tbody > tr > td.f.ico_linescore")) {
            if (i == 0) {
                if (element1.select("span.ico_f_point").text().equals("첫득점")) {
                    aTeamModel.setFirstPoint("패배");
                } else {
                    aTeamModel.setFirstPoint("승리");
                }
                if (element1.select("span.ico_f_block").text().equals("블로킹")) {
                    aTeamModel.setFirstBlock("패배");
                } else {
                    aTeamModel.setFirstBlock("승리");
                }
                if (element1.select("span.ico_f_serve").text().equals("서브득")) {
                    aTeamModel.setFirstServe("패배");
                } else {
                    aTeamModel.setFirstServe("승리");
                }
            }
            i++;
        }

        int cnt = 0;
        for (Element ele : element.select("tbody tr td.f.navy")){
//                        String[] arrayFirstPointList = ele.select("td.f.navy").text().split(" ");
            String arrayFirst = ele.text();
            if(cnt == 0 && arrayFirst.equals("●")){
                aTeamModel.setFirst5Point("패배");
                bTeamModel.setFirst5Point("승리");
            } else if(cnt == 0){
                aTeamModel.setFirst5Point("승리");
                bTeamModel.setFirst5Point("패배");
            }
            if(cnt == 1 && arrayFirst.equals("●")){
                aTeamModel.setFirst7Point("패배");
                bTeamModel.setFirst7Point("승리");

            } else if(cnt == 1){
                aTeamModel.setFirst7Point("승리");
                bTeamModel.setFirst7Point("패배");

            }
            if(cnt == 2 && arrayFirst.equals("●")){
                aTeamModel.setFirst10Point("패배");
                bTeamModel.setFirst10Point("승리");
            } else if(cnt == 2){
                aTeamModel.setFirst10Point("승리");
                bTeamModel.setFirst10Point("패배");
            }
            cnt++;
        }

    }

    public void setBteamStat(VolleyballModel aTeamModel , VolleyballModel bTeamModel){
        bTeamModel.setLeague(aTeamModel.getLeague());


        bTeamModel.setPointLine(aTeamModel.getPointLine());
        bTeamModel.setHandiCap(-(aTeamModel.getHandiCap()));

        bTeamModel.setSetHandiCap(-aTeamModel.getSetHandiCap());

        bTeamModel.setBTeamSetScore(aTeamModel.getATeamSetScore());
        bTeamModel.setATeamSetScore(aTeamModel.getBTeamSetScore());

        if (aTeamModel.getHandiCapResult().equals("패배")) {
            bTeamModel.setHandiCapResult("승리");
        } else if (aTeamModel.getHandiCapResult().equals("승리")) {
            bTeamModel.setHandiCapResult("패배");
        } else {
            bTeamModel.setHandiCapResult("적특");
        }

        bTeamModel.setATeamTotalPoint(bTeamModel.getTotalPoint());
        bTeamModel.setBTeamTotalPoint(aTeamModel.getTotalPoint());

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

        if (aTeamModel.getSecondQHandiCapResult().equals("패배")) {
            bTeamModel.setSecondQHandiCapResult("승리");
        } else if  (aTeamModel.getSecondQHandiCapResult().equals("승리")) {
            bTeamModel.setSecondQHandiCapResult("패배");
        } else {
            bTeamModel.setSecondQHandiCapResult("적특");
        }

        if (aTeamModel.getThirdQHandiCapResult().equals("패배")) {
            bTeamModel.setThirdQHandiCapResult("승리");
        } else if  (aTeamModel.getThirdQHandiCapResult().equals("승리")) {
            bTeamModel.setThirdQHandiCapResult("패배");
        } else {
            bTeamModel.setThirdQHandiCapResult("적특");
        }

        if(!aTeamModel.getFourthQHandiCapResult().isEmpty()){
            if (aTeamModel.getFourthQHandiCapResult().equals("패배")) {
                bTeamModel.setFourthQHandiCapResult("승리");
            } else if  (aTeamModel.getFourthQHandiCapResult().equals("승리")) {
                bTeamModel.setFourthQHandiCapResult("패배");
            } else if (aTeamModel.getFourthQHandiCapResult().equals("0")){
                bTeamModel.setFourthQHandiCapResult("0");
            }else {
                bTeamModel.setFourthQHandiCapResult("적특");
            }
        }

        if(!aTeamModel.getFifthQHandiCapResult().isEmpty()){
            if (aTeamModel.getFifthQHandiCapResult().equals("패배")) {
                bTeamModel.setFifthQHandiCapResult("승리");
            } else if  (aTeamModel.getFifthQHandiCapResult().equals("승리")) {
                bTeamModel.setFifthQHandiCapResult("패배");
            } else if (aTeamModel.getFifthQHandiCapResult().equals("0")){
                bTeamModel.setFifthQHandiCapResult("0");
            }else {
                bTeamModel.setFifthQHandiCapResult("적특");
            }
        }


        if (aTeamModel.getSetHandiCapResult().equals("패배")) {
            bTeamModel.setSetHandiCapResult("승리");
        } else if  (aTeamModel.getSetHandiCapResult().equals("승리")) {
            bTeamModel.setSetHandiCapResult("패배");
        } else {
            bTeamModel.setSetHandiCapResult("적특");
        }

        bTeamModel.setFirstQTotalPoint(aTeamModel.getFirstQTotalPoint());
        bTeamModel.setSecondQTotalPoint(aTeamModel.getSecondQTotalPoint());
        bTeamModel.setThirdQTotalPoint(aTeamModel.getThirdQTotalPoint());
        bTeamModel.setFourthQTotalPoint(aTeamModel.getFourthQTotalPoint());
        bTeamModel.setFifthQTotalPoint(aTeamModel.getFifthQTotalPoint());

        bTeamModel.setATeamFirstQPoint(aTeamModel.getBTeamFirstQPoint());
        bTeamModel.setBTeamFirstQPoint(aTeamModel.getATeamFirstQPoint());

        bTeamModel.setTime(aTeamModel.getTime());
        bTeamModel.setBTeam(aTeamModel.getATeam());
        bTeamModel.setATeam(aTeamModel.getBTeam());

        //첫2득
        if(aTeamModel.getFirstServe().equals("승리")){
            bTeamModel.setFirstServe("패배");
        } else {
            bTeamModel.setFirstServe("승리");
        }


        //첫득점
        if(aTeamModel.getFirstPoint().equals("승리")){
            bTeamModel.setFirstPoint("패배");
        } else {
            bTeamModel.setFirstPoint("승리");
        }


        //자유투
        if(aTeamModel.getFirstBlock().equals("승리")){
            bTeamModel.setFirstBlock("패배");
        } else {
            bTeamModel.setFirstBlock("승리");
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

