package com.batch.job.task.writer;

import com.batch.mapper.BasketballMapper;
import com.batch.model.BasketballModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@Slf4j
public class NamedBasketballAllMatchWriter implements ItemWriter<List<BasketballModel>> {

    private final BasketballMapper basketballMapper;

    public NamedBasketballAllMatchWriter(BasketballMapper basketballMapper) {
        this.basketballMapper = basketballMapper;
    }

    @Override
    public void write(List<? extends List<BasketballModel>> list) throws Exception {
        for(List<BasketballModel> basketballModels : list){
            for(BasketballModel basketballModel : basketballModels){
                int cnt = basketballMapper.checkGameIdCount(basketballModel);
                if(cnt < 2){
                    basketballMapper.insertBasketMatch(basketballModel);
                }
            }

        }
    }
}
