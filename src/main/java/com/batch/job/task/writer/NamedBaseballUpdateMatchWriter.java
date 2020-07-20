package com.batch.job.task.writer;

import com.batch.mapper.BaseballMapper;
import com.batch.model.BaseballModel;
import com.batch.model.FilterConditionModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NamedBaseballUpdateMatchWriter implements ItemWriter< List<BaseballModel>> {

    private final BaseballMapper baseballMapper;

    public NamedBaseballUpdateMatchWriter(BaseballMapper baseballMapper) {
        this.baseballMapper = baseballMapper;
    }

    @Override
    public void write(List<? extends List<BaseballModel>> list) throws Exception {

        for(List<BaseballModel> baseballModels : list){
            for(BaseballModel baseballModel : baseballModels){
                    baseballMapper.updateBaseballStat(baseballModel);
            }
        }

        FilterConditionModel filterConditionModel = new FilterConditionModel();
        filterConditionModel.setGround(true);
        baseballMapper.insertBaseballSummary(filterConditionModel);

        filterConditionModel = new FilterConditionModel();
        filterConditionModel.setAll(true);
        baseballMapper.insertBaseballSummary(filterConditionModel);

        filterConditionModel = new FilterConditionModel();
        filterConditionModel.setOdd(true);
        baseballMapper.insertBaseballSummary(filterConditionModel);

        filterConditionModel = new FilterConditionModel();
        filterConditionModel.setWeek(true);
        baseballMapper.insertBaseballSummary(filterConditionModel);

        filterConditionModel = new FilterConditionModel();
        filterConditionModel.setPitcher(true);
        baseballMapper.insertBaseballSummary(filterConditionModel);

    }
}
