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

import static java.lang.Math.round;

@Component
@Slf4j
public class NamedBaseballUpdateMatchProcessor implements ItemProcessor<String, List<BaseballModel>> {

    private String initSeasonDate;
    private String finishSeasonDate = "2020-12-14";
    private String baseBall_Url = "https://api.picksmatch.com/v1.0/sports/baseball/games?date=";

    @Autowired
    private NamedUtil namedUtil;

    @Override
    public  List<BaseballModel> process(String s) throws Exception {

        return updateBaseballMatch();
    }

    public List<BaseballModel> updateBaseballMatch() throws Exception {

        int addDate = 0;

        List<BaseballModel> baseballModelList = new ArrayList<>();

        while (true){
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            cal.set(2020, 4, 01);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            cal.add(Calendar.DATE, addDate);
            String matchDate = df.format(cal.getTime());
            if(df.format(cal.getTime()).equals("2020-07-14")){
                log.info("설정한 시즌 마김 기한까지 파싱 완료 : " + "2020-07-14");
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
                    BaseballModel aTeamModel = new BaseballModel();
                    BaseballModel bTeamModel = new BaseballModel();

                    JSONObject matchObject = jsonArray.getJSONObject(i);

                    aTeamModel.setGameId(String.valueOf(matchObject.getInt("id")));
                    bTeamModel.setGameId(String.valueOf(matchObject.getInt("id")));
                    aTeamModel.setLeague(matchObject.getJSONObject("league").getString("shortName"));
                    bTeamModel.setLeague(matchObject.getJSONObject("league").getString("shortName"));

                    if(!aTeamModel.getLeague().equals("KBO") && !aTeamModel.getLeague().equals("NPB") && !aTeamModel.getLeague().equals("MLB") && !aTeamModel.getLeague().equals("퓨처스") ){
                        continue;
                    }
                    //기본 데이터 파싱
                    parseBaseData(matchObject,aTeamModel,bTeamModel);

                    //우취, 콜드일 경우 점수 99로 설정
                    String gameStatus = matchObject.getJSONObject("broadcast").getString("playText");
                    if (gameStatus.contains("취소") || gameStatus.contains("콜드") || gameStatus.contains("우천")) {
                        aTeamModel.setATeamTotalPoint(99);
                        aTeamModel.setBTeamTotalPoint(99);

                        bTeamModel.setATeamTotalPoint(99);
                        bTeamModel.setBTeamTotalPoint(99);

                        baseballModelList.add(aTeamModel);
                        baseballModelList.add(bTeamModel);

                        continue;
                    }

                    //선발투수, 정역배, 핸디, 언오버, 3,4,5이닝 기준점 설정
                    parsePitcherAndOdd(matchObject,aTeamModel,bTeamModel);

                    //이닝 스코어 설정
                    parseScore(matchObject,aTeamModel,bTeamModel);

                    //이닝별 핸디 언오버 결과 설정
                    parseInningHandiUnover(matchObject,aTeamModel,bTeamModel);

                    //스폐셜 설정
                    parseSpecial(matchObject,aTeamModel,bTeamModel);


                    if(validationData(aTeamModel)){
                        continue;
                    }

                    baseballModelList.add(aTeamModel);
                    baseballModelList.add(bTeamModel);

                }
            } catch (Exception e) {
                log.error(String.valueOf(e));
            }
            addDate++;

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

    private void parsePitcherAndOdd( JSONObject matchObject , BaseballModel aTeamModel , BaseballModel bTeamModel) throws JSONException {

        JSONObject homeTeam = matchObject.getJSONObject("teams").getJSONObject("home");
        JSONObject awayTeam = matchObject.getJSONObject("teams").getJSONObject("away");

        boolean checkHomeNull = homeTeam.isNull("startPitcher");
        boolean checkAwayNull = awayTeam.isNull("startPitcher");

        if (!checkHomeNull) {
            aTeamModel.setATeamPitcher(homeTeam.getJSONObject("startPitcher").getString("name"));
            bTeamModel.setBTeamPitcher(homeTeam.getJSONObject("startPitcher").getString("name"));
        } else {
            aTeamModel.setATeamPitcher("");
            bTeamModel.setBTeamPitcher("");
        }

        if (!checkAwayNull) {
            aTeamModel.setBTeamPitcher(awayTeam.getJSONObject("startPitcher").getString("name"));
            bTeamModel.setATeamPitcher(awayTeam.getJSONObject("startPitcher").getString("name"));
        } else {
            aTeamModel.setBTeamPitcher("");
            bTeamModel.setATeamPitcher("");
        }
        double handi = 0.0;
        double unOver = 0.0;
        double firstInningPointLine = 0.0;

        JSONArray internationalHandicapOdds = matchObject.getJSONObject("odds").getJSONArray("internationalHandicapOdds");
        JSONArray internationalUnderOverOdds = matchObject.getJSONObject("odds").getJSONArray("internationalUnderOverOdds");
        JSONArray domesticHandicapOdds = matchObject.getJSONObject("odds").getJSONArray("domesticHandicapOdds");
        JSONArray domesticUnderOverOdds = matchObject.getJSONObject("odds").getJSONArray("domesticUnderOverOdds");

        if(internationalHandicapOdds.length() > 0 ){
            handi = internationalHandicapOdds.getJSONObject(0).getDouble("optionValue");
        }else if (domesticHandicapOdds.length() > 0) {
            handi = domesticHandicapOdds.getJSONObject(0).getDouble("optionValue");
        }

        if(internationalUnderOverOdds.length() > 0 ){
            unOver = internationalUnderOverOdds.getJSONObject(0).getDouble("optionValue");
            firstInningPointLine = unOver / 9;
        }else if (domesticUnderOverOdds.length() > 0  ){
            unOver = domesticUnderOverOdds.getJSONObject(0).getDouble("optionValue");
            firstInningPointLine = unOver / 9;
        }

        aTeamModel.setHandiCap(handi);
        bTeamModel.setHandiCap(handi * -1);

        aTeamModel.setPointLine(unOver);
        bTeamModel.setPointLine(unOver);

        if (aTeamModel.getHandiCap() > 0) {
            aTeamModel.setOdd("역배");
            bTeamModel.setOdd("정배");

            aTeamModel.setThirdHandiCap(0.5);
            bTeamModel.setThirdHandiCap(-0.5);

            aTeamModel.setFourthHandiCap(0.5);
            bTeamModel.setFourthHandiCap(-0.5);

            aTeamModel.setFifthHandiCap(0.5);
            bTeamModel.setFifthHandiCap(-0.5);

        } else if (aTeamModel.getHandiCap() < 0) {
            aTeamModel.setOdd("정배");
            bTeamModel.setOdd("역배");

            aTeamModel.setThirdHandiCap(-0.5);
            bTeamModel.setThirdHandiCap(0.5);

            aTeamModel.setFourthHandiCap(-0.5);
            bTeamModel.setFourthHandiCap(0.5);

            aTeamModel.setFifthHandiCap(-0.5);
            bTeamModel.setFifthHandiCap(0.5);
        } else {
            aTeamModel.setOdd("없음");
            bTeamModel.setOdd("없음");

            aTeamModel.setThirdHandiCap(0.0);
            bTeamModel.setThirdHandiCap(0.0);

            aTeamModel.setFourthHandiCap(0.0);
            bTeamModel.setFourthHandiCap(0.0);

            aTeamModel.setFifthHandiCap(0.0);
            bTeamModel.setFifthHandiCap(0.0);
        }

        if(unOver == 0){

        }
        double thirdPointLine = firstInningPointLine * 4;
        int thirdPointLineInt = (int) thirdPointLine;
        double pointLine = thirdPointLine - thirdPointLineInt;

        if ((pointLine <= 0.333 && pointLine >= 0.001)) {
            aTeamModel.setThirdPointLine((double) thirdPointLineInt - 0.5);
            bTeamModel.setThirdPointLine((double) thirdPointLineInt - 0.5);

        } else if ((pointLine <= 0.999 && pointLine >= 0.666)) {
            aTeamModel.setThirdPointLine((double) (thirdPointLineInt + 1) - 0.5);
            bTeamModel.setThirdPointLine((double) (thirdPointLineInt + 1) - 0.5);

        } else if ((pointLine <= 0.665 && pointLine >= 0.334)) {
            aTeamModel.setThirdPointLine((double) thirdPointLineInt);
            bTeamModel.setThirdPointLine((double) thirdPointLineInt);

        } else {
            aTeamModel.setThirdPointLine((double) round(thirdPointLineInt));
            bTeamModel.setThirdPointLine((double) round(thirdPointLineInt));

        }

        double forthPointLine = firstInningPointLine * 4;
        int forthPointLineInt = (int) forthPointLine;
        pointLine = forthPointLine - forthPointLineInt;

        if ((pointLine <= 0.333 && pointLine >= 0.001)) {
            aTeamModel.setFourthPointLine((double) forthPointLineInt);
            bTeamModel.setFourthPointLine((double) forthPointLineInt);

        } else if ((pointLine <= 0.999 && pointLine >= 0.666)) {
            aTeamModel.setFourthPointLine((double) (forthPointLineInt + 1));
            bTeamModel.setFourthPointLine((double) (forthPointLineInt + 1));

        } else if ((pointLine <= 0.665 && pointLine >= 0.334)) {
            aTeamModel.setFourthPointLine(forthPointLineInt + 0.5);
            bTeamModel.setFourthPointLine(forthPointLineInt + 0.5);

        } else {
            aTeamModel.setFourthPointLine((double) round(forthPointLine));
            bTeamModel.setFourthPointLine((double) round(forthPointLine));

        }

        double fifthPointLine = firstInningPointLine * 5;
        int fifthPointLineInt = (int) fifthPointLine;
        pointLine = fifthPointLine - fifthPointLineInt;

        if ((pointLine <= 0.333 && pointLine >= 0.001)) {
            aTeamModel.setFifthPointLine((double) fifthPointLineInt);
            bTeamModel.setFifthPointLine((double) fifthPointLineInt);

        } else if ((pointLine <= 0.999 && pointLine >= 0.666)) {
            aTeamModel.setFifthPointLine((double) (fifthPointLineInt + 1));
            bTeamModel.setFifthPointLine((double) (fifthPointLineInt + 1));

        } else if ((pointLine <= 0.665 && pointLine >= 0.334)) {
            aTeamModel.setFifthPointLine(fifthPointLineInt + 0.5);
            bTeamModel.setFifthPointLine(fifthPointLineInt + 0.5);

        } else {
            aTeamModel.setFifthPointLine((double) round(fifthPointLine));
            bTeamModel.setFifthPointLine((double) round(fifthPointLine));

        }



    }

    private void parseScore( JSONObject matchObject , BaseballModel aTeamModel , BaseballModel bTeamModel) throws JSONException{
        JSONArray homeTeamScore = matchObject.getJSONObject("teams").getJSONObject("home").getJSONArray("periodData");
        JSONArray awayTeamScore = matchObject.getJSONObject("teams").getJSONObject("away").getJSONArray("periodData");

        aTeamModel.setFirstScore(homeTeamScore.getJSONObject(0).getInt("score"));
        aTeamModel.setSecondScore(homeTeamScore.getJSONObject(1).getInt("score"));
        aTeamModel.setThirdScore(homeTeamScore.getJSONObject(2).getInt("score"));
        aTeamModel.setFourthScore(homeTeamScore.getJSONObject(3).getInt("score"));
        aTeamModel.setFifthScore(homeTeamScore.getJSONObject(4).getInt("score"));
        aTeamModel.setSixthScore(homeTeamScore.getJSONObject(5).getInt("score"));
        aTeamModel.setSeventhScore(homeTeamScore.getJSONObject(6).getInt("score"));
        aTeamModel.setEighthScore(homeTeamScore.getJSONObject(7).getInt("score"));
        if (homeTeamScore.length() > 8) {
            aTeamModel.setNinthScore(homeTeamScore.getJSONObject(8).getInt("score"));
        } else {
            aTeamModel.setNinthScore(0);
        }

        bTeamModel.setFirstScore(awayTeamScore.getJSONObject(0).getInt("score"));
        bTeamModel.setSecondScore(awayTeamScore.getJSONObject(1).getInt("score"));
        bTeamModel.setThirdScore(awayTeamScore.getJSONObject(2).getInt("score"));
        bTeamModel.setFourthScore(awayTeamScore.getJSONObject(3).getInt("score"));
        bTeamModel.setFifthScore(awayTeamScore.getJSONObject(4).getInt("score"));
        bTeamModel.setSixthScore(awayTeamScore.getJSONObject(5).getInt("score"));
        bTeamModel.setSeventhScore(awayTeamScore.getJSONObject(6).getInt("score"));
        bTeamModel.setEighthScore(awayTeamScore.getJSONObject(7).getInt("score"));
        if (awayTeamScore.length() > 8) {
            bTeamModel.setNinthScore(awayTeamScore.getJSONObject(8).getInt("score"));
        } else {
            bTeamModel.setNinthScore(0);
        }

        int extendScore = 0;
        if(homeTeamScore.length() > 9){
            for (int id = 9 ; id < homeTeamScore.length() ; id ++){
                extendScore = extendScore + homeTeamScore.getJSONObject(id).getInt("score");
            }
            aTeamModel.setExtendScore(extendScore);
            aTeamModel.setExtendYn(true);
        }else {
            aTeamModel.setExtendScore(extendScore);
            aTeamModel.setExtendYn(false);

        }

        extendScore = 0;
        if(awayTeamScore.length() > 9){
            for (int id = 9 ; id < awayTeamScore.length() ; id ++){
                extendScore = extendScore + awayTeamScore.getJSONObject(id).getInt("score");
            }
            bTeamModel.setExtendScore(extendScore);
            bTeamModel.setExtendYn(true);

        }else {
            bTeamModel.setExtendScore(extendScore);
            bTeamModel.setExtendYn(false);
        }

        aTeamModel.setATeamTotalPoint(aTeamModel.getTotalScore());
        aTeamModel.setBTeamTotalPoint(bTeamModel.getTotalScore());

        bTeamModel.setATeamTotalPoint(bTeamModel.getTotalScore());
        bTeamModel.setBTeamTotalPoint(aTeamModel.getTotalScore());

        aTeamModel.setATeamThirdPoint(aTeamModel.get3InningScore());
        aTeamModel.setBTeamThirdPoint(bTeamModel.get3InningScore());

        bTeamModel.setATeamThirdPoint(bTeamModel.get3InningScore());
        bTeamModel.setBTeamThirdPoint(aTeamModel.get3InningScore());

        aTeamModel.setATeamFourthPoint(aTeamModel.get4InningScore());
        aTeamModel.setBTeamFourthPoint(bTeamModel.get4InningScore());

        bTeamModel.setATeamFourthPoint(bTeamModel.get4InningScore());
        bTeamModel.setBTeamFourthPoint(aTeamModel.get4InningScore());

        aTeamModel.setATeamFifthPoint(aTeamModel.get5InningScore());
        aTeamModel.setBTeamFifthPoint(bTeamModel.get5InningScore());

        bTeamModel.setATeamFifthPoint(bTeamModel.get5InningScore());
        bTeamModel.setBTeamFifthPoint(aTeamModel.get5InningScore());

    }

    private void parseInningHandiUnover( JSONObject matchObject , BaseballModel aTeamModel , BaseballModel bTeamModel) throws Exception{
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

        if (aTeamModel.getThirdHandiCap() == 0) {
            aTeamModel.setThirdHandiCapResult("적특");
            bTeamModel.setThirdHandiCapResult("적특");

        } else {
            if ((aTeamModel.getATeamThirdPoint() + aTeamModel.getThirdHandiCap()) > aTeamModel.getBTeamThirdPoint()) {
                aTeamModel.setThirdHandiCapResult("승리");
                bTeamModel.setThirdHandiCapResult("패배");

            } else if ((aTeamModel.getATeamThirdPoint() + aTeamModel.getThirdHandiCap()) < aTeamModel.getBTeamThirdPoint()) {
                aTeamModel.setThirdHandiCapResult("패배");
                bTeamModel.setThirdHandiCapResult("승리");

            } else {
                aTeamModel.setThirdHandiCapResult("적특");
                bTeamModel.setThirdHandiCapResult("적특");
            }
        }

        if (aTeamModel.getFourthHandiCap() == 0) {
            aTeamModel.setFourthHandiCapResult("적특");
            bTeamModel.setFourthHandiCapResult("적특");

        } else {
            if ((aTeamModel.getATeamFourthPoint() + aTeamModel.getFourthHandiCap()) > aTeamModel.getBTeamFourthPoint()) {
                aTeamModel.setFourthHandiCapResult("승리");
                bTeamModel.setFourthHandiCapResult("패배");

            } else if ((aTeamModel.getATeamFourthPoint() + aTeamModel.getFourthHandiCap()) < aTeamModel.getBTeamFourthPoint()) {
                aTeamModel.setFourthHandiCapResult("패배");
                bTeamModel.setFourthHandiCapResult("승리");

            } else {
                aTeamModel.setFourthHandiCapResult("적특");
                bTeamModel.setFourthHandiCapResult("적특");
            }
        }

        if (aTeamModel.getFifthHandiCap() == 0) {
            aTeamModel.setFifthHandiCapResult("적특");
            bTeamModel.setFifthHandiCapResult("적특");

        } else {
            if ((aTeamModel.getATeamFifthPoint() + aTeamModel.getFifthHandiCap()) > aTeamModel.getBTeamFifthPoint()) {
                aTeamModel.setFifthHandiCapResult("승리");
                bTeamModel.setFifthHandiCapResult("패배");

            } else if ((aTeamModel.getATeamFifthPoint() + aTeamModel.getFifthHandiCap()) < aTeamModel.getBTeamFifthPoint()) {
                aTeamModel.setFifthHandiCapResult("패배");
                bTeamModel.setFifthHandiCapResult("승리");

            } else {
                aTeamModel.setFifthHandiCapResult("적특");
                bTeamModel.setFifthHandiCapResult("적특");
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

        if (aTeamModel.getThirdPointLine() == 0) {
            aTeamModel.setThirdPointLineResult("적특");
            bTeamModel.setThirdPointLineResult("적특");

        } else {
            if ((aTeamModel.getATeamThirdPoint() + aTeamModel.getBTeamThirdPoint()) > aTeamModel.getThirdPointLine()) {
                aTeamModel.setThirdPointLineResult("오버");
                bTeamModel.setThirdPointLineResult("오버");

            } else if ((aTeamModel.getATeamThirdPoint() + aTeamModel.getBTeamThirdPoint()) < aTeamModel.getThirdPointLine()) {
                aTeamModel.setThirdPointLineResult("언더");
                bTeamModel.setThirdPointLineResult("언더");

            } else {
                aTeamModel.setThirdPointLineResult("적특");
                bTeamModel.setThirdPointLineResult("적특");
            }
        }


        if (aTeamModel.getFourthPointLine() == 0) {
            aTeamModel.setFourthPointLineResult("적특");
            bTeamModel.setFourthPointLineResult("적특");

        } else {
            if ((aTeamModel.getATeamFourthPoint() + aTeamModel.getBTeamFourthPoint()) > aTeamModel.getFourthPointLine()) {
                aTeamModel.setFourthPointLineResult("오버");
                bTeamModel.setFourthPointLineResult("오버");

            } else if ((aTeamModel.getATeamFourthPoint() + aTeamModel.getBTeamFourthPoint()) < aTeamModel.getFourthPointLine()) {
                aTeamModel.setFourthPointLineResult("언더");
                bTeamModel.setFourthPointLineResult("언더");

            } else {
                aTeamModel.setFourthPointLineResult("적특");
                bTeamModel.setFourthPointLineResult("적특");
            }
        }

        if (aTeamModel.getFifthPointLine() == 0) {
            aTeamModel.setFifthPointLineResult("적특");
            bTeamModel.setFifthPointLineResult("적특");

        } else {
            if ((aTeamModel.getATeamFifthPoint() + aTeamModel.getBTeamFifthPoint()) > aTeamModel.getFifthPointLine()) {
                aTeamModel.setFifthPointLineResult("오버");
                bTeamModel.setFifthPointLineResult("오버");

            } else if ((aTeamModel.getATeamFifthPoint() + aTeamModel.getBTeamFifthPoint()) < aTeamModel.getFifthPointLine()) {
                aTeamModel.setFifthPointLineResult("언더");
                bTeamModel.setFifthPointLineResult("언더");

            } else {
                aTeamModel.setFifthPointLineResult("적특");
                bTeamModel.setFifthPointLineResult("적특");
            }
        }

    }

    private void parseSpecial( JSONObject matchObject , BaseballModel aTeamModel , BaseballModel bTeamModel) throws JSONException {
        boolean checkFirstBaseOnBall = matchObject.getJSONObject("special").isNull("firstBaseOnBall");
        boolean checkFirstHomerun = matchObject.getJSONObject("special").isNull("firstHomerun");
        boolean checkFirstStrikeOut = matchObject.getJSONObject("special").isNull("firstStrikeOut");

        if(!checkFirstBaseOnBall){
            JSONObject firstBaseOnBall = matchObject.getJSONObject("special").getJSONObject("firstBaseOnBall");

            if(firstBaseOnBall.getString("location").equals("HOME")){
                aTeamModel.setFirstBaseOnBall("승리");
                bTeamModel.setFirstBaseOnBall("패배");
            }else {
                aTeamModel.setFirstBaseOnBall("패배");
                bTeamModel.setFirstBaseOnBall("승리");
            }
        }else {
            aTeamModel.setFirstBaseOnBall("적특");
            bTeamModel.setFirstBaseOnBall("적특");
        }

        if(!checkFirstHomerun){
            JSONObject firstHomerun = matchObject.getJSONObject("special").getJSONObject("firstHomerun");

            if(firstHomerun.getString("location").equals("HOME")){
                aTeamModel.setFirstHomerun("승리");
                bTeamModel.setFirstHomerun("패배");
            }else {
                aTeamModel.setFirstHomerun("패배");
                bTeamModel.setFirstHomerun("승리");
            }
        }else {
            aTeamModel.setFirstHomerun("적특");
            bTeamModel.setFirstHomerun("적특");
        }

        if(!checkFirstStrikeOut){
            JSONObject firstStrikeOut = matchObject.getJSONObject("special").getJSONObject("firstStrikeOut");

            if(firstStrikeOut.getString("location").equals("HOME")){
                aTeamModel.setFirstStrikeOut("승리");
                bTeamModel.setFirstStrikeOut("패배");
            }else {
                aTeamModel.setFirstStrikeOut("패배");
                bTeamModel.setFirstStrikeOut("승리");
            }
        }else {
            aTeamModel.setFirstStrikeOut("적특");
            bTeamModel.setFirstStrikeOut("적특");
        }

    }

    private boolean validationData(BaseballModel item){

        if(item.getATeam().isEmpty()){
            return Boolean.TRUE;
        }else {
            return Boolean.FALSE;
        }

    }

}
