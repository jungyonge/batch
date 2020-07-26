package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBaseballNextMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedBaseballNextMatchWriter;
import com.batch.model.BaseballModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobOperator;
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
public class NamedBaseballNextMatchJobConfig {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private JobCompletionNotificationListener notificationListener;
    private DummyReader dummyReader;

    private NamedBaseballNextMatchProcessor namedBaseballNextMatchProcessor;
    private NamedBaseballNextMatchWriter namedBaseballNextMatchWriter;

    @Autowired
    public NamedBaseballNextMatchJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, JobOperator simpleJobOperator, JobCompletionNotificationListener notificationListener, DummyReader dummyReader, NamedBaseballNextMatchProcessor namedBaseballNextMatchProcessor, NamedBaseballNextMatchWriter namedBaseballNextMatchWriter) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.notificationListener = notificationListener;
        this.dummyReader = dummyReader;

        this.namedBaseballNextMatchProcessor = namedBaseballNextMatchProcessor;
        this.namedBaseballNextMatchWriter = namedBaseballNextMatchWriter;
    }

    @Bean
    public Job namedBaseballNextMatchJob() {
        return jobBuilderFactory.get("namedBaseballNextMatchJob")
                .preventRestart()
                .listener(notificationListener)
                .flow(namedBaseballNextMatchStep())
                .end()
                .build();
    }

    @Bean
    public Step namedBaseballNextMatchStep() {
        return stepBuilderFactory.get("namedBaseballNextMatchStep")
                .<String, List<BaseballModel>>chunk(1)
                .reader(dummyReader)
                .processor(namedBaseballNextMatchProcessor)
                .writer(namedBaseballNextMatchWriter)
                .build();
    }

}
