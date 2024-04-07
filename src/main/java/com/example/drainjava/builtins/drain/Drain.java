package com.example.drainjava.builtins.drain;

import com.example.drainjava.builtins.FullSearchStrategy;
import com.example.drainjava.common.util.EnumUtil;
import com.example.drainjava.common.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Drain
 *
 * @author gunha
 * @version 0.1
 * @since 2024-04-01 오전 8:57
 */
@Component
public class Drain extends DrainBase {

    /**
     * 트리를 검색하여, 로그 토큰과 일치하는 클러스터 반환
     *
     * @param rootNode 트리의 루트 노드
     * @param tokens 토큰화된 로그메시지
     * @param simTh 유사도 임계값
     * @param includeParams 매개변수를 포함할지 여부
     * @return LogCluster 일치하는 클러스터
     */
    public LogCluster treeSearch(Node rootNode, List<String> tokens, double simTh, boolean includeParams) {

        // 로그 토큰 갯수
        int tokenCount = tokens.size();
        // 현재 노드
        Node curNode = rootNode.getKeyToChildNode().get(String.valueOf(tokenCount));

        if (curNode == null) {
            return null;
        }

        // 토큰이 없으면, 해당 그룹의 단일 클러스터 반환
        if (tokenCount == 0) {
            return idToCluster.get(String.valueOf(curNode.getClusterIds().get(0)));
        }

        // 이 로그에 대한 leaf 노드를 찾음 - 첫 N 토큰과 일치하는 노드 경로 (N=트리 깊이)를 따름
        int curNodeDepth = 1;
        for (String token : tokens) {
            // 최대 깊이에 도달 시, 종료
            if (curNodeDepth >= maxNodeDepth) {
                break;
            }

            // 마지막 토큰에 도달 시, 종료
            if (curNodeDepth == tokenCount) {
                break;
            }

            Map<String, Node> keyToChildNode = curNode.getKeyToChildNode();
            curNode = keyToChildNode.get(token);

            // 정확한 다음 토큰이 없는 경우, 와일드카드 노드 시도
            if (curNode == null) {
                curNode = keyToChildNode.get(paramStr);
            }

            // 와일드카드 노드도 없는 경우, 중단
            if (curNode == null) {
                return null;
            }

            curNodeDepth++;
        }

        // 유사도 기준, 가장 적합한 클러스터 찾기
        LogCluster cluster = fastMatch(curNode.getClusterIds(), tokens, simTh, includeParams);

        return cluster;
    }

    /**
     * 기존 클러스터와 로그 메시지를 매칭 <br>
     *  : 새 클러스터 생성 및 수정 X
     *
     *  <ol>
     *     <li> Tree Search </li>
     *     <li> Fast Match </li>
     *     <li> get_seq_distance </li>
     *  </ol>
     *
     * @param content 매치할 로그 메시지 (내용 부분)
     * @param fullSearchStrategy  전체 클러스터 검색을 언제 수행할지
     * @return LogCluster
     */
    public LogCluster match(String content, String fullSearchStrategy) {

        // 예외처리
        if (StringUtil.isEmpty(content)) {
            throw new IllegalArgumentException("Content is empty");
        }

        // 예외처리
        if (fullSearchStrategy == null || EnumUtil.isValid(FullSearchStrategy.class, fullSearchStrategy.toUpperCase()) == false) {
            throw new IllegalArgumentException("Invalid fullSearchStrategy provided: " + fullSearchStrategy);
        }

        double requiredSimTh = 1.0;
        // 로그메시지 -> 토큰화
        List<String> contentTokens = getContentAsTokens(content);

        // always 처리
        if (FullSearchStrategy.valueOf(fullSearchStrategy.toUpperCase()) == FullSearchStrategy.ALWAYS) {
            return fullSearch(contentTokens);
        }

        // tree search 수행
        LogCluster matchCluster = this.treeSearch(rootNode, contentTokens, requiredSimTh, true);
        if (matchCluster != null) {
            return matchCluster;
        }

        // never 처리
        if (FullSearchStrategy.valueOf(fullSearchStrategy.toUpperCase()) == FullSearchStrategy.NEVER) {
            return null;
        }

        return fullSearch(contentTokens);
    }

    /**
     *
     *
     * @return LogCluster
     */
    private LogCluster fullSearch(List<String> contentTokens) {
        List<String> allIds = getClustersIdsForSeqlen(contentTokens.size());
        LogCluster cluster = fastMatch(allIds, contentTokens, simTh, isParametrizeNumericTokens());
        return cluster;
    }
}
