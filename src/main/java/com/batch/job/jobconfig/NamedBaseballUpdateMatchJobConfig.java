package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBaseballUpdateMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedBaseballUpdateMatchWriter;
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
public class NamedBaseballUpdateMatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final JobCompletionNotificationListener notificationListener;
    private final DummyReader dummyReader;
    private final NamedBaseballUpdateMatchProcessor namedBaseballUpdateMatchProcessor;
    private final NamedBaseballUpdateMatchWriter namedBaseballUpdateMatchWriter;

    @Bean
    public Job namedBaseballUpdateMatchJob() {
        return jobBuilderFactory.get("namedBaseballUpdateMatchJob")
            .preventRestart()
            .listener(notificationListener)
            .flow(namedBaseballUpdateMatchStep())
            .end()
            .build();
    }

    @Bean
    public Step namedBaseballUpdateMatchStep() {
        return stepBuilderFactory.get("namedBaseballUpdateMatchStep")
            .<String, List<BaseballModel>>chunk(1)
            .reader(dummyReader)
            .processor(namedBaseballUpdateMatchProcessor)
            .writer(namedBaseballUpdateMatchWriter)
            .build();
    }

}
