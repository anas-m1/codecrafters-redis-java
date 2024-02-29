package utils;

import java.util.List;

public class RedisParser {
    static String clrf="\r\n";
    public static String getRespStr(List<String> strList) {
        String result = "*" + strList.size();
        StringBuffer buffer = new StringBuffer();
        for(String str : strList) {
            buffer.append(clrf);
            buffer.append("$"+str.length());
            buffer.append(clrf);
            buffer.append(str);
        }
        result+=(buffer.toString()+clrf);
        return result;
    }

//    public static int getRedisBytes(String reqRespStr) {
////        each char is considered a byte, and "\r\n" is 2 bytes
//        int numBytes=0;
//        for(int i=0; i<reqRespStr.length(); i++){
//            if(i+3<reqRespStr.length() && reqRespStr.substring(i,i+4).equalsIgnoreCase("\r\n")){
//                i+=3;
//            }
//            numBytes++;
//        }
//        return numBytes;
//    }
}
