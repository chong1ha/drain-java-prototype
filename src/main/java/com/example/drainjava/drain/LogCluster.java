package com.example.drainjava.drain;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 로그 클러스터
 *
 * @author gunha
 * @version 0.1
 * @since 2024-04-01 오전 10:01
 */
@Getter
public class LogCluster {

    /** 로그 템플릿 토큰 */
    @SerializedName("log_template_tokens")
    private List<String> logTemplateTokens;

    /** 클러스터 ID, 로그가 속한 클러스터의 순차적인 식별자 */
    @SerializedName("cluster_id")
    private int clusterId;

    /** 로그가 속한 클러스터의 크기 (메시지 수) */
    @SerializedName("size")
    private int size;

    public LogCluster(List<String> logTemplateTokens, int clusterId) {
        this.logTemplateTokens = new ArrayList<>(logTemplateTokens);
        this.clusterId = clusterId;
        this.size = 1;
    }

    /**
     * 현재 로그 클러스터의 로그 템플릿 반환
     *
     * @return 로그템플릿
     */
    public String getTemplate() {

        StringBuilder templateBuilder = new StringBuilder();

        for (String token : logTemplateTokens) {
            templateBuilder.append(token).append(" ");
        }
        return templateBuilder.toString().trim();
    }

    /**
     * 객체 문자열 표현 반환 <br>
     *
     * 포맷 <br>
     * (ID=클러스터 ID : size=크기: 템플릿)
     *
     * @return
     */
    @Override
    public String toString() {
        return String.format("ID=%-5d : size=%-10d: %s", clusterId, size, getTemplate());
    }
}
