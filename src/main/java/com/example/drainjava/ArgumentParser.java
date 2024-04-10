package com.example.drainjava;

import com.example.drainjava.builtins.FilePersistence;
import com.example.drainjava.builtins.TemplateMiner;
import com.example.drainjava.builtins.drain.LogCluster;
import com.example.drainjava.common.CommonUtil;
import com.example.drainjava.common.Pair;
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
    private final CommonUtil commonUtil;

    @Autowired
    public ArgumentParser(TemplateMiner templateMiner, FilePersistence filePersistence, CommonUtil commonUtil) {
        this.templateMiner = templateMiner;
        this.filePersistence = filePersistence;
        this.commonUtil = commonUtil;
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
        List<String> headers = new ArrayList<>();
        Pattern regex = null;
        if (args.containsOption("l") || args.containsOption("-log-format")) {
            List<String> optionValues = args.getOptionValues("l");
            String logFormat = optionValues.get(0);

            Pair<Pattern, List<String>> result = commonUtil.generateLogFormatRegex(logFormat, headers);
            regex = result.getFirst();
            headers = result.getSecond();
        }

        // 스냅샷 기능이 활성화 상태라면, j, k, r 중 하나라도 선택되어야 함
        if ((inferenceMode || trainingMode) && !args.containsOption("f") && !args.containsOption("k") && !args.containsOption("r")) {
            System.out.println(displayUsage());
            System.out.println("Error: f, k, r 중 하나를 선택해야 합니다.");
            return;
        }

        // 학습모드 (현재 X)
        if (trainingMode) {
            return;
        }

        // 모드, 둘 중 하나는 선택
        if (!inferenceMode && !trainingMode) {
            System.out.println(displayUsage());
            System.out.println("Error: 추론 모드 또는 학습 모드를 선택해야 합니다.");
            return;
        }

        // 모드, 동시 선택 X
        if (inferenceMode && trainingMode) {
            System.out.println(displayUsage());
            System.out.println("Error: 추론 모드와 학습 모드는 동시에 사용할 수 없습니다.");
            return;
        }

        // 옵션 5. 상태정보 저장 및 로드, JSON 파일
        String binFilePath = "";
        if (args.containsOption("f") || args.containsOption("-file")) {
            List<String> optionValues = args.getOptionValues("f");
            if (optionValues == null || optionValues.isEmpty()) {
                System.out.println("Error: 파일이 비어있습니다.");
                return;
            }

            binFilePath = optionValues.get(0);
        }

        // 옵션 6. 상태정보 저장 및 로드, Kafka
        if (args.containsOption("k") || args.containsOption("-kafka")) {
            return;
        }

        // 옵션 7. 상태정보 저장 및 로드, Redis
        if (args.containsOption("r") || args.containsOption("-redis")) {
            return;
        }

        // 옵션 8. 세부정보 출력
        if (args.containsOption("-verbose")) {
            return;
        }

        // 옵션 9. 드레인 설정파일 (.ini)
        String drainIniFilePath = "";
        if (args.containsOption("e")) {
            drainIniFilePath = args.getOptionValues("e").get(0);

            // 예외처리
            if (StringUtil.isEmpty(drainIniFilePath)) {
                System.out.println("Error: drain3.ini 파일이 비어있습니다.");
                return;
            }
        }

        // FILE 인자들 (로그데이터 파일들, Drain 입력데이터)
        List<String> logFIlePathList = args.getNonOptionArgs();
        if (logFIlePathList.isEmpty() == true) {
            System.out.println(displayUsage());
            System.out.println("Error: FILE 인자 누락되었습니다.");
            return;
        }

        // 전체 프로세스 실행
        filePersistence.setFilePath(binFilePath);
        templateMiner.init(drainIniFilePath);
        templateMiner.loadState();

        for (String logFilePath : logFIlePathList) {

            // 예외처리
            if (StringUtil.isEmpty(logFilePath)) {
                throw new IllegalArgumentException("logFilePath is empty");
            }

            // 확장자 체크
            if (logFilePath.endsWith(".log") == false) {
                throw new IllegalArgumentException("The file must have a .log extension.");
            }

            int noTemplateCount = 0;
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
                    } else {
                        line = line.substring(line.indexOf(": ") + 2);
                    }
                    System.out.println("----------------------------");
                    // 매칭
                    LogCluster cluster = templateMiner.match(line, "never");

                    System.out.println("# INPUT: " + line);
                    if (cluster != null) {
                        System.out.println("# OUTPUT: " + cluster.getTemplate());
                    } else {
                        System.out.println("# OUTPUT: No matching template found.");
                        noTemplateCount += 1;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("============================");
            System.out.printf("No matching template: %d", noTemplateCount);
        }
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
                "  -e, --log-format\t\tdrain ini file path\n" +
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
//        return "Usage: java -jar xx.jar [-hv] [--verbose] [-it] [-l=LOG_FORMAT_STRING] [-f=SNAPSHOT_BIN_PATH] [-r=REDIS_PATH] [-k=KAFKA TOPIC PATH] FILE";
        return "Usage: java -jar xx.jar [-hv] [-it] [-f=SNAPSHOT_BIN_PATH] FILE";
    }
}
