import models.*;
import utils.Printer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  public static void main(String[] args) throws IOException {

      System.out.println("Logs from your program will appear here!");
      ExecutorService executorService= Executors.newCachedThreadPool();
      ServerSocket serverSocket = null;
      Socket clientSocket = null;

      int port = 6379;
      Server serverDetails=null;
      String serverType="master";
      String masterHost="localhost";
      int masterPort=port;

      for(int i=0;i<args.length;i++){
          String x=args[i];
          if(x.equalsIgnoreCase("--port")){
              port=Integer.parseInt(args[i+1]);
          }
          else if(x.equalsIgnoreCase("--replicaof")){
              serverType="slave";
              masterHost=args[i+1];
              masterPort=Integer.parseInt(args[i+2]);
          }
      }

      if(serverType=="master"){
          serverDetails=new MasterServer();
          serverDetails.setType("master");
          ((MasterServer) serverDetails).setReplid("8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb");
          ((MasterServer) serverDetails).setOffset(0);
      }
      else{
          serverDetails=new SlaveServer();
          serverDetails.setType("slave");
          ((SlaveServer) serverDetails).setMasterHost(masterHost);
          ((SlaveServer) serverDetails).setMasterPort(masterPort);
          Socket socketToMaster=new Socket(masterHost, masterPort);
          ((SlaveServer) serverDetails).setSocketToMaster(socketToMaster);
          ((SlaveServer) serverDetails).setExecutorService(executorService);
      }

      serverDetails.setSelfServerPort(port);


    try {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);

        if(serverDetails.getType().equalsIgnoreCase("slave")){
            ((SlaveServer) serverDetails).handshakeWithMaster();
        }

        // Wait for connection from client.
        while(true){
            clientSocket = serverSocket.accept();
            System.out.println("new client connection");
            ClientHandler clientHandler=new ClientHandler(clientSocket,serverDetails);
            executorService.submit(clientHandler::run);
        }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
  }
}
