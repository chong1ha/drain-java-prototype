package com.example.drainjava.builtins.drain;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    @SerializedName("log_cluster_depth")
    protected int depth = 4;

    /** 내부노드 수 */
    @SerializedName("max_node_depth")
    protected int maxNodeDepth = depth - 2;

    /** 유사도 임계값, 이 값보다 낮으면 새로운 로그 클러스터 생성 */
    @SerializedName("sim_th")
    protected double simTh = 0.3;

    /** 각 내부 노드가 가질 수 있는 최대 자식 수 */
    @SerializedName("max_children")
    protected int maxChildren = 100;

    /** 추적할 최대클러스터 수 (이 값에 도달하면 old cluster -> new cluster 교체, LRU 캐시 방침에 의거) */
    @SerializedName("max_clusters")
    protected Integer maxClusters = null;

    /** 로그 메시지를 단어로 분할할 때, 적용되는 구분 기호 (공백 외에 추가 구분 기호가 필요한 경우 사용) */
    @SerializedName("extra_delimiters")
    protected List<String> extraDelimiters = new ArrayList<>();

    /** 템플릿 내 식별된 매개변수의 wrapping */
    @SerializedName("param_str")
    protected String paramStr = "<*>";

    /** 적어도 하나의 숫자를 포함하는 토큰을 템플릿 매개변수로 취급할지 여부 */
    @SerializedName("parametrize_numeric_tokens")
    protected boolean parametrizeNumericTokens = true;

    /** (key: String, value: LogCluster) */
    @SerializedName("id_to_cluster")
    protected LogClusterCache idToCluster;

    /**  */
    @SerializedName("clusters_counter")
    protected int clustersCounter;

    /**  */
    @SerializedName("root_node")
    protected Node rootNode;

    /**  */
    @SerializedName("profiler")
    protected NullProfiler profiler;


    public DrainBase() {

        // depth 최소조건
        if (depth < 3) {
            throw new IllegalArgumentException("depth argument must be at least 3");
        }

        // LogClusterCache 초기화
        if (maxClusters != null) {
            this.idToCluster = new LogClusterCache(maxClusters);
        } else {
            this.idToCluster = new LogClusterCache(0);
        }
        this.clustersCounter = 0;
    }

    /**
     * 현재 캐시에 저장된 모든 로그 클러스터를 반환
     *
     * @return cache
     */
    public Map<String, LogCluster> getClusters() {
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

