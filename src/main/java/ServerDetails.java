import lombok.Data;

@Data
public class ServerDetails {
    public String replid;
    public int offset;
    public String type;
    public String masterHost;
    public String masterPort;

}
