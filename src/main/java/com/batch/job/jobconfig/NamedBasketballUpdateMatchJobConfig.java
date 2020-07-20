package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBasketballUpdateMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedBasketballUpdateMatchWriter;
import com.batch.model.BasketballModel;
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
public class NamedBasketballUpdateMatchJobConfig {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private JobCompletionNotificationListener notificationListener;
    private DummyReader dummyReader;
    private NamedBasketballUpdateMatchProcessor namedBasketballUpdateMatchProcessor;
    private NamedBasketballUpdateMatchWriter namedBasketballUpdateMatchWriter;


    @Autowired
    public NamedBasketballUpdateMatchJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                                               JobCompletionNotificationListener notificationListener, DummyReader dummyReader, NamedBasketballUpdateMatchProcessor namedBasketballUpdateMatchProcessor, NamedBasketballUpdateMatchWriter namedBasketballUpdateMatchWriter)
    {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.notificationListener = notificationListener;
        this.dummyReader = dummyReader;
        this.namedBasketballUpdateMatchProcessor = namedBasketballUpdateMatchProcessor;
        this.namedBasketballUpdateMatchWriter = namedBasketballUpdateMatchWriter;
    }

    @Bean
    public Job NamedBasketballUpdateMatchJob(){
        return jobBuilderFactory.get("NamedBasketballUpdateMatchJob")
                .preventRestart()
                .listener(notificationListener)
                .flow(NamedBasketballUpdateMatchStep())
                .end()
                .build();
    }

    @Bean
    public Step NamedBasketballUpdateMatchStep(){
        return stepBuilderFactory.get("NamedBasketballUpdateMatchStep")
                .<String, List<BasketballModel>> chunk(1)
                .reader(dummyReader)
                .processor(namedBasketballUpdateMatchProcessor)
                .writer(namedBasketballUpdateMatchWriter)
                .build();
    }
}
