package com.batch.job.task.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DummyProcessor implements ItemProcessor<String , String > {

    @Override
    public String process(String s) throws Exception {
        return new String("s");
    }
}
