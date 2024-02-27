import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
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
        StringBuilder keyValStrBuilder=new StringBuilder();
        String clrf="\r\n";
        for(String key : infoMap.keySet()){
            String val=infoMap.get(key);
            keyValStrBuilder.append(key+":"+val);
            keyValStrBuilder.append(clrf);
        }
        Integer zlen=keyValStrBuilder.length()-4;
        infoStr="$"+zlen.toString()+clrf+keyValStrBuilder;

        System.out.println(infoStr+":infostr");
        OutputStream outputStream=clientSocket.getOutputStream();
        byte[] byteArr=infoStr.getBytes();
        outputStream.write(byteArr);
        outputStream.flush();
    }

    public static void sendPing(String masterHost, String masterPort) throws IOException {
        //            *1\r\n$4\r\nping\r\n
        Socket masterSocket=new Socket(masterHost, Integer.parseInt(masterPort,10));

//        PrintWriter writer = new PrintWriter(
//                new OutputStreamWriter(masterSocket.getOutputStream()));
//        String pingCommand = "*1\r\n$4\r\nPING\r\n";
//        writer.print(pingCommand);
//        writer.flush();

        OutputStream outputStream= masterSocket.getOutputStream();
        outputStream.write("*1\r\n$4\r\nping\r\n".getBytes());
        outputStream.flush();
    }
}
