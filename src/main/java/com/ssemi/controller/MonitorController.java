package com.ssemi.controller;

import com.ssemi.model.Order;
import com.ssemi.model.Sample;
import com.ssemi.model.StockStatus;
import com.ssemi.model.OrderStatus;
import com.ssemi.service.MonitorService;
import com.ssemi.view.ConsoleView;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MonitorController {

    private final MonitorService service;
    private final ConsoleView view;

    public MonitorController(MonitorService service, ConsoleView view) {
        this.service = service;
        this.view = view;
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
        // Service가 매 호출마다 파일을 새로 읽으므로 별도 캐시 무효화 불필요.
        view.printRefreshed(
                service.getSamplesPath().toAbsolutePath().toString(),
                service.getOrdersPath().toAbsolutePath().toString()
        );
    }
}
