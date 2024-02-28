package models;

import lombok.Data;

@Data
public class RedisEntry {
    String key;
    String value;
    long expiryAt;
    public RedisEntry(String key, String value) {
        this.key = key;
        this.value = value;
        this.expiryAt = Long.MAX_VALUE;
    }

    public void setExpiryAt(long expiryAt) {
        this.expiryAt = expiryAt;
    }

    public long getExpiryAt() {
        return this.expiryAt;
    }
}
