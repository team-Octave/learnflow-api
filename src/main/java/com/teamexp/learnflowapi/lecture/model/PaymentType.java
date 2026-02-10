package com.teamexp.learnflowapi.lecture.model;

public enum PaymentType {
    FREE("FREE"),
    PAID("PAID");
    
    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentType forEntity(String displayName) {
        for (PaymentType type : PaymentType.values()) {
            if (type.getDisplayName().equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid PaymentType: " + displayName);
    }
}
