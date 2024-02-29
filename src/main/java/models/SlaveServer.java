package models;

import lombok.Data;
import utils.Printer;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

@Data
public class SlaveServer extends Server {
    public String masterHost;
    public int masterPort;
    public Socket socketToMaster;
    public void handshakeWithMaster() throws IOException {
        Printer.sendPing(this.socketToMaster);
        System.out.println("ping sent");
        Printer.sendReplConfigToMaster(this.socketToMaster, this.selfServerPort);
        System.out.println("replconfig sent");
        Printer.sendPsyncToServer(this.socketToMaster);
    }

    @Override
    public void sendReplicationDetailsToClient(Socket clientSocket) throws Exception {
        HashMap<String,String> infoMap = new HashMap<>();
        infoMap.put("role", "slave");
        Printer.printInfo(clientSocket, infoMap);
    }

    public void setExecutorService(ExecutorService executorService) throws IOException {
        ClientHandler clientHandler=new ClientHandler(this.socketToMaster,this);
        executorService.submit(clientHandler::run);
    }
}
