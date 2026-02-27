package com.system.batch.section1.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class ListenerSystemTerminationConfig {
    // 간단히 말하면 리스너에서 beforeJob에서 키값을 넣고 첫번째 스텝에서 해당 값을 조회 후 ExecutionContext Put
    // 이후 다음 스텝에서 ExecutionContext 기반으로 값 조회하여 마치 리스너에 넣은 값을 전체 Job에서 사용할 수 있게 끔 사용
    // 단, Step간의 데이터 의존성은 낮추는게 좋다. 너무 많으면 복잡해짐으로

    @Bean
    public Job systemTerminationJob(JobRepository jobRepository, Step scanningStep, Step eliminationStep) {
        return new JobBuilder("systemTerminationJob", jobRepository)
                .start(scanningStep)
                .next(eliminationStep)
                .build();
    }

    @Bean
    public Step scanningStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("scanningStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String target = "판교 서버실";
                    // Step에서 리스너에 넣었던 키값을 ExecutionContext에 넣음으로 Job에서 접근이 가능하게 할 것이다.
                    ExecutionContext stepContext = contribution.getStepExecution().getExecutionContext();
                    stepContext.put("targetSystem", target);
                    log.info("타겟 스캔 완료: {}", target);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(promotionListener())
                .build();
    }

    @Bean
    public Step eliminationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Tasklet eliminationTasklet
    ) {
        return new StepBuilder("eliminationStep", jobRepository)
                .tasklet(eliminationTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet eliminationTasklet(
            // Job의 ExecutionContext에서 값을 조회함으로 targetSystem 값이 자동으로 job수준에 ExecutionContext으로 승격 된다.
            @Value("#{jobExecutionContext['targetSystem']}") String target
    ) {
        return (contribution, chunkContext) -> {
            log.info("시스템 제거 작업 실행: {}", target);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        // targetSystem 키를 승격 대상으로 지정
        listener.setKeys(new String[]{"targetSystem"});
        return listener;
    }

}
