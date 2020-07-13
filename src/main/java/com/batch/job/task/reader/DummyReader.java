package com.batch.job.task.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class DummyReader  implements ItemReader<String> {
    private boolean batchJobState = false;

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        log.info("reader");
        if(!batchJobState){
            batchJobState=true;
            return "1";
        }
        return null;
    }
}
