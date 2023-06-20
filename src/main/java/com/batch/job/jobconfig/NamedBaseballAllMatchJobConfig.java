package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBaseballAllMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedBaseballAllMatchWriter;
import com.batch.model.BaseballModel;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
@Configuration
public class NamedBaseballAllMatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final JobOperator simpleJobOperator;
    private final JobCompletionNotificationListener notificationListener;
    private final DummyReader dummyReader;
    private final NamedBaseballAllMatchProcessor namedBaseballAllMatchProcessor;
    private final NamedBaseballAllMatchWriter namedBaseballAllMatchWriter;


    @Bean
    public Job namedBaseBallAllMatchJob() {
        return jobBuilderFactory.get("namedBaseBallAllMatchJob")
            .preventRestart()
            .listener(notificationListener)
            .flow(namedBaseBallAllMatchStep())
            .end()
            .build();
    }

    @Bean
    public Step namedBaseBallAllMatchStep() {
        return stepBuilderFactory.get("namedBaseBallAllMatchStep")
            .<String, List<BaseballModel>>chunk(1)
            .reader(dummyReader)
            .processor(namedBaseballAllMatchProcessor)
            .writer(namedBaseballAllMatchWriter)
            .build();
    }

}
