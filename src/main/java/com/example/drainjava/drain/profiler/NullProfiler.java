package com.example.drainjava.drain.profiler;

import org.springframework.stereotype.Component;

/**
 * 프로파일링 비활성화
 *
 * @author gunha
 * @version 0.1
 * @since 2024-04-02 오후 5:35
 */
@Component
public class NullProfiler extends Profiler {

    /**
     * 섹션 시작 (비활성화)
     *
     * @param sectionName 섹션 이름
     */
    @Override
    public void startSection(String sectionName) {}

    /**
     * 섹션 종료 (비활성화)
     *
     * @param sectionName 섹션 이름
     */
    @Override
    public void endSection(String sectionName) {}

    /**
     * 주기적 보고 (비활성화)
     */
    @Override
    public void report() {}

    /**
     * 지정된 메소드에 대한 프로파일링 수행 (비활성화)
     *
     * @param sectionName 섹션 이름
     * @param method 실행할 메소드
     */
    @Override
    public void executeWithProfiling(String sectionName, Runnable method) {}
}
