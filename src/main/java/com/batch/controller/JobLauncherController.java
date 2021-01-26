package com.batch.controller;


import com.batch.model.JobRunModel;
import com.batch.service.JobScheduleService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@RestController
@Slf4j
@EnableScheduling
public class JobLauncherController {
    @Autowired
    private JobScheduleService jobScheduleService;
//    @Autowired
//    private JobDetailService jobDetailService;

//    @RequestMapping(value = "/batch/schedule/getJobScheduleList", method = RequestMethod.GET)
//    public JobScheduleModel getJobScheduleList(@ModelAttribute JobScheduleModel jobScheduleModel) throws NoSuchJobInstanceException, NoSuchJobException {
//        jobScheduleModel.setJobScheduleModelList(jobScheduleService.getJobScheduleList());
//        return jobScheduleModel;
//    }

    @RequestMapping(value = "/batch/schedule/startJob", method = RequestMethod.POST)
    public Long startJob(@ModelAttribute JobRunModel jobRunModel) throws JobParametersInvalidException, JobInstanceAlreadyExistsException, NoSuchJobException {
        return jobScheduleService.startJob(jobRunModel);
    }

    @RequestMapping(value = "/batch/schedule/startJob/{jobName}/{jobParameters}", method = RequestMethod.GET)
    public Long startJob(@PathVariable String jobName, @PathVariable String jobParameters) throws JobParametersInvalidException, JobInstanceAlreadyExistsException, NoSuchJobException {
        return jobScheduleService.startJob(jobName, jobParameters);
    }

    @RequestMapping(value = "/index")
    public String test() throws JobParametersInvalidException,  NoSuchJobException {
        return "index";
    }


    @RequestMapping(value = "jobs/allSports")
    @Scheduled(cron = "0 0 14,23 ? * *")
    public void runJob() throws Exception {
        String _jobParameters = "mode=update";
        Calendar nextDate = Calendar.getInstance();
        nextDate.setTime(new Date());

        DateFormat hourDf = new SimpleDateFormat("HH");
        String checkHour = hourDf.format(nextDate.getTime());

        jobScheduleService.startJob("namedBaseballPitcherJob", _jobParameters);
        Thread.sleep(1000 * 60 * 1);
        jobScheduleService.startJob("namedBaseBallAllMatchJob", _jobParameters);
        Thread.sleep(1000 * 60 * 1);
        jobScheduleService.startJob("namedBaseballUpdateMatchJob", _jobParameters);
        Thread.sleep(1000 * 60 * 1);

        if(checkHour.equals("23")){
            jobScheduleService.startJob("namedBasketballAllMatchJob", _jobParameters);
            Thread.sleep(1000 * 60 * 1);
            jobScheduleService.startJob("namedBasketballUpdateMatchJob", _jobParameters);
            Thread.sleep(1000 * 60 * 1);
            jobScheduleService.startJob("namedVolleyballAllMatchJob", _jobParameters);
            Thread.sleep(1000 * 60 * 1);
            jobScheduleService.startJob("namedVolleyballUpdateMatchJob", _jobParameters);
            Thread.sleep(1000 * 60 * 1);
            jobScheduleService.startJob("namedHockeyAllMatchJob", _jobParameters);
            Thread.sleep(1000 * 60 * 1);
            jobScheduleService.startJob("namedHockeyUpdateMatchJob", _jobParameters);
            Thread.sleep(1000 * 60 * 1);
        }

        jobScheduleService.startJob("namedBaseballNextMatchJob", _jobParameters);
        Thread.sleep(1000 * 60 * 1);
        jobScheduleService.startJob("makeExcelJob", _jobParameters);
    }


//    @RequestMapping(value = "/batch/schedule/startSchedule", method = RequestMethod.POST)
//    public int startSchedule(@ModelAttribute JobScheduleModel jobScheduleModel) throws NoSuchJobInstanceException, NoSuchJobException {
//        return jobScheduleService.startSchedule(jobScheduleModel);
//    }
//
//    @RequestMapping(value = "/batch/schedule/stopSchedule", method = RequestMethod.POST)
//    public int stopSchedule(@ModelAttribute JobScheduleModel jobScheduleModel) throws NoSuchJobInstanceException, NoSuchJobException {
//        return jobScheduleService.stopSchedule(jobScheduleModel);
//    }
//
//    @RequestMapping(value = "/batch/schedule/saveSchedule", method = RequestMethod.POST)
//    public int saveSchedule(@ModelAttribute JobScheduleModel jobScheduleModel) throws NoSuchJobInstanceException, NoSuchJobException {
//        return jobScheduleService.saveSchedule(jobScheduleModel);
//    }
//
//    @RequestMapping(value = "/batch/schedule/stopJobExecution", method = RequestMethod.POST)
//    public boolean stopJobExecution(@ModelAttribute JobExecutionModel jobExecutionModel) throws NoSuchJobExecutionException, JobExecutionNotRunningException {
//        return jobDetailService.stopJobExecution(jobExecutionModel);
//    }
//
//    @RequestMapping(value = "/batch/schedule/abandonJobExecution", method = RequestMethod.POST)
//    public boolean abandonJobExecution(@ModelAttribute JobExecutionModel jobExecutionModel) throws NoSuchJobExecutionException, JobExecutionAlreadyRunningException {
//        boolean isCompleted;
//        JobExecution jobExecution = jobDetailService.abandonJobExecution(jobExecutionModel);
//        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
//            isCompleted = true;
//        } else {
//            isCompleted = false;
//        }
//        return isCompleted;
//    }
}
