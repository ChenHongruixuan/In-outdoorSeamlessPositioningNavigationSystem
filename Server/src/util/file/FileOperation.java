package util.file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 类FileOperation是一个工具类
 * 提供了文件操作的一些静态方法
 *
 * @author Qchrx
 * @version 1.0
 */
public class FileOperation {

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName 要删除的文件名
     * @return 删除成功返回true，否则返回false
     */
    public static boolean deleteMnyFileOrDir(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile())
                return deleteFile(fileName);
            else
                return deleteDirectory(fileName);
        }
    }

    /**
     * 删除单个文件
     *
     * @param filePath 要删除的文件的文件路径
     * @return 单个文件删除成功返回true，否则返回false
     */
    private static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        return file.exists() && file.isFile() && file.delete();
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dirPath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    private static boolean deleteDirectory(String dirPath) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dirPath.endsWith(File.separator))
            dirPath = dirPath + File.separator;
        File dirFile = new File(dirPath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (File file : Objects.requireNonNull(files)) {
            // 删除子文件
            if (file.isFile()) {
                flag = FileOperation.deleteFile(file.getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (file.isDirectory()) {
                flag = FileOperation.deleteDirectory(file.getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            return false;
        }
        // 删除当前目录
        return dirFile.delete();
    }

    /**
     * 按行读取文本文件中的所有内容，将每一行的文本作为一个元素存入List中并返回
     *
     * @param filePath 文本文件路径
     * @return 文本中以各行为元素的List<String>
     * @throws IOException 向上抛出IOException
     */
    public static List<String> readTextByLine(String filePath) throws IOException {
        List<String> postureList = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));   // 构造一个BufferedReader类来读取文件
        String s;
        while ((s = br.readLine()) != null) {   // 使用readLine方法，一次读一行
            postureList.add(s);
        }
        br.close();
        return postureList;
    }

    /**
     * 给定一个文件路径，判断文件是否存在
     *
     * @param filePath 文件路径
     * @return 文件是否存在
     */
    public static boolean judeFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }
}
