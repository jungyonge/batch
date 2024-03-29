package com.batch.job.task.tasklet;

import com.batch.mapper.*;
import com.batch.util.EmailUtil;
import com.batch.util.MakeExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class MakeExcelTasklet implements Tasklet {

    private final BaseballMapper baseballMapper;
    private final BaseballPitcherMapper baseballPitcherMapper;
    private final BasketballMapper basketballMapper;
    private final VolleyballMapper volleyballMapper;
    private final HockeyMapper hockeyMapper;
    private final CommonMapper commonMapper;
    private final MakeExcelUtil makeExcelUtil;
    private final EmailUtil emailUtil;

    public MakeExcelTasklet(BaseballMapper baseballMapper, BaseballPitcherMapper baseballPitcherMapper, BasketballMapper basketballMapper, VolleyballMapper volleyballMapper, HockeyMapper hockeyMapper, CommonMapper commonMapper, MakeExcelUtil makeExcelUtil, EmailUtil emailUtil) {
        this.baseballMapper = baseballMapper;
        this.baseballPitcherMapper = baseballPitcherMapper;
        this.basketballMapper = basketballMapper;
        this.volleyballMapper = volleyballMapper;
        this.hockeyMapper = hockeyMapper;
        this.commonMapper = commonMapper;
        this.makeExcelUtil = makeExcelUtil;
        this.emailUtil = emailUtil;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        List sportMakeExcelList = new ArrayList<>();
        List sportMakeExcelListWithoutPitcher = new ArrayList<>();
        List sportDataList = new ArrayList<>();
        List memberList = new ArrayList<>();
        HashMap pitcherMap = new HashMap();
        pitcherMap.put("SPORT", "baseball_pitcher");

        String sport;

        sportMakeExcelList = commonMapper.selectSportMakeExcelList();
        sportMakeExcelListWithoutPitcher = commonMapper.selectSportMakeExcelListWithoutPitcher();
        for (int i = 0; i < sportMakeExcelList.size(); i++) {
            HashMap sportMap = (HashMap) sportMakeExcelList.get(i);
            sport = String.valueOf(sportMap.get("SPORT"));
            if (sport != null) {
                sportDataList = getExcelDataList(sport);
                makeExcelUtil.statXlsDown(sport, sportDataList);
            }
        }

        memberList = commonMapper.selectMemberList();

        for (int i = 0; i < memberList.size(); i++) {
            HashMap memberMap = (HashMap) memberList.get(i);
            if (memberMap.get("SPORT_YN").toString().equals("true") && memberMap.get("PITCHER_YN").toString().equals("true")) {
                emailUtil.sendSSLMessage(memberMap.get("EMAIL").toString(), (Date) memberMap.get("END_DATE"), sportMakeExcelList, "jungyongee@gmail.com");
            } else if (memberMap.get("SPORT_YN").toString().equals("true") && memberMap.get("PITCHER_YN").toString().equals("false")) {
                emailUtil.sendSSLMessage(memberMap.get("EMAIL").toString(), (Date) memberMap.get("END_DATE"), sportMakeExcelListWithoutPitcher, "jungyongee@gmail.com");
            } else if (memberMap.get("SPORT_YN").toString().equals("false") && memberMap.get("PITCHER_YN").toString().equals("true")) {
                emailUtil.sendSSLMessage(memberMap.get("EMAIL").toString(), (Date) memberMap.get("END_DATE"), Collections.singletonList(pitcherMap), "jungyongee@gmail.com");
            }
        }

        return RepeatStatus.FINISHED;
    }

    private List getExcelDataList(String type) {
        List excelDataList = new ArrayList<>();

        if (type.equals("basketball")) {
            excelDataList = basketballMapper.selectBasketballStat();
        } else if (type.equals("volleyball")) {
            excelDataList = volleyballMapper.selectVolleyballStat();
        } else if (type.equals("hockey")) {
            excelDataList = hockeyMapper.selectHockeyStat();
        } else if (type.equals("baseball_summary")) {
            excelDataList = baseballMapper.selectBaseballAllSummary();
        } else if (type.equals("baseball")) {
            excelDataList = baseballMapper.selectBaseballStat();
        } else if (type.equals("baseball_pitcher")) {
            excelDataList = baseballPitcherMapper.selectBaseballPitcherStat();
        } else if (type.equals("football")) {
            excelDataList = baseballPitcherMapper.selectBaseballPitcherStat();
        } else if (type.equals("basketball_summary")) {
            excelDataList = basketballMapper.selectBasketballAllSummary();
        } else if (type.equals("volleyball_avg_point")) {
            excelDataList = volleyballMapper.selectVolleyballAvgPoint();
        }
        return excelDataList;
    }
}
