package utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class Printer {
    static String clrf="\r\n";

    public static void printPong(Socket clientSocket) throws Exception {
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

    public static void printOK(Socket clientSocket) throws Exception {
        OutputStream outputStream=clientSocket.getOutputStream();
        byte[] byteArr="+OK\r\n".getBytes();
        outputStream.write(byteArr);
        outputStream.flush();
    }

    public static void printNullBulk(Socket clientSocket) throws Exception {
        OutputStream outputStream=clientSocket.getOutputStream();
        byte[] byteArr="*-1\r\n".getBytes();
        outputStream.write(byteArr);
        outputStream.flush();
    }

    public static void printInfo(Socket clientSocket, HashMap<String, String> infoMap) throws Exception {
        String infoStr="";
        StringBuilder keyValStrBuilder=new StringBuilder();
        String clrf="\r\n";
        for(String key : infoMap.keySet()){
            String val=infoMap.get(key);
            keyValStrBuilder.append(key+":"+val);
            keyValStrBuilder.append(clrf);
        }
        Integer zlen=keyValStrBuilder.toString().length();
        infoStr="$"+zlen.toString()+clrf+keyValStrBuilder+clrf;

        System.out.println(infoStr+":infostr");
        OutputStream outputStream=clientSocket.getOutputStream();
        byte[] byteArr=infoStr.getBytes();
        outputStream.write(byteArr);
        outputStream.flush();
    }

    public static void sendPing(Socket socket) throws IOException {
        OutputStream outputStream= socket.getOutputStream();
        outputStream.write("*1\r\n$4\r\nping\r\n".getBytes());
        outputStream.flush();
    }

    public static void sendReplConfigToMaster(Socket socket, int port) throws IOException {
        OutputStream outputStream= socket.getOutputStream();
        int portLen= String.valueOf(port).length();
        outputStream.write(("*3\r\n$8\r\nREPLCONF\r\n$14\r\nlistening-port\r\n$"+portLen+"\r\n"+String.valueOf(port)+"\r\n").getBytes());
        outputStream.flush();
        outputStream.write(("*3\r\n$8\r\nREPLCONF\r\n$4\r\ncapa\r\n$6\r\npsync2\r\n").getBytes());
        outputStream.flush();
    }

    public static void sendPsyncToServer(Socket masterSocket) throws IOException {
        OutputStream outputStream= masterSocket.getOutputStream();
        outputStream.write(("*3\r\n$5\r\nPSYNC\r\n$1\r\n?\r\n$2\r\n-1\r\n").getBytes());
        outputStream.flush();
    }

    public static void respondToReplConfFromSlave(Socket clientSocket) throws IOException {
        OutputStream outputStream=clientSocket.getOutputStream();
        outputStream.write("+OK\r\n".getBytes());
        outputStream.flush();
    }

    public static void respondToPsyncFromSlave(Socket clientSocket, String replid, int offset) throws IOException {
        OutputStream outputStream=clientSocket.getOutputStream();
        outputStream.write(("+FULLRESYNC "+replid+" "+offset+"\r\n").getBytes());

        outputStream.flush();
        String emptyRDBbase64="524544495330303131fa0972656469732d76657205372e322e30fa0a72656469732d62697473c040fa056374696d65c26d08bc65fa08757365642d6d656dc2b0c41000fa08616f662d62617365c000fff06e3bfec0ff5aa2";
        outputStream.write(("$"+emptyRDBbase64.length()+clrf+emptyRDBbase64).getBytes());

    }
}
