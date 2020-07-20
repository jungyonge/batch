package com.batch.job.task.tasklet;

import com.batch.mapper.BaseballMapper;
import com.batch.mapper.BaseballPitcherMapper;
import com.batch.mapper.BasketballMapper;
import com.batch.mapper.CommonMapper;
import com.batch.util.EmailUtil;
import com.batch.util.MakeExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class MakeExcelTasklet implements Tasklet {

    private final BaseballMapper baseballMapper;
    private final BaseballPitcherMapper baseballPitcherMapper;

    private final BasketballMapper basketballMapper;
    private final CommonMapper commonMapper;
    private final MakeExcelUtil makeExcelUtil;
    private final EmailUtil emailUtil;


    public MakeExcelTasklet(BaseballMapper baseballMapper, BaseballPitcherMapper baseballPitcherMapper, BasketballMapper basketballMapper, CommonMapper commonMapper, MakeExcelUtil makeExcelUtil, EmailUtil emailUtil) {
        this.baseballMapper = baseballMapper;
        this.baseballPitcherMapper = baseballPitcherMapper;
        this.basketballMapper = basketballMapper;
        this.commonMapper = commonMapper;
        this.makeExcelUtil = makeExcelUtil;
        this.emailUtil = emailUtil;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        List sportMakeExcelList = new ArrayList<>();
        List sportDataList = new ArrayList<>();

        String sport;

        sportMakeExcelList = commonMapper.selectSportMakeExcelList();
        for (int i = 0 ; i < sportMakeExcelList.size() ; i++){
            HashMap sportMap = (HashMap) sportMakeExcelList.get(i);
            sport = String.valueOf(sportMap.get("SPORT"));
            if(sport != null) {
                sportDataList = getExcelDataList(sport);
                makeExcelUtil.statXlsDown(sport, sportDataList);
            }
        }
        emailUtil.sendSSLMessage("qjsro1204@naver.com",sportMakeExcelList,"jungyongee@gmail.com");
        return RepeatStatus.FINISHED;
    }

    private List getExcelDataList(String type){
        List excelDataList = new ArrayList<>();

        if(type.equals("basketball")){
            excelDataList = basketballMapper.selectBasketStat();
        }else if (type.equals("volleyball")){
//            excelDataList = setalarmDAO.selectVolleyStat();
        }else if (type.equals("hockey")){
//            excelDataList = setalarmDAO.selectHockeyStat();
        }else if (type.equals("soccer")){
//            excelDataList = setalarmDAO.selectSoccerStat();
        }else if (type.equals("basketball_special")){
//            excelDataList = setalarmDAO.selectBasketSpecialSummary();
        }else if (type.equals("basketball_quarter_special_combo")){
//            excelDataList = setalarmDAO.selectBasketSpecialComboSummary();
        }else if (type.equals("basketball_quarter_handi_over")){
//            excelDataList = setalarmDAO.selectBasketQuarterHandiOverSummary();
        }else if (type.equals("basketball_quarter_handi_combo")){
//            excelDataList = setalarmDAO.selectBasketQuarterHandiComboSummary();
        }else if (type.equals("baseball")){
            excelDataList = baseballMapper.selectBaseballStat();
        }else if (type.equals("baseball_pitcher")){
            excelDataList = baseballPitcherMapper.selectBaseballPitcherStat();
        }
        return excelDataList;
    }
}
