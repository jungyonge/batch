package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBasketballUpdateMatchProcessor;
import com.batch.job.task.processor.NamedVolleyballUpdateMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedBasketballUpdateMatchWriter;
import com.batch.job.task.writer.NamedVolleyballUpdateMatchWriter;
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
@Controller
public class NamedVolleyballUpdateMatchJobConfig {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private JobCompletionNotificationListener notificationListener;
    private DummyReader dummyReader;
    private NamedVolleyballUpdateMatchProcessor namedVolleyballUpdateMatchProcessor;
    private NamedVolleyballUpdateMatchWriter namedVolleyballUpdateMatchWriter;


    @Autowired
    public NamedVolleyballUpdateMatchJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                                               JobCompletionNotificationListener notificationListener, DummyReader dummyReader, NamedVolleyballUpdateMatchProcessor namedVolleyballUpdateMatchProcessor, NamedVolleyballUpdateMatchWriter namedVolleyballUpdateMatchWriter)
    {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.notificationListener = notificationListener;
        this.dummyReader = dummyReader;
        this.namedVolleyballUpdateMatchProcessor = namedVolleyballUpdateMatchProcessor;
        this.namedVolleyballUpdateMatchWriter = namedVolleyballUpdateMatchWriter;
    }

    @Bean
    public Job NamedVolleyballUpdateMatchJob(){
        return jobBuilderFactory.get("namedVolleyballUpdateMatchJob")
                .preventRestart()
                .listener(notificationListener)
                .flow(NamedVolleyballUpdateMatchStep())
                .end()
                .build();
    }

    @Bean
    public Step NamedVolleyballUpdateMatchStep(){
        return stepBuilderFactory.get("namedVolleyballUpdateMatchStep")
                .<String, List<VolleyballModel>> chunk(1)
                .reader(dummyReader)
                .processor(namedVolleyballUpdateMatchProcessor)
                .writer(namedVolleyballUpdateMatchWriter)
                .build();
    }
}
