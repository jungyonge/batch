package com.batch.job.jobconfig;

import com.batch.job.listener.JobCompletionNotificationListener;
import com.batch.job.task.processor.DummyProcessor;
import com.batch.job.task.processor.NamedBaseballAllMatchProcessor;
import com.batch.job.task.reader.DummyReader;
import com.batch.job.task.writer.DummyWriter;
import com.batch.job.task.writer.NamedBaseballAllMatchWriter;
import com.batch.model.BaseballModel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
@Configuration
@Controller
public class NamedBaseballCrawlingJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private JobOperator simpleJobOperator;
    @Autowired
    private JobCompletionNotificationListener notificationListener;
    @Autowired
    private DummyReader dummyReader;
    @Autowired
    private NamedBaseballAllMatchProcessor namedBaseballAllMatchProcessor;
    @Autowired
    private NamedBaseballAllMatchWriter namedBaseballAllMatchWriter;


//    @Scheduled(cron = "* * * * * ?")
    @RequestMapping(value = "jobs/baseballAllMatch")
    public void runJob() throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String _jobParameters = "now=" + sdf.format(new Date());

        simpleJobOperator.start("namedBaseBallCrawlingJob", _jobParameters);
     }

    @Bean
    public Job namedBaseBallCrawlingJob(){
        return jobBuilderFactory.get("namedBaseBallCrawlingJob")
                .preventRestart()
                .listener(notificationListener)
                .start(namedBaseBallCrawlingStep())
                .build();
    }

    @Bean
    public Step namedBaseBallCrawlingStep(){
        return stepBuilderFactory.get("namedBaseBallCrawlingStep")
                .<String, List<BaseballModel>> chunk(1)
                .reader(dummyReader)
                .processor(namedBaseballAllMatchProcessor)
                .writer(namedBaseballAllMatchWriter)
                .build();
    }

}
