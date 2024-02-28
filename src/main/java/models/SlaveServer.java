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
    public void handshakeWithMaster() throws IOException {
        System.out.println("handshake");
        Socket socketToMaster=new Socket(masterHost, masterPort);
        Printer.sendPing(socketToMaster);
        Printer.sendReplConfigToMaster(socketToMaster, this.selfServerPort);
        Printer.sendPsyncToServer(socketToMaster);
    }

    @Override
    public void sendReplicationDetailsToClient(Socket clientSocket) throws Exception {
        HashMap<String,String> infoMap = new HashMap<>();
        infoMap.put("role", "slave");
        Printer.printInfo(clientSocket, infoMap);
    }
}
