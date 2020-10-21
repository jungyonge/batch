package com.batch.job.task.writer;

import com.batch.mapper.VolleyballMapper;
import com.batch.model.VolleyballModel;
import com.batch.model.FilterConditionModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@StepScope
public class NamedVolleyballUpdateMatchWriter implements ItemWriter<List<VolleyballModel>> {

    private final VolleyballMapper volleyballMapper;

    public NamedVolleyballUpdateMatchWriter(VolleyballMapper volleyballMapper) {
        this.volleyballMapper = volleyballMapper;
    }

    @Override
    public void write(List<? extends List<VolleyballModel>> list) throws Exception {
        for (List<VolleyballModel> volleyballModels : list) {
            for (VolleyballModel volleyballModel : volleyballModels) {
                volleyballMapper.updateVolleyballStat(volleyballModel);
            }
        }

    }
}
