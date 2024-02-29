package models;

import lombok.Data;
import utils.Printer;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

@Data
public class MasterServer extends Server{
    public String replid;
    public int offset;
    Queue<String> setCommandQueue;
    List<Socket> slaveSockets;
    @Override
    public void sendReplicationDetailsToClient(Socket clientSocket) throws Exception {
        HashMap<String,String> infoMap = new HashMap<>();
        infoMap.put("role", "master");
        infoMap.put("master_replid", this.getReplid());
        infoMap.put("master_repl_offset", String.valueOf(this.getOffset()));
        Printer.printInfo(clientSocket, infoMap);
    }

    public void respondToPsyncFromSlave(Socket clientSocket) throws Exception {
        System.out.println("master responding to psync from slave"+slaveSockets.get(0));
        Printer.respondToPsyncFromSlave(clientSocket,this.replid,this.offset);
//        if(setCommandQueue.isEmpty())return;

        for(Socket slaveSocket : slaveSockets){
//            since the earlier sent command i.e. RDB file doesnt have clrf at the end, the next command gets mixed in same line
//            Printer.sendCommand(slaveSocket,"");
            for(String respSetcommand: setCommandQueue){
                Printer.sendCommand(slaveSocket,respSetcommand);
            }
        }
    }

    public void handleReplConfReqFromSlave(Socket slaveSocket) throws IOException {
        this.slaveSockets.add(slaveSocket);
        Printer.respondToReplConfFromSlave(slaveSocket);
    }

    public MasterServer(int selfServerPort){
        super(selfServerPort);
        this.type="master";
        this.slaveSockets=new ArrayList<>();
        this.setCommandQueue=new LinkedList<>();
    }

    public void addToSetCommandQueue(String respStr) throws IOException {
        this.setCommandQueue.add(respStr);
        for(Socket slaveSocket : slaveSockets){
            Printer.sendCommand(slaveSocket,respStr);
        }
    }
}
