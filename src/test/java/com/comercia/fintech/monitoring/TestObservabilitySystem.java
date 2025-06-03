        double utilization = (Double) gauges.getOrDefault("selenium_grid_utilization", 0.0);
        if (utilization > 0.8) { // 80% threshold
            sendAlert("GRID_CAPACITY", 
                "Selenium grid utilization is " + String.format("%.1f%%", utilization * 100));
        }
    }
    
    // Utility methods
    private static void incrementCounter(String key) {
        counters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    private static void recordTiming(String key, Duration duration) {
        timings.computeIfAbsent(key, k -> new ArrayList<>()).add(duration);
    }
    
    private static void recordGauge(String key, Object value) {
        gauges.put(key, value);
    }
    
    private static long getCounterValue(String key) {
        return counters.getOrDefault(key, new AtomicLong(0)).get();
    }
    
    private static AtomicLong getFeatureCounter(String feature) {
        return featureCounters.computeIfAbsent(feature, f -> new AtomicLong(0));
    }
    
    private static double calculateAverageTime(String key) {
        List<Duration> times = timings.get(key);
        if (times == null || times.isEmpty()) return 0.0;
        
        return times.stream()
            .mapToLong(Duration::toMillis)
            .average()
            .orElse(0.0);
    }
    
    private static double calculatePassRate() {
        long passed = getCounterValue("tests_passed_total");
        long failed = getCounterValue("tests_failed_total");
        long total = passed + failed;
        
        return total > 0 ? (double) passed / total : 1.0;
    }
    
    private static double calculateFailureRate() {
        return 1.0 - calculatePassRate();
    }
    
    private static void updateSLAMetrics(String feature, Duration duration) {
        double slaThreshold = getSLAThreshold(feature);
        boolean slaBreached = duration.toMillis() > slaThreshold;
        
        if (slaBreached) {
            incrementCounter("sla_breaches_total");
            incrementCounter("sla_breaches_" + feature);
        }
    }
    
    private static boolean isCriticalPath(String feature) {
        return Arrays.asList("payment", "authentication", "checkout", "transaction")
            .contains(feature.toLowerCase());
    }
    
    private static void sendCriticalPathAlert(String testName, String feature, String error) {
        String message = String.format("🚨 CRITICAL PATH FAILURE\nTest: %s\nFeature: %s\nError: %s\nTime: %s", 
            testName, feature, error, LocalDateTime.now());
        
        System.err.println(message);
        
        // TODO: Integrate with real alerting systems
        // sendSlackAlert(message);
        // sendPagerDutyAlert(message);
    }
    
    private static void sendAlert(String alertType, String message) {
        System.out.println("🚨 ALERT [" + alertType + "]: " + message);
        // TODO: Implement actual alerting mechanism
    }
    
    private static String categorizeError(String errorMessage) {
        if (errorMessage == null) return "unknown";
        
        String lower = errorMessage.toLowerCase();
        if (lower.contains("timeout")) return "timeout";
        if (lower.contains("element not found")) return "element_not_found";
        if (lower.contains("network")) return "network";
        if (lower.contains("assertion")) return "assertion_failure";
        return "other";
    }
    
    private static Map<String, String> getCriticalPathStatus() {
        Map<String, String> status = new HashMap<>();
        for (String feature : Arrays.asList("payment", "authentication", "checkout")) {
            long passed = getCounterValue("tests_passed_" + feature);
            long failed = getCounterValue("tests_failed_" + feature);
            long total = passed + failed;
            
            if (total > 0) {
                double rate = (double) passed / total;
                status.put(feature, rate > 0.95 ? "HEALTHY" : rate > 0.8 ? "WARNING" : "CRITICAL");
            } else {
                status.put(feature, "NO_DATA");
            }
        }
        return status;
    }
    
    private static double calculateRevenueImpact() {
        // Simplified revenue impact calculation
        double failureRate = calculateFailureRate();
        double avgTransactionValue = 100.0; // Mock value
        long totalTransactions = getCounterValue("business_transactions_total");
        
        return failureRate * avgTransactionValue * totalTransactions;
    }
    
    private static double getSLAThreshold(String feature) {
        Map<String, Double> thresholds = Map.of(
            "payment", 5000.0,
            "authentication", 2000.0,
            "checkout", 3000.0,
            "api", 1000.0
        );
        return thresholds.getOrDefault(feature.toLowerCase(), 5000.0);
    }
    
    // Metrics export for Prometheus
    public static String exportMetrics() {
        StringBuilder metrics = new StringBuilder();
        
        // Export counters
        counters.forEach((key, value) -> {
            metrics.append("# TYPE ").append(key).append(" counter\n");
            metrics.append(key).append(" ").append(value.get()).append("\n");
        });
        
        // Export gauges
        gauges.forEach((key, value) -> {
            metrics.append("# TYPE ").append(key).append(" gauge\n");
            metrics.append(key).append(" ").append(value).append("\n");
        });
        
        // Export timing summaries
        timings.forEach((key, durations) -> {
            if (!durations.isEmpty()) {
                double avg = durations.stream().mapToLong(Duration::toMillis).average().orElse(0.0);
                metrics.append("# TYPE ").append(key).append("_avg gauge\n");
                metrics.append(key).append("_avg ").append(avg).append("\n");
            }
        });
        
        return metrics.toString();
    }
    
    // Reset metrics (for testing)
    public static void reset() {
        counters.clear();
        timings.clear();
        gauges.clear();
        activeTests.set(0);
        queuedTests.set(0);
        availableNodes.set(0);
        featureCounters.clear();
        businessProcessTimers.clear();
    }
    
    // Get all metrics for reporting
    public static Map<String, Object> getAllMetrics() {
        Map<String, Object> allMetrics = new HashMap<>();
        allMetrics.put("counters", new HashMap<>(counters));
        allMetrics.put("gauges", new HashMap<>(gauges));
        allMetrics.put("timings", calculateTimingSummaries());
        return allMetrics;
    }
    
    private static Map<String, Map<String, Double>> calculateTimingSummaries() {
        Map<String, Map<String, Double>> summaries = new HashMap<>();
        
        timings.forEach((key, durations) -> {
            if (!durations.isEmpty()) {
                Map<String, Double> summary = new HashMap<>();
                List<Long> times = durations.stream().mapToLong(Duration::toMillis).sorted().boxed().toList();
                
                summary.put("min", (double) times.get(0));
                summary.put("max", (double) times.get(times.size() - 1));
                summary.put("avg", times.stream().mapToDouble(Long::doubleValue).average().orElse(0.0));
                summary.put("p50", (double) times.get(times.size() / 2));
                summary.put("p95", (double) times.get((int) (times.size() * 0.95)));
                summary.put("p99", (double) times.get((int) (times.size() * 0.99)));
                
                summaries.put(key, summary);
            }
        });
        
        return summaries;
    }
}
