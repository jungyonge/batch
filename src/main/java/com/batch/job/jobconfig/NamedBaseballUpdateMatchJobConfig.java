package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedBaseballAllMatchProcessor;
import com.batch.job.task.processor.NamedBaseballUpdateMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedBaseballAllMatchWriter;
import com.batch.job.task.writer.NamedBaseballUpdateMatchWriter;
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
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
@Configuration
@Controller
public class NamedBaseballUpdateMatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private JobCompletionNotificationListener notificationListener;
    private DummyReader dummyReader;
    private NamedBaseballUpdateMatchProcessor namedBaseballUpdateMatchProcessor;
    private NamedBaseballUpdateMatchWriter namedBaseballUpdateMatchWriter;

    @Autowired
    public NamedBaseballUpdateMatchJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, JobOperator simpleJobOperator, JobCompletionNotificationListener notificationListener, DummyReader dummyReader, NamedBaseballUpdateMatchProcessor namedBaseballUpdateMatchProcessor, NamedBaseballUpdateMatchWriter namedBaseballUpdateMatchWriter) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.notificationListener = notificationListener;
        this.dummyReader = dummyReader;
        this.namedBaseballUpdateMatchProcessor = namedBaseballUpdateMatchProcessor;
        this.namedBaseballUpdateMatchWriter = namedBaseballUpdateMatchWriter;
    }

    @Bean
    public Job namedBaseballUpdateMatchJob(){
        return jobBuilderFactory.get("namedBaseballUpdateMatchJob")
                .preventRestart()
                .listener(notificationListener)
                .flow(namedBaseballUpdateMatchStep())
                .end()
                .build();
    }

    @Bean
    public Step namedBaseballUpdateMatchStep(){
        return stepBuilderFactory.get("namedBaseballUpdateMatchStep")
                .<String, List<BaseballModel>> chunk(1)
                .reader(dummyReader)
                .processor(namedBaseballUpdateMatchProcessor)
                .writer(namedBaseballUpdateMatchWriter)
                .build();
    }

}
