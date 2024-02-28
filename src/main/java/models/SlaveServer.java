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
        Socket masterSocket=new Socket(masterHost, masterPort);
        Printer.sendPing(masterSocket);
        Printer.sendReplConfigToMaster(masterSocket, this.selfServerPort);
        Printer.sendPsyncToServer(masterSocket);
    }

    @Override
    public void sendReplicationDetailsToClient(Socket clientSocket) throws Exception {
        HashMap<String,String> infoMap = new HashMap<>();
        infoMap.put("role", "slave");
        Printer.printInfo(clientSocket, infoMap);
    }
}
