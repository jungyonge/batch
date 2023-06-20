package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedVolleyballAllMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedVolleyballAllMatchWriter;
import com.batch.model.VolleyballModel;
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
@Configuration
@RequiredArgsConstructor
public class NamedVolleyballAllMatchJobConfig {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final JobCompletionNotificationListener notificationListener;
    private final DummyReader dummyReader;
    private final NamedVolleyballAllMatchProcessor namedVolleyballAllMatchProcessor;
    private final NamedVolleyballAllMatchWriter namedVolleyballAllMatchWriter;

    @Bean
    public Job namedVolleyballAllMatchJob() {
        return jobBuilderFactory.get("namedVolleyballAllMatchJob")
            .preventRestart()
            .listener(notificationListener)
            .flow(namedVolleyballAllMatchStep())
            .end()
            .build();
    }

    @Bean
    public Step namedVolleyballAllMatchStep() {
        return stepBuilderFactory.get("namedVolleyballAllMatchStep")
            .<String, List<VolleyballModel>>chunk(1)
            .reader(dummyReader)
            .processor(namedVolleyballAllMatchProcessor)
            .writer(namedVolleyballAllMatchWriter)
            .build();
    }
}
