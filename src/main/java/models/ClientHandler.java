package models;

import utils.Printer;
import utils.RedisParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.*;

import static java.lang.Integer.parseInt;

public class ClientHandler implements Runnable {
    private final Server serverDetails;
    public Socket clientSocket;
    HashMap<String, RedisEntry> redisStore;

    public ClientHandler(Socket socket, HashMap<String, RedisEntry> store, Server serverDetails) {
        this.clientSocket = socket;
        this.redisStore = store;
        this.serverDetails = serverDetails;
    }

    ;

    public void run() {

        try {
            InputStream inputStream = clientSocket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                List<String> cmdList = new ArrayList<>();
                System.out.println(line + ": line");
                if (line.charAt(0) == '*') {
                    int numWords = parseInt(line.substring(1));
                    for (int i = 0; i < numWords; i++) {
                        String wordLenLine = bufferedReader.readLine();
                        int WordLength = parseInt(wordLenLine.substring(1));
                        String word = bufferedReader.readLine();
                        System.out.println(word + " :word");
                        cmdList.add(word);
                    }
                }


                String actionVerb = cmdList.get(0);
                System.out.println(actionVerb + " : action");
                System.out.println(this.clientSocket + " :socket");
                if (actionVerb.equalsIgnoreCase("ping")) {
                    Printer.printPong(clientSocket);
                } else if (actionVerb.equalsIgnoreCase("echo")) {
                    String arg = cmdList.get(1);
                    Printer.printEcho(clientSocket, arg);
                } else if (actionVerb.equalsIgnoreCase("set")) {
                    String key = cmdList.get(1);
                    String value = cmdList.get(2);


                    if(serverDetails.getType().equalsIgnoreCase("master")){
                        List<String> strList = cmdList.subList(0,2);
                        Queue<String> setCommandQueue=new LinkedList<>();
                        setCommandQueue.add(RedisParser.getRespStr(strList));

                        ((MasterServer)serverDetails).setSetCommandQueue(setCommandQueue);
                    }

                    System.out.println("key: " + key + " value: " + value);
                    RedisEntry entry = new RedisEntry(key, value);

                    if (cmdList.size() > 3) {
                        if (cmdList.get(3).equalsIgnoreCase("px")) {
                            long timeToLive = Long.parseLong(cmdList.get(4));
                            System.out.println("key: " + key + " value: " + value + " timeToLive: " + timeToLive);
                            long currUNIXts = System.currentTimeMillis();
                            entry.setExpiryAt(currUNIXts + timeToLive);
                        }
                    }
                    redisStore.put(key, entry);
                    Printer.printOK(clientSocket);
                } else if (actionVerb.equalsIgnoreCase("get")) {
                    String key = cmdList.get(1);
                    System.out.println("key: " + key);
                    if (redisStore.containsKey(key)) {
                        RedisEntry entry = redisStore.get(key);
                        System.out.println(entry);
                        System.out.println("hello " + entry.getExpiryAt() + " seconds " + System.currentTimeMillis());

                        if (entry.getExpiryAt() > System.currentTimeMillis()) {
                            System.out.println(entry.getExpiryAt() + " seconds " + System.currentTimeMillis());
                            Printer.printEcho(clientSocket, entry.getValue());
                        } else {
                            redisStore.remove(key);
                            Printer.printNullBulk(clientSocket);
                        }
                    } else {
                        Printer.printNullBulk(clientSocket);
                    }
                }
                else if(actionVerb.equalsIgnoreCase("replconf")){
                    ((MasterServer)serverDetails).handleReplConfReqFromSlave(clientSocket);

                }
                else if(actionVerb.equalsIgnoreCase("PSYNC")){
                    ((MasterServer)serverDetails).respondToPsyncFromSlave(clientSocket);
//                    Printer.respondToPsyncFromSlave(clientSocket);
                }

                for (int i = 0; i < cmdList.size(); i++) {
                    System.out.println(cmdList.get(i));
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
}

