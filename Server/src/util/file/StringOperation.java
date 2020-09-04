package util.file;

import data.ClientPos;
import data.Node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串操作工具类
 */
public class StringOperation {
    /**
     * 从字符串中提取位置信息
     *
     * @param posStr 包含位置信息的字符串
     * @return 位置信息
     */
    public static Node getPosFromStr(String posStr) {

        // 控制正则表达式的匹配行为的参数(小数)
        Pattern p = Pattern.compile("(\\d+\\.\\d+)");
        // Matcher类的构造方法也是私有的，只能通过Pattern.matcher(CharSequence input)方法得到该类的实例.
        Matcher m = p.matcher(posStr);
        //m.find用来判断该字符串中是否含有与"(\\d+\\.\\d+)"相匹配的子串
        int count = 0;
        double lon = 0.0, lat = 0.0;
        while (m.find()) {
            // group()中的参数：
            // 0表示匹配整个正则，1表示匹配第一个括号的正则,2表示匹配第二个正则...
            if (count == 0) {
                lon = Double.valueOf(m.group());
            } else if (count == 1) {
                lat = Double.valueOf(m.group());
            }
            count++;
        }
        Node node = new Node();
        node.setLongitude(lon);
        node.setLatitude(lat);
        return node;
    }

    public static void main(String[] args) {
        String posStr;
        // 控制正则表达式的匹配行为的参数(小数)
        Pattern p = Pattern.compile("(\\d+\\.\\d+)");
        //Matcher类的构造方法也是私有的,不能随意创建,只能通过Pattern.matcher(CharSequence input)方法得到该类的实例.
        Matcher m = p.matcher("POINT(111.234567 33.222222)");
        //m.find用来判断该字符串中是否含有与"(\\d+\\.\\d+)"相匹配的子串
        while (m.find()) {
            // 如果有相匹配的,则判断是否为null操作
            // group()中的参数：0表示匹配整个正则，1表示匹配第一个括号的正则,2表示匹配第二个正则,在这只有一个括号,即1和0是一样的
            posStr = m.group(0) == null ? "" : m.group(0);
            System.out.println(posStr);
        }
    }
}
