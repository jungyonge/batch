package com.batch.job.task.writer;

import com.batch.mapper.BaseballMapper;
import com.batch.mapper.BaseballPitcherMapper;
import com.batch.model.BaseballModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NamedBaseballPitcherWriter  implements ItemWriter<List<BaseballModel>> {

    private final BaseballPitcherMapper baseballPitcherMapper;

    public NamedBaseballPitcherWriter(BaseballPitcherMapper baseballPitcherMapper) {
        this.baseballPitcherMapper = baseballPitcherMapper;
    }

    @Override
    public void write(List<? extends List<BaseballModel>> list) throws Exception {

        for(List<BaseballModel> baseballModels : list){
            for(BaseballModel baseballModel : baseballModels){
                int cnt = baseballPitcherMapper.checkGameIdCount(baseballModel);
                if(cnt < 2){
                    baseballPitcherMapper.insertBaseballPitcherStat(baseballModel);
                }
            }

        }
    }
}
