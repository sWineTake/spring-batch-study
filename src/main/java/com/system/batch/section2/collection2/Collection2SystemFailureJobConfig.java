package com.system.batch.section2.collection2;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
// ./gradlew bootRun --args='--spring.batch.job.name=section2SystemFailureJob inputFile=/Users/twocowsong/Documents/01.study/02.source/39.spring-batch-server/system-failures.csv'
public class Collection2SystemFailureJobConfig {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job section2SystemFailureJob(Step collection2SystemFailureStep) {
        return new JobBuilder("section2SystemFailureJob", jobRepository)
            .start(collection2SystemFailureStep)
            .build();
    }

    @Bean
    public Step collection2SystemFailureStep(
        FlatFileItemReader<SystemFailure> systemFailureItemReader,
        SystemFailureStdoutItemWriter systemFailureStdoutItemWriter
    ) {
        /**
         * 청크단위로 10개의 reader호출 . 한번의 writer호출 됨
         * 청크 단위가 높아지면, 메모리에 가지고 있는 값이 높아짐으로 위험
         * **/
        return new StepBuilder("collection2SystemFailureStep", jobRepository)
                .<SystemFailure, SystemFailure>chunk(10, transactionManager)
                .reader(systemFailureItemReader)
                .writer(systemFailureStdoutItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<SystemFailure> systemFailureItemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile)
    {
        return new FlatFileItemReaderBuilder<SystemFailure>()
                // FlatFileItemReader 를 식별하기 위한 고유 이름을 부여
                .name("systemFailureItemReader")
                // 읽어 들일 Resource 지정
                .resource(new FileSystemResource(inputFile))
                // 구분자로 데이터를 토큰화할때 사용할 클래스 지정 -> DelimitedLineTokenizer 를 지정 (읽어들일 파일이 구분자로 분리된 형식임을 설정)
                .delimited()
                // 어떤 문자로 구분 할지 설정 -> DelimitedLineTokenizer의 기본 구분자가 쉼표이기 때문에 생략 가능 하지만 명시적 선언으로 선언
                .delimiter(",")
                // FieldSet의 names 필드에 사용할 객체의 프로퍼티 이름 전달
                // 데이터의 각 토큰과 순서대로 1:1 매핑
                // ex) CSV 파일 한 줄의 첫번째 토큰은 "errorId" 프로퍼티에, 두번째 토큰은 "errorDateTime" 프로퍼티에 매핑된다.
                .names("errorId", "errorDateTime", "severity", "processId", "errorMessage")
                // BeanWrapperFieldSetMapper에서 FieldSet을 객체로 매핑할 대상 도메인 클래스를 지정
                // 런타임 객체 생성에 필요한 실제 클레스 정보를 제공, Java의 타입 소거로 인해 제네릭 타입 정보는 런타임에 사라지므로 BeanWrapperFieldSetMapper가 새로운 인스턴스를 생성하기 위해서는 필수 설정
                .targetType(SystemFailure.class)
                // 대부분 CSV파일에 첫번째 라인은 헤더용도임으로 (이름) 스킵 가능
                .linesToSkip(1)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<SystemFailure> systemFailureItemReaderFixedColumn(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        // 고정문자열 파일을 공백으로 읽어 드리는 방법
        // ERR001  2024-01-19 10:15:23  CRITICAL  1234  SYSTEM  CRASH DETECT \n
        // ERR002  2024-01-19 10:15:25  FATAL     1235  MEMORY  OVERFLOW FAIL\n
        return new FlatFileItemReaderBuilder<SystemFailure>()
                .name("systemFailureItemReader")
                .resource(new FileSystemResource(inputFile))
                // FlatFileItemReader에게 읽어들일 파일이 고정 길이 형식임을 알리는 설정
                .fixedLength()
                .columns(new Range[]{
                        new Range(1, 8),     // errorId: ERR001 + 공백 2칸
                        new Range(9, 29),    // errorDateTime: 날짜시간 + 공백 2칸
                        new Range(30, 39),   // severity: CRITICAL/FATAL + 패딩
                        new Range(40, 45),   // processId: 1234 + 공백 2칸
                        new Range(46, 66)    // errorMessage: 메시지 + \n
                })
                .names("errorId", "errorDateTime", "severity", "processId", "errorMessage")
                // 이 파일에서 읽은 날짜 문자열이 자동으로 LocalDateTime 객체로 변환된다.
                .customEditors(Map.of(LocalDateTime.class, dateTimeEditor()))
                .targetType(SystemFailure.class)
                .build();
    }

    @Bean
    public SystemFailureStdoutItemWriter systemFailureStdoutItemWriter() {
        return new SystemFailureStdoutItemWriter();
    }

    public static class SystemFailureStdoutItemWriter implements ItemWriter<SystemFailure> {
        @Override
        public void write(Chunk<? extends SystemFailure> chunk) throws Exception {
            for (SystemFailure failure : chunk) {
                log.info("Processing system failure: {}", failure);
            }
        }
    }

    @Data
    public static class SystemFailure {
        private String errorId;
        private String errorDateTime;
        private String severity;
        private Integer processId;
        private String errorMessage;
    }

    private PropertyEditor dateTimeEditor() {
        return new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                setValue(LocalDateTime.parse(text, formatter));
            }
        };
    }
}
