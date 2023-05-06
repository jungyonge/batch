package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedHockeyAllMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedHockeyAllMatchWriter;
import com.batch.model.HockeyModel;
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
public class NamedHockeyAllMatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final JobCompletionNotificationListener notificationListener;
    private final DummyReader dummyReader;
    private final NamedHockeyAllMatchProcessor namedHockeyAllMatchProcessor;
    private final NamedHockeyAllMatchWriter namedHockeyAllMatchWriter;

    @Bean
    public Job namedHockeyAllMatchJob(){
        return jobBuilderFactory.get("namedHockeyAllMatchJob")
                .preventRestart()
                .listener(notificationListener)
                .flow(namedHockeyAllMatchStep())
                .end()
                .build();
    }

    @Bean
    public Step namedHockeyAllMatchStep(){
        return stepBuilderFactory.get("namedHockeyAllMatchStep")
                .<String, List<HockeyModel>> chunk(1)
                .reader(dummyReader)
                .processor(namedHockeyAllMatchProcessor)
                .writer(namedHockeyAllMatchWriter)
                .build();
    }
}
