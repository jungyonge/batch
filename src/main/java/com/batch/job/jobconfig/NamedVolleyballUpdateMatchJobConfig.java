package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.NamedVolleyballUpdateMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.NamedVolleyballUpdateMatchWriter;
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
import org.springframework.stereotype.Controller;


@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
@Configuration
@Controller
public class NamedVolleyballUpdateMatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final JobCompletionNotificationListener notificationListener;
    private final DummyReader dummyReader;
    private final NamedVolleyballUpdateMatchProcessor namedVolleyballUpdateMatchProcessor;
    private final NamedVolleyballUpdateMatchWriter namedVolleyballUpdateMatchWriter;

    @Bean
    public Job namedVolleyballUpdateMatchJob() {
        return jobBuilderFactory.get("namedVolleyballUpdateMatchJob")
            .preventRestart()
            .listener(notificationListener)
            .flow(namedVolleyballUpdateMatchStep())
            .end()
            .build();
    }

    @Bean
    public Step namedVolleyballUpdateMatchStep() {
        return stepBuilderFactory.get("namedVolleyballUpdateMatchStep")
            .<String, List<VolleyballModel>>chunk(1)
            .reader(dummyReader)
            .processor(namedVolleyballUpdateMatchProcessor)
            .writer(namedVolleyballUpdateMatchWriter)
            .build();
    }
}
