package models;
import lombok.Data;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Data
public abstract class Server {
    public String type;
    public int selfServerPort;
    HashMap<String, RedisEntry> redisStore;
    public List<Socket> clientSockets;

    public abstract void sendReplicationDetailsToClient(Socket clientSocket) throws Exception;

    Server(int selfServerPort){
        this.selfServerPort =selfServerPort;
        this.redisStore = new HashMap<>();
        this.clientSockets=new ArrayList<>();
    }

    public void start(ExecutorService executorService) throws IOException {
        System.out.println("here:  " + selfServerPort);
        ServerSocket serverSocket = new ServerSocket(this.selfServerPort);
        serverSocket.setReuseAddress(true);

        System.out.println("here2");
        if(this.getType().equalsIgnoreCase("slave")){
            System.out.println("here3");
            System.out.println("here:  " + ((SlaveServer)this).masterHost+ ((SlaveServer)this).masterPort);
            Socket socketToMaster=new Socket(((SlaveServer)this).masterHost, ((SlaveServer)this).masterPort);
            ((SlaveServer)this).socketToMaster=socketToMaster;
            ClientHandler clientHandler=new ClientHandler(socketToMaster,this,"socketToMaster");
            executorService.submit(clientHandler::run);
            ((SlaveServer) this).handshakeWithMaster();
        }


        // Wait for connection from client.
        while(true){
            Socket clientSocket = serverSocket.accept();
            this.clientSockets.add(clientSocket);
            System.out.println("new client connection");
            ClientHandler clientHandler=new ClientHandler(clientSocket,(Server)this,"socketFromClient");
            executorService.submit(clientHandler::run);
        }
    }
}
