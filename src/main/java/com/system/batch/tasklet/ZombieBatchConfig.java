package com.system.batch.tasklet;

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
public class ZombieBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public ZombieBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Tasklet zombieProcessCleanupTasklet() {
        return new ZombieProcessCleanupTasklet();
    }

    @Bean
    public Step zombieCleanupStep() {
        return new StepBuilder("zombieCleanupStep", jobRepository)
                // Tasklet과 transactionManager 등록
                // transactionManager를 왜 별도로 등록해주는가?
                /**
                * Spring Batch는 멀티 데이터 소스 환경을 제공
                 * 만약 아래와 같은 케이스가 발생하였다고 생각해보자
                    * DataSource 1 → Batch 메타데이터 DB (BATCH_JOB_INSTANCE 등)
                    * DataSource 2 → 실제 업무 DB
                * 위와 같은 케이스의 경우 여러개의 트랜잭션 매니저가 존재 할 수도있어 Step이 어떤 트랜잭션 매니저를 써야하는지 알 수 없음
                * 메소드체인 tasklet에서 트랜잭션을 명시적으로 선언함으로 어떤 트랜잭션 매니저를 써야하는지 알 수 있음
                 * 왜? 명시적 선언으로 하는가?
                    * 스프링 배치 4에서는 묵시적 선언이었지만, 배치 5에서는 명시적 선언으로 바뀜
                    * 암묵적 의존성을 없애고 명시적으로 만들겠다는 의도가 더 보임
                    * 이로써, 내가 알아본 코드의 지향성은 아래와 같다.
                        * 자동 설정은 단순한 기본 케이스만, 복잡한 건 명시적으로 선언
                **/
                .tasklet(zombieProcessCleanupTasklet(), transactionManager)

                /**
                 * 만약? 나는 DB트랜잭션을 쓸일이 없는 배치 작업을 하고싶은 경우 ResourcelessTransactionManager를 사용한다.
                 * no-op(아무것도 하지 않는) 방식으로 동작하는 PlatformTransactionManager 구현체로
                 * 이를 사용하면 불필요한 DB 트랜잭션 처리를 생략 할 수 있다.
                 *
                 * 단, 조심해야하는 경우가 있다.
                 * ResourcelessTransactionManager를 Bean으로 등록하여 사용하고싶은경우 문제가 발생한다.
                 * Spring Batch는 Job과 Step을 별도의 테이블에 진행현황을 저장하게되는데 이때 ResourcelessTransactionManager를 사용 할 경우
                 * 의도치 않게 Job과 Step 진행 현황을 DB 테이블에 저장해야하는데, PlatformTransactionManager 구현체를 사용하게되면
                 * 문제가 발생할 수 있다. 이런 경우를 조심하자
                 * **/
                // .tasklet(zombieProcessCleanupTasklet(), new ResourcelessTransactionManager())

                .build();
    }

    @Bean
    public Job zombieCleanupJob() {
        return new JobBuilder("zombieCleanupJob", jobRepository)
                .start(zombieCleanupStep())
                .build();
    }
}
