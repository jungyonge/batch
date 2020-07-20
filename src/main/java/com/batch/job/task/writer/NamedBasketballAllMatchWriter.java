package com.batch.job.task.writer;

import com.batch.model.BaseballModel;
import com.batch.model.BasketballModel;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class NamedBasketballAllMatchWriter implements ItemWriter<List<BasketballModel>> {
    @Override
    public void write(List<? extends List<BasketballModel>> list) throws Exception {

    }
}
