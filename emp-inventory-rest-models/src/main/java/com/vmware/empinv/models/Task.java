package com.vmware.empinv.models;

import com.vmware.empinv.enums.TaskStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Task {
    private String taskId;
    private String queryUrl;
    private TaskStatus status;
    private String  description;
}
