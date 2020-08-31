package com.batch.job.task.processor;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;


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

import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
@StepScope
public class NamedBasketballAllMatchProcessor implements ItemProcessor<String, List<BasketballModel>> {

    private String initSeasonDate;
    private String finishSeasonDate = "2020-12-14";

    @Autowired
    private NamedUtil namedUtil;

    @Value("#{jobParameters[mode]}")
    private String mode;

    @Override
    public List<BasketballModel> process(String s) throws Exception {
        return allBasketballMatch();
    }


    public List<BasketballModel> allBasketballMatch() throws IOException, ParseException, JSONException {

        List<BasketballModel> basketballModelList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();

        String rootHtml = "";
        String url = "https://livescore.co.kr/sports/score_board/basket/view.php?date=";
        int addDate = 0;

        Calendar curDate = Calendar.getInstance();
        curDate.setTime(new Date());
        curDate.add(Calendar.DATE, 4);

        while (true) {

            Calendar startDate = Calendar.getInstance();
            startDate.setTime(new Date());
            if(mode.equals("all")) {
                startDate.set(2020, 6, 01);
            }else {
                startDate.add(Calendar.DATE, -2);

            }
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            startDate.add(Calendar.DATE, addDate);

            //nba 10-22 ~ 2020.10.16
            //wkbl  2019년 10월 19일 (토) ~ 2020년 3월 19일 (목)
            //kbl 2019년 10월 5일 (토) ~ 2020년 3월 31일 (화)
            if(df.format(startDate.getTime()).equals(df.format(curDate.getTime()))){
                log.info("설정한 시즌 마김 기한까지 파싱 완료 : " + df.format(startDate.getTime()));
                break;
            }

            int dayNum = startDate.get(Calendar.DAY_OF_WEEK);
            String dayOfWeek = namedUtil.getDayoOfWeek(dayNum);

            rootHtml = namedUtil.liveScoreUrlToString(url + df.format(startDate.getTime()));

            Document rootDoc = Jsoup.parse(rootHtml);
            Elements elements = rootDoc.select("div#score_board div.score_tbl_individual");

            for (Element element : rootDoc.select("div#score_board div.score_tbl_individual")) {
                BasketballModel aTeamModel = new BasketballModel();
                BasketballModel bTeamModel = new BasketballModel();

                int i = 0;

                String league = element.select("thead tr th.reague").text();

                if(!league.contains("KBL") && !league.contains("NBA") && !league.contains("WKBL")){
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

