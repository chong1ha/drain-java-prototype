package com.example.drainjava.builtins.drain;

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
     *
     * @param periodSec 주기 (초 단위)
     */
    @Override
    public void report(int periodSec) {}
}
