import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class ClientHandler implements Runnable {
    Socket clientSocket;

    ClientHandler(Socket socket){
        this.clientSocket=socket;
    };

    public void run(){

        try{
            InputStream inputStream= clientSocket.getInputStream();
            InputStreamReader inputStreamReader= new InputStreamReader(inputStream);
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            String line="";

            while((line=bufferedReader.readLine()) !=null){
                OutputStream outputStream= clientSocket.getOutputStream();
                outputStream.write(line.getBytes());
                outputStream.flush();
                return;
//                List<String> commandList=new ArrayList<>();
//                System.out.println(line);
//                commandList=parseRedisCommand(line);
//
//                for (String command : commandList){
//                    System.out.println(" " + command + ":here:");
//                }
//
//                String actionVerb=commandList.get(0);
//                if(actionVerb.equalsIgnoreCase("ping")){
//                    Printer.printPong(clientSocket);
//                }
//                else if(actionVerb.equalsIgnoreCase("echo")){
//                    String arg=commandList.get(1);
//                    Printer.printEcho(clientSocket,arg);
//                }

//                Pattern pattern = Pattern.compile(Pattern.quote("ping"));
//                Matcher matcher = pattern.matcher(line);
//                while(matcher.find()){
//                    Printer.printPong(clientSocket);
//                }
            }

            if (clientSocket != null) {
                clientSocket.close();
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }


//        try {
//
//        }
//        catch (Exception e) {
//            System.out.println("IOException: " + e.getMessage());
//        }


    }

    List<String> parseRedisCommand(String line){
        System.out.println(line);
        List<String> cmdList=new ArrayList<>();
        int numWords=parseInt(line.substring(1,2),10);
        System.out.println("numWords: " + numWords);

//        *2\r\n$4\r\necho\r\n$3\r\nhey\r\n
//        *1\r\n$4\r\nping\r\n

        int idx=6;
        for(int i=0;i<numWords;i++){
            if(line.charAt(idx)=='$'){
                int nextBSidx=findIdx(line,idx,'\\');
                System.out.println(nextBSidx+":next");
                int wordLen=parseInt(line.substring(idx+1,nextBSidx),10);
                idx=nextBSidx+4;
                System.out.println(wordLen+" :wordstart");
                String word=line.substring(idx,idx+wordLen);
                System.out.println(idx+wordLen+" :wordend");

                System.out.println(word+":word");
                cmdList.add(word);
                idx+=wordLen;
                idx+=4;
            }
        }

//        int idx=8;
//        while(idx<line.length()){
//            int wordLength=parseInt(line.substring(idx,idx+1),10);
//            idx+=5;
//            cmdList.add(line.substring(idx,idx+wordLength));
//            idx+=wordLength;
//            idx+=4;
//
//        }

//        int wordStartIdx=-1;
//        for(int i=0;i<line.length();i++){
//            char ch= line.charAt(i);
//            if(ch==' '){
//                if(wordStartIdx>=0){
//                   cmdList.add(line.substring(wordStartIdx,i));
//                }
//                wordStartIdx=-1;
//            }
//            else{
//                if(wordStartIdx<0){
//                    wordStartIdx=i;
//                }
//            }
//
//            if(i==line.length()-1){
//                cmdList.add(line.substring(wordStartIdx,i+1));
//            }
//        }

        return cmdList;
    }

    private int findIdx(String line, int idx, char c) {
        System.out.println(c+ "::fou");
        for(int i=idx;i<line.length();i++){
            if(line.charAt(i)==c){
                return i;
            }
        }
        return -1;
    }
}
