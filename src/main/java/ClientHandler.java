import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

                List<String> commandList=new ArrayList<>();
                commandList=breakWords(line);

                for (String command : commandList){
                    System.out.println(" " + command);
                }

                String actionVerb=commandList.get(1);
                if(actionVerb.equalsIgnoreCase("ping")){
                    Printer.printPong(clientSocket);
                }
                else if(actionVerb.equalsIgnoreCase("echo")){
                    String arg=commandList.get(2);
                    Printer.printEcho(clientSocket,arg);
                }

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

    List<String> breakWords(String line){
        List<String> cmdList=new ArrayList<>();

        int wordStartIdx=-1;
        for(int i=0;i<line.length();i++){
            char ch= line.charAt(i);
            if(ch==' '){
                if(wordStartIdx>=0){
                   cmdList.add(line.substring(wordStartIdx,i));
                }
                wordStartIdx=-1;
            }
            else{
                if(wordStartIdx<0){
                    wordStartIdx=i;
                }
            }

            if(i==line.length()-1){
                cmdList.add(line.substring(wordStartIdx,i+1));
            }
        }

        return cmdList;
    }
}
