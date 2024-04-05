package com.example.drainjava.builtins.drain;


/**
 * 추상 클래스
 *
 * @author gunha
 * @version 0.1
 * @since 2024-04-02 오후 5:09
 */
public abstract class Profiler {

    /**
     * 섹션 시작
     *
     * @param sectionName 섹션 이름
     */
    public abstract void startSection(String sectionName);

    /**
     * 섹션 종료
     *
     * @param sectionName 섹션 이름
     */
    public abstract void endSection(String sectionName);

    /**
     * 주기적 보고
     *
     * @param periodSec 주기 (초 단위)
     */
    public abstract void report(int periodSec);
}