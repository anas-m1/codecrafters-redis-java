import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

//      Uncomment this block to pass the first stage
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        try {
          serverSocket = new ServerSocket(port);
          serverSocket.setReuseAddress(true);
          // Wait for connection from client.
          clientSocket = serverSocket.accept();

            InputStream inputStream= clientSocket.getInputStream();
            InputStreamReader inputStreamReader= new InputStreamReader(inputStream);

            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            String line="";
            while((line=bufferedReader.readLine()) !=null){
                for(int i=1;i<line.length();i++){
                    if(line.charAt(i-1)=='\\' && line.charAt(i)=='n'){
                        System.out.println("got \\n");
                        printPong(clientSocket);
                    }
                }
            }

//          printPong(clientSocket);

        } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
          try {
            if (clientSocket != null) {
              clientSocket.close();
            }
          }
          catch (Exception e) {
            System.out.println("IOException: " + e.getMessage());
          }
        }
  }

    private static void printPong(Socket clientSocket) throws Exception {
        OutputStream outputStream=clientSocket.getOutputStream();
        byte[] byteArr="+PONG\r\n".getBytes();
        outputStream.write(byteArr);
        outputStream.flush();
    }
}
