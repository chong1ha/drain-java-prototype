package com.example.drainjava;

import com.example.drainjava.drain.AbstractPersistenceHandler;
import com.example.drainjava.drain.Drain;
import com.example.drainjava.drain.LogCluster;
import com.example.drainjava.common.util.StringUtil;
import com.example.drainjava.drain.config.EngineType;
import com.google.gson.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

/**
 * Drain Engine Wrapper
 *
 * @author gunha
 * @version 0.1
 * @since 2024-03-28 오후 5:56
 */
@Log4j2
@Component
public class TemplateMiner {

    private final AbstractPersistenceHandler abstractPersistenceHandler;
    private final TemplateMinerConfig templateMinerConfig;
    private final Drain drain;

    @Autowired
    public TemplateMiner(AbstractPersistenceHandler abstractPersistenceHandler, TemplateMinerConfig templateMinerConfig, Drain drain) {
        this.abstractPersistenceHandler = abstractPersistenceHandler;
        this.templateMinerConfig = templateMinerConfig;
        this.drain = drain;
    }

    /**
     * 초기화
     *
     * @param configFilePath 스냅샷 바이너리 파일 경로
     */
    public void init(String configFilePath) throws IOException {

        try {
            // TemplateMinerConfig 초기화
            templateMinerConfig.load(configFilePath);
        } catch (IOException e) {
            log.error("Config file not found: drain3.ini", e);
        }

        // 엔진값 유효성 검사 (Drain 또는 JaccardDrain)
        if (EngineType.isValid(templateMinerConfig.getEngine()) == false) {
            throw new IllegalArgumentException("Invalid engine: must be either 'Drain' or 'JaccardDrain'");
        }

        // Drain 설정값 세팅
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(templateMinerConfig.getMaskPrefix());
        stringBuilder.append("*");
        stringBuilder.append(templateMinerConfig.getMaskSuffix());
        String param_str = stringBuilder.toString();

        drain.setDepth(templateMinerConfig.getDepth());
        drain.setMaxNodeDepth(templateMinerConfig.getDepth() - 2);
        drain.setSimTh(templateMinerConfig.getSimTh());
        drain.setMaxChildren(templateMinerConfig.getMaxChildren());
        drain.setMaxClusters(templateMinerConfig.getMaxClusters());
        drain.setExtraDelimiters(templateMinerConfig.getExtraDelimiters());
        drain.setParamStr(param_str);
        drain.setParametrizeNumericTokens(templateMinerConfig.isParameterizeNumericTokens());
    }

    /**
     * 이전에 저장된 상태 로드
     *
     * @throws IOException
     */
    public void loadState() throws IOException {

        try {
            // 상태 로드
            byte[] state = abstractPersistenceHandler.loadState();

            // 예외처리
            if (state == null) {
                return;
            }

            // 상태정보, Base64 디코딩 및 압축 해제
            if (templateMinerConfig.isSnapshotCompressState() == true) {
                state = decompressState(state);
            }

            // JSON 형식의 상태를 Drain 객체로 파싱
            Drain loadedDrain = parseJsonState(state);
            if (loadedDrain == null) {
                log.warn("Failed to parse loaded state. State may be corrupted.");
            }

            System.out.println("Log Template:");
            loadedDrain.getClusters().forEach((key, value) -> System.out.println(key + ": " + value));

            // loadedDrain 필드들을 drain 필드들로 설정
            drain.setIdToCluster(loadedDrain.getIdToCluster());
            drain.setClustersCounter(loadedDrain.getClustersCounter());
            drain.setRootNode(loadedDrain.getRootNode());
            } catch (IOException e) {
            log.error("Error loading saved state", e);
        }
    }

    /**
     * state 압축 해제<br>
     *
     * <ol>
     * <li> Base64 디코딩 </li>
     * <li> 압축 해제 </li>
     * </ol>
     *
     * @param compressedState 압축된 상태정보
     * @throws IOException
     * @return byteArray
     */
    private static byte[] decompressState(byte[] compressedState) throws IOException {

        // Base64 디코딩
        byte[] decodedBytes = Base64.getDecoder().decode(compressedState);

        // 압축 해제하는데 사용되는 InflaterInputStream 객체 생성
        try (InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(decodedBytes))) {
            // 압축 해제된 데이터를 담는 곳
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;
            // 압축 해제된 데이터 읽고, outputStream 에 쓰기
            while ((length = inflaterInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            // 바이트배열로 반환
            return outputStream.toByteArray();
        }
    }

    /**
     * 전처리 <br>
     *  : 바이트 배열 -> 문자열 -> 파싱 및 재구성 -> Drain 객체
     *
     * <pre>
     * log_cluster_depth
     * max_node_depth
     * sim_th
     * max_children
     * extra_delimiters
     * max_clusters
     * param_str
     * parametrize_numeric_tokens
     * root_node
     *   - key_to_child_node
     *      - [seq Length] (ex."7")
     *          - key_to_child_node
     *              - [first Token]
     *                  - key_to_child_node
     *                  - cluster_ids
     *   - cluster_ids
     * id_to_cluster
     *   - _Cache__data
     *      - [json://number]
     *          - log_template_tokens
     *              - [tokens]
     *          - cluster_id
     *          - size
     *   - _Cache__currsize
     *   - _Cache__maxsize
     * LRUCache
     * profiler
     * </pre>
     *
     * @param state JSON 형식의 상태를 담은 byte 배열
     * @return Drain 객체로 파싱된 상태
     */
    private Drain parseJsonState(byte[] state) {

        Gson gson = new Gson();

        // 바이트 배열 -> 문자열
        String stateString = new String(state, StandardCharsets.UTF_8);

        // JsonObject 로 파싱
        JsonObject jsonObject = JsonParser.parseString(stateString).getAsJsonObject();

        // newJsonObject 에 값들 재구성
        JsonObject newJsonObject = new JsonObject();
        // ( log_cluster_depth, max_node_depth, sim_th, max_children )
        newJsonObject.addProperty("log_cluster_depth", jsonObject.get("log_cluster_depth").getAsInt());
        newJsonObject.addProperty("max_node_depth", jsonObject.get("max_node_depth").getAsInt());
        newJsonObject.addProperty("sim_th", jsonObject.get("sim_th").getAsDouble());
        newJsonObject.addProperty("max_children", jsonObject.get("max_children").getAsInt());
        newJsonObject.addProperty("max_clusters", jsonObject.get("max_clusters").getAsInt());
        newJsonObject.addProperty("param_str", jsonObject.get("param_str").getAsString());
        newJsonObject.addProperty("parametrize_numeric_tokens", jsonObject.get("parametrize_numeric_tokens").getAsBoolean());

        // root_node
        JsonObject rootNode = jsonObject.getAsJsonObject("root_node");
        JsonObject keyToChildNode = rootNode.getAsJsonObject("key_to_child_node");
        JsonObject newRootNodeObject = new JsonObject();
        for (String key : rootNode.keySet()) {
            if (!key.equals("py/object")) {
                newRootNodeObject.add(key, rootNode.get(key));
            }
        }
        JsonObject newKeyToChildNodeObject = new JsonObject();
        // Length Node
        for (String key : keyToChildNode.keySet()) {
            JsonObject childNode = keyToChildNode.getAsJsonObject(key);
            JsonObject newChildNode = new JsonObject();
            for (String childKey : childNode.keySet()) {
                if (!childKey.equals("py/object")) {
                    if (childNode.get(childKey).isJsonObject()) {
                        JsonObject childObject = childNode.getAsJsonObject(childKey);
                        JsonObject newChildObject = new JsonObject();
                        for (String innerKey : childObject.keySet()) {
                            if (!innerKey.equals("py/object")) {
                                JsonElement element = childObject.get(innerKey);
                                if (element.isJsonObject()) {
                                    JsonObject innerObject = new JsonObject();
                                    JsonObject originalInnerObject = element.getAsJsonObject();
                                    for (String innerInnerKey : originalInnerObject.keySet()) {
                                        if (!innerInnerKey.equals("py/object")) {
                                            innerObject.add(innerInnerKey, originalInnerObject.get(innerInnerKey));
                                        }
                                    }
                                    newChildObject.add(innerKey, innerObject);
                                } else {
                                    newChildObject.add(innerKey, element);
                                }
                            }
                        }
                        newChildNode.add(childKey, newChildObject);
                    } else {
                        newChildNode.add(childKey, childNode.get(childKey));
                    }
                }
            }
            newKeyToChildNodeObject.add(key, newChildNode);
        }
        newRootNodeObject.add("key_to_child_node", newKeyToChildNodeObject);
        newJsonObject.add("root_node", newRootNodeObject);

        // id_to_cluster 데이터 파싱을 위한 JsonObject 생성
        JsonObject idToClusterObject = new JsonObject();
        // _Cache__data와 관련된 데이터 파싱을 위한 JsonObject 생성
        JsonObject cacheOrgDataObject = new JsonObject();

        // id_to_cluster 속성의 데이터 파싱
        JsonObject cacheObject = jsonObject.getAsJsonObject("id_to_cluster");
        JsonObject cacheDataObject = cacheObject.getAsJsonObject("_Cache__data");
        for (String key : cacheDataObject.keySet()) {
            JsonObject clusterObject = cacheDataObject.getAsJsonObject(key);
            JsonObject newClusterObject = new JsonObject();
            newClusterObject.addProperty("cluster_id", clusterObject.get("cluster_id").getAsInt());
            newClusterObject.addProperty("size", clusterObject.get("size").getAsInt());

            JsonObject logTemplateTokensObject = clusterObject.getAsJsonObject("log_template_tokens");
            newClusterObject.add("log_template_tokens", logTemplateTokensObject.get("py/tuple"));

            // idToClusterObject 에 추가
            idToClusterObject.add(extractJsonNumber(key), newClusterObject);
        }

        cacheOrgDataObject.add("_Cache__data", idToClusterObject);
        newJsonObject.add("id_to_cluster", cacheOrgDataObject);
        newJsonObject.addProperty("_Cache__currsize", cacheObject.get("_Cache__currsize").getAsInt());
        newJsonObject.addProperty("_Cache__maxsize", cacheObject.get("_Cache__maxsize").getAsInt());

        // LRUCache 와 관련된 데이터 파싱
        JsonObject lrucacheOrderObject = new JsonObject();
        JsonObject lrucacheObject = cacheObject.getAsJsonObject("_LRUCache__order");
        for (String key : lrucacheObject.keySet()) {
            JsonArray tuple = lrucacheObject.getAsJsonArray(key);
            int size = tuple.size();
            if (size > 0) {
                JsonElement lastTuple = tuple.get(size-1);
                if (lastTuple.isJsonObject()) {
                    JsonObject lastTupleObject = lastTuple.getAsJsonObject();
                    for (String innerKey : lastTupleObject.keySet()) {
                        JsonArray tuple2 = lastTupleObject.getAsJsonArray(innerKey);
                        for (JsonElement element : tuple2) {
                            JsonObject tupleObject = element.getAsJsonObject();
                            for (String innerKey2 : tupleObject.keySet()) {
                                JsonArray tuple22 = tupleObject.getAsJsonArray(innerKey2);
                                lrucacheOrderObject.addProperty(tuple22.get(0).toString(), String.valueOf(tuple22.get(1)));
                            }
                        }
                    }
                }
            }
        }
        newJsonObject.add("_LRUCache__order", lrucacheOrderObject);

        return gson.fromJson(newJsonObject.toString(), Drain.class);
    }

    /**
     * 'json://1" 에서 숫자 추출 <br>
     *  : 파이썬에서 사용되는 JSON 키 문자열
     *
     * @param str 'json://1'과 같은 문자열
     * @return 추출한 숫자값을 문자열 형태로 반환
     */
    public static String extractJsonNumber(String str) {

        // 예외처리
        if (StringUtil.isEmpty(str)) {
            throw new IllegalArgumentException("Str is empty");
        }

        // 정규표현식 패턴
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            // 찾은 경우,
            return matcher.group();
        } else {
            // 찾지 못한 경우, 그대로 반환
            return str;
        }
    }

    /**
     * 로그 메시지 마스킹 (생략) <br>
     * 기존 클러스터와 로그 메시지(마스킹된)를 매칭
     *
     * @param logMessage 일치시킬 로그 메시지
     * @param fullSearchStrategy 전체 클러스터 검색을 수행할 때의 전략
     * @return LogCluster
     */
    public LogCluster match(String logMessage, String fullSearchStrategy) {

        // 예외처리
        if (StringUtil.isEmpty(logMessage)) {
            throw new IllegalArgumentException("logMessage is empty");
        }

        // 예외처리
        if (StringUtil.isEmpty(fullSearchStrategy)) {
            throw new IllegalArgumentException("fullSearchStrategy is empty");
        }

        // (생략) self.masker.mask(log_message)
        return drain.match(logMessage, fullSearchStrategy);
    }
}