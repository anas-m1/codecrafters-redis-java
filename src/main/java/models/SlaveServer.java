package models;

import lombok.Data;
import utils.Printer;
import utils.RedisParser;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class SlaveServer extends Server {
    public String masterHost;
    public int masterPort;
    public Socket socketToMaster;
    public int offset;
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

    public SlaveServer(int selfServerPort,String masterHost,int masterPort) throws IOException {
        super(selfServerPort);
        this.type="slave";
        this.masterHost=masterHost;
        this.masterPort=masterPort;
        this.offset=0;
    }

    public void handleReplConfAckFromMaster(Socket socketToMaster, List<String> reqCmdList) throws IOException {
//        REPLCONF ACK 0
        List<String>responseCmdList=new ArrayList<>();
        responseCmdList.add("replconf");
        responseCmdList.add("ack") ;
        System.out.println(offset+"   :offset");
        responseCmdList.add(String.valueOf(this.offset));
        System.out.println(String.valueOf(this.offset)+" :offsetString");
        String responseRespStr=RedisParser.getRespStr(responseCmdList);
        System.out.println(responseRespStr+"  :resprespstr");

        String reqRespStr=RedisParser.getRespStr(reqCmdList);
        System.out.println(reqRespStr+" :respstr");
        this.offset+=reqRespStr.length();
//        System.out.println(offset+"   :offset");
        Printer.sendCommand(socketToMaster,responseRespStr);
    }

    public void handlePingFromMaster(Socket clientSocket) {
//        1char is 1 byte in redis, but here 1 char is 2 bytes, therefore only calculating chars not bytes
        this.offset+=(("*1\r\n$4\r\nping\r\n").length());
        System.out.println(offset+"   :offset");
    }
}
