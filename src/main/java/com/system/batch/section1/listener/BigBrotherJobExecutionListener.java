package com.system.batch.section1.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BigBrotherJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job 이 시작되기전에 실행된다.");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Job이 종료될때 호출된다.");
        log.info("시스템 상태: {}", jobExecution.getStatus());
    }

}
