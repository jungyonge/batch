package com.batch.job.task.writer;

import com.batch.mapper.BaseballMapper;
import com.batch.model.BaseballModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NamedBaseballAllMatchWriter implements ItemWriter< List<BaseballModel>> {

    private final BaseballMapper baseballMapper;

    public NamedBaseballAllMatchWriter(BaseballMapper baseballMapper) {
        this.baseballMapper = baseballMapper;
    }

    @Override
    public void write(List<? extends List<BaseballModel>> list) throws Exception {
        log.info("write");

        for(List<BaseballModel> baseballModels : list){
            for(BaseballModel baseballModel : baseballModels){
                baseballMapper.insertBaseballMatch(baseballModel);
            }
        }
    }
}
