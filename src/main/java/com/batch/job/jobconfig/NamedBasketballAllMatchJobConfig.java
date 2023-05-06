package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBasketballAllMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedBasketballAllMatchWriter;
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
public class NamedBasketballAllMatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final JobCompletionNotificationListener notificationListener;
    private final DummyReader dummyReader;
    private final NamedBasketballAllMatchProcessor namedBasketballAllMatchProcessor;
    private final NamedBasketballAllMatchWriter namedBasketballAllMatchWriter;

    @Bean
    public Job namedBasketballAllMatchJob(){
        return jobBuilderFactory.get("namedBasketballAllMatchJob")
                .preventRestart()
                .listener(notificationListener)
                .flow(namedBasketballAllMatchStep())
                .end()
                .build();
    }

    @Bean
    public Step namedBasketballAllMatchStep(){
        return stepBuilderFactory.get("namedBasketballAllMatchStep")
                .<String, List<BasketballModel>> chunk(1)
                .reader(dummyReader)
                .processor(namedBasketballAllMatchProcessor)
                .writer(namedBasketballAllMatchWriter)
                .build();
    }
}
