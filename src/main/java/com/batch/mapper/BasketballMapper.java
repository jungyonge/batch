package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.BasketballModel;
import com.batch.model.FilterConditionModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@MasterDb
@Repository
public interface BasketballMapper {

    public void insertBasketMatch(BasketballModel basketballModel);

    public int checkGameIdCount(BasketballModel basketballModel);

    public void updateBasketStat(BasketballModel basketballModel);

    public List selectBasketballStat();

    public void insertBasketballSpecialSummary(FilterConditionModel filterConditionModel);

    public void truncateBasketballSpecialSummary();

    public void insertBasketHandiOverSummary(FilterConditionModel filterConditionModel);

    public void truncateBasketHandiOverSummary();

    public void insertBasketQuarterHandiOverSummary(FilterConditionModel filterConditionModel);

    public void truncateBasketQuarterHandiOverSummary();

}