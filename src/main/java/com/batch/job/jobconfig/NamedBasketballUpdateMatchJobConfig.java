package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBasketballUpdateMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedBasketballUpdateMatchWriter;
import com.batch.model.BasketballModel;
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
public class NamedBasketballUpdateMatchJobConfig {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final JobCompletionNotificationListener notificationListener;
    private final DummyReader dummyReader;
    private final NamedBasketballUpdateMatchProcessor namedBasketballUpdateMatchProcessor;
    private final NamedBasketballUpdateMatchWriter namedBasketballUpdateMatchWriter;

    @Bean
    public Job namedBasketballUpdateMatchJob() {
        return jobBuilderFactory.get("namedBasketballUpdateMatchJob")
            .preventRestart()
            .listener(notificationListener)
            .flow(namedBasketballUpdateMatchStep())
            .end()
            .build();
    }

    @Bean
    public Step namedBasketballUpdateMatchStep() {
        return stepBuilderFactory.get("namedBasketballUpdateMatchStep")
            .<String, List<BasketballModel>>chunk(1)
            .reader(dummyReader)
            .processor(namedBasketballUpdateMatchProcessor)
            .writer(namedBasketballUpdateMatchWriter)
            .build();
    }
}
