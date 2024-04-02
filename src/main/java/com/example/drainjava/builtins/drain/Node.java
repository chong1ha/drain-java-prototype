package com.example.drainjava.builtins.drain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 트리 노드 <br>
 * 각 노드: (자식노드+클러스터 ID 목록) 포함
 *
 * @author gunha
 * @version 0.1
 * @since 2024-04-01 오전 10:33
 */
public class Node {

    /** 자식 노드를 키로 매핑 */
    private final Map<String, Node> keyToChildNode;

    /** clusterId 리스트 */
    private final List<Integer> clusterIds;

    public Node() {
        this.keyToChildNode = new HashMap<>();
        this.clusterIds = new ArrayList<>();
    }
}
