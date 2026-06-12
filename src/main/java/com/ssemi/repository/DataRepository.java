package com.ssemi.repository;

import com.ssemi.model.Order;
import com.ssemi.model.OrderStatus;
import com.ssemi.model.Sample;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataRepository {

    private final Path samplesPath;
    private final Path ordersPath;
    private final JsonReader jsonReader;

    public DataRepository(String dataDir) {
        this.samplesPath = Paths.get(dataDir, "samples.json");
        this.ordersPath = Paths.get(dataDir, "orders.json");
        this.jsonReader = new JsonReader();
    }

    public List<Sample> loadSamples() {
        try {
            List<Sample> samples = new ArrayList<>();
            List<Map<String, String>> records = jsonReader.readArray(samplesPath);
            for (Map<String, String> record : records) {
                Sample s = new Sample();
                s.setId(record.getOrDefault("id", ""));
                s.setName(record.getOrDefault("name", ""));
                s.setAvgProductionTime(parseDouble(record.get("avgProductionTime"), 0.0));
                s.setYield(parseDouble(record.get("yield"), 1.0));
                s.setStock(parseInt(record.get("stock"), 0));
                samples.add(s);
            }
            return samples;
        } catch (IOException e) {
            throw new UncheckedIOException("시료 파일 읽기 실패: " + samplesPath, e);
        }
    }

    public List<Order> loadOrders() {
        try {
            List<Order> orders = new ArrayList<>();
            List<Map<String, String>> records = jsonReader.readArray(ordersPath);
            for (Map<String, String> record : records) {
                Order o = new Order();
                o.setOrderId(record.getOrDefault("orderId", ""));
                o.setSampleId(record.getOrDefault("sampleId", ""));
                o.setCustomerName(record.getOrDefault("customerName", ""));
                o.setQuantity(parseInt(record.get("quantity"), 0));
                o.setStatus(parseStatus(record.get("status")));
                orders.add(o);
            }
            return orders;
        } catch (IOException e) {
            throw new UncheckedIOException("주문 파일 읽기 실패: " + ordersPath, e);
        }
    }

    public Path getSamplesPath() { return samplesPath; }
    public Path getOrdersPath() { return ordersPath; }

    private double parseDouble(String value, double defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private OrderStatus parseStatus(String value) {
        if (value == null || value.isBlank()) return OrderStatus.RESERVED;
        try { return OrderStatus.valueOf(value.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return OrderStatus.RESERVED; }
    }
}
