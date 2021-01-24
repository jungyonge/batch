package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedHockeyAllMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedHockeyAllMatchWriter;
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
public class NamedHockeyAllMatchJobConfig {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private JobCompletionNotificationListener notificationListener;
    private DummyReader dummyReader;
    private NamedHockeyAllMatchProcessor namedHockeyAllMatchProcessor;
    private NamedHockeyAllMatchWriter namedHockeyAllMatchWriter;


    @Autowired
    public NamedHockeyAllMatchJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                                        JobCompletionNotificationListener notificationListener, DummyReader dummyReader,
                                        NamedHockeyAllMatchProcessor namedHockeyAllMatchProcessor, NamedHockeyAllMatchWriter namedHockeyAllMatchWriter) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.notificationListener = notificationListener;
        this.dummyReader = dummyReader;
        this.namedHockeyAllMatchProcessor = namedHockeyAllMatchProcessor;
        this.namedHockeyAllMatchWriter = namedHockeyAllMatchWriter;
    }

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
