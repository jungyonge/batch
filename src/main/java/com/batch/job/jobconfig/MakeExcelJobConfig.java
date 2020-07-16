package com.batch.job.jobconfig;


import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBaseballAllMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.tasklet.MakeExcelTasklet;
import com.batch.job.task.writer.NamedBaseballAllMatchWriter;
import com.batch.model.BaseballModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
@Configuration
public class MakeExcelJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private JobCompletionNotificationListener notificationListener;
    private DummyReader dummyReader;

    @Autowired
    private MakeExcelTasklet makeExcelTasklet;

    @Autowired
    public MakeExcelJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, JobCompletionNotificationListener notificationListener, DummyReader dummyReader) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.notificationListener = notificationListener;
        this.dummyReader = dummyReader;

    }

    @Bean
    public Job makeExcelJob(){
        return jobBuilderFactory.get("makeExcelJob")
                .preventRestart()
                .listener(notificationListener)
                .flow(makeExcelStep())
                .end()
                .build();
    }

    @Bean
    public Step makeExcelStep(){
        return stepBuilderFactory.get("makeExcelStep")
                .tasklet(makeExcelTasklet)
                .build();
    }


}
