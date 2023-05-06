package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBaseballPitcherProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedBaseballPitcherWriter;
import com.batch.model.BaseballModel;
import java.util.List;
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
public class NamedBaseballPitcherJobConfig {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final JobCompletionNotificationListener notificationListener;
    private final DummyReader dummyReader;
    private final NamedBaseballPitcherProcessor namedBaseballPitcherProcessor;
    private final NamedBaseballPitcherWriter namedBaseballPitcherWriter;


    @Bean
    public Job namedBaseballPitcherJob() {
        return jobBuilderFactory.get("namedBaseballPitcherJob")
            .preventRestart()
            .listener(notificationListener)
            .flow(namedBaseballPitcherStep())
            .end()
            .build();
    }

    @Bean
    public Step namedBaseballPitcherStep() {
        return stepBuilderFactory.get("namedBaseballPitcherStep")
            .<String, List<BaseballModel>>chunk(1)
            .reader(dummyReader)
            .processor(namedBaseballPitcherProcessor)
            .writer(namedBaseballPitcherWriter)
            .build();
    }

}
