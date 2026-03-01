package com.system.batch.section3.collection1;

import java.time.LocalDateTime;
import java.util.List;
import javax.sql.DataSource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
// ./gradlew bootRun --args='--spring.batch.job.name=victimRecordJob'
public class VictimRecordConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    @Bean
    public Job processVictimJob() {
        return new JobBuilder("victimRecordJob", jobRepository)
                .start(processVictimStep())
                .build();
    }

    @Bean
    public Step processVictimStep() {
        return new StepBuilder("victimRecordStep", jobRepository)
                .<Victim, Victim>chunk(5, transactionManager)
                .reader(terminatedVictimReader())
                .writer(victimWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Victim> terminatedVictimReader() {
        /**
         * SQL로 조회 후 대량에 데이터를 메모리에 모두 올리지 않고 Result.next()로 한 행씩 DB에서 가져옴
         * 커넥션은 1번만 하고 그뒤로 커넥션은 유지한채로 ResultSet커서만 앞으로 이동시킴
             * Step 시작
             *   └─ DB Connection 오픈 (1회)
             *        └─ SQL 실행 → ResultSet 생성 (커서: before first row)
             *
             * read() 1번째 → rs.next() → 1번째 행 (커넥션 유지 중)
             * read() 2번째 → rs.next() → 2번째 행 (커넥션 유지 중)
             * read() 3번째 → rs.next() → 3번째 행 (커넥션 유지 중)
             * ...
             * read() N번째 → rs.next() == false → null 반환
             *
             * Step 종료 → Connection close (1회)
         * **/
        return new JdbcCursorItemReaderBuilder<Victim>()
                .name("terminatedVictimReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM victims WHERE status = ? AND terminated_at <= ?")
                .queryArguments(List.of("TERMINATED", LocalDateTime.now()))
                .beanRowMapper(Victim.class)
                /*
                커스텀으로 바인딩이 필요한 경우 아래처럼 가능하다.
                .rowMapper((rs, rowNum) -> {
                    Victim victim = new Victim();
                    victim.setId(rs.getLong("id"));
                    victim.setName(rs.getString("name"));
                    victim.setProcessId(rs.getString("process_id"));
                    victim.setTerminatedAt(rs.getTimestamp("terminated_at").toLocalDateTime());
                    victim.setStatus(rs.getString("status"));
                    return victim;
                })
                */
                .build();
    }

    @Bean
    public ItemWriter<Victim> victimWriter() {
        return items -> {
            for (Victim victim : items) {
                log.info("{}", victim);
            }
        };
    }

    @NoArgsConstructor
    @Data
    public static class Victim {
        private Long id;
        private String name;
        private String processId;
        private LocalDateTime terminatedAt;
        private String status;
    }
}
