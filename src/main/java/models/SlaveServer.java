package models;

import lombok.Data;
import utils.Printer;

import java.io.IOException;

@Data
public class SlaveServer extends Server {
    public String masterHost;
    public String masterPort;
    public void handshakeWithMaster() throws IOException {
        System.out.println("handshake");
        Printer.sendPing(this.masterHost,this.masterPort);
    }
}
