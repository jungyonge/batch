package com.batch.job.jobconfig;


import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.tasklet.MakeExcelTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
@Configuration
public class MakeExcelJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final JobCompletionNotificationListener notificationListener;
    private final DummyReader dummyReader;
    private final MakeExcelTasklet makeExcelTasklet;


    @Bean
    public Job makeExcelJob() {
        return jobBuilderFactory.get("makeExcelJob")
            .preventRestart()
            .listener(notificationListener)
            .flow(makeExcelStep())
            .end()
            .build();
    }

    @Bean
    public Step makeExcelStep() {
        return stepBuilderFactory.get("makeExcelStep")
            .tasklet(makeExcelTasklet)
            .build();
    }


}
