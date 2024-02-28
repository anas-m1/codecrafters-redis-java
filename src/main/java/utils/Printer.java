package utils;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        FileWriter writer=new FileWriter("./emptyFile");
        writer.write("");
        writer.close();
        FileInputStream fileInputStream=new FileInputStream("./emptyFile");
        byte[] byteArr= fileInputStream.readAllBytes();

        String rdbStr=byteArr.toString();

        String binStr="01010010010001010100010001001001010100110011000000110000001100010011000111111010000010010111001001100101011001000110100101110011001011010111011001100101011100100000010100110111001011100011001000101110001100001111101000001010011100100110010101100100011010010111001100101101011000100110100101110100011100111100000001000000111110100000010101100011011101000110100101101101011001011100001001101101000010001011110001100101111110100000100001110101011100110110010101100100001011010110110101100101011011011100001010110000110001000001000000000000111110100000100001100001011011110110011000101101011000100110000101110011011001011100000000000000111111111111000001101110001110111111111011000000111111110101101010100010";
        outputStream.write(("$"+binStr.length()+clrf).getBytes());
        outputStream.write(binStr.getBytes());
        outputStream.flush();
    }
}
