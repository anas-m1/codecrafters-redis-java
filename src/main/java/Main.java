import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
  public static void main(String[] args){


      System.out.println("Logs from your program will appear here!");
      ExecutorService executorService= Executors.newCachedThreadPool();
      ServerSocket serverSocket = null;
      Socket clientSocket = null;

    int port = 6379;
    try {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        // Wait for connection from client.

        while(true){
            clientSocket = serverSocket.accept();
            ClientHandler clientHandler=new ClientHandler(clientSocket);

            executorService.submit(clientHandler::run);
        }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
  }


}
