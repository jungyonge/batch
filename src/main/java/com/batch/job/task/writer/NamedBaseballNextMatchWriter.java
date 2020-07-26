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
public class NamedBaseballNextMatchWriter implements ItemWriter<List<BaseballModel>> {

    private final BaseballMapper baseballMapper;

    public NamedBaseballNextMatchWriter(BaseballMapper baseballMapper) {
        this.baseballMapper = baseballMapper;
    }

    @Override
    public void write(List<? extends List<BaseballModel>> list) throws Exception {

        for (List<BaseballModel> baseballModels : list) {
            for (BaseballModel baseballModel : baseballModels) {
                baseballMapper.updateBaseballNextMatchStat(baseballModel);
            }
        }

    }
}
