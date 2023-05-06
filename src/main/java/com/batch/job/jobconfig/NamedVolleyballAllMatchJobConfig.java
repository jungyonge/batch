package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBasketballAllMatchProcessor;
import com.batch.job.task.processor.NamedVolleyballAllMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedBasketballAllMatchWriter;
import com.batch.job.task.writer.NamedVolleyballAllMatchWriter;
import com.batch.model.BasketballModel;
import com.batch.model.VolleyballModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

import java.util.List;


@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
@Configuration
public class NamedVolleyballAllMatchJobConfig {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private JobCompletionNotificationListener notificationListener;
    private DummyReader dummyReader;
    private NamedVolleyballAllMatchProcessor namedVolleyballAllMatchProcessor;
    private NamedVolleyballAllMatchWriter namedVolleyballAllMatchWriter;


    @Autowired
    public NamedVolleyballAllMatchJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                                            JobCompletionNotificationListener notificationListener, DummyReader dummyReader,
                                            NamedVolleyballAllMatchProcessor namedVolleyballAllMatchProcessor, NamedVolleyballAllMatchWriter namedVolleyballAllMatchWriter) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.notificationListener = notificationListener;
        this.dummyReader = dummyReader;
        this.namedVolleyballAllMatchProcessor = namedVolleyballAllMatchProcessor;
        this.namedVolleyballAllMatchWriter = namedVolleyballAllMatchWriter;
    }

    @Bean
    public Job namedVolleyballAllMatchJob(){
        return jobBuilderFactory.get("namedVolleyballAllMatchJob")
                .preventRestart()
                .listener(notificationListener)
                .flow(namedVolleyballAllMatchStep())
                .end()
                .build();
    }

    @Bean
    public Step namedVolleyballAllMatchStep(){
        return stepBuilderFactory.get("namedVolleyballAllMatchStep")
                .<String, List<VolleyballModel>> chunk(1)
                .reader(dummyReader)
                .processor(namedVolleyballAllMatchProcessor)
                .writer(namedVolleyballAllMatchWriter)
                .build();
    }
}
