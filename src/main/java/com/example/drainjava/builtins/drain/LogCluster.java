package com.example.drainjava.builtins.drain;

import java.util.ArrayList;
import java.util.List;

/**
 * 로그 클러스터
 *
 * @author gunha
 * @version 0.1
 * @since 2024-04-01 오전 10:01
 */
public class LogCluster {

    /** 로그 템플릿 토큰 */
    private final List<String> logTemplateTokens;
    /** 클러스터 ID */
    private final int clusterId;
    /** 크기 */
    private int size;

    public LogCluster(List<String> logTemplateTokens, int clusterId) {
        this.logTemplateTokens = new ArrayList<>(logTemplateTokens);
        this.clusterId = clusterId;
        this.size = 1;
    }

    public String getTemplate() {
        return String.join(" ", logTemplateTokens);
    }

    @Override
    public String toString() {
        return String.format("ID=%-5d : size=%-10d: %s", clusterId, size, getTemplate());
    }
}
