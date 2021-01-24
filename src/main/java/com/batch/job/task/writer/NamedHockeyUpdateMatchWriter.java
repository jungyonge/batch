package com.batch.job.task.writer;

import com.batch.mapper.HockeyMapper;
import com.batch.model.HockeyModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@StepScope
public class NamedHockeyUpdateMatchWriter implements ItemWriter<List<HockeyModel>> {

    private final HockeyMapper volleyballMapper;

    public NamedHockeyUpdateMatchWriter(HockeyMapper volleyballMapper) {
        this.volleyballMapper = volleyballMapper;
    }

    @Override
    public void write(List<? extends List<HockeyModel>> list) throws Exception {
        for (List<HockeyModel> volleyballModels : list) {
            for (HockeyModel volleyballModel : volleyballModels) {
                volleyballMapper.updateHockeyStat(volleyballModel);
            }
        }
    }
}
