package com.batch.job.task.writer;

import com.batch.mapper.BasketballMapper;
import com.batch.model.BasketballModel;
import com.batch.model.FilterConditionModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NamedBasketballUpdateMatchWriter implements ItemWriter<List<BasketballModel>> {

    private final BasketballMapper basketballMapper;

    public NamedBasketballUpdateMatchWriter(BasketballMapper basketballMapper) {
        this.basketballMapper = basketballMapper;
    }

    @Override
    public void write(List<? extends List<BasketballModel>> list) throws Exception {
        for (List<BasketballModel> basketballModels : list) {
            for (BasketballModel basketballModel : basketballModels) {
                basketballMapper.updateBasketStat(basketballModel);
            }
        }

        basketballMapper.truncateBasketballSpecialSummary();
        basketballMapper.truncateBasketHandiOverSummary();
        basketballMapper.truncateBasketQuarterHandiOverSummary();
        basketballMapper.truncateBasketSpecialComboSummary();
        basketballMapper.truncateBasketQuarterHandiComboSummary();

        for (int i = 0; i < 4; i++) {
            FilterConditionModel filterConditionModel = new FilterConditionModel();
            if (i == 0) {
                filterConditionModel.setAll(true);
            } else if (i == 1) {
                filterConditionModel.setGround(true);
            } else if (i == 2) {
                filterConditionModel.setOdd(true);
            } else {
                filterConditionModel.setWeek(true);
            }
            basketballMapper.insertBasketballSpecialSummary(filterConditionModel);
            basketballMapper.insertBasketHandiOverSummary(filterConditionModel);
            basketballMapper.insertBasketQuarterHandiOverSummary(filterConditionModel);
        }

        basketballMapper.insertBasketSpecialComboSummary();
        basketballMapper.insertBasketQuarterHandiComboSummary();
    }
}
