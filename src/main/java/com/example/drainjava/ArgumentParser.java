package com.example.drainjava;

import com.example.drainjava.drain.FilePersistence;
import com.example.drainjava.drain.LogCluster;
import com.example.drainjava.common.util.CommonUtil;
import com.example.drainjava.common.util.StringUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Drain 동작을 위한 인자(arguments) 옵션 처리
 *
 * @author gunha
 * @version 0.1
 * @since 2024-03-28 오전 10:29
 */
@Log4j2
@Component
public class ArgumentParser implements ApplicationRunner {

    private final TemplateMiner templateMiner;
    private final FilePersistence filePersistence;

    @Autowired
    public ArgumentParser(TemplateMiner templateMiner, FilePersistence filePersistence) {
        this.templateMiner = templateMiner;
        this.filePersistence = filePersistence;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 옵션 처리
        processOptions(args);

        // 정상 종료
        System.exit(0);
    }

    /**
     * 옵션 처리
     *
     * @param args 인자들
     */
    private void processOptions(ApplicationArguments args) throws Exception {

        // 옵션 1. 추론모드 선택 여부
        boolean inferenceMode = args.containsOption("i") || args.containsOption("-infer");
        //옵션 1. 학습모드 선택 여부
        boolean trainingMode = args.containsOption("t") || args.containsOption("-train");


        // 옵션 2. CLI 도움말 메시지
        if (args.containsOption("h") || args.containsOption("-help")) {
            System.out.println(displayHelp());
            return;
        }


        // 옵션 3. 애플리케이션 버전 정보
        if (args.containsOption("v") || args.containsOption("-version")) {
            System.out.println(displayVersion());
            return;
        }


        // 옵션 4. Drain 수행 전, 로그 포맷을 기준으로 해당 로그메시지 전처리
        Pattern regex = null;
        if (args.containsOption("l") || args.containsOption("-log-format")) {

            Optional<String> logFormatOption = Optional.ofNullable(
                    args.containsOption("-log-format") ? args.getOptionValues("-log-format").get(0) : args.getOptionValues("l").get(0)
            );

            if (logFormatOption.isPresent() == true) {
                String logFormat = logFormatOption.get();

                // 로그 포맷 - 정규표현식 생성
                regex = CommonUtil.generateLogFormatRegex(logFormat, new ArrayList<>()).getFirst();
            }
        }


        // 학습모드 (현재 X)
        if (trainingMode == true) {
            return;
        }

        // 모드, 둘 중 하나는 선택
        if ((inferenceMode == false) && (trainingMode == false)) {
            System.out.println(displayUsage());
            log.warn("ARGS: Choose Inference or Training Mode");
            return;
        }

        // 모드, 동시 선택 X
        if ((inferenceMode == true) && (trainingMode == true)) {
            System.out.println(displayUsage());
            log.warn("ARGS: Only One Choose (Not all)");
            return;
        }


        // 옵션 5. 상태정보 저장 및 로드, JSON 파일
        String templateFilePath = "";
        if (args.containsOption("f") || args.containsOption("-file")) {

            Optional<String> binFilePathOption = Optional.ofNullable(
                    args.containsOption("-file") ? args.getOptionValues("-file").get(0) : args.getOptionValues("f").get(0)
            );

            if (binFilePathOption.isPresent() == false) {
                log.warn("ARGS: Missing Snapshot Binary File");
                return;
            }
            templateFilePath = binFilePathOption.get();
        }


        // 옵션 6. 상태정보 저장 및 로드, Kafka
        if (args.containsOption("k") || args.containsOption("-kafka")) {
            return;
        }


        // 옵션 7. 상태정보 저장 및 로드, Redis
        if (args.containsOption("r") || args.containsOption("-redis")) {
            return;
        }


        // 스냅샷 기능 (j, k, r) 중 하나라도 선택되어야 함
        if (!args.containsOption("f") && !args.containsOption("k") &&
                !args.containsOption("r") && !args.containsOption("-file") &&
                !args.containsOption("-kafka") && !args.containsOption("-redis")
        ) {
            System.out.println(displayUsage());
            log.warn("ARGS: At least one of f, k, r must be selected");
            return;
        }


        // 옵션 8. 세부정보 출력
        if (args.containsOption("-verbose")) {
            return;
        }


        // 옵션 9. 드레인 설정파일 (.ini)
        String configFilePath = "";
        if (args.containsOption("e") || args.containsOption("-env")) {

            Optional<String> drainIniOption = Optional.ofNullable(
                    args.containsOption("-env") ? args.getOptionValues("-env").get(0) : args.getOptionValues("e").get(0)
            );

            if (drainIniOption.isPresent() == false) {
                log.warn("ARGS: Missing Environment ini File");
                return;
            }
            configFilePath = drainIniOption.get();
        }


        // FILE 인자들 (로그데이터 파일들, Drain 입력데이터)
        List<String> logFIlePathList = args.getNonOptionArgs();
        if (logFIlePathList.isEmpty() == true || logFIlePathList == null) {
            System.out.println(displayUsage());
            log.warn("ARGS: Missing Log data File");
            return;
        }


        // (match) 전체 프로세스 실행
        filePersistence.setFilePath(templateFilePath);
        templateMiner.init(configFilePath);
        templateMiner.loadState();

        // 실행시간 측정
        long startTime = System.currentTimeMillis();

        for (String logFilePath : logFIlePathList) {

            // 예외처리
            if (StringUtil.isEmpty(logFilePath)) {
                throw new IllegalArgumentException("logFilePath is empty");
            }

            // 확장자 체크
            if (logFilePath.endsWith(".log") == false) {
                throw new IllegalArgumentException("The file must have a .log extension.");
            }

            // 개별 (xxx.log) 로그 파일 처리
            try (BufferedReader br = new BufferedReader(new FileReader(logFilePath))) {

                String line;
                // 한 줄씩
                while ((line = br.readLine()) != null) {

                    line = line.trim();

                    if (regex != null) {
                        Matcher matcher = regex.matcher(line);
                        if (matcher.find()) {
                            // "Content" 그룹에서 로그 메시지 추출
                            line = matcher.group("Content");
                        }
                    }
                    // 매칭
                    LogCluster cluseter = templateMiner.match(line, "never");
                    if (cluseter != null) {
                        log.info("INPUT: " + line);
                        log.info("OUTPUT: " + cluseter.getTemplate());
                    } else {
                        log.info("No Matching template exists");
                    }
                }
            } catch (IOException e) {
                log.error("Error reading log file: " + logFilePath, e);
            }
        }
        log.info("Execution time: " + (System.currentTimeMillis() - startTime) + " milliseconds");
    }

    /**
     * CLI 도움말 메시지 (틀)
     *
     * @return String
     */
    private String displayHelp() {
        return "\n============================================\n" +
                "Usage: java -jar drain-java.jar [options] FILE\n" +
                "Options:\n" +
                "FILE, \t\tlog file\n" +
                "  -h, --help\t\tshow this help message and exit\n" +
                "  -v, --version\t\tprint version information and exit\n" +
                "  -l, --log-format\t\tuse formatting logs used for preprocessing\n" +
                "  -i, --infer\t\tInference Mode\n" +
                "  -t, --train\t\tTraining Mode\n" +
                "  -e, --env\t\tdrain ini file path\n" +
                "  -f, --file\t\tspecify the bin file path for loading/storing the model state when using file option (inference mode:Loading / training:Storing)\n" +
                "  -k, --kafka\t\tuse Kafka for loading/storing the model state (inference mode:Loading / training:Storing)\n" +
                "  -r, --redis\t\tuse Redis for loading/storing the model state (inference mode:Loading / training:Storing)\n" +
                "\n============================================";
    }

    /**
     * Verison
     */
    private String displayVersion() {
        return String.format("Springboot Version: %s, Java Version: %s", System.getProperty("spring-boot.version"), System.getProperty("java.version"));
    }

    /**
     * USAGE
     */
    private String displayUsage() {
        return "Usage: java -jar xx.jar [-hv] [-i] [-l=LOG_FORMAT_STRING] [-e=DRAIN_INI_FILE_PATH] [-f=SNAPSHOT_BIN_PATH] FILE";
    }
}
