package com.ssemi;

import com.ssemi.controller.MonitorController;
import com.ssemi.repository.DataRepository;
import com.ssemi.service.MonitorService;
import com.ssemi.view.ConsoleView;

import java.util.Scanner;

public class MonitorApp {

    private static final String SEPARATOR = "=".repeat(70);

    public static void main(String[] args) {
        String dataDir = (args.length > 0) ? args[0] : "data";

        DataRepository repository = new DataRepository(dataDir);
        MonitorService service = new MonitorService(repository);
        ConsoleView view = new ConsoleView();
        MonitorController controller = new MonitorController(service, view);

        System.out.println(SEPARATOR);
        System.out.println("  S-Semi 데이터 모니터링 도구 시작");
        System.out.println("  데이터 경로: " + dataDir);
        System.out.println(SEPARATOR);

        try (Scanner scanner = new Scanner(System.in)) {
            controller.run(scanner);
        }
    }
}
