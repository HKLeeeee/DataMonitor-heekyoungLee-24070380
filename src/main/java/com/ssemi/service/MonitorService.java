package com.ssemi.service;

import com.ssemi.model.Order;
import com.ssemi.model.OrderStatus;
import com.ssemi.model.Sample;
import com.ssemi.model.StockStatus;
import com.ssemi.repository.DataRepository;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MonitorService {

    private static final List<OrderStatus> MONITORED_STATUSES = List.of(
            OrderStatus.RESERVED,
            OrderStatus.PRODUCING,
            OrderStatus.CONFIRMED,
            OrderStatus.RELEASE
    );

    private final DataRepository repository;

    public MonitorService(DataRepository repository) {
        this.repository = repository;
    }

    public List<Sample> getSamples() {
        return repository.loadSamples();
    }

    public List<Order> getAllOrders() {
        return repository.loadOrders();
    }

    public Map<OrderStatus, Long> countByStatus() {
        List<Order> orders = repository.loadOrders();
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : MONITORED_STATUSES) {
            counts.put(status, 0L);
        }
        for (Order order : orders) {
            OrderStatus status = order.getStatus();
            if (counts.containsKey(status)) {
                counts.put(status, counts.get(status) + 1);
            }
        }
        return counts;
    }

    /**
     * CONFIRMED + PRODUCING 주문의 총 수량을 구해 재고 상태를 판단한다.
     *
     * - 재고 == 0 : 고갈
     * - 재고 < 대기 주문 총 수량 : 부족
     * - 재고 >= 대기 주문 총 수량 (또는 대기 주문 없음) : 여유
     */
    public StockStatus evaluateStockStatus(Sample sample, List<Order> orders) {
        if (sample.getStock() == 0) {
            return StockStatus.고갈;
        }
        int pendingQuantity = orders.stream()
                .filter(o -> o.getSampleId().equals(sample.getId()))
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED || o.getStatus() == OrderStatus.PRODUCING)
                .mapToInt(Order::getQuantity)
                .sum();
        if (sample.getStock() < pendingQuantity) {
            return StockStatus.부족;
        }
        return StockStatus.여유;
    }

    public SummaryStats getSummary() {
        List<Sample> samples = repository.loadSamples();
        List<Order> orders = repository.loadOrders();

        int totalSamples = samples.size();
        int totalStock = samples.stream().mapToInt(Sample::getStock).sum();
        long totalOrders = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.REJECTED)
                .count();
        Map<OrderStatus, Long> statusCounts = countByStatus();

        return new SummaryStats(totalSamples, totalStock, totalOrders, statusCounts);
    }

    // Controller가 DataRepository를 직접 참조하지 않도록 경로 위임
    public Path getSamplesPath() {
        return repository.getSamplesPath();
    }

    public Path getOrdersPath() {
        return repository.getOrdersPath();
    }

    public static class SummaryStats {
        private final int totalSamples;
        private final int totalStock;
        private final long totalOrders;
        private final Map<OrderStatus, Long> statusCounts;

        public SummaryStats(int totalSamples, int totalStock, long totalOrders,
                            Map<OrderStatus, Long> statusCounts) {
            this.totalSamples = totalSamples;
            this.totalStock = totalStock;
            this.totalOrders = totalOrders;
            this.statusCounts = statusCounts;
        }

        public int getTotalSamples() { return totalSamples; }
        public int getTotalStock() { return totalStock; }
        public long getTotalOrders() { return totalOrders; }
        public Map<OrderStatus, Long> getStatusCounts() { return statusCounts; }
    }
}
