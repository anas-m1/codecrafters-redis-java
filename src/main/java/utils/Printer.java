package utils;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.*;

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
        System.out.println("sent psync to master");
        OutputStream outputStream= masterSocket.getOutputStream();
        outputStream.write(("*3\r\n$5\r\nPSYNC\r\n$1\r\n?\r\n$2\r\n-1\r\n").getBytes());
        outputStream.flush();
    }

    public static void respondToReplConfFromSlave(Socket clientSocket) throws IOException {
        OutputStream outputStream=clientSocket.getOutputStream();
        outputStream.write("+OK\r\n".getBytes());
        outputStream.flush();
    }

    public static void respondToPsyncFromSlave(Socket slaveSocket, String replid, int offset) throws IOException {
        OutputStream outputStream=slaveSocket.getOutputStream();
        outputStream.write(("+FULLRESYNC "+replid+" "+offset+"\r\n").getBytes());
//        outputStream.flush();

        String base64str="UkVESVMwMDEx+glyZWRpcy12ZXIFNy4yLjD6CnJlZGlzLWJpdHPAQPoFY3RpbWXCbQi8ZfoIdXNlZC1tZW3CsMQQAPoIYW9mLWJhc2XAAP/wbjv+wP9aog==";
        byte[] rdbBytes=Base64.getDecoder().decode(base64str);
        int lenrdbBytesStr=rdbBytes.length *2;

        outputStream.write(("$"+lenrdbBytesStr+clrf).getBytes());
        outputStream.write(rdbBytes);
        outputStream.flush();
    }

    public static void sendCommand(Socket socket, String respMsg) throws IOException {
        System.out.println("sending command to client socket"+socket.getRemoteSocketAddress());
        System.out.println("sending command to client socket:  "+respMsg);
        OutputStream outputStream=socket.getOutputStream();
        outputStream.write(respMsg.getBytes());
        outputStream.flush();
    }
}