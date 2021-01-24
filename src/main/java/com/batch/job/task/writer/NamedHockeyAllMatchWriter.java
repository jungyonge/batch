package com.batch.job.task.writer;

import com.batch.mapper.HockeyMapper;
import com.batch.model.HockeyModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NamedHockeyAllMatchWriter implements ItemWriter< List<HockeyModel>> {

    private final HockeyMapper HockeyMapper;

    public NamedHockeyAllMatchWriter(HockeyMapper HockeyMapper) {
        this.HockeyMapper = HockeyMapper;
    }

    @Override
    public void write(List<? extends List<HockeyModel>> list) throws Exception {

        for(List<HockeyModel> HockeyModels : list){
            for(HockeyModel hockeyModel : HockeyModels){
                int cnt = HockeyMapper.checkGameIdCount(hockeyModel);
                if(cnt < 2){
                    HockeyMapper.insertHockeyMatch(hockeyModel);
                }
            }
        }
    }
}
