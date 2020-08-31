package com.batch.job.task.processor;


import com.batch.model.BasketballModel;
import com.batch.util.NamedUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
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
public class NamedBasketballUpdateMatchProcessor implements ItemProcessor<String, List<BasketballModel>> {

    private String initSeasonDate;
    private String finishSeasonDate = "2019-12-14";

    @Autowired
    private NamedUtil namedUtil;

    @Value("#{jobParameters[mode]}")
    private String mode;

    @Override
    public List<BasketballModel> process(String s) throws Exception {
        return updateBasketballMatch();
    }


    public List<BasketballModel> updateBasketballMatch() throws IOException, ParseException, JSONException, InterruptedException {

        List<BasketballModel> basketballModelList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();

        String rootHtml = "";
        String url = "https://livescore.co.kr/sports/score_board/basket/view.php?date=";
        int addDate = 0;

        Calendar curDate = Calendar.getInstance();
        curDate.setTime(new Date());
        curDate.add(Calendar.DATE, 1);

        int tempCnt= 0 ;
        while (true) {

            Calendar startDate = Calendar.getInstance();
            startDate.setTime(new Date());
            if(mode.equals("all")){
                startDate.set(2020, 6, 01);
            }else {
                startDate.add(Calendar.DATE, -2);
            }
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            startDate.add(Calendar.DATE, addDate);

            //nba 10-22 ~ 2020.10.16
            //wkbl  2019년 10월 19일 (토) ~ 2020년 3월 19일 (목)
            //kbl 2019년 10월 5일 (토) ~ 2020년 3월 31일 (화)
            if (df.format(startDate.getTime()).equals(df.format(curDate.getTime()))) {
//            if (df.format(startDate.getTime()).equals("2020-06-03")) {
                log.info("농구 Update Match 완료 : " + df.format(startDate.getTime()));
                break;
            }

            int dayNum = startDate.get(Calendar.DAY_OF_WEEK);
            String dayOfWeek = namedUtil.getDayoOfWeek(dayNum);

            Thread.sleep(2000);

            rootHtml = namedUtil.liveScoreUrlToString(url + df.format(startDate.getTime()));

            Document rootDoc = Jsoup.parse(rootHtml);
            Elements elements = rootDoc.select("div#score_board div.score_tbl_individual");

            for (Element element : rootDoc.select("div#score_board div.score_tbl_individual")) {
                BasketballModel aTeamModel = new BasketballModel();
                BasketballModel bTeamModel = new BasketballModel();

                int i = 0;

                String league = element.select("thead tr th.reague").text();

                if(!league.equals("KBL") && !league.equals("NBA") && !league.equals("WKBL")){
                    continue;
                }

                String gameStatus = element.select("thead tr th.ping").text();

                if (!gameStatus.equals("종료")){
                    continue;
                }

                String gameId = element.select("div.score_tbl_individual").attr("id");

                aTeamModel.setGameId(gameId);
                bTeamModel.setGameId(gameId);
                aTeamModel.setDayOfWeek(dayOfWeek);
                bTeamModel.setDayOfWeek(dayOfWeek);
                aTeamModel.setLeague(league);
                bTeamModel.setLeague(league);


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


                //풀, 하프, 이닝별 스코어, 핸디, 언오버, 정역배 설정
                parseScoreAndOdd(element, aTeamModel, bTeamModel);

                //풀, 하프, 핸드 언오버 결과 설정
                parseFullHalfHandiUnover(element, aTeamModel, bTeamModel);

                //이닝별 핸디 언오버 결과 설정
                parseInningHandiUnover(element, aTeamModel, bTeamModel);

                //이닝별 스폐셜 결과 설정
                parseSpecial(element, aTeamModel, bTeamModel);

                if (aTeamModel.getGameId() != null && bTeamModel.getGameId() != null) {
                    if (checkTeam(aTeamModel.getATeam()) && checkTeam(bTeamModel.getATeam())) {
                        basketballModelList.add(aTeamModel);
                        basketballModelList.add(bTeamModel);
                    }
                }

            }

            addDate++;
        }


        return basketballModelList;
    }

    private void parseScoreAndOdd(Element element, BasketballModel aTeamModel, BasketballModel bTeamModel) {
        String[] totalScoreArr = element.select("tbody > tr > td.score").text().split(" ");

        if (totalScoreArr.length > 1) {
            aTeamModel.setBTeamTotalPoint(Integer.valueOf(totalScoreArr[0]));
            aTeamModel.setATeamTotalPoint(Integer.valueOf(totalScoreArr[1]));

            bTeamModel.setBTeamTotalPoint(Integer.valueOf(totalScoreArr[1]));
            bTeamModel.setATeamTotalPoint(Integer.valueOf(totalScoreArr[0]));
        } else {
            aTeamModel.setBTeamTotalPoint(0);
            aTeamModel.setATeamTotalPoint(0);

            bTeamModel.setBTeamTotalPoint(0);
            bTeamModel.setATeamTotalPoint(0);
        }

        String[] inningScoreArr = element.select("tbody > tr > td.s").text().split(" ");

        if (inningScoreArr.length > 8) {
            aTeamModel.setBTeamFirstQPoint(Integer.valueOf(inningScoreArr[0]));
            aTeamModel.setBTeamSecondQPoint(Integer.valueOf(inningScoreArr[1]));
            aTeamModel.setBTeamThirdQPoint(Integer.valueOf(inningScoreArr[2]));
            aTeamModel.setBTeamFourthQPoint(Integer.valueOf(inningScoreArr[3]));

            bTeamModel.setATeamFirstQPoint(Integer.valueOf(inningScoreArr[0]));
            bTeamModel.setATeamSecondQPoint(Integer.valueOf(inningScoreArr[1]));
            bTeamModel.setATeamThirdQPoint(Integer.valueOf(inningScoreArr[2]));
            bTeamModel.setATeamFourthQPoint(Integer.valueOf(inningScoreArr[3]));

            if (inningScoreArr.length == 10) {
                aTeamModel.setBTeamExtendQPoint(Integer.valueOf(inningScoreArr[4]));
                bTeamModel.setATeamExtendQPoint(Integer.valueOf(inningScoreArr[4]));

            } else {
                aTeamModel.setBTeamExtendQPoint(0);
                bTeamModel.setBTeamExtendQPoint(0);

            }

            aTeamModel.setATeamFirstQPoint(Integer.valueOf(inningScoreArr[5]));
            aTeamModel.setATeamSecondQPoint(Integer.valueOf(inningScoreArr[6]));
            aTeamModel.setATeamThirdQPoint(Integer.valueOf(inningScoreArr[7]));
            aTeamModel.setATeamFourthQPoint(Integer.valueOf(inningScoreArr[8]));

            bTeamModel.setBTeamFirstQPoint(Integer.valueOf(inningScoreArr[5]));
            bTeamModel.setBTeamSecondQPoint(Integer.valueOf(inningScoreArr[6]));
            bTeamModel.setBTeamThirdQPoint(Integer.valueOf(inningScoreArr[7]));
            bTeamModel.setBTeamFourthQPoint(Integer.valueOf(inningScoreArr[8]));
            if (inningScoreArr.length == 10) {
                aTeamModel.setATeamExtendQPoint(Integer.valueOf(inningScoreArr[9]));
                bTeamModel.setBTeamExtendQPoint(Integer.valueOf(inningScoreArr[9]));

            } else {
                aTeamModel.setATeamExtendQPoint(0);
                bTeamModel.setBTeamExtendQPoint(0);

            }
        } else {

            bTeamModel.setATeamFirstQPoint(0);
            bTeamModel.setATeamSecondQPoint(0);
            bTeamModel.setATeamThirdQPoint(0);
            bTeamModel.setATeamFourthQPoint(0);
            bTeamModel.setATeamExtendQPoint(0);

            aTeamModel.setATeamFirstQPoint(0);
            aTeamModel.setATeamSecondQPoint(0);
            aTeamModel.setATeamThirdQPoint(0);
            aTeamModel.setATeamFourthQPoint(0);
            aTeamModel.setATeamExtendQPoint(0);

            bTeamModel.setBTeamFirstQPoint(0);
            bTeamModel.setBTeamSecondQPoint(0);
            bTeamModel.setBTeamThirdQPoint(0);
            bTeamModel.setBTeamFourthQPoint(0);
            bTeamModel.setBTeamExtendQPoint(0);

            aTeamModel.setBTeamFirstQPoint(0);
            aTeamModel.setBTeamSecondQPoint(0);
            aTeamModel.setBTeamThirdQPoint(0);
            aTeamModel.setBTeamFourthQPoint(0);
            aTeamModel.setBTeamExtendQPoint(0);
        }

        aTeamModel.setATeamFirstHalfPoint(aTeamModel.getATeamFirstQPoint() + aTeamModel.getATeamSecondQPoint());
        aTeamModel.setBTeamFirstHalfPoint(aTeamModel.getBTeamFirstQPoint() + aTeamModel.getBTeamSecondQPoint());

        bTeamModel.setATeamFirstHalfPoint(bTeamModel.getATeamFirstQPoint() + bTeamModel.getATeamSecondQPoint());
        bTeamModel.setBTeamFirstHalfPoint(bTeamModel.getBTeamFirstQPoint() + bTeamModel.getBTeamSecondQPoint());

        aTeamModel.setATeamSecondHalfPoint(aTeamModel.getATeamThirdQPoint() + aTeamModel.getATeamFourthQPoint());
        aTeamModel.setBTeamSecondHalfPoint(aTeamModel.getBTeamThirdQPoint() + aTeamModel.getBTeamFourthQPoint());

        bTeamModel.setATeamSecondHalfPoint(bTeamModel.getATeamThirdQPoint() + bTeamModel.getATeamFourthQPoint());
        bTeamModel.setBTeamSecondHalfPoint(bTeamModel.getBTeamThirdQPoint() + bTeamModel.getBTeamFourthQPoint());

        String[] qTotalScoreArr = element.select("tfoot > tr > td.s").text().split(" ");

        System.out.println(aTeamModel.getGameId());

        if (qTotalScoreArr.length > 1) {
            aTeamModel.setFirstQTotalPoint(Integer.valueOf(qTotalScoreArr[0]));
            aTeamModel.setSecondQTotalPoint(Integer.valueOf(qTotalScoreArr[1]));
            aTeamModel.setThirdQTotalPoint(Integer.valueOf(qTotalScoreArr[2]));
            aTeamModel.setFourthQTotalPoint(Integer.valueOf(qTotalScoreArr[3]));

            if (qTotalScoreArr.length > 4) {
                aTeamModel.setExtendQTotalPoint(Integer.valueOf(qTotalScoreArr[4]));
            } else {
                aTeamModel.setExtendQTotalPoint(0);
            }
        } else {
            aTeamModel.setFirstQTotalPoint(0);
            aTeamModel.setSecondQTotalPoint(0);
            aTeamModel.setThirdQTotalPoint(0);
            aTeamModel.setFourthQTotalPoint(0);
            aTeamModel.setExtendQTotalPoint(0);
        }


        bTeamModel.setFirstQTotalPoint(aTeamModel.getFirstQTotalPoint());
        bTeamModel.setSecondQTotalPoint(aTeamModel.getSecondQTotalPoint());
        bTeamModel.setThirdQTotalPoint(aTeamModel.getThirdQTotalPoint());
        bTeamModel.setFourthQTotalPoint(aTeamModel.getFourthQTotalPoint());
        bTeamModel.setExtendQTotalPoint(aTeamModel.getExtendQTotalPoint());


        String[] handiArr = element.select("tbody > tr > td.line").text().split(" ");

        if (handiArr.length > 1) {
            aTeamModel.setPointLine(Double.valueOf(handiArr[0]));
            aTeamModel.setHandiCap(Double.valueOf(handiArr[1]));

            bTeamModel.setPointLine(Double.valueOf(handiArr[0]));
            bTeamModel.setHandiCap(Double.valueOf(handiArr[1]) * -1);
        } else {
            aTeamModel.setPointLine(0.0);
            aTeamModel.setHandiCap(0.0);

            bTeamModel.setPointLine(0.0);
            bTeamModel.setHandiCap(0.0);
        }

        if (aTeamModel.getHandiCap() > 0) {
            aTeamModel.setOdd("역배");
            bTeamModel.setOdd("정배");
        } else if (aTeamModel.getHandiCap() < 0) {
            aTeamModel.setOdd("정배");
            bTeamModel.setOdd("역배");
        } else {
            aTeamModel.setOdd("없음");
            bTeamModel.setOdd("없음");
        }
    }

    private void parseFullHalfHandiUnover(Element element, BasketballModel aTeamModel, BasketballModel bTeamModel) {

        if (aTeamModel.getHandiCap() == 0) {
            aTeamModel.setHandiCapResult("적특");
            bTeamModel.setHandiCapResult("적특");
        } else {
            if ((aTeamModel.getATeamTotalPoint() + aTeamModel.getHandiCap()) > aTeamModel.getBTeamTotalPoint()) {
                aTeamModel.setHandiCapResult("승리");
                bTeamModel.setHandiCapResult("패배");
            } else if ((aTeamModel.getATeamTotalPoint() + aTeamModel.getHandiCap()) < aTeamModel.getBTeamTotalPoint()) {
                aTeamModel.setHandiCapResult("패배");
                bTeamModel.setHandiCapResult("승리");
            } else {
                aTeamModel.setHandiCapResult("적특");
                bTeamModel.setHandiCapResult("적특");
            }
        }


        if (aTeamModel.getPointLine() == 0) {
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

        double halfHandi = aTeamModel.getHandiCap() / 2;
        int halfHandiInt = (int) halfHandi;
        double pointHalfHandi = halfHandi - halfHandiInt;

        if (pointHalfHandi == 0.5 || pointHalfHandi == -0.5) {
            aTeamModel.setHalfHandiCap(aTeamModel.getHandiCap() / 2);
            bTeamModel.setHalfHandiCap((aTeamModel.getHandiCap() / 2) * -1);

        } else if (aTeamModel.getHandiCap() >= 0.5 && aTeamModel.getHandiCap() <= 1.5) {
            aTeamModel.setHalfHandiCap(0.5);
            bTeamModel.setHalfHandiCap(-0.5);
        } else if (aTeamModel.getHandiCap() <= -0.5 && aTeamModel.getHandiCap() >= -1.5) {
            aTeamModel.setHalfHandiCap(-0.5);
            bTeamModel.setHalfHandiCap(0.5);
        } else {
            aTeamModel.setHalfHandiCap((double) Math.round(aTeamModel.getHandiCap() / 4));
            bTeamModel.setHalfHandiCap((double) Math.round(aTeamModel.getHandiCap() / 4) * -1);
        }

        if (aTeamModel.getHalfHandiCap() == 0) {
            aTeamModel.setFirstHalfHandiCapResult("적특");
            bTeamModel.setFirstHalfHandiCapResult("적특");
            aTeamModel.setSecondHalfHandiCapResult("적특");
            bTeamModel.setSecondHalfHandiCapResult("적특");

        } else {
            if ((aTeamModel.getATeamFirstHalfPoint() + aTeamModel.getHalfHandiCap()) > aTeamModel.getBTeamFirstHalfPoint()) {
                aTeamModel.setFirstHalfHandiCapResult("승리");
                bTeamModel.setFirstHalfHandiCapResult("패배");
            } else if ((aTeamModel.getATeamFirstHalfPoint() + aTeamModel.getHalfHandiCap()) < aTeamModel.getBTeamFirstHalfPoint()) {
                aTeamModel.setFirstHalfHandiCapResult("패배");
                bTeamModel.setFirstHalfHandiCapResult("승리");
            } else {
                aTeamModel.setFirstHalfHandiCapResult("적특");
                bTeamModel.setFirstHalfHandiCapResult("적특");
            }

            if ((aTeamModel.getATeamSecondHalfPoint() + aTeamModel.getHalfHandiCap()) > aTeamModel.getBTeamSecondHalfPoint()) {
                aTeamModel.setSecondHalfHandiCapResult("승리");
                bTeamModel.setSecondHalfHandiCapResult("패배");
            } else if ((aTeamModel.getATeamSecondHalfPoint() + aTeamModel.getHalfHandiCap()) < aTeamModel.getBTeamSecondHalfPoint()) {
                aTeamModel.setSecondHalfHandiCapResult("패배");
                bTeamModel.setSecondHalfHandiCapResult("승리");
            } else {
                aTeamModel.setSecondHalfHandiCapResult("적특");
                bTeamModel.setSecondHalfHandiCapResult("적특");
            }

        }

        double halfPoint = aTeamModel.getPointLine() / 2;
        int halfPointInt = (int) halfPoint;
        double halftPointLine = halfPoint - halfPointInt;

        if ((halftPointLine <= 0.333 && halftPointLine >= 0.001)) {
            aTeamModel.setHalfPointLine(Math.floor(aTeamModel.getPointLine() / 2));
            bTeamModel.setHalfPointLine(Math.floor(aTeamModel.getPointLine() / 2));
        } else if ((halftPointLine <= 0.999 && halftPointLine >= 0.666)) {
            aTeamModel.setHalfPointLine(Math.ceil(aTeamModel.getPointLine() / 2));
            bTeamModel.setHalfPointLine(Math.ceil(aTeamModel.getPointLine() / 2));
        } else if ((halftPointLine <= 0.665 && halftPointLine >= 0.334)) {
            aTeamModel.setHalfPointLine(halfPointInt + 0.5);
            bTeamModel.setHalfPointLine(halfPointInt + 0.5);
        } else {
            aTeamModel.setHalfPointLine((double) Math.round(aTeamModel.getPointLine() / 2));
            bTeamModel.setHalfPointLine((double) Math.round(aTeamModel.getPointLine() / 2));
        }

        if (aTeamModel.getHalfPointLine() == 0) {
            aTeamModel.setFirstHalfPointLineResult("적특");
            aTeamModel.setSecondHalfPointLineResult("적특");

            bTeamModel.setFirstHalfPointLineResult("적특");
            bTeamModel.setSecondHalfPointLineResult("적특");

        } else {
            if ((aTeamModel.getATeamFirstHalfPoint() + aTeamModel.getBTeamFirstHalfPoint()) > aTeamModel.getHalfPointLine()) {
                aTeamModel.setFirstHalfPointLineResult("오버");
                bTeamModel.setFirstHalfPointLineResult("오버");

            } else if ((aTeamModel.getATeamFirstHalfPoint() + aTeamModel.getBTeamFirstHalfPoint()) < aTeamModel.getHalfPointLine()) {
                aTeamModel.setFirstHalfPointLineResult("언더");
                bTeamModel.setFirstHalfPointLineResult("언더");

            } else {
                aTeamModel.setFirstHalfPointLineResult("적특");
                bTeamModel.setFirstHalfPointLineResult("적특");
            }

            if ((aTeamModel.getATeamSecondHalfPoint() + aTeamModel.getBTeamSecondHalfPoint()) > aTeamModel.getHalfPointLine()) {
                aTeamModel.setSecondHalfPointLineResult("오버");
                bTeamModel.setSecondHalfPointLineResult("오버");

            } else if ((aTeamModel.getATeamSecondHalfPoint() + aTeamModel.getBTeamSecondHalfPoint()) < aTeamModel.getHalfPointLine()) {
                aTeamModel.setSecondHalfPointLineResult("언더");
                bTeamModel.setSecondHalfPointLineResult("언더");

            } else {
                aTeamModel.setSecondHalfPointLineResult("적특");
                bTeamModel.setSecondHalfPointLineResult("적특");
            }
        }

    }

    private void parseInningHandiUnover(Element element, BasketballModel aTeamModel, BasketballModel bTeamModel) {

        double handi = aTeamModel.getHandiCap() / 4;
        int handiInt = (int) handi;
        double pointHandi = handi - handiInt;

        if (pointHandi == 0.5 || pointHandi == -0.5) {
            aTeamModel.setFirstQHandiCap(aTeamModel.getHandiCap() / 4);
            bTeamModel.setFirstQHandiCap((aTeamModel.getHandiCap() / 4) * -1);
        } else if (aTeamModel.getHandiCap() >= 0.5 && aTeamModel.getHandiCap() <= 1.5) {
            aTeamModel.setFirstQHandiCap(0.5);
            bTeamModel.setFirstQHandiCap(-0.5);
        } else if (aTeamModel.getHandiCap() <= -0.5 && aTeamModel.getHandiCap() >= -1.5) {
            aTeamModel.setFirstQHandiCap(-0.5);
            bTeamModel.setFirstQHandiCap(0.5);
        } else {
            aTeamModel.setFirstQHandiCap((double) Math.round(aTeamModel.getHandiCap() / 4));
            bTeamModel.setFirstQHandiCap((double) Math.round(aTeamModel.getHandiCap() / 4) * -1);
        }

        if (aTeamModel.getFirstQHandiCap() == 0) {
            aTeamModel.setFirstQHandiCapResult("적특");
            aTeamModel.setSecondQHandiCapResult("적특");
            aTeamModel.setThirdQHandiCapResult("적특");
            aTeamModel.setFourthQHandiCapResult("적특");

            bTeamModel.setFirstQHandiCapResult("적특");
            bTeamModel.setSecondQHandiCapResult("적특");
            bTeamModel.setThirdQHandiCapResult("적특");
            bTeamModel.setFourthQHandiCapResult("적특");

        } else {
            if ((aTeamModel.getATeamFirstQPoint() + aTeamModel.getFirstQHandiCap()) > aTeamModel.getBTeamFirstQPoint()) {
                aTeamModel.setFirstQHandiCapResult("승리");
                bTeamModel.setFirstQHandiCapResult("패배");
            } else if ((aTeamModel.getATeamFirstQPoint() + aTeamModel.getFirstQHandiCap()) < aTeamModel.getBTeamFirstQPoint()) {
                aTeamModel.setFirstQHandiCapResult("패배");
                bTeamModel.setFirstQHandiCapResult("승리");
            } else {
                aTeamModel.setFirstQHandiCapResult("적특");
                bTeamModel.setFirstQHandiCapResult("적특");
            }

            if ((aTeamModel.getATeamSecondQPoint() + aTeamModel.getFirstQHandiCap()) > aTeamModel.getBTeamSecondQPoint()) {
                aTeamModel.setSecondQHandiCapResult("승리");
                bTeamModel.setSecondQHandiCapResult("패배");
            } else if ((aTeamModel.getATeamSecondQPoint() + aTeamModel.getFirstQHandiCap()) < aTeamModel.getBTeamSecondQPoint()) {
                aTeamModel.setSecondQHandiCapResult("패배");
                bTeamModel.setSecondQHandiCapResult("승리");
            } else {
                aTeamModel.setSecondQHandiCapResult("적특");
                bTeamModel.setSecondQHandiCapResult("적특");
            }

            if ((aTeamModel.getATeamThirdQPoint() + aTeamModel.getFirstQHandiCap()) > aTeamModel.getBTeamThirdQPoint()) {
                aTeamModel.setThirdQHandiCapResult("승리");
                bTeamModel.setThirdQHandiCapResult("패배");
            } else if ((aTeamModel.getATeamThirdQPoint() + aTeamModel.getFirstQHandiCap()) < aTeamModel.getBTeamThirdQPoint()) {
                aTeamModel.setThirdQHandiCapResult("패배");
                bTeamModel.setThirdQHandiCapResult("승리");
            } else {
                aTeamModel.setThirdQHandiCapResult("적특");
                bTeamModel.setThirdQHandiCapResult("적특");
            }

            if ((aTeamModel.getATeamFourthQPoint() + aTeamModel.getFirstQHandiCap()) > aTeamModel.getBTeamFourthQPoint()) {
                aTeamModel.setFourthQHandiCapResult("승리");
                bTeamModel.setFourthQHandiCapResult("패배");

            } else if ((aTeamModel.getATeamFourthQPoint() + aTeamModel.getFirstQHandiCap()) < aTeamModel.getBTeamFourthQPoint()) {
                aTeamModel.setFourthQHandiCapResult("패배");
                bTeamModel.setFourthQHandiCapResult("승리");

            } else {
                aTeamModel.setFourthQHandiCapResult("적특");
                bTeamModel.setFourthQHandiCapResult("적특");
            }
        }

        double point = aTeamModel.getPointLine() / 4;
        int pointInt = (int) point;
        double pointLine = point - pointInt;

        if ((pointLine <= 0.333 && pointLine >= 0.001)) {
            aTeamModel.setFirstQPointLine(Math.floor(aTeamModel.getPointLine() / 4));
            bTeamModel.setFirstQPointLine(Math.floor(bTeamModel.getPointLine() / 4));
        } else if ((pointLine <= 0.999 && pointLine >= 0.666)) {
            aTeamModel.setFirstQPointLine(Math.ceil(aTeamModel.getPointLine() / 4));
            bTeamModel.setFirstQPointLine(Math.ceil(bTeamModel.getPointLine() / 4));
        } else if ((pointLine <= 0.665 && pointLine >= 0.334)) {
            aTeamModel.setFirstQPointLine(pointInt + 0.5);
            bTeamModel.setFirstQPointLine(pointInt + 0.5);
        } else {
            aTeamModel.setFirstQPointLine((double) Math.round(aTeamModel.getPointLine() / 4));
            bTeamModel.setFirstQPointLine((double) Math.round(bTeamModel.getPointLine() / 4));
        }


        if (aTeamModel.getFirstQPointLine() == 0) {
            aTeamModel.setFirstQPointLineResult("적특");
            aTeamModel.setSecondQPointLineResult("적특");
            aTeamModel.setThirdQPointLineResult("적특");
            aTeamModel.setFourthQPointLineResult("적특");
            bTeamModel.setFirstQPointLineResult("적특");
            bTeamModel.setSecondQPointLineResult("적특");
            bTeamModel.setThirdQPointLineResult("적특");
            bTeamModel.setFourthQPointLineResult("적특");
        } else {
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

        }
    }

    private void parseSpecial(Element element, BasketballModel aTeamModel, BasketballModel bTeamModel) {

        int i = 0;

        aTeamModel.setFirstQFirstPoint("적특");
        aTeamModel.setFirstQFirstFreeTwo("적특");
        aTeamModel.setFirstQFirstTwoPoint("적특");
        aTeamModel.setFirstQFirstThreePoint("적특");

        aTeamModel.setSecondQFirstPoint("적특");
        aTeamModel.setSecondQFirstFreeTwo("적특");
        aTeamModel.setSecondQFirstTwoPoint("적특");
        aTeamModel.setSecondQFirstThreePoint("적특");

        aTeamModel.setThirdQFirstPoint("적특");
        aTeamModel.setThirdQFirstFreeTwo("적특");
        aTeamModel.setThirdQFirstTwoPoint("적특");
        aTeamModel.setThirdQFirstThreePoint("적특");

        aTeamModel.setFourthQFirstPoint("적특");
        aTeamModel.setFourthQFirstFreeTwo("적특");
        aTeamModel.setFourthQFirstTwoPoint("적특");
        aTeamModel.setFourthQFirstThreePoint("적특");

        bTeamModel.setFirstQFirstPoint("적특");
        bTeamModel.setFirstQFirstFreeTwo("적특");
        bTeamModel.setFirstQFirstTwoPoint("적특");
        bTeamModel.setFirstQFirstThreePoint("적특");

        bTeamModel.setSecondQFirstPoint("적특");
        bTeamModel.setSecondQFirstFreeTwo("적특");
        bTeamModel.setSecondQFirstTwoPoint("적특");
        bTeamModel.setSecondQFirstThreePoint("적특");

        bTeamModel.setThirdQFirstPoint("적특");
        bTeamModel.setThirdQFirstFreeTwo("적특");
        bTeamModel.setThirdQFirstTwoPoint("적특");
        bTeamModel.setThirdQFirstThreePoint("적특");

        bTeamModel.setFourthQFirstPoint("적특");
        bTeamModel.setFourthQFirstFreeTwo("적특");
        bTeamModel.setFourthQFirstTwoPoint("적특");
        bTeamModel.setFourthQFirstThreePoint("적특");

        for (Element specailEle : element.select("tbody tr td.f.ico_linescore > p")) {
            if (i == 0) {
                if (specailEle.select("span.ico_firstpoint").text().equals("첫득점")) {
                    aTeamModel.setFirstQFirstPoint("패배");
                    bTeamModel.setFirstQFirstPoint("승리");
                }
                if (specailEle.select("span.ico_freetwo").text().equals("자유투")) {
                    aTeamModel.setFirstQFirstFreeTwo("패배");
                    bTeamModel.setFirstQFirstFreeTwo("승리");
                }
                if (specailEle.select("span.ico_twopoint").text().equals("2점슛")) {
                    aTeamModel.setFirstQFirstTwoPoint("패배");
                    bTeamModel.setFirstQFirstTwoPoint("승리");

                }
                if (specailEle.select("span.ico_threepoint").text().equals("3점슛")) {
                    aTeamModel.setFirstQFirstThreePoint("패배");
                    bTeamModel.setFirstQFirstThreePoint("승리");
                }
            }
            if (i == 1) {
                if (specailEle.select("span.ico_firstpoint").text().equals("첫득점")) {
                    aTeamModel.setSecondQFirstPoint("패배");
                    bTeamModel.setSecondQFirstPoint("승리");
                }
                if (specailEle.select("span.ico_freetwo").text().equals("자유투")) {
                    aTeamModel.setSecondQFirstFreeTwo("패배");
                    bTeamModel.setSecondQFirstFreeTwo("승리");
                }
                if (specailEle.select("span.ico_twopoint").text().equals("2점슛")) {
                    aTeamModel.setSecondQFirstTwoPoint("패배");
                    bTeamModel.setSecondQFirstTwoPoint("승리");
                }
                if (specailEle.select("span.ico_threepoint").text().equals("3점슛")) {
                    aTeamModel.setSecondQFirstThreePoint("패배");
                    bTeamModel.setSecondQFirstThreePoint("승리");
                }
            }
            if (i == 2) {
                if (specailEle.select("span.ico_firstpoint").text().equals("첫득점")) {
                    aTeamModel.setThirdQFirstPoint("패배");
                    bTeamModel.setThirdQFirstPoint("승리");
                }
                if (specailEle.select("span.ico_freetwo").text().equals("자유투")) {
                    aTeamModel.setThirdQFirstFreeTwo("패배");
                    bTeamModel.setThirdQFirstFreeTwo("승리");
                }
                if (specailEle.select("span.ico_twopoint").text().equals("2점슛")) {
                    aTeamModel.setThirdQFirstTwoPoint("패배");
                    bTeamModel.setThirdQFirstTwoPoint("승리");
                }
                if (specailEle.select("span.ico_threepoint").text().equals("3점슛")) {
                    aTeamModel.setThirdQFirstThreePoint("패배");
                    bTeamModel.setThirdQFirstThreePoint("승리");
                }
            }
            if (i == 3) {
                if (specailEle.select("span.ico_firstpoint").text().equals("첫득점")) {
                    aTeamModel.setFourthQFirstPoint("패배");
                    bTeamModel.setFourthQFirstPoint("승리");
                }
                if (specailEle.select("span.ico_freetwo").text().equals("자유투")) {
                    aTeamModel.setFourthQFirstFreeTwo("패배");
                    bTeamModel.setFourthQFirstFreeTwo("승리");
                }
                if (specailEle.select("span.ico_twopoint").text().equals("2점슛")) {
                    aTeamModel.setFourthQFirstTwoPoint("패배");
                    bTeamModel.setFourthQFirstTwoPoint("승리");
                }
                if (specailEle.select("span.ico_threepoint").text().equals("3점슛")) {
                    aTeamModel.setFourthQFirstThreePoint("패배");
                    bTeamModel.setFourthQFirstThreePoint("승리");
                }
            }

            if (i == 4) {
                if (specailEle.select("span.ico_firstpoint").text().equals("첫득점")) {
                    bTeamModel.setFirstQFirstPoint("패배");
                    aTeamModel.setFirstQFirstPoint("승리");
                }
                if (specailEle.select("span.ico_freetwo").text().equals("자유투")) {
                    bTeamModel.setFirstQFirstFreeTwo("패배");
                    aTeamModel.setFirstQFirstFreeTwo("승리");
                }
                if (specailEle.select("span.ico_twopoint").text().equals("2점슛")) {
                    bTeamModel.setFirstQFirstTwoPoint("패배");
                    aTeamModel.setFirstQFirstTwoPoint("승리");
                }
                if (specailEle.select("span.ico_threepoint").text().equals("3점슛")) {
                    bTeamModel.setFirstQFirstThreePoint("패배");
                    aTeamModel.setFirstQFirstThreePoint("승리");
                }
            }
            if (i == 5) {
                if (specailEle.select("span.ico_firstpoint").text().equals("첫득점")) {
                    bTeamModel.setSecondQFirstPoint("패배");
                    aTeamModel.setSecondQFirstPoint("승리");
                }
                if (specailEle.select("span.ico_freetwo").text().equals("자유투")) {
                    bTeamModel.setSecondQFirstFreeTwo("패배");
                    aTeamModel.setSecondQFirstFreeTwo("승리");
                }
                if (specailEle.select("span.ico_twopoint").text().equals("2점슛")) {
                    bTeamModel.setSecondQFirstTwoPoint("패배");
                    aTeamModel.setSecondQFirstTwoPoint("승리");
                }
                if (specailEle.select("span.ico_threepoint").text().equals("3점슛")) {
                    bTeamModel.setSecondQFirstThreePoint("패배");
                    aTeamModel.setSecondQFirstThreePoint("승리");
                }
            }
            if (i == 6 ) {
                if (specailEle.select("span.ico_firstpoint").text().equals("첫득점")) {
                    bTeamModel.setThirdQFirstPoint("패배");
                    aTeamModel.setThirdQFirstPoint("승리");
                }
                if (specailEle.select("span.ico_freetwo").text().equals("자유투")) {
                    bTeamModel.setThirdQFirstFreeTwo("패배");
                    aTeamModel.setThirdQFirstFreeTwo("승리");
                }
                if (specailEle.select("span.ico_twopoint").text().equals("2점슛")) {
                    bTeamModel.setThirdQFirstTwoPoint("패배");
                    aTeamModel.setThirdQFirstTwoPoint("승리");
                }
                if (specailEle.select("span.ico_threepoint").text().equals("3점슛")) {
                    bTeamModel.setThirdQFirstThreePoint("패배");
                    aTeamModel.setThirdQFirstThreePoint("승리");
                }
            }
            if (i == 7) {
                if (specailEle.select("span.ico_firstpoint").text().equals("첫득점")) {
                    bTeamModel.setFourthQFirstPoint("패배");
                    aTeamModel.setFourthQFirstPoint("승리");
                }
                if (specailEle.select("span.ico_freetwo").text().equals("자유투")) {
                    bTeamModel.setFourthQFirstFreeTwo("패배");
                    aTeamModel.setFourthQFirstFreeTwo("승리");
                }
                if (specailEle.select("span.ico_twopoint").text().equals("2점슛")) {
                    bTeamModel.setFourthQFirstTwoPoint("패배");
                    aTeamModel.setFourthQFirstTwoPoint("승리");
                }
                if (specailEle.select("span.ico_threepoint").text().equals("3점슛")) {
                    bTeamModel.setFourthQFirstThreePoint("패배");
                    aTeamModel.setFourthQFirstThreePoint("승리");
                }
            }
            i++;
        }

    }

    private boolean checkTeam(String team) {

        String[] teamArr = {
                "울산 현대모비스",
                "인천 전자랜드",
                "전주 KCC",
                "서울 SK",
                "창원 LG",
                "서울 삼성",
                "고양 오리온",
                "안양 KGC",
                "부산 KT",
                "원주 DB",
                "KEB하나",
                "하나은행",
                "BNK 썸",
                "KB스타즈",
                "신한은행",
                "삼성생명",
                "우리은행",
                "토론토 랩터스",
                "뉴올리언즈 펠리컨스",
                "LA 클리퍼스",
                "LA 레이커스",
                "올랜도 매직",
                "클리블랜드 캐벌리어스",
                "샬럿 호네츠",
                "시카고 불스",
                "인디애나 페이서스",
                "디트로이트 피스톤즈",
                "마이애미 히트",
                "멤피스 그리즐리스",
                "필라델피아 세븐티식서스",
                "보스턴 셀틱스",
                "브루클린 네츠",
                "미네소타 팀버울브스",
                "샌안토니오 스퍼스",
                "뉴욕 닉스",
                "댈러스 매버릭스",
                "워싱턴 위저즈",
                "유타 재즈",
                "오클라호마시티 썬더",
                "피닉스 선즈",
                "새크라멘토 킹스",
                "포틀랜드 트레일블레이저스",
                "덴버 너기츠",
                "애틀랜타 호크스",
                "휴스턴 로키츠",
                "밀워키 벅스",
                "골든스테이트 워리어스"
        };

        for (int i = 0; i < teamArr.length; i++) {
            if (team.equals(teamArr[i])) {
                return true;
            }
        }
        return false;
    }
}

