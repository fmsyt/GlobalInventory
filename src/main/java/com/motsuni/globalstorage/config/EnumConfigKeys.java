package com.motsuni.globalstorage.config;

public enum EnumConfigKeys {
    MAX_PULL_AMOUNT("item.max_pull_amount"),
    BACKUP_INTERVAL("server.backup_interval");

    private final String key;

    EnumConfigKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
