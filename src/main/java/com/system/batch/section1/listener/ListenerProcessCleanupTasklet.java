package com.system.batch.section1.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
public class ListenerProcessCleanupTasklet implements Tasklet {

    private final int processesToKill = 10;
    private int killedProcesses = 0;

    /**
     * return 값이 RepeatStatus 객체인데, True / False로 반복 여부를 결정하고있다.
     * Spring Batch Step이 Tasklet의 execute() 메서드 실행을 계속 할지 멈출지를 결정하는 기준
     * - True : 반복
     * - False : 중지
     *
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        killedProcesses++;

        log.info("☠️  프로세스 강제 종료... ({}/{})", killedProcesses, processesToKill);

        if (killedProcesses >= processesToKill) {
            log.info("💀 시스템 안정화 완료. 모든 좀비 프로세스 제거.");
            return RepeatStatus.FINISHED; // 다 끝났다. 이제 Step을 종료해도 된다. => 다음 스텝으로 진행 됨
        }

        return RepeatStatus.CONTINUABLE; // 작업 진행 중. 추가 실행이 필요하다. => 추가로 더 실행되어야 함을 Spring Batch Step에 알리는 신호
    }
}
