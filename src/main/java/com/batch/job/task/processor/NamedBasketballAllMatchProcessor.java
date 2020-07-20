package com.batch.job.task.processor;

import com.batch.model.BaseballModel;
import com.batch.model.BasketballModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NamedBasketballAllMatchProcessor implements ItemProcessor<String, List<BasketballModel>> {
    @Override
    public List<BasketballModel> process(String s) throws Exception {
        return null;
    }
}
