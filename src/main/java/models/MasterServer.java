package models;

import lombok.Data;
import utils.Printer;

import java.net.Socket;
import java.util.HashMap;

@Data
public class MasterServer extends Server{
    public String replid;
    public int offset;

    @Override
    public void sendReplicationDetailsToClient(Socket clientSocket) throws Exception {
        HashMap<String,String> infoMap = new HashMap<>();
        infoMap.put("role" , "master");
        infoMap.put("master_replid", this.getReplid());
        infoMap.put("master_repl_offset", String.valueOf(this.getOffset()));
        Printer.printInfo(clientSocket, infoMap);
    }
}
