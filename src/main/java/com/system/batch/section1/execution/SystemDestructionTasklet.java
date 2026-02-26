package com.system.batch.section1.execution;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SystemDestructionTasklet implements Tasklet {

    /**
     * SpringBatch에서 JobExecution이라는 녀석이 Job의 실행 정보를 쥐고 있다.
     * JobParameters도 당연히 JobExecution 안에 있다.
     * 결국 스텝 안에서 JobParameters 를 찾고 싶다면, JobExecution를 통해야 한다.
     * **/
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // JobParameters을 접근하기 위해 StepExecution에 접근하고 있다.
        // StepExecution => getJobParameters => JobExecution
        JobParameters jobParameters = chunkContext.getStepContext()
                .getStepExecution()
                .getJobParameters();

        String targetSystem = jobParameters.getString("system.target");
        Long destructionLevel = jobParameters.getLong("system.destruction.level");

        return RepeatStatus.FINISHED;
    }
}
