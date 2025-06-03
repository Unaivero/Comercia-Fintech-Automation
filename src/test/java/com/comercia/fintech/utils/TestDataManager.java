package com.comercia.fintech.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Clase utilitaria para generar y gestionar datos de prueba
 */
public class TestDataManager {
    
    private static final Random random = new Random();
    
    // Datos de tarjetas de prueba
    public static final Map<String, String> VALID_CARDS = new HashMap<>();
    public static final Map<String, String> INVALID_CARDS = new HashMap<>();
    
    static {
        // Tarjetas válidas para diferentes proveedores
        VALID_CARDS.put("VISA", "4000000000000000");
        VALID_CARDS.put("VISA_DEBIT", "4000000000000002");
        VALID_CARDS.put("MASTERCARD", "5555555555554444");
        VALID_CARDS.put("MASTERCARD_DEBIT", "5200828282828210");
        VALID_CARDS.put("AMEX", "378282246310005");
        
        // Tarjetas que generan errores específicos
        INVALID_CARDS.put("EXPIRED", "4111111111111111");
        INVALID_CARDS.put("INSUFFICIENT_FUNDS", "4000000000000341");
        INVALID_CARDS.put("INVALID_CVC", "4000000000000127");
        INVALID_CARDS.put("PROCESSING_ERROR", "4000000000000119");
        INVALID_CARDS.put("DECLINED", "4000000000000002");
    }
    
    /**
     * Genera un número de tarjeta válido aleatorio
     */
    public static String getRandomValidCard() {
        String[] cards = VALID_CARDS.values().toArray(new String[0]);
        return cards[random.nextInt(cards.length)];
    }
    
    /**
     * Obtiene una tarjeta específica por tipo
     */
    public static String getCardByType(String type) {
        return VALID_CARDS.getOrDefault(type.toUpperCase(), VALID_CARDS.get("VISA"));
    }
    
    /**
     * Obtiene una tarjeta inválida por tipo de error
     */
    public static String getInvalidCardByType(String errorType) {
        return INVALID_CARDS.getOrDefault(errorType.toUpperCase(), INVALID_CARDS.get("EXPIRED"));
    }
    
    /**
     * Genera un nombre de titular aleatorio
     */
    public static String generateRandomCardholderName() {
        String[] firstNames = {"John", "Jane", "Michael", "Sarah", "David", "Emma", "James", "Lisa"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};
        
        String firstName = firstNames[random.nextInt(firstNames.length)];
        String lastName = lastNames[random.nextInt(lastNames.length)];
        
        return firstName + " " + lastName;
    }
    
    /**
     * Genera un CVV aleatorio
     */
    public static String generateRandomCVV() {
        return String.format("%03d", random.nextInt(1000));
    }

    /**
     * Genera una fecha de expiración válida futura
     */
    public static String[] generateValidExpiryDate() {
        int currentYear = java.time.Year.now().getValue();
        int futureYear = currentYear + random.nextInt(5) + 1; // 1-5 años en el futuro
        int month = random.nextInt(12) + 1;
        
        return new String[]{
            String.format("%02d", month),
            String.valueOf(futureYear)
        };
    }
    
    /**
     * Genera una fecha de expiración expirada
     */
    public static String[] generateExpiredExpiryDate() {
        int currentYear = java.time.Year.now().getValue();
        int pastYear = currentYear - random.nextInt(3) - 1; // 1-3 años en el pasado
        int month = random.nextInt(12) + 1;
        
        return new String[]{
            String.format("%02d", month),
            String.valueOf(pastYear)
        };
    }
    
    /**
     * Genera un ID de transacción único
     */
    public static String generateTransactionId() {
        return "txn_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
    }
    
    /**
     * Genera un monto aleatorio para pruebas
     */
    public static double generateRandomAmount() {
        return Math.round((10.0 + (random.nextDouble() * 990.0)) * 100.0) / 100.0; // Entre 10.00 y 1000.00
    }
    
    /**
     * Genera datos de prueba completos para un pago
     */
    public static PaymentTestData generateCompletePaymentData() {
        return new PaymentTestData(
            getRandomValidCard(),
            generateRandomCardholderName(),
            generateRandomCVV(),
            generateValidExpiryDate(),
            generateRandomAmount(),
            generateTransactionId()
        );
    }
    
    /**
     * Clase interna para encapsular datos de pago de prueba
     */
    public static class PaymentTestData {
        private final String cardNumber;
        private final String cardholderName;
        private final String cvv;
        private final String[] expiryDate;
        private final double amount;
        private final String transactionId;
        
        public PaymentTestData(String cardNumber, String cardholderName, String cvv, 
                              String[] expiryDate, double amount, String transactionId) {
            this.cardNumber = cardNumber;
            this.cardholderName = cardholderName;
            this.cvv = cvv;
            this.expiryDate = expiryDate;
            this.amount = amount;
            this.transactionId = transactionId;
        }
        
        // Getters
        public String getCardNumber() { return cardNumber; }
        public String getCardholderName() { return cardholderName; }
        public String getCvv() { return cvv; }
        public String getExpiryMonth() { return expiryDate[0]; }
        public String getExpiryYear() { return expiryDate[1]; }
        public double getAmount() { return amount; }
        public String getTransactionId() { return transactionId; }
    }
}
