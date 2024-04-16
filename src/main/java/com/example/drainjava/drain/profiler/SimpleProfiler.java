package com.example.drainjava.drain.profiler;

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

    private Map<String, Long> cumulativeTimes = new HashMap<>();


    private Map<String, Integer> callCount = new HashMap<>();

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

        // 메소드 개별 시간 저장
        elapsedTimes.put(sectionName, elapsedTime);
        // 섹션별, 누적 시간 저장 (동일 섹션)
        cumulativeTimes.put(sectionName, cumulativeTimes.getOrDefault(sectionName, 0L) + elapsedTime);
        // 섹션별, 누적 카운트
        callCount.put(sectionName, callCount.getOrDefault(sectionName, 0) + 1);
        // 시작시간 초기화
        startTimes.remove(sectionName);
    }

    /**
     * 주기적 보고 (Printer)
     */
    @Override
    public void report() {
        for (Map.Entry<String, Long> entry : elapsedTimes.entrySet()) {
            // 개별 (나노초 --> 초 변환)
            String sectionName = entry.getKey();
            long elapsedTimeInNano = entry.getValue();
            double elapsedTimeInSec = (double) elapsedTimeInNano / 1000000000.0;

            // 누적 (나노초 --> 초 변환)
            long cumulativeTimeInNano = cumulativeTimes.getOrDefault(sectionName, 0L);
            double cumulativeTimeInSec = (double) cumulativeTimeInNano / 1000000000.0;

            // 메시지 갯수 카운트
            int count = callCount.getOrDefault(sectionName, 0);
            // 1건당 처리
            double averageTimePerCall = cumulativeTimeInSec / count;
            System.out.printf("Section '%s' took %.8f seconds (Total: %.8f seconds, CallCount: %d, Average Time Per Call: %.8f seconds)%n",
                    sectionName, elapsedTimeInSec, cumulativeTimeInSec, count, averageTimePerCall);

        }
    }

    /**
     * 지정된 메소드에 대한 프로파일링 수행
     *
     * @param sectionName 섹션 이름
     * @param method 실행할 메소드
     */
    @Override
    public void executeWithProfiling(String sectionName, Runnable method) {
        startSection(sectionName);
        method.run();
        endSection(sectionName);
    }
}
