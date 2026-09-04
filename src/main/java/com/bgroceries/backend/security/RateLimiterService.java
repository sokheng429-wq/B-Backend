package com.bgroceries.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class RateLimiterService {

    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> requestCounts = new ConcurrentHashMap<>();

    @Value("${app.security.rate-limit.auth.max-requests:15}")
    private int maxAuthRequests;

    @Value("${app.security.rate-limit.auth.window-ms:60000}")
    private long authWindowMs;

    @Value("${app.security.rate-limit.otp.max-requests:5}")
    private int maxOtpRequests;

    public boolean isAllowed(String clientIp, String endpoint) {
        long now = System.currentTimeMillis();
        boolean isOtp = endpoint.contains("/otp/");
        int limit = isOtp ? maxOtpRequests : maxAuthRequests;
        long window = authWindowMs;

        String key = clientIp + ":" + (isOtp ? "otp" : "auth");

        ConcurrentLinkedQueue<Long> timestamps = requestCounts.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peek() > window) {
                timestamps.poll();
            }

            if (timestamps.size() >= limit) {
                return false;
            }

            timestamps.add(now);
            return true;
        }
    }

    public long getRetryAfterSeconds(String clientIp, String endpoint) {
        boolean isOtp = endpoint.contains("/otp/");
        String key = clientIp + ":" + (isOtp ? "otp" : "auth");
        ConcurrentLinkedQueue<Long> timestamps = requestCounts.get(key);
        if (timestamps == null || timestamps.isEmpty()) {
            return 1;
        }
        Long oldest = timestamps.peek();
        if (oldest == null) return 1;
        long elapsed = System.currentTimeMillis() - oldest;
        long remaining = authWindowMs - elapsed;
        return Math.max(1, remaining / 1000);
    }

    @Scheduled(fixedDelay = 60_000)
    public void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        requestCounts.entrySet().removeIf(entry -> {
            ConcurrentLinkedQueue<Long> q = entry.getValue();
            synchronized (q) {
                while (!q.isEmpty() && now - q.peek() > authWindowMs) {
                    q.poll();
                }
                return q.isEmpty();
            }
        });
    }
}
