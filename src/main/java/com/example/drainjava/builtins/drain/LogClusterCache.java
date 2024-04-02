package com.example.drainjava.builtins.drain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU Cache (Least Recently Used) 를 통해 로그 클러스터 관리
 *
 * @author gunha
 * @version 0.1
 * @since 2024-04-01 오전 10:01
 */
public class LogClusterCache {

    /** 캐시 Map */
    private final Map<Integer, LogCluster> map;
    /** 캐시의 최대용량 */
    private final int capacity;

    /**
     * 생성자
     *
     * @param capacity 캐시의 최대 용량
     */
    public LogClusterCache(int capacity) {
        // (초기용량, 로드팩터, 최근 접근 순서로 정렬 여부) 설정
        map = new LinkedHashMap<>(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    /**
     * 특정 키에 대한 값 반환
     *
     * @param key 조회할 키
     * @return 캐시에서 항목을 찾을 수 없을 때, -1 반환 (missing)
     */
    public LogCluster get(int key) {
        return map.getOrDefault(key, null);
    }

    /**
     * key-value 추가
     *
     * @param key 캐시에 추가할 키
     * @param value 캐시에 추가할 값
     */
    public void put(int key, LogCluster value) {
        map.put(key, value);

        // 캐시가 용량 초과하면, 가장 오래된 항목 제거
        if (map.size() > capacity) {
            int leastUsedKey = map.keySet().iterator().next();
            map.remove(leastUsedKey);
        }
    }

    /**
     *
     *
     * @return
     */
    public Collection<LogCluster> getClusters() {
        return map.values();
    }
}
