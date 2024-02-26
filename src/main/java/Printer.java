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
}
