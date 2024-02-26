import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Printer {

    static void printPong(Socket clientSocket) throws Exception {
        OutputStream outputStream=clientSocket.getOutputStream();
        byte[] byteArr="+PONG\r\n".getBytes();
        outputStream.write(byteArr);
        outputStream.flush();
//        outputStream.close();
    }

    public static void printEcho(Socket clientSocket, String arg) throws IOException {
        OutputStream outputStream=clientSocket.getOutputStream();
        byte[] byteArr="$3\r\nhey\r\n".getBytes();
        outputStream.write(byteArr);
        outputStream.flush();
    }
}
