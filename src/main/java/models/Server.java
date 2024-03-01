package models;
import lombok.Data;
import utils.Printer;
import utils.RedisParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Data
public abstract class Server {
    public String type;
    public int selfServerPort;
    HashMap<String, RedisEntry> redisStore;
    public List<Socket> clientSockets;
    String dir="";
    String dbfilename="";
    public abstract void sendReplicationDetailsToClient(Socket clientSocket) throws Exception;

    Server(int selfServerPort){
        this.selfServerPort =selfServerPort;
        this.redisStore = new HashMap<>();
        this.clientSockets=new ArrayList<>();
    }

    public void start(ExecutorService executorService) throws IOException {
        System.out.println("here:  " + selfServerPort);
        ServerSocket serverSocket = new ServerSocket(this.selfServerPort);
        serverSocket.setReuseAddress(true);

        System.out.println("here2");
        if(this.getType().equalsIgnoreCase("slave")){
            System.out.println("here3");
            System.out.println("here:  " + ((SlaveServer)this).masterHost+ ((SlaveServer)this).masterPort);
            Socket socketToMaster=new Socket(((SlaveServer)this).masterHost, ((SlaveServer)this).masterPort);
            ((SlaveServer)this).socketToMaster=socketToMaster;
            ClientSocketHandler clientHandler=new ClientSocketHandler(socketToMaster,this,"socketToMaster");
            executorService.submit(clientHandler::run);
            ((SlaveServer) this).handshakeWithMaster();
        }


        // Wait for connection from client.
        while(true){
            Socket clientSocket = serverSocket.accept();
            this.clientSockets.add(clientSocket);
            System.out.println("new client connection");
            ClientSocketHandler clientHandler=new ClientSocketHandler(clientSocket,(Server)this,"socketFromClient");
            executorService.submit(clientHandler::run);
        }
    }

    public void handleGetConfigDir(Socket clientSocket) throws IOException {
        List<String> respArr=new ArrayList<>();
        respArr.add("dir");
        respArr.add(this.dir);
        Printer.sendCommand(clientSocket, RedisParser.getRespStr(respArr));
    }

    public void handleGetConfigDbFilename(Socket clientSocket) throws IOException {
        List<String> respArr=new ArrayList<>();
        respArr.add("dbfilename");
        respArr.add(this.dbfilename);
        Printer.sendCommand(clientSocket, RedisParser.getRespStr(respArr));
    }

    public HashMap<String,RedisEntry> getRedisStoreFromRDB() throws IOException {
        List<String> keyList = new ArrayList<>();
        HashMap<String,RedisEntry> redisStoreFromRDB= new HashMap<>();

//        InputStreamReader inputStreamReader = new InputStreamReader();
        File file = new File(this.dir+"/"+this.dbfilename);
        file.setReadable(true);
//        FileReader fileReader = new FileReader(this.dbfilename);
        FileInputStream fileInputStream = new FileInputStream(file);
//        key section is between fd and fc
        int byteInt;
        while (true) {
            byteInt = fileInputStream.read();
            System.out.println(byteInt + " :byteint1");
            if (byteInt == -1) break;
            if (byteInt == 0xFE)
                //read till fe
                while (true) {
                    byteInt = fileInputStream.read();
                    System.out.println(byteInt + " :byteint");
                    if (byteInt == -1) return redisStoreFromRDB;
                    if (byteInt == 0xFB) {
                        //  next 2 lines are resizedb fields
                        int hashTableLen=fileInputStream.read();
                        System.out.println(fileInputStream.read());
                        for (int i=0; i<hashTableLen; i++) {
                            // next fd or fc or directly value type
                            int next = fileInputStream.read();
                            if (next == 0xFD) {
                                int sec = fileInputStream.read();
                                RedisEntry re = getRedisEntryFromInputFileStream(fileInputStream);

                                redisStoreFromRDB.put(re.getKey(), re);
                            } else if (next == 0xFC) {
                                int sec = fileInputStream.read();
                                RedisEntry re = getRedisEntryFromInputFileStream(fileInputStream);
                                redisStoreFromRDB.put(re.getKey(), re);
                            } else {
                                int valueType=next;
                                RedisEntry re = getRedisEntryFromInputFileStream(fileInputStream);
                                redisStoreFromRDB.put(re.getKey(), re);
                            }
                        }
                }
        }

        }
        return redisStoreFromRDB;
}

    private RedisEntry getRedisEntryFromInputFileStream(FileInputStream fileInputStream) throws IOException {
        int keyLen = fileInputStream.read();
        if (keyLen == -1) return null;
        String key="";
        for(int i=0;i<keyLen;i++){
            char c = (char)fileInputStream.read();
            System.out.println(c+" : key");
            key+=(c);
        }

        int valLen = fileInputStream.read();
        if (keyLen == -1) return null;
        String val="";
        for(int i=0;i<valLen;i++){
            char c = (char)fileInputStream.read();
            System.out.println(c+" : val");
            val+=(c);
        }

        RedisEntry redisEntry=new RedisEntry(key,val);
        return redisEntry;
    }


//    populate in memory store from file
    private void getInfoFromRDBToInMemory() throws IOException {
        HashMap<String,RedisEntry> redisStoreFromRDB=getRedisStoreFromRDB();

        for(String key:redisStoreFromRDB.keySet()){
            if(this.redisStore.containsKey(key)){
//                resolve based on latest added
            }
            else{
                this.redisStore.put(key,redisStoreFromRDB.get(key));
            }
        }
    }



}
