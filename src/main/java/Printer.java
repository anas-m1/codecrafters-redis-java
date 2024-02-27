import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

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
        byte[] byteArr=("$"+arg.length()+"\r\n"+arg+"\r\n").getBytes();
        outputStream.write(byteArr);
        outputStream.flush();
    }

    static void printOK(Socket clientSocket) throws Exception {
        OutputStream outputStream=clientSocket.getOutputStream();
        byte[] byteArr="+OK\r\n".getBytes();
        outputStream.write(byteArr);
        outputStream.flush();
    }

    static void printNullBulk(Socket clientSocket) throws Exception {
        OutputStream outputStream=clientSocket.getOutputStream();
        byte[] byteArr="*-1\r\n".getBytes();
        outputStream.write(byteArr);
        outputStream.flush();
    }

    static void printInfo(Socket clientSocket, HashMap<String, String> infoMap) throws Exception {
        String infoStr="";
        StringBuilder infoStrBuilder=new StringBuilder();
        String clrf="\r\n";
        for(String key : infoMap.keySet()){
            String val=infoMap.get(key);
            infoStrBuilder.append("$");
            infoStrBuilder.append(key.length()+val.length()+1);
            infoStrBuilder.append(clrf);
            infoStrBuilder.append(key+":"+val);
            infoStrBuilder.append(clrf);
        }
        infoStr=infoStrBuilder.toString();

        OutputStream outputStream=clientSocket.getOutputStream();
        byte[] byteArr=infoStr.getBytes();
        outputStream.write(byteArr);
        outputStream.flush();
    }
}
