package com.ssemi.view;

import com.ssemi.model.Order;
import com.ssemi.model.OrderStatus;
import com.ssemi.model.Sample;
import com.ssemi.model.StockStatus;
import com.ssemi.service.MonitorService;

import java.util.List;
import java.util.Map;

/**
 * 콘솔 출력 전담 뷰. 비즈니스 로직 없이 포매팅·출력만 수행한다.
 */
public class ConsoleView {

    private static final String SEPARATOR = "=".repeat(70);
    private static final String THIN_SEP  = "-".repeat(70);

    // ── 메인 메뉴 ──────────────────────────────────────────────────────────

    public void printMenu() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  S-Semi 데이터 모니터링 도구");
        System.out.println(SEPARATOR);
        System.out.println("  [1] 시료 현황   - 전체 시료 목록 + 재고");
        System.out.println("  [2] 주문 현황   - 상태별 건수 (REJECTED 제외)");
        System.out.println("  [3] 재고 상태   - 시료별 재고 + 여유/부족/고갈");
        System.out.println("  [4] 전체 요약   - 등록 시료 수 / 총 재고 / 주문 수 / 상태별 건수");
        System.out.println("  [R] 새로고침    - 데이터 소스 재로드");
        System.out.println("  [0] 종료");
        System.out.println(THIN_SEP);
        System.out.print("  명령 입력: ");
    }

    // ── [1] 시료 현황 ──────────────────────────────────────────────────────

    public void printSamples(List<Sample> samples) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.printf("  %-10s %-28s %8s %6s %6s%n",
                "시료 ID", "시료 이름", "생산시간", "수율", "재고");
        System.out.println(THIN_SEP);
        if (samples.isEmpty()) {
            System.out.println("  (등록된 시료 없음)");
        } else {
            for (Sample s : samples) {
                System.out.printf("  %-10s %-28s %7.1f분 %5.0f%% %5dea%n",
                        s.getId(),
                        s.getName(),
                        s.getAvgProductionTime(),
                        s.getYield() * 100,
                        s.getStock());
            }
        }
        System.out.println(THIN_SEP);
        System.out.printf("  총 %d개 시료 등록%n", samples.size());
    }

    // ── [2] 주문 현황 ──────────────────────────────────────────────────────

    public void printOrderStatusCounts(Map<OrderStatus, Long> counts) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  [주문 현황] 상태별 건수 (REJECTED 제외)");
        System.out.println(THIN_SEP);
        long total = 0;
        for (OrderStatus status : new OrderStatus[]{
                OrderStatus.RESERVED, OrderStatus.PRODUCING,
                OrderStatus.CONFIRMED, OrderStatus.RELEASE}) {
            long count = counts.getOrDefault(status, 0L);
            total += count;
            System.out.printf("  %-12s : %5d건%n", status.name(), count);
        }
        System.out.println(THIN_SEP);
        System.out.printf("  합계          : %5d건%n", total);
    }

    // ── [3] 재고 상태 ──────────────────────────────────────────────────────

    public void printStockStatus(List<Sample> samples, List<Order> orders,
                                  java.util.function.BiFunction<Sample, List<Order>, StockStatus> evaluator) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  [재고 상태] 시료별 재고 + 상태");
        System.out.println(THIN_SEP);
        System.out.printf("  %-10s %-28s %6s %6s%n",
                "시료 ID", "시료 이름", "재고", "상태");
        System.out.println(THIN_SEP);
        if (samples.isEmpty()) {
            System.out.println("  (등록된 시료 없음)");
        } else {
            for (Sample s : samples) {
                StockStatus status = evaluator.apply(s, orders);
                String statusMark = switch (status) {
                    case 여유 -> "[ 여유 ]";
                    case 부족 -> "[ 부족 ]";
                    case 고갈 -> "[ 고갈 ]";
                };
                System.out.printf("  %-10s %-28s %5dea %s%n",
                        s.getId(), s.getName(), s.getStock(), statusMark);
            }
        }
        System.out.println(THIN_SEP);
    }

    // ── [4] 전체 요약 ──────────────────────────────────────────────────────

    public void printSummary(MonitorService.SummaryStats stats) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  [전체 요약]");
        System.out.println(THIN_SEP);
        System.out.printf("  등록 시료 수  : %5d개%n", stats.totalSamples);
        System.out.printf("  총 재고       : %5dea%n", stats.totalStock);
        System.out.printf("  전체 주문 수  : %5d건 (REJECTED 제외)%n", stats.totalOrders);
        System.out.println(THIN_SEP);
        System.out.println("  상태별 주문 건수:");
        for (OrderStatus status : new OrderStatus[]{
                OrderStatus.RESERVED, OrderStatus.PRODUCING,
                OrderStatus.CONFIRMED, OrderStatus.RELEASE}) {
            long count = stats.statusCounts.getOrDefault(status, 0L);
            System.out.printf("    %-12s : %5d건%n", status.name(), count);
        }
        System.out.println(THIN_SEP);
    }

    // ── 공통 메시지 ────────────────────────────────────────────────────────

    public void printRefreshed(String samplesPath, String ordersPath) {
        System.out.println();
        System.out.println("  [새로고침] 데이터 소스를 다시 읽었습니다.");
        System.out.println("    samples: " + samplesPath);
        System.out.println("    orders : " + ordersPath);
    }

    public void printError(String message) {
        System.out.println("  [오류] " + message);
    }

    public void printBye() {
        System.out.println();
        System.out.println("  모니터링 도구를 종료합니다.");
        System.out.println(SEPARATOR);
    }
}
