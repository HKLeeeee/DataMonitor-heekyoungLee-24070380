package com.ssemi.service;

import com.ssemi.model.Order;
import com.ssemi.model.OrderStatus;
import com.ssemi.model.Sample;
import com.ssemi.model.StockStatus;
import com.ssemi.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MonitorService 단위 테스트")
class MonitorServiceTest {

    private MonitorService service;

    @BeforeEach
    void setUp() {
        URL resourceDir = getClass().getClassLoader().getResource("data");
        assertNotNull(resourceDir, "테스트 리소스 data 디렉토리가 존재해야 합니다.");
        DataRepository repository = new DataRepository(resourceDir.getPath());
        service = new MonitorService(repository);
    }

    @Test
    @DisplayName("상태별 주문 건수: RESERVED=1, CONFIRMED=2, PRODUCING=1, RELEASE=1")
    void countByStatus_returnsCorrectCounts() {
        Map<OrderStatus, Long> counts = service.countByStatus();

        assertEquals(1L, counts.get(OrderStatus.RESERVED),  "RESERVED 건수");
        assertEquals(2L, counts.get(OrderStatus.CONFIRMED), "CONFIRMED 건수");
        assertEquals(1L, counts.get(OrderStatus.PRODUCING), "PRODUCING 건수");
        assertEquals(1L, counts.get(OrderStatus.RELEASE),   "RELEASE 건수");
    }

    @Test
    @DisplayName("REJECTED 주문은 집계에 포함되지 않는다")
    void countByStatus_excludesRejected() {
        Map<OrderStatus, Long> counts = service.countByStatus();

        assertFalse(counts.containsKey(OrderStatus.REJECTED), "REJECTED 키가 없어야 한다");
    }

    @Test
    @DisplayName("주문이 없으면 모든 카운트가 0")
    void countByStatus_emptyOrders_allZero() {
        DataRepository emptyRepo = new DataRepository("/nonexistent/path");
        MonitorService emptyService = new MonitorService(emptyRepo);

        Map<OrderStatus, Long> counts = emptyService.countByStatus();

        assertEquals(0L, counts.get(OrderStatus.RESERVED));
        assertEquals(0L, counts.get(OrderStatus.CONFIRMED));
        assertEquals(0L, counts.get(OrderStatus.PRODUCING));
        assertEquals(0L, counts.get(OrderStatus.RELEASE));
    }

    @Test
    @DisplayName("재고가 0이면 고갈")
    void evaluateStockStatus_zeroStock_returns고갈() {
        Sample sample = new Sample("S-002", "GaN-on-Si", 1.2, 0.85, 0);
        List<Order> orders = List.of();

        StockStatus status = service.evaluateStockStatus(sample, orders);

        assertEquals(StockStatus.고갈, status);
    }

    @Test
    @DisplayName("재고가 0이고 CONFIRMED 주문이 있어도 고갈이 우선한다")
    void evaluateStockStatus_zeroStockWithPendingOrders_returns고갈() {
        Sample sample = new Sample("S-002", "GaN-on-Si", 1.2, 0.85, 0);
        Order confirmed = new Order("ORD-001", "S-002", "고객A", 10, OrderStatus.CONFIRMED);

        StockStatus status = service.evaluateStockStatus(sample, List.of(confirmed));

        assertEquals(StockStatus.고갈, status);
    }

    @Test
    @DisplayName("재고가 대기 주문 수량보다 작으면 부족")
    void evaluateStockStatus_stockLessThanPending_returns부족() {
        Sample sample = new Sample("S-003", "Si 포토레지스트", 0.5, 0.95, 10);
        Order confirmed = new Order("ORD-001", "S-003", "고객A", 12, OrderStatus.CONFIRMED);

        StockStatus status = service.evaluateStockStatus(sample, List.of(confirmed));

        assertEquals(StockStatus.부족, status);
    }

    @Test
    @DisplayName("재고가 대기 주문 수량과 같으면 여유")
    void evaluateStockStatus_stockEqualsPending_returns여유() {
        Sample sample = new Sample("S-001", "SiC 파워기판", 0.8, 0.92, 30);
        Order confirmed = new Order("ORD-001", "S-001", "고객A", 30, OrderStatus.CONFIRMED);

        StockStatus status = service.evaluateStockStatus(sample, List.of(confirmed));

        assertEquals(StockStatus.여유, status);
    }

    @Test
    @DisplayName("재고가 대기 주문 수량보다 크면 여유")
    void evaluateStockStatus_stockGreaterThanPending_returns여유() {
        Sample sample = new Sample("S-001", "SiC 파워기판", 0.8, 0.92, 50);
        Order confirmed = new Order("ORD-001", "S-001", "고객A", 20, OrderStatus.CONFIRMED);

        StockStatus status = service.evaluateStockStatus(sample, List.of(confirmed));

        assertEquals(StockStatus.여유, status);
    }

    @Test
    @DisplayName("RESERVED 주문은 재고 상태 판단에서 제외된다")
    void evaluateStockStatus_reservedOrdersIgnored() {
        Sample sample = new Sample("S-001", "SiC 파워기판", 0.8, 0.92, 5);
        Order reserved = new Order("ORD-001", "S-001", "고객A", 100, OrderStatus.RESERVED);

        StockStatus status = service.evaluateStockStatus(sample, List.of(reserved));

        assertEquals(StockStatus.여유, status);
    }

    @Test
    @DisplayName("PRODUCING 주문은 재고 부족 판단에 포함된다")
    void evaluateStockStatus_producingOrdersIncluded() {
        Sample sample = new Sample("S-002", "GaN-on-Si", 1.2, 0.85, 5);
        Order producing = new Order("ORD-001", "S-002", "고객A", 10, OrderStatus.PRODUCING);

        StockStatus status = service.evaluateStockStatus(sample, List.of(producing));

        assertEquals(StockStatus.부족, status);
    }

    @Test
    @DisplayName("전체 요약: 시료 3개, 총 재고 60, 전체 주문 5건(REJECTED 제외)")
    void getSummary_returnsCorrectStats() {
        MonitorService.SummaryStats stats = service.getSummary();

        assertEquals(3, stats.getTotalSamples(), "등록 시료 수");
        assertEquals(60, stats.getTotalStock(), "총 재고 (50+0+10)");
        assertEquals(5L, stats.getTotalOrders(), "전체 주문 수 (REJECTED 제외, 6개 중 1개 제외)");
    }

    @Test
    @DisplayName("시료 파일을 정상적으로 읽는다")
    void getSamples_loadsFromFile() {
        List<Sample> samples = service.getSamples();

        assertEquals(3, samples.size());
        assertEquals("S-001", samples.get(0).getId());
        assertEquals("SiC 파워기판-6인치", samples.get(0).getName());
        assertEquals(50, samples.get(0).getStock());
    }

    @Test
    @DisplayName("주문 파일을 정상적으로 읽는다")
    void getAllOrders_loadsFromFile() {
        List<Order> orders = service.getAllOrders();

        assertEquals(6, orders.size());
    }

    @Test
    @DisplayName("파일이 없으면 빈 리스트를 반환한다")
    void getSamples_missingFile_returnsEmpty() {
        DataRepository emptyRepo = new DataRepository("/nonexistent/path");
        MonitorService emptyService = new MonitorService(emptyRepo);

        List<Sample> samples = emptyService.getSamples();
        List<Order> orders = emptyService.getAllOrders();

        assertTrue(samples.isEmpty(), "시료 목록이 비어있어야 한다");
        assertTrue(orders.isEmpty(), "주문 목록이 비어있어야 한다");
    }
}
