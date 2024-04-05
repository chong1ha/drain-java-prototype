package com.example.drainjava.builtins;

/**
 * 클러스터 매칭 전략 <br>
 *
 * <ol>
 *     <li>never: 항상 트리 검색 수행, 가장 빠르지만 가끔 불일치 유발 </li>
 *     <li>fallback: 우선 트리 검색 시도, 트리 검색에서 일치를 찾지 못하는 경우에만 선형 검색 수행 </li>
 *     <li>always: 모든 알려진 클러스터를 평가하여 가장 적합한 매치를 찾음, 가장 정확하나 가장 느림 </li>
 * </ol>
 *
 * @author gunha
 * @version 0.1
 * @since 2024-03-29 오후 1:00
 */
public enum FullSearchStrategy {
    NEVER,
    FALLBACK,
    ALWAYS
}
