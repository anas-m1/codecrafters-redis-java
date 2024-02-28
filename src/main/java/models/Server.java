package models;

import lombok.Data;

import java.net.Socket;

@Data
public abstract class Server {
    public String type;
    public abstract void sendReplicationDetailsToClient(Socket clientSocket) throws Exception;
}
