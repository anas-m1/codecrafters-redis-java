import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    Socket clientSocket;

    ClientHandler(Socket socket){
        this.clientSocket=socket;
    };

    public void run(){

        try{
//            System.out.println("from run0");
            InputStream inputStream= clientSocket.getInputStream();
//            System.out.println("from run1");
            InputStreamReader inputStreamReader= new InputStreamReader(inputStream);
//            System.out.println("from run2");
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            String line="";
//            System.out.println("from run3");
            while((line=bufferedReader.readLine()) !=null){
                Pattern pattern = Pattern.compile(Pattern.quote("ping"));
                // Create a matcher with the input string
                Matcher matcher = pattern.matcher(line);
                while(matcher.find()){
                    Printer.printPong(clientSocket);
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
}
