import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  public static void main(String[] args){

      HashMap<String,RedisEntry> redisStore = new HashMap<>();

      System.out.println("Logs from your program will appear here!");
      ExecutorService executorService= Executors.newCachedThreadPool();
      ServerSocket serverSocket = null;
      Socket clientSocket = null;

      int port = 6379;
//      null if self is master
      ServerDetails serverDetails=new ServerDetails();
      serverDetails.setType("master");
//      MasterServerOfSelf masterServerOfSelf=null;
      for(int i=0;i<args.length;i++){
          String x=args[i];
          System.out.println(x+" : arg");
          if(x.equalsIgnoreCase("--port")){
              port=Integer.parseInt(args[i+1]);
          }
          else if(x.equalsIgnoreCase("--replicaof")){
              serverDetails.setType("slave");
              serverDetails.setMasterHost(args[i+1]);
              serverDetails.setMasterPort(args[i+1]);
          }
      }

      if(serverDetails.getType().equalsIgnoreCase("master")){
          serverDetails.setReplid("8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb");
          serverDetails.setOffset(0);
      }

      System.out.println(port+" :port");

    try {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        // Wait for connection from client.

        while(true){
            clientSocket = serverSocket.accept();
            ClientHandler clientHandler=new ClientHandler(clientSocket,redisStore,serverDetails);
            executorService.submit(clientHandler::run);
        }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
  }


}
