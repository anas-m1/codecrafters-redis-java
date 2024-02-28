package models;

import lombok.Data;
import utils.Printer;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

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
        Printer.respondToPsyncFromSlave(clientSocket,this.replid,this.offset);
        for(Socket slaveSocket : slaveSockets){
            for(String respSetcommand: setCommandQueue){
                Printer.sendCommand(slaveSocket,respSetcommand);
            }
        }
    }

    public void handleReplConfReqFromSlave(Socket slaveSocket) throws IOException {
        this.slaveSockets.add(slaveSocket);
        Printer.respondToReplConfFromSlave(slaveSocket);
    }

    public MasterServer(){
        this.slaveSockets=new ArrayList<>();
    }
}
