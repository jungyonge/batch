package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedHockeyUpdateMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedHockeyUpdateMatchWriter;
import com.batch.model.HockeyModel;
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
@Controller
public class NamedHockeyUpdateMatchJobConfig {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private JobCompletionNotificationListener notificationListener;
    private DummyReader dummyReader;
    private NamedHockeyUpdateMatchProcessor namedHockeyUpdateMatchProcessor;
    private NamedHockeyUpdateMatchWriter namedHockeyUpdateMatchWriter;


    @Autowired
    public NamedHockeyUpdateMatchJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                                           JobCompletionNotificationListener notificationListener, DummyReader dummyReader, NamedHockeyUpdateMatchProcessor namedHockeyUpdateMatchProcessor, NamedHockeyUpdateMatchWriter namedHockeyUpdateMatchWriter)
    {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.notificationListener = notificationListener;
        this.dummyReader = dummyReader;
        this.namedHockeyUpdateMatchProcessor = namedHockeyUpdateMatchProcessor;
        this.namedHockeyUpdateMatchWriter = namedHockeyUpdateMatchWriter;
    }

    @Bean
    public Job NamedHockeyUpdateMatchJob(){
        return jobBuilderFactory.get("namedHockeyUpdateMatchJob")
                .preventRestart()
                .listener(notificationListener)
                .flow(NamedHockeyUpdateMatchStep())
                .end()
                .build();
    }

    @Bean
    public Step NamedHockeyUpdateMatchStep(){
        return stepBuilderFactory.get("namedHockeyUpdateMatchStep")
                .<String, List<HockeyModel>> chunk(1)
                .reader(dummyReader)
                .processor(namedHockeyUpdateMatchProcessor)
                .writer(namedHockeyUpdateMatchWriter)
                .build();
    }
}
