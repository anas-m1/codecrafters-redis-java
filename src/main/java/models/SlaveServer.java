package models;

import lombok.Data;
import utils.Printer;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

@Data
public class SlaveServer extends Server {
    public String masterHost;
    public int masterPort;
    public Socket socketToMaster;
    public void handshakeWithMaster() throws IOException {
        System.out.println("handshake");
        this.socketToMaster=new Socket(masterHost, masterPort);
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
}
