package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBaseballAllMatchProcessor;
import com.batch.job.task.processor.namedBaseballPitcherProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedBaseballPitcherWriter;
import com.batch.model.BaseballModel;
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
public class NamedBaseballPitcherJobConfig {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private JobCompletionNotificationListener notificationListener;
    private DummyReader dummyReader;
    private namedBaseballPitcherProcessor namedBaseballPitcherProcessor;
    private NamedBaseballPitcherWriter namedBaseballPitcherWriter;

    @Autowired
    public NamedBaseballPitcherJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, JobCompletionNotificationListener notificationListener, DummyReader dummyReader, namedBaseballPitcherProcessor namedBaseballPitcherProcessor, NamedBaseballPitcherWriter namedBaseballPitcherWriter) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.notificationListener = notificationListener;
        this.dummyReader = dummyReader;
        this.namedBaseballPitcherProcessor = namedBaseballPitcherProcessor;
        this.namedBaseballPitcherWriter = namedBaseballPitcherWriter;
    }


    @Bean
    public Job NamedBaseballPitcherJob(){
        return jobBuilderFactory.get("namedBaseballPitcherJob")
                .preventRestart()
                .listener(notificationListener)
                .flow(NamedBaseballPitcherStep())
                .end()
                .build();
    }

    @Bean
    public Step NamedBaseballPitcherStep(){
        return stepBuilderFactory.get("namedBaseballPitcherStep")
                .<String, List<BaseballModel>> chunk(1)
                .reader(dummyReader)
                .processor(namedBaseballPitcherProcessor)
                .writer(namedBaseballPitcherWriter)
                .build();
    }

}
