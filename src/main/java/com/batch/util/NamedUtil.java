package com.batch.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

@Component
@Slf4j
public class NamedUtil {

    private final static String REQUEST_METHOD = "GET";
    private final static String BASEURL= "https://sports-api.picksmatch.com";

    private final static String BASEBALL_PATH = "/named/v1/sports/baseball/games/?";
    private final static String BASKETBALL_PATH = "/named/v1/sports/basketball/games/?";

    private final static String BASEBALL_PITCHER_PATH = "/named/v1/sports/games/";
    private final static String BASE_PITCHER_PARAM= "?broadcast=true&odds=true&scores=true&specials=true&lineups=true&seasonTeamLeagueRankingStat=true&broadcastDesc=true&v=";


    private final static String BASEPARAM= "broadcast=true&broadcastLatest=true&odds=true&scores=true&specials=true&seasonTeamStat=true&startDate=";

    private final static String API_KEY = "1rar2zCZvKjp";
    private final static String API_NAME = "named_score";


    //구 야구 api
    //URL 없지만 api 되는 중
    public String getOldApiResponse(String matchDate, String gameId) throws IOException{
        //matchDate yyyyMMdd


        StringBuilder responseText = new StringBuilder(1024);
        URLConnection con;
        con = new URL(BASEURL + BASEBALL_PATH + BASEPARAM + matchDate + "&endDate=" + matchDate).openConnection();

        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        con.setRequestProperty("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        con.setRequestProperty("origin","https://sports.picksmatch.com");
        con.setRequestProperty("oki-api-key", API_KEY);
        con.setRequestProperty("oki-api-name", API_NAME);


        con.setUseCaches(false);

        con.setReadTimeout(1000 * 30);
        con.setConnectTimeout(1000 * 30);

        con.connect();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null){
            responseText.append(inputLine);
        }

        in.close();

        return responseText.toString();
    }
    //named 전체 경기 리스트 가지고오는 api
    //http://named.com/main/sub/#/scorecenter/inplay
    public String postOldApiResponse(String matchDate, String gameId) throws IOException {
        //matchDate yyyy-M-d

        StringBuilder responseText = new StringBuilder(1024);
        URLConnection con;
        con = new URL("https://sports.picksmatch.com/gateway/livegames/live_schedule_gateway.php").openConnection();

        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        con.setRequestProperty("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        con.setRequestProperty("origin","https://sports.picksmatch.com");

        String param = "url=/gateway/livegames/live_schedule_gateway.php&date=" + matchDate + "&football_mode=7m";


        con.setDoOutput(true);
        con.setDoInput(true);

        OutputStream opstrm = con.getOutputStream();
        opstrm.write(param.getBytes());
        opstrm.flush();
        opstrm.close();

        con.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null){
            responseText.append(inputLine);
        }

        in.close();

        return responseText.toString();
    }

    //신규 야구 api
    //http://named.com/main/sub/#/scorecenter/baseball/home
    public String getNewApiResponse(String url, String gameId) throws IOException {
        //matchDate yyyy-MM-dd
        StringBuilder responseText = new StringBuilder(1024);

        URLConnection con;
//        con = new URL("https://api.picksmatch.com/v1.0/sports/baseball/games?date=" + matchDate + "&status=ALL").openConnection();
        con = new URL(url).openConnection();

        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        con.setRequestProperty("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        con.setRequestProperty("origin","https://sports.named.com");

        con.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null){
            responseText.append(inputLine);
        }

        in.close();

        return responseText.toString();

    }

    //야구 전광판 api
    //http://named.com/main/sub/#/scorecenter/baseball/broadcast/10559593
    public String getPitcherApiResponse(String url, String gameId) throws IOException{
        //matchDate yyyyMMdd

        StringBuilder responseText = new StringBuilder(1024);
        URLConnection con;
        con = new URL(url).openConnection();

        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        con.setRequestProperty("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        con.setRequestProperty("origin","https://score.named.com");
        con.setRequestProperty("referer","https://score.named.com/baseball/10559642");
        con.setRequestProperty("oki-api-key", API_KEY);
        con.setRequestProperty("oki-api-name", API_NAME);

        con.setUseCaches(false);

        con.setReadTimeout(1000 * 5);
        con.setConnectTimeout(1000 * 5);

        con.connect();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null){
            responseText.append(inputLine);
        }

        in.close();

        return responseText.toString();
    }

    public String liveScoreUrlToString(String url) throws IOException {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setDoOutput(true);
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36");
        con.setRequestProperty("referer", "https://livescore.co.kr/");
        con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        con.setRequestProperty("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        con.setRequestProperty("Content-Type", "text/html;charset=UTF-8");

        con.setConnectTimeout(2000);

        StringBuffer sInputData = new StringBuffer(1024);
        String sInputLine = "";
        BufferedReader in;

        in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
        int BUFFER_SIZE = 1024;
        char[] buffer = new char[BUFFER_SIZE];
        int charsRead = 0;

        while ((sInputLine = in.readLine()) != null) {
            sInputData.append(sInputLine).append("\n");
        }
//
//        while( ( charsRead = in.read(buffer,0,BUFFER_SIZE)) != -1){
//            sInputData.append(buffer,0,charsRead).append("\n");
//                        sInputData.append(charsRead).append("\n");
//
//        }

        in.close();

        return sInputData.toString();

    }
    public String getDayoOfWeek(int dayNum) {
        String dayOfWeek = "";

        switch (dayNum) {
            case 1:
                dayOfWeek = "일요일";
                break;
            case 2:
                dayOfWeek = "월요일";
                break;
            case 3:
                dayOfWeek = "화요일";
                break;
            case 4:
                dayOfWeek = "수요일";
                break;
            case 5:
                dayOfWeek = "목요일";
                break;
            case 6:
                dayOfWeek = "금요일";
                break;
            case 7:
                dayOfWeek = "토요일";
                break;
        }
        return dayOfWeek;

    }

        public static void main(String[] args) {
        NamedUtil namedUtil = new NamedUtil();
        try {
//            String result = namedUtil.getOldApiResponse("20200701","");
            String result = namedUtil.getPitcherApiResponse("2020-07-01","10564548");

            log.info(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
