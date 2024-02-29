package models;

import utils.Printer;
import utils.RedisParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.*;

import static java.lang.Integer.parseInt;

public class ClientHandler implements Runnable {
    private final Server serverOfThis;
    private final String socketType;
    public Socket clientSocket;


    public ClientHandler(Socket socket, Server serverOfThis,String socketType) {
        this.clientSocket = socket;
        this.serverOfThis = serverOfThis;
        this.socketType=socketType;
    }

    public void run() {

        try {
            InputStream inputStream = clientSocket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line+"     :line");
                List<String> cmdList = new ArrayList<>();

                if(line.startsWith("+FULLRESYNC")){
                    handleFullResync(bufferedReader, cmdList);
                }

                if (line.charAt(0) == '*') {
                    int numWords = parseInt(line.substring(1));
                    for (int i = 0; i < numWords; i++) {
                        String wordLenLine = bufferedReader.readLine();
                        int WordLength = parseInt(wordLenLine.substring(1));
                        String word = bufferedReader.readLine();
                        cmdList.add(word);
                    }
                }

                if(cmdList.size() <= 0) {
                    System.out.println("zero length in cmdlist");
                    continue;
                }

                String actionVerb = cmdList.get(0);
                System.out.println("received command: "+cmdList.get(0));
                if (actionVerb.equalsIgnoreCase("ping")) {
                    System.out.println("ping received");
                    if(socketType.equalsIgnoreCase("socketToMaster")){
                        ((SlaveServer)serverOfThis).handlePingFromMaster(clientSocket);
                    }
                    else{
                        Printer.printPong(clientSocket);
                    }
                } else if (actionVerb.equalsIgnoreCase("echo")) {
                    String arg = cmdList.get(1);
                    Printer.printEcho(clientSocket, arg);
                } else if (actionVerb.equalsIgnoreCase("set")) {
                    System.out.println("received setcommand:  "+cmdList.get(1));
                    handlSetCommand(cmdList);
                } else if (actionVerb.equalsIgnoreCase("get")) {
                    handlGetCommand(cmdList);
                }
                else if(actionVerb.equalsIgnoreCase("replconf" )){
                    //REPLCONF GETACK *
                    if(cmdList.get(1).equalsIgnoreCase("getack")) {
                        if(cmdList.get(2).equalsIgnoreCase("*")){
                            ((SlaveServer)serverOfThis).handleReplConfGetAckFromMaster(clientSocket,cmdList);
                        }
                    }
                    else{
                        if(cmdList.get(1).equalsIgnoreCase("ack")) {
                            ((MasterServer)serverOfThis).handleReplConfAckResponseFromSlave(clientSocket,cmdList);
                        }
                        // for initial replconf (in handshake )
                        else ((MasterServer)serverOfThis).handleReplConfReqFromSlave(clientSocket);
                    }
                }
                else if(actionVerb.equalsIgnoreCase("PSYNC")){
                    ((MasterServer)serverOfThis).respondToPsyncFromSlave(clientSocket);
                }
                else if(actionVerb.equalsIgnoreCase("wait")){
                    ((MasterServer)serverOfThis).respondToWaitFromClient(clientSocket,cmdList);
                }

                for (int i = 0; i < cmdList.size(); i++) {
                    if (cmdList.get(i).equalsIgnoreCase("info")) {
                        if (cmdList.get(i + 1).equalsIgnoreCase("replication")) {
                            serverOfThis.sendReplicationDetailsToClient(this.clientSocket);
                        }
                    }
                }
            }

            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private void handleFullResync(BufferedReader bufferedReader,List<String> cmdList) throws IOException {
        //  after +FULLRESYNC <> <offset>\r\n is sent, rdb file in below format is sent
        //        $<length>\r\n<contents>
        String lengthStr=bufferedReader.readLine();
        int numBytes= Integer.parseInt(lengthStr.substring(1));

        char[] buffer = new char[numBytes];
        int bytesRead=bufferedReader.read(buffer,0,numBytes-1);
        String rdbContentsStr = new String(buffer, 0, numBytes);
    }

    private void handlGetCommand(List<String> cmdList) throws Exception {
        String key = cmdList.get(1);
        HashMap<String,RedisEntry> redisStore=this.serverOfThis.getRedisStore();
        System.out.println(key+"  :key");
        if ( redisStore.containsKey(key)) {
            RedisEntry entry = redisStore.get(key);
            System.out.println(key+"  :key1");

            if (entry.getExpiryAt() > System.currentTimeMillis()) {
                Printer.printEcho(clientSocket, entry.getValue());
            } else {
                redisStore.remove(key);
                Printer.printNullBulk(clientSocket);
            }
        } else {
            Printer.printNullBulk(clientSocket);
        }
    }

    private void handlSetCommand(List<String> cmdList) throws Exception {
        String key = cmdList.get(1);
        String value = cmdList.get(2);

        RedisEntry entry = new RedisEntry(key, value) ;

        if (cmdList.size() > 3) {
            if (cmdList.get(3).equalsIgnoreCase("px")) {
                long timeToLive = Long.parseLong(cmdList.get(4));
                long currUNIXts = System.currentTimeMillis();
                entry.setExpiryAt(currUNIXts + timeToLive);
            }
        }
        this.serverOfThis.getRedisStore().put(key, entry);


        if(socketType.equalsIgnoreCase("socketFromClient")){
            System.out.println("master  is adding to queue all the available commands");
            List<String> strList = cmdList;
            ((MasterServer)serverOfThis).addToSetCommandQueue(RedisParser.getRespStr(strList));
            Printer.printOK(clientSocket);
        }else{
           //do nothing i.e , do not print if it current server is slave and set is coming from master socket
            if(socketType.equalsIgnoreCase("socketToMaster")){
                System.out.println("slave got set commands from store of master");
                ((SlaveServer)serverOfThis).handleSetCommandFromMaster(clientSocket,cmdList);
            }
//            else{
//                System.out.println("slave got set commands from clients");
//                Printer.printOK(clientSocket);
//            }
        }
    }
}

