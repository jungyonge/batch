package com.batch.job.task.writer;

import com.batch.mapper.BaseballMapper;
import com.batch.model.BaseballModel;
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
        log.info("write");

        for(List<BaseballModel> baseballModels : list){
            for(BaseballModel baseballModel : baseballModels){
                int cnt = baseballMapper.checkGameIdCount(baseballModel);
                if(cnt < 2){
                    baseballMapper.insertBaseballMatch(baseballModel);
                }
            }
        }
    }
}