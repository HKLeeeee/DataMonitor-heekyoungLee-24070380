package com.ssemi.controller;

import com.ssemi.model.Order;
import com.ssemi.model.Sample;
import com.ssemi.model.StockStatus;
import com.ssemi.model.OrderStatus;
import com.ssemi.repository.DataRepository;
import com.ssemi.service.MonitorService;
import com.ssemi.view.ConsoleView;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 메뉴 루프를 제어하며 Service와 View를 연결하는 컨트롤러.
 */
public class MonitorController {

    private final MonitorService service;
    private final ConsoleView view;
    private final DataRepository repository;

    public MonitorController(MonitorService service, ConsoleView view, DataRepository repository) {
        this.service = service;
        this.view = view;
        this.repository = repository;
    }

    public void run(Scanner scanner) {
        boolean running = true;
        while (running) {
            view.printMenu();
            String input = scanner.nextLine().trim().toUpperCase();
            switch (input) {
                case "1" -> handleSamples();
                case "2" -> handleOrderStatus();
                case "3" -> handleStockStatus();
                case "4" -> handleSummary();
                case "R" -> handleRefresh();
                case "0" -> {
                    view.printBye();
                    running = false;
                }
                default -> view.printError("알 수 없는 명령입니다: " + input);
            }
        }
    }

    private void handleSamples() {
        List<Sample> samples = service.getSamples();
        view.printSamples(samples);
    }

    private void handleOrderStatus() {
        Map<OrderStatus, Long> counts = service.countByStatus();
        view.printOrderStatusCounts(counts);
    }

    private void handleStockStatus() {
        List<Sample> samples = service.getSamples();
        List<Order> orders = service.getAllOrders();
        view.printStockStatus(samples, orders, service::evaluateStockStatus);
    }

    private void handleSummary() {
        MonitorService.SummaryStats stats = service.getSummary();
        view.printSummary(stats);
    }

    private void handleRefresh() {
        // MonitorService는 매 호출마다 파일을 다시 읽으므로 별도 캐시 무효화 불필요.
        // 사용자에게 경로와 재로드 완료를 알린다.
        view.printRefreshed(
                repository.getSamplesPath().toAbsolutePath().toString(),
                repository.getOrdersPath().toAbsolutePath().toString()
        );
    }
}
