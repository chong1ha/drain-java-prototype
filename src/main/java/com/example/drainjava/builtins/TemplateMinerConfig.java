package com.example.drainjava.builtins;


import com.example.drainjava.common.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TemplateMiner config 정보
 *
 * @author gunha
 * @version 0.1
 * @since 2024-03-28 오후 5:56
 */
@Getter
@Setter
@Component
public class TemplateMinerConfig {

    /** Drain 또는 Jaccard Drain 선택 */
    private String engine = EngineType.DRAIN.toString();

    /** 프로파일링 활성화 여부 */
    private boolean profilingEnabled = false;
    /** 프로파일링 데이터 보고간격 (초 단위) */
    private int profilingReportInterval = 60;

    /** 현재 상태, 자동 저장 간격 (분 단위) */
    private int snapshotInterval = 5;
    /** 상태를 로드/저장하기 전, 압축 여부 */
    private boolean snapshotCompressState = true;

    /** 유사도 임계값, 이 값보다 낮으면 새로운 로그 클러스터 생성 */
    private double simTh = 0.4;
    /** 파싱트리의 최대 깊이 (최소값 3) */
    private int depth = 4;
    /** 각 내부 노드가 가질 수 있는 최대 자식 수 */
    private int maxChildren = 100;
    /** 추적할 최대클러스터 수 (이 값에 도달하면 old cluster -> new cluster 교체, LRU 캐시 방침에 의거) */
    private Integer maxClusters = null;
    /** 로그 메시지를 단어로 분할할 때, 적용되는 구분 기호 (공백 외에 추가 구분 기호가 필요한 경우 사용) */
    private List<String> extraDelimiters = new ArrayList<>();

    /** 템플릿 내 식별된 매개변수의 wrapping, 접두사 */
    private String maskPrefix = "<";
    /** 템플릿 내 식별된 매개변수의 wrapping, 접미사 */
    private String maskSuffix = ">";
    /** 적어도 하나의 숫자를 포함하는 토큰을 템플릿 매개변수로 취급할지 여부 */
    private boolean parameterizeNumericTokens = true;


    /**
     * 구성파일(.ini)의 설정값 로드 및 객체에 할당
     *
     * @param configFilePath 구성파일의 파일경로
     * @throws
     */
    public void load(String configFilePath) throws IOException {

        // 파라미터 체크 (빈 값)
        if (StringUtil.isEmpty(configFilePath) == true) {
            throw new IllegalArgumentException("configFilePath is empty");
        }

        // 구성파일 로드
        Ini ini = new Ini(new File(configFilePath));

        // 섹션 별, key-value 처리
        Profile.Section profilingSection = ini.get("PROFILING");
        if (profilingSection != null) {
            this.profilingEnabled = Boolean.parseBoolean(profilingSection.get("enabled"));
            this.profilingReportInterval = Integer.parseInt(profilingSection.get("report_sec"));
        }

        Profile.Section snapshotSection = ini.get("SNAPSHOT");
        if (snapshotSection != null) {
            this.snapshotInterval = Integer.parseInt(snapshotSection.get("snapshot_interval_minutes"));
            this.snapshotCompressState = Boolean.parseBoolean(snapshotSection.get("compress_state"));
        }

        Profile.Section drainSection = ini.get("DRAIN");
        if (drainSection != null) {
            this.simTh = Double.parseDouble(drainSection.get("sim_th"));
            this.depth = Integer.parseInt(drainSection.get("depth"));
            this.maxChildren = Integer.parseInt(drainSection.get("max_children"));
            this.maxClusters = Integer.parseInt(drainSection.get("max_clusters"));

            String extraDelimitersStr = drainSection.get("extra_delimiters");
            if (extraDelimitersStr != null && !extraDelimitersStr.isEmpty()) {
                // 설정 파일에서 extra_delimiters 값은 쉼표로 구분된 문자열일 것으로 가정
                String[] extraDelimitersArr = extraDelimitersStr.split(",");
                this.extraDelimiters.addAll(Arrays.asList(extraDelimitersArr));
            }
        }

        Profile.Section maskingSection = ini.get("MASKING");
        if (maskingSection != null) {
            this.maskPrefix = maskingSection.get("mask_prefix");
            this.maskSuffix = maskingSection.get("mask_suffix");
        }
    }
}
