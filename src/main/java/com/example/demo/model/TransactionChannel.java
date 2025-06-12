package com.example.demo.model;

public enum TransactionChannel {
    OTC("OTC", "Over the Counter"),
    ATS("ATS", "Automated Teller System");

    private final String channel;
    private final String description;

    TransactionChannel(String channel, String description) {
        this.channel = channel;
        this.description = description;
    }

    public String getChannel() {
        return channel;
    }

    public String getDescription() {
        return description;
    }

    public static TransactionChannel fromChannel(String channel) {
        for (TransactionChannel code : TransactionChannel.values()) {
            if (code.getChannel().equals(channel)) {
                return code;
            }
        }
        throw new IllegalArgumentException("Invalid transaction type code: " + channel);
    }
}