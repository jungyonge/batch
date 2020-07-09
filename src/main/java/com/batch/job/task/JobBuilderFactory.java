package com.batch.job.task;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;

public class JobBuilderFactory {
    private JobRepository jobRepository;

    public JobBuilderFactory(JobRepository jobRepository){
        this.jobRepository = jobRepository;
    }

    public JobBuilder get(String name){
        return new JobBuilder(name).repository(jobRepository);
    }
}
