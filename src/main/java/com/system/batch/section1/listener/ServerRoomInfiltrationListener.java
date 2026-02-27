package com.system.batch.section1.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.stereotype.Component;

@Slf4j
@Component
/**
 * BigBrotherJobExecutionListener 처럼 상속을 받아 오버라이딩하는 방식말고도 어노테이션을 활용한 방식도 있다.
 * **/
public class ServerRoomInfiltrationListener {

    @BeforeJob
    public void infiltrateServerRoom(JobExecution jobExecution) {
        log.info("Job 이 시작되기전에 실행된다.");
    }

    @AfterJob
    public void escapeServerRoom(JobExecution jobExecution) {
        log.info("Job이 종료될때 호출된다.");
    }
}
