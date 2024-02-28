package models;

import lombok.Data;
import utils.Printer;

import java.io.IOException;
import java.net.Socket;

@Data
public class SlaveServer extends Server {
    public String masterHost;
    public String masterPort;
    public void handshakeWithMaster() throws IOException {
        System.out.println("handshake");
        Socket masterSocket=new Socket(masterHost, Integer.parseInt(masterPort,10));
        Printer.sendPing(masterSocket);
        Printer.sendReplConfigToMaster(masterSocket, masterSocket.getPort());
    }
}
