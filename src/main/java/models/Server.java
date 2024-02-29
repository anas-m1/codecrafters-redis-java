package models;

import lombok.Data;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

@Data
public abstract class Server {
    public String type;
    public int selfServerPort;
    HashMap<String, RedisEntry> redisStore;
    public abstract void sendReplicationDetailsToClient(Socket clientSocket) throws Exception;

    Server(int selfServerPort){
        this.selfServerPort =selfServerPort;
        this.redisStore = new HashMap<>();
    }

    public void start(ExecutorService executorService) throws IOException {
        ServerSocket serverSocket = new ServerSocket(this.selfServerPort);
        serverSocket.setReuseAddress(true);

        if(this.getType().equalsIgnoreCase("slave")){
            ((SlaveServer) this).handshakeWithMaster();
        }

        // Wait for connection from client.
        while(true){
            Socket clientSocket = serverSocket.accept();
            System.out.println("new client connection");
            ClientHandler clientHandler=new ClientHandler(clientSocket,this);
            executorService.submit(clientHandler::run);
        }
    }
}
