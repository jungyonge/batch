package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.BasketballModel;
import com.batch.model.FilterConditionModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@MasterDb
@Repository
public interface BasketballMapper {

    void insertBasketMatch(BasketballModel basketballModel);

    int checkGameIdCount(BasketballModel basketballModel);

    void updateBasketStat(BasketballModel basketballModel);

    List selectBasketballStat();

    void insertBasketballSpecialSummary(FilterConditionModel filterConditionModel);

    void truncateBasketballSpecialSummary();

    void insertBasketHandiOverSummary(FilterConditionModel filterConditionModel);

    void truncateBasketHandiOverSummary();

    void insertBasketQuarterHandiOverSummary(FilterConditionModel filterConditionModel);

    void truncateBasketQuarterHandiOverSummary();

    void insertBasketSpecialComboSummary();

    void truncateBasketSpecialComboSummary();

    void insertBasketQuarterHandiComboSummary();

    void truncateBasketQuarterHandiComboSummary();

}