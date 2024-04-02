package com.example.drainjava.builtins.drain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Drain Baseline
 *
 * @author gunha
 * @version 0.1
 * @since 2024-04-01 오전 8:56
 */
@Getter
@Setter
public abstract class DrainBase {

    /** 파싱트리의 최대 깊이 (최소값 3) */
    protected int depth = 4;
    /** 유사도 임계값 */
    protected double simTh = 0.3;
    /** 각 내부 노드가 가질 수 있는 최대 자식 수 */
    protected int maxChildren = 100;
    /** 추적할 최대클러스터 수 (이 값에 도달하면 old cluster -> new cluster 교체, LRU 캐시 방침에 의거) */
    protected Integer maxClusters = null;
    /** 로그 메시지를 단어로 분할할 때, 적용되는 구분 기호 (공백 외에 추가 구분 기호가 필요한 경우 사용) */
    protected List<Character> extraDelimiters = new ArrayList<>();
    /**  */
    protected String paramStr = "<*>";
    /** 적어도 하나의 숫자를 포함하는 토큰을 템플릿 매개변수로 취급할지 여부 */
    protected boolean parametrizeNumericTokens = true;
    /** key: int, value: LogCluster */
    protected LogClusterCache idToCluster;
    protected int clustersCounter;

    public DrainBase() {
        // depth 최소조건
        if (depth < 3) {
            throw new IllegalArgumentException("depth argument must be at least 3");
        }

        if (maxClusters != null) {
            this.idToCluster = new LogClusterCache(maxClusters);
        } else {
            this.idToCluster = new LogClusterCache(0);
        }
        this.clustersCounter = 0;
    }

    public Collection<LogCluster> getClusters() {
        return idToCluster.getClusters();
    }

    /**
     * 문자열이 숫자를 포함하는지 여부
     *
     * @param s 확인할 문자열
     * @return 문자열에 숫자가 포함되어있으면 true
     */
    public boolean hasNumbers(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }
}

