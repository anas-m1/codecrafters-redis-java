package models;

import lombok.Data;
import utils.Printer;

import java.net.Socket;
import java.util.HashMap;

@Data
public abstract class Server {
    public String type;
    public int selfServerPort;
    HashMap<String, RedisEntry> redisStore;
    public abstract void sendReplicationDetailsToClient(Socket clientSocket) throws Exception;

    Server(){
        this.redisStore = new HashMap<>();
    }
}
