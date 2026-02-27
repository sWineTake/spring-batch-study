package com.system.batch.section1.listener;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ListenerTerminationConfig {


    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public ListenerTerminationConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }


    @Bean
    public Job systemMonitoringJob(JobRepository jobRepository, Step monitoringStep) {
        return new JobBuilder("systemMonitoringJob", jobRepository)
                .listener(new BigBrotherJobExecutionListener())
                .start(monitoringStep)
                .build();
    }

    @Bean
    public Step monitoringStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                Tasklet terminatorTasklet) {
        return new StepBuilder("monitoringStep", jobRepository)
                .tasklet(terminatorTasklet, transactionManager)
                .build();
    }


    @Bean
    public Step listenerDestructiveTasklet() {
        return new StepBuilder("serverRackControlStep", jobRepository)
                .tasklet(listenerProcessCleanupTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet listenerProcessCleanupTasklet() {
        return new ListenerProcessCleanupTasklet();
    }

}
