package com.batch.job.task.writer;

import com.batch.mapper.BaseballMapper;
import com.batch.mapper.VolleyballMapper;
import com.batch.model.BaseballModel;
import com.batch.model.VolleyballModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NamedVolleyballAllMatchWriter implements ItemWriter< List<VolleyballModel>> {

    private final VolleyballMapper volleyballMapper;

    public NamedVolleyballAllMatchWriter(VolleyballMapper volleyballMapper) {
        this.volleyballMapper = volleyballMapper;
    }

    @Override
    public void write(List<? extends List<VolleyballModel>> list) throws Exception {

        for(List<VolleyballModel> volleyballModels : list){
            for(VolleyballModel volleyballModel : volleyballModels){
                int cnt = volleyballMapper.checkGameIdCount(volleyballModel);
                if(cnt < 2){
                    volleyballMapper.insertVolleyballMatch(volleyballModel);
                }
            }
        }
    }
}
