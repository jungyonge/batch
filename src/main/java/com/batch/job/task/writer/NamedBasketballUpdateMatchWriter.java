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

        FilterConditionModel filterConditionModel = new FilterConditionModel();
        filterConditionModel.setGround(true);
        basketballMapper.insertBasketballSpecialSummary(filterConditionModel);

        filterConditionModel = new FilterConditionModel();
        filterConditionModel.setAll(true);
        basketballMapper.insertBasketballSpecialSummary(filterConditionModel);

        filterConditionModel = new FilterConditionModel();
        filterConditionModel.setOdd(true);
        basketballMapper.insertBasketballSpecialSummary(filterConditionModel);

        filterConditionModel = new FilterConditionModel();
        filterConditionModel.setWeek(true);
        basketballMapper.insertBasketballSpecialSummary(filterConditionModel);

    }
}
