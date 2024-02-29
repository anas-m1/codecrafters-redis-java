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
    private final Server serverDetails;
    public Socket clientSocket;


    public ClientHandler(Socket socket, Server serverDetails) {
        this.clientSocket = socket;
        this.serverDetails = serverDetails;
    }

    public void run() {

        try {
            InputStream inputStream = clientSocket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";

//            int bytesToRead=0;
//            while(inputStream.available() != -1) {
//                System.out.println(inputStream.read() + "::bytes");
//                int byteValue = inputStream.read();
//                char ch = (char)byteValue;
//                if(ch=='*'){
//                    int byteValue = inputStream.read();
//                    char ch = (char)byteValue;
//                }
//            }

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
                System.out.println("received command:  "+cmdList.get(0));
                if (actionVerb.equalsIgnoreCase("ping")) {
                    Printer.printPong(clientSocket);
                } else if (actionVerb.equalsIgnoreCase("echo")) {
                    String arg = cmdList.get(1);
                    Printer.printEcho(clientSocket, arg);
                } else if (actionVerb.equalsIgnoreCase("set")) {
                    System.out.println("received setcommand:  "+cmdList.get(1));
                    handlSetCommand(cmdList);
                } else if (actionVerb.equalsIgnoreCase("get")) {
                    handlGetCommand(cmdList);
                }
                else if(actionVerb.equalsIgnoreCase("replconf")){
                    ((MasterServer)serverDetails).handleReplConfReqFromSlave(clientSocket);
                }
                else if(actionVerb.equalsIgnoreCase("PSYNC")){
                    ((MasterServer)serverDetails).respondToPsyncFromSlave(clientSocket);
//                    Printer.respondToPsyncFromSlave(clientSocket);
                }

                for (int i = 0; i < cmdList.size(); i++) {
                    if (cmdList.get(i).equalsIgnoreCase("info")) {
                        if (cmdList.get(i + 1).equalsIgnoreCase("replication")) {
                            serverDetails.sendReplicationDetailsToClient(this.clientSocket);
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
        HashMap<String,RedisEntry> redisStore=this.serverDetails.getRedisStore();
        if ( redisStore.containsKey(key)) {
            RedisEntry entry = redisStore.get(key);

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
        this.serverDetails.getRedisStore().put(key, entry);

        if(serverDetails.getType().equalsIgnoreCase("master")){
            System.out.println("master is adding to queue all the available commands");
            List<String> strList = cmdList.subList(0,3);
            ((MasterServer)serverDetails).addToSetCommandQueue(RedisParser.getRespStr(strList));
            Printer.printOK(clientSocket);
        }else{
           //do nothing i.e , do not print if it current server is slave and set is coming from master socket
            if(clientSocket==((SlaveServer)serverDetails).getSocketToMaster()){
                System.out.println("slave got set commands from store of master");
            }
            else{
                System.out.println("slave got set commands from clients");
                Printer.printOK(clientSocket);
            }
        }
    }
}

