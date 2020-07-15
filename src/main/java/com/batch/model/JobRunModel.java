package com.batch.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class JobRunModel {
    private String jobName;
    private String jobParameter;
}
