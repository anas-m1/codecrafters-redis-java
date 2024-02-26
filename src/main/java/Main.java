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

        clientSocket = serverSocket.accept();
        ClientHandler clientHandler=new ClientHandler(clientSocket);

        executorService.submit(clientHandler::run);

//        clientHandler.handle(clientSocket);

//        InputStream inputStream= clientSocket.getInputStream();
//        InputStreamReader inputStreamReader= new InputStreamReader(inputStream);
//
//        BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
//        String line="";
//        while((line=bufferedReader.readLine()) !=null){
//            Pattern pattern = Pattern.compile(Pattern.quote("ping"));
//            // Create a matcher with the input string
//            Matcher matcher = pattern.matcher(line);
//            while(matcher.find()){
//                printPong(clientSocket);
//            }
//        }

//          printPong(clientSocket);

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
  }


}
