package com.batch.job.task.writer;

import com.batch.mapper.BaseballMapper;
import com.batch.model.BaseballModel;
import com.batch.model.FilterConditionModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@StepScope
public class NamedBaseballUpdateMatchWriter implements ItemWriter<List<BaseballModel>> {

    private final BaseballMapper baseballMapper;

    public NamedBaseballUpdateMatchWriter(BaseballMapper baseballMapper) {
        this.baseballMapper = baseballMapper;
    }

    @Override
    public void write(List<? extends List<BaseballModel>> list) throws Exception {

        for (List<BaseballModel> baseballModels : list) {
            for (BaseballModel baseballModel : baseballModels) {
                baseballMapper.updateBaseballStat(baseballModel);
            }
        }

        baseballMapper.truncateBaseballSummary();

        for (int i = 0; i < 5; i++) {
            FilterConditionModel filterConditionModel = new FilterConditionModel();
            if (i == 0) {
                filterConditionModel.setAll(true);
            } else if (i == 1) {
                filterConditionModel.setGround(true);
            } else if (i == 2) {
                filterConditionModel.setOdd(true);
            } else if (i == 3) {
                filterConditionModel.setPitcher(true);
            } else {
                filterConditionModel.setWeek(true);
            }
            baseballMapper.insertBaseballSummary(filterConditionModel);
        }


    }
}
