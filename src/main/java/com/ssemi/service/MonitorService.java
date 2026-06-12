package com.ssemi.service;

import com.ssemi.model.Order;
import com.ssemi.model.OrderStatus;
import com.ssemi.model.Sample;
import com.ssemi.model.StockStatus;
import com.ssemi.repository.DataRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 모니터링에 필요한 집계·조회 비즈니스 로직을 담당.
 */
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

    /** 파일에서 최신 시료 목록을 읽어 반환 */
    public List<Sample> getSamples() {
        return repository.loadSamples();
    }

    /** 파일에서 최신 주문 목록을 읽어 반환 (REJECTED 포함 전체) */
    public List<Order> getAllOrders() {
        return repository.loadOrders();
    }

    /**
     * 모니터링 대상 상태(REJECTED 제외)의 상태별 주문 건수를 반환.
     */
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
     * 특정 시료에 대해 CONFIRMED + PRODUCING 주문의 총 수량을 구해
     * 재고 상태(여유/부족/고갈)를 판단한다.
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

    /**
     * 전체 요약 통계를 반환.
     */
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

    public static class SummaryStats {
        public final int totalSamples;
        public final int totalStock;
        public final long totalOrders;
        public final Map<OrderStatus, Long> statusCounts;

        public SummaryStats(int totalSamples, int totalStock, long totalOrders,
                            Map<OrderStatus, Long> statusCounts) {
            this.totalSamples = totalSamples;
            this.totalStock = totalStock;
            this.totalOrders = totalOrders;
            this.statusCounts = statusCounts;
        }
    }
}
