package com.system.batch.section1.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BigBrotherStepExecutionListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Step 이 시작되기전에 실행된다.");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Step 가 종료될때 호출된다.");
        return ExitStatus.COMPLETED;
    }
}
