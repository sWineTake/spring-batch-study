package com.system.batch.section1.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class Section2_SystemTerminatorConfig {

    @Bean
    public Job processTerminatorJob(JobRepository jobRepository, Step terminationStep) {
        return new JobBuilder("processTerminatorJob", jobRepository)
                .start(terminationStep)
                .build();
    }

    @Bean
    public Step terminationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                Tasklet terminatorTasklet) {
        return new StepBuilder("terminationStep", jobRepository)
                .tasklet(terminatorTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet terminatorTasklet (
            /**
             * 해당 메서드에서는 두 가지 타입의 파라미터를 job에서 전달 받는다.
             * 예를들어 아래와 같은 실행 문이 들어왔다고 가정해 보자
                * ./gradlew bootRun --args='--spring.batch.job.name=processTerminatorJob terminatorId=KILL-9,java.lang.String targetCount=5,java.lang.Integer'
             * 실행 결과에서 중요한 부분을 확인해보자
                 *  [SimpleJob: [name=terminatorJob]] completed with the following parameters:
                 *  [{'terminatorId':'{value=KILL-9, type=class java.lang.String, identifying=true}',
                 *  'targetCount':'{value=5, type=class java.lang.Integer, identifying=true}'}] and the following status: [COMPLETED] in 21ms
             *
             * **/
            @Value("#{jobParameters['terminatorId']}") String terminatorId,
            @Value("#{jobParameters['targetCount']}") Integer targetCount
    ) {
        return (contribution, chunkContext) -> {
            log.info("시스템 종결자 정보:");
            log.info("ID: {}", terminatorId);
            log.info("제거 대상 수: {}", targetCount);
            log.info("⚡ SYSTEM TERMINATOR {} 작전을 개시합니다.", terminatorId);
            log.info("☠️ {}개의 프로세스를 종료합니다.", targetCount);

            for (int i = 1; i <= targetCount; i++) {
                log.info("💀 프로세스 {} 종료 완료!", i);
            }

            log.info("🎯 임무 완료: 모든 대상 프로세스가 종료되었습니다.");
            return RepeatStatus.FINISHED;
        };
    }

    // ////////////////////////////////////////////////////////

    @Bean
    public Job terminatorJob(JobRepository jobRepository, Step terminationStepLocalDate) {
        return new JobBuilder("terminatorJob", jobRepository)
                .start(terminationStepLocalDate)
                .build();
    }

    @Bean
    public Step terminationStepLocalDate(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                         Tasklet terminatorTaskletLocalDate) {
        return new StepBuilder("terminatorJobStep", jobRepository)
                .tasklet(terminatorTaskletLocalDate, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    /**
     * 파라미터 타입으로 LocalDate를 전달하는 방법을 예제로 볼수있다.
     * ./gradlew bootRun --args='--spring.batch.job.name=terminatorJob executionDate=2024-01-01,java.time.LocalDate startTime=2024-01-01T14:30:00,java.time.LocalDateTime'
     * **/
    public Tasklet terminatorTaskletLocalDate(
            @Value("#{jobParameters['executionDate']}") LocalDate executionDate,
            @Value("#{jobParameters['startTime']}") LocalDateTime startTime
    ) {
        return (contribution, chunkContext) -> {
            log.info("시스템 처형 정보:");
            log.info("처형 예정일: {}", executionDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
            log.info("작전 개시 시각: {}", startTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초")));
            log.info("⚡ {}에 예정된 시스템 정리 작전을 개시합니다.", executionDate);
            log.info("💀 작전 시작 시각: {}", startTime);

            // 작전 진행 상황 추적
            LocalDateTime currentTime = startTime;
            for (int i = 1; i <= 3; i++) {
                currentTime = currentTime.plusHours(1);
                log.info("☠️ 시스템 정리 {}시간 경과... 현재 시각:{}", i, currentTime.format(DateTimeFormatter.ofPattern("HH시 mm분")));
            }

            log.info("🎯 임무 완료: 모든 대상 시스템이 성공적으로 제거되었습니다.");
            log.info("⚡ 작전 종료 시각: {}", currentTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초")));


            return RepeatStatus.FINISHED;
        };
    }

    // ////////////////////////////////////////////////////////

    @Bean
    public Job terminatorJobEnum(JobRepository jobRepository, Step terminationStepEnum) {
        return new JobBuilder("terminatorJobEnum", jobRepository)
                .start(terminationStepEnum)
                .build();
    }

    @Bean
    public Step terminationStepEnum(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                         Tasklet terminatorTaskletEnum) {
        return new StepBuilder("terminatorJobEnum", jobRepository)
                .tasklet(terminatorTaskletEnum, transactionManager)
                .build();
    }

    // ./gradlew bootRun --args='--spring.batch.job.name=terminatorJobEnum questDifficulty=HARD'
    @Bean
    @StepScope
    public Tasklet terminatorTaskletEnum(
        @Value("#{jobParameters['questDifficulty']}") QuestDifficulty questDifficulty
    ) {
        return (contribution, chunkContext) -> {
            log.info("⚔️ 시스템 침투 작전 개시!");
            log.info("임무 난이도: {}", questDifficulty);
            // 난이도에 따른 보상 계산
            int baseReward = 100;
            int rewardMultiplier = switch (questDifficulty) {
                case EASY -> 1;
                case NORMAL -> 2;
                case HARD -> 3;
                case EXTREME -> 5;
            };
            int totalReward = baseReward * rewardMultiplier;
            log.info("💥 시스템 해킹 진행 중...");
            log.info("🏆 시스템 장악 완료!");
            log.info("💰 획득한 시스템 리소스: {} 메가바이트", totalReward);
            return RepeatStatus.FINISHED;
        };
    }
}
