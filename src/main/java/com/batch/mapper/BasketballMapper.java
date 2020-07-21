package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.BasketballModel;
import com.batch.model.FilterConditionModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@MasterDb
@Repository
public interface BasketballMapper {

    public int insertBasketMatch(BasketballModel basketballModel);

    public int checkGameIdCount(BasketballModel basketballModel);

    public int updateBasketStat(BasketballModel basketballModel);

    public List selectBasketStat();

    public int insertBaseBallSpecialSummary(FilterConditionModel filterConditionModel);
}