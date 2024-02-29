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

}
