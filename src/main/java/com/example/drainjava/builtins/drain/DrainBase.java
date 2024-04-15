package com.example.drainjava.builtins.drain;

import com.example.drainjava.common.Pair;
import com.example.drainjava.common.util.StringUtil;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
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

    /** 노드 트리 */
    @SerializedName("root_node")
    protected Node rootNode = new Node();

    /** 실행시간 측정 (프로파일러 설정) */
    @SerializedName("profiler")
    protected Profiler profiler = new NullProfiler();


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
     * @param str 확인할 문자열
     * @return 문자열에 숫자가 포함되어있으면 true
     */
    public boolean hasNumbers(String str) {

        // 예외처리
        if (StringUtil.isEmpty(str)) {
            throw new IllegalArgumentException("Str is empty");
        }

        for (char c : str.toCharArray()) {

            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fast Match 수행 <br>
     *  : 로그 메시지(토큰으로 표현)와 클러스터 목록 간의 가장 적합한 매치 찾기
     *
     * @param clusterIds 매치할 클러스터 ID 목록
     * @param tokens 로그 메시지 토큰 목록
     * @param simTh 유사도 임계값
     * @param includeParams 유사도 임계값에 와일드카드 매개변수를 포함할지 여부
     * @return 가장 적합한 매치 클러스터, 매치되는 클러스터가 없는 경우 null
     */
    public LogCluster fastMatch(List<String> clusterIds, List<String> tokens, double simTh, boolean includeParams) {

        // 일치하는 클러스터 초기화
        LogCluster matchCluster = null;

        double maxSim = -1;
        int maxParamCount = -1;
        LogCluster maxCluster = null;

        // 클러스터 목록 순회
        for (String clusterId : clusterIds) {
            LogCluster cluster = idToCluster.get(String.valueOf(clusterId));

            if (cluster == null) {
                continue;
            }

            double curSim = 0;
            int paramCount;

            // 클러스터와 로그 메시지 간의 유사도 및 매개변수 수 계산
            Pair<Double, Integer> result = getSeqDistance(cluster.getLogTemplateTokens(), tokens, includeParams);
            curSim = result.getFirst();
            paramCount = result.getSecond();

            // 최대 유사도 클러스터 업데이트
            if (curSim > maxSim || (curSim == maxSim && paramCount > maxParamCount)) {
                maxSim = curSim;
                maxParamCount = paramCount;
                maxCluster = cluster;
            }
        }

        // 유사도 임계값 이상이면, 일치하는 클러스터로 설정
        if (maxSim >= simTh) {
            matchCluster = maxCluster;
        }

        return matchCluster;
    }

    /**
     * 두 시퀀스 간의 유사도 계산
     *
     * @param seq1 기존 로그 템플릿 (토큰)
     * @param seq2 들어오는 시퀀스 로그 메시지 (토큰)
     * @param includeParams 매개변수를 포함하여 유사도를 계산할지 여부 (디폴트 True)
     * @return
     */
    public Pair<Double, Integer> getSeqDistance(List<String> seq1, List<String> seq2, boolean includeParams) {

        int simTokens = 0;
        int paramCount = 0;

        // 유사도 계산
       for (int i = 0; i < seq1.size(); i++) {
            String token1 = seq1.get(i);
            String token2 = seq2.get(i);

            // 매개변수인 경우,
            if (token1.equals(this.paramStr)) {
                paramCount++;
                continue;
            }

            // 토큰이 동일한 경우, 토큰 카운트 증가
            if (token1.equals(token2)) {
                simTokens++;
            }
        }

       // 매개변수 포함하여 유사도 계산 여부 확인
        if (includeParams) {
            simTokens += paramCount;
        }

        // 시퀀스 길이에 대한 유사도 계산
        double retVal = (double) simTokens / seq1.size();

        return new Pair<>(retVal, paramCount);
    }

    /**
     * 주어진 로그 메시지를 토큰으로 분할 <br>
     * extraDelimiters 사용 시, 추가 토큰 분할 수행
     *
     * @param content 로그 메시지
     * @return 토큰으로 분할된 로그 메시지
     */
    public List<String> getContentAsTokens(String content) {

        // 예외처리
        if (StringUtil.isEmpty(content)) {
            throw new IllegalArgumentException("Content is empty");
        }

        // 앞뒤 공백 제거
        content = content.trim();

        // 추가 구분 기호가 있는 경우, 각 구분 기호를 공백으로 대체
        for (String delimiter : extraDelimiters) {
            content = content.replace(delimiter, " ");
        }

        // 공백을 기준으로 분할하여 토큰화
        String[] contentTokensArray = content.split("\\s+");

        return new ArrayList<>(Arrays.asList(contentTokensArray));
    }

    /**
     * 트리에서 주어진 첫번째 토큰과 일치하는 노드 찾고, <br>
     * 해당 노드의 클러스터 ID를 가져와 목록에 추가
     *
     * @param seqFirst 시퀀스의 첫번째 토큰
     * @return List<String> 지정된 토큰 수를 가진 모든 클러스터 반환
     */
    public List<String> getClustersIdsForSeqlen(int seqFirst) {

        List<String> target = new ArrayList<>();

        // 현재 노드 get
        Node curNode = this.rootNode.getKeyToChildNode().get(String.valueOf(seqFirst));

        // 같은 토큰 수를 가진 템플릿이 없을 때
        if (curNode == null) {
            return target;
        }

        // 클러스터 ID 추가
        appendClustersRecursive(curNode, target);
        return target;
    }

    /**
     * 노드의 클러스터 ID를 추가 <br>
     *  : 재귀호출
     *
     * @param node 현재 노드
     * @param idListToFill 채워질 클러스터 ID 목록
     */
    private void appendClustersRecursive(Node node, List<String> idListToFill) {

        // 현재 노드의 클러스터 ID 추가
        for (String clusterId : node.getClusterIds()) {
            idListToFill.add(String.valueOf(clusterId));
        }

        // 재귀 호출
        for (Node childNode : node.getKeyToChildNode().values()) {
            appendClustersRecursive(childNode, idListToFill);
        }
    }
}

