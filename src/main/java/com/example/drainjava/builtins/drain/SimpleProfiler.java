package com.example.drainjava.builtins.drain;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 메소드 경과시간 측정
 *
 * @author kang-geonha
 * @version 0.1
 * @since 2024/04/07 9:50 PM
 */
@Component
public class SimpleProfiler extends Profiler {

    /** 섹션별, 시작 시간 저장 */
    private Map<String, Long> startTimes = new HashMap<>();

    /** 섹션별, 경과 시간 저장 */
    private Map<String, Long> elapsedTimes = new HashMap<>();

    /**
     * 섹션 시작 <br>
     *  : 나노초 단위 사용
     *
     * @param sectionName 섹션 이름
     */
    @Override
    public void startSection(String sectionName) {
        startTimes.put(sectionName, System.nanoTime());
    }

    /**
     * 섹션 종료
     *
     * @param sectionName 섹션 이름
     */
    @Override
    public void endSection(String sectionName) {
        long startTime = startTimes.getOrDefault(sectionName, 0L);
        long endTime = System.nanoTime();

        // 경과 시간 측정
        long elapsedTime = endTime - startTime;

        // 섹션별, 누적 시간 저장 (동일 섹션)
        elapsedTimes.put(sectionName, elapsedTimes.getOrDefault(sectionName, 0L) + elapsedTime);
    }

    /**
     * 주기적 보고 (Printer)
     */
    @Override
    public void report() {
        for (Map.Entry<String, Long> entry : elapsedTimes.entrySet()) {
            String sectionName = entry.getKey();
            long elapsedTimeInNano = entry.getValue();
            // 나노초 --> 초 변환
            double elapsedTimeInSec = (double) elapsedTimeInNano / 1000000000.0;
            System.out.printf("Section '%s' took %.4f seconds.%n", sectionName, elapsedTimeInSec);
        }
    }

    /**
     * 지정된 메소드에 대한 프로파일링 수행
     *
     * @param sectionName 섹션 이름
     * @param method 실행할 메소드
     */
    public void executeWithProfiling(String sectionName, Runnable method) {
        startSection(sectionName);
        method.run();
        endSection(sectionName);
    }
}
