package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBasketballAllMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedBasketballAllMatchWriter;
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
public class NamedBasketballAllMatchJobConfig {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private JobCompletionNotificationListener notificationListener;
    private DummyReader dummyReader;
    private NamedBasketballAllMatchProcessor namedBasketballAllMatchProcessor;
    private NamedBasketballAllMatchWriter namedBasketballAllMatchWriter;


    @Autowired
    public NamedBasketballAllMatchJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                                            JobCompletionNotificationListener notificationListener, DummyReader dummyReader, NamedBasketballAllMatchProcessor namedBasketballAllMatchProcessor, NamedBasketballAllMatchWriter namedBasketballAllMatchWriter) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.notificationListener = notificationListener;
        this.dummyReader = dummyReader;

        this.namedBasketballAllMatchProcessor = namedBasketballAllMatchProcessor;
        this.namedBasketballAllMatchWriter = namedBasketballAllMatchWriter;
    }

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
