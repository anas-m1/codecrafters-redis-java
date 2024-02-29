package models;

import lombok.Data;
import utils.Printer;
import utils.RedisParser;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Data
public class SlaveServer extends Server {
    public String masterHost;
    public int masterPort;
    public Socket socketToMaster;
    public void handshakeWithMaster() throws IOException {
        Printer.sendPing(this.socketToMaster);
        Printer.sendReplConfigToMaster(this.socketToMaster, this.selfServerPort);
        Printer.sendPsyncToServer(this.socketToMaster);
    }

    @Override
    public void sendReplicationDetailsToClient(Socket clientSocket) throws Exception {
        HashMap<String,String> infoMap = new HashMap<>();
        infoMap.put("role", "slave");
        Printer.printInfo(clientSocket, infoMap);
    }

//    public void setExecutorService(ExecutorService executorService) throws IOException {
//        ClientHandler clientHandler=new ClientHandler(this.socketToMaster,this);
//        executorService.submit(clientHandler::run);
//    }

    public SlaveServer(int selfServerPort,String masterHost,int masterPort) throws IOException {
        super(selfServerPort);
        this.type="slave";
        this.masterHost=masterHost;
        this.masterPort=masterPort;
    }

    public void handleReplConfAckFromMaster(Socket socketToMaster) throws IOException {
//        REPLCONF ACK 0
        List<String>cmdList=new ArrayList<>();
        cmdList.add("replconf");
        cmdList.add("ack");
        cmdList.add("0");
        String respStr=RedisParser.getRespStr(cmdList);
        Printer.sendCommand(socketToMaster,respStr);
    }
}
