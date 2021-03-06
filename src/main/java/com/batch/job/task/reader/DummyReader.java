package com.batch.job.task.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@StepScope
public class DummyReader  implements ItemReader<String> {
    private boolean batchJobState = false;

    @BeforeStep
    public void setStepExecution(StepExecution stepExecution) {
        batchJobState = false;
    }

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        log.info(" Dummy Reader");
        if(!batchJobState){
            batchJobState=true;
            return "1";
        }
        return null;
    }
}
