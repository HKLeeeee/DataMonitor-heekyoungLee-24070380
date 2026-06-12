package com.ssemi;

import com.ssemi.controller.MonitorController;
import com.ssemi.repository.DataRepository;
import com.ssemi.service.MonitorService;
import com.ssemi.view.ConsoleView;

import java.util.Scanner;

/**
 * S-Semi 데이터 모니터링 도구 진입점.
 *
 * 사용법:
 *   java -cp ... com.ssemi.MonitorApp [dataDir]
 *
 * dataDir 기본값: data  (프로젝트 루트 기준)
 */
public class MonitorApp {

    public static void main(String[] args) {
        String dataDir = (args.length > 0) ? args[0] : "data";

        DataRepository repository = new DataRepository(dataDir);
        MonitorService service = new MonitorService(repository);
        ConsoleView view = new ConsoleView();
        MonitorController controller = new MonitorController(service, view, repository);

        System.out.println("======================================================================");
        System.out.println("  S-Semi 데이터 모니터링 도구 시작");
        System.out.println("  데이터 경로: " + dataDir);
        System.out.println("======================================================================");

        try (Scanner scanner = new Scanner(System.in)) {
            controller.run(scanner);
        }
    }
}
