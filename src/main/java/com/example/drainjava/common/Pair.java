package com.example.drainjava.common;

import lombok.Getter;

/**
 * Custom Pair <br>
 *  : 두 값 저장 및 접근
 *
 * @author gunha
 * @version 0.1
 * @since 2024-04-05 오후 4:09
 */
@Getter
public class Pair<A, B> {

    /** 첫번째 요소 */
    private A first;
    /** 두번째 요소 */
    private B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }
}
