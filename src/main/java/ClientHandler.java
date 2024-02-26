import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static java.lang.Integer.parseInt;

public class ClientHandler implements Runnable {
    Socket clientSocket;
    HashMap<String,RedisEntry> redisStore;

    ClientHandler(Socket socket,HashMap<String,RedisEntry> store) {
        this.clientSocket=socket;
        this.redisStore=store;
    };

    public void run(){

        try{
            InputStream inputStream= clientSocket.getInputStream();
            InputStreamReader inputStreamReader= new InputStreamReader(inputStream);
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            String line="";


            while((line=bufferedReader.readLine()) !=null){
//                *1\r\n$4\r\nping\r\n
                List<String> cmdList=new ArrayList<>();
                System.out.println(line+ ": line");
                if(line.charAt(0)=='*'){
                    int numWords=parseInt(line.substring(1));
                    for(int i=0; i<numWords; i++){
                        String wordLenLine=bufferedReader.readLine();
                        int WordLength=parseInt(wordLenLine.substring(1));
                        String word=bufferedReader.readLine();
                        System.out.println(word+" :word");
                        cmdList.add(word);
                    }
                }

//
                String actionVerb=cmdList.get(0);
                if(actionVerb.equalsIgnoreCase("ping")){
                    Printer.printPong(clientSocket);
                }
                else if(actionVerb.equalsIgnoreCase("echo")){
                    String arg=cmdList.get(1);
                    Printer.printEcho(clientSocket,arg);
                }
                else if(actionVerb.equalsIgnoreCase("set")){
                    String key=cmdList.get(1);
                    String value=cmdList.get(2);

                    System.out.println("key: " + key + " value: " + value);
                    RedisEntry entry=new RedisEntry(key,value);

                    if(cmdList.size()>3){
                        if(cmdList.get(3).equalsIgnoreCase("px")){
                            long timeToLive=Long.parseLong(cmdList.get(4));
                            System.out.println("key: " + key + " value: " + value + " timeToLive: " + timeToLive);
                            long currUNIXts = System.currentTimeMillis();
                            entry.setExpiryAt(currUNIXts+timeToLive);
                        }
                    }
                    redisStore.put(key,entry);
                    Printer.printOK(clientSocket);
                }
                else if(actionVerb.equalsIgnoreCase("get")){
                    String key=cmdList.get(1);
                    System.out.println("key: " + key);
                    if(redisStore.containsKey(key)){
                        RedisEntry entry=redisStore.get(key);
                        System.out.println(entry);
                        System.out.println("hello "+entry.getExpiryAt()+" seconds "+System.currentTimeMillis());

                        if(entry.getExpiryAt()>System.currentTimeMillis()){
                            System.out.println(entry.getExpiryAt()+" seconds "+System.currentTimeMillis());
                            Printer.printEcho(clientSocket,entry.getValue());
                        }
                        else{
                            redisStore.remove(key);
                            Printer.printEcho(clientSocket,"$-1\r\n");
                        }
                    }
                    else{
                        Printer.printEcho(clientSocket,"$-1\r\n");
                    }
                }

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

//    List<String> parseRedisCommand(String line){
//        System.out.println(line);
//        List<String> cmdList=new ArrayList<>();
//        int numWords=parseInt(line.substring(1,2),10);
//        System.out.println("numWords: " + numWords);
//
////        *2\r\n$4\r\necho\r\n$3\r\nhey\r\n
////        *1\r\n$4\r\nping\r\n
//
//        int idx=6;
//        for(int i=0;i<numWords;i++){
//            if(line.charAt(idx)=='$'){
//                int nextBSidx=findIdx(line,idx,'\\');
//                System.out.println(nextBSidx+":next");
//                int wordLen=parseInt(line.substring(idx+1,nextBSidx),10);
//                idx=nextBSidx+4;
//                System.out.println(wordLen+" :wordstart");
//                String word=line.substring(idx,idx+wordLen);
//                System.out.println(idx+wordLen+" :wordend");
//
//                System.out.println(word+":word");
//                cmdList.add(word);
//                idx+=wordLen;
//                idx+=4;
//            }
//        }
//
////        int idx=8;
////        while(idx<line.length()){
////            int wordLength=parseInt(line.substring(idx,idx+1),10);
////            idx+=5;
////            cmdList.add(line.substring(idx,idx+wordLength));
////            idx+=wordLength;
////            idx+=4;
////
////        }
//
////        int wordStartIdx=-1;
////        for(int i=0;i<line.length();i++){
////            char ch= line.charAt(i);
////            if(ch==' '){
////                if(wordStartIdx>=0){
////                   cmdList.add(line.substring(wordStartIdx,i));
////                }
////                wordStartIdx=-1;
////            }
////            else{
////                if(wordStartIdx<0){
////                    wordStartIdx=i;
////                }
////            }
////
////            if(i==line.length()-1){
////                cmdList.add(line.substring(wordStartIdx,i+1));
////            }
////        }
//
//        return cmdList;
//    }

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
