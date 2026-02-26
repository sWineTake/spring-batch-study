/*
package com.system.batch.config;

import javax.sql.DataSource;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
// DefaultBatchConfiguration란?
// => JobRepository, JobLauncher 등 Spring Batch의 핵심 컴포넌트들을 자동으로 구성
public class BatchConfig extends DefaultBatchConfiguration {

    // Spring Batch는 Job과 Step의 실행 정보(메타데이터)를 데이터베이스에 저장
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("org/springframework/batch/core/schema-h2.sql")
            .build();
    }

    // 배치 코어 컴포넌트와 우리 Job에서 공통으로 사용할 PlatformTransactionManager Bean을 등록
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

}
*/
