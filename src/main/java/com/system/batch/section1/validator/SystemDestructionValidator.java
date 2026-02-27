package com.system.batch.section1.validator;

import io.micrometer.common.lang.Nullable;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SystemDestructionValidator implements JobParametersValidator {

    // JobParametersValidator를 사용하면 잘못된 파라미터가 들어오는 순간 즉시 차단 할 수 있다.
    @Override
    public void validate(@Nullable JobParameters parameters) throws JobParametersInvalidException {
        if (parameters == null) {
            throw new JobParametersInvalidException("파라미터가 NULL입니다");
        }

        Long destructionPower = parameters.getLong("destructionPower");
        if (destructionPower == null) {
            throw new JobParametersInvalidException("destructionPower 파라미터는 필수값입니다");
        }

        if (destructionPower > 9) {
            throw new JobParametersInvalidException(
                    "파괴력 수준이 허용치를 초과했습니다: " + destructionPower + " (최대 허용치: 9)");
        }
    }
/*
    @Bean
    public Job systemDestructionJob(
            JobRepository jobRepository,
            Step systemDestructionStep,
            SystemDestructionValidator validator
    ) {
        return new JobBuilder("systemDestructionJob", jobRepository)
                .validator(validator)
                .start(systemDestructionStep)
                .build();
    }*/
}
