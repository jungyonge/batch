package com.batch.model;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.batch.core.BatchStatus;

@Getter
@Setter
@ToString
public class JobScheduleModel extends CommonModel {
    private long jobScheduleId;
    private String cronExpression;
    private String description;
    private String jobName;
    private String jobParameter;
    private int scheduled;
    private Date rdate;
    private Date mdate;

    private BatchStatus lastStatus;
    private Date lastUpdateTime;
    private Date nextStartTime;

    private List<JobScheduleModel> jobScheduleModelList;

    public JobScheduleModel() {
    }

    public JobScheduleModel(String jobName) {
        this.jobName = jobName;
    }
}
