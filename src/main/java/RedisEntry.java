import lombok.Data;

@Data
public class RedisEntry {
    String key;
    String value;
    public RedisEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
