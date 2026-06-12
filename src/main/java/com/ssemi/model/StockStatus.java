package com.ssemi.model;

/**
 * 재고 상태를 나타내는 열거형.
 * - 여유: 주문 대비 재고 충분
 * - 부족: 주문 대비 재고 부족
 * - 고갈: 재고 0
 */
public enum StockStatus {
    여유,
    부족,
    고갈
}
