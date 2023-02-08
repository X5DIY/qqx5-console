package qqx5;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

import static qqx5.Main.time;

class Bytes {

    static File[] files;// 所有要处理的文件
    static int fileNum;
    static int threadNum = 24;// 线程数目

    Bytes(String rootBytesPath) {
        this.rootBytesPath = rootBytesPath;
        files = new File[5000];// 所有要处理的文件，5000 是谱面文件最大数量
        fileNum = 0;
    }

    private String rootBytesPath;

    void toXML() {
        long startTime;
        long endTime;

        startTime = System.nanoTime();
        System.out.println("正在查找bytes文件ing");
        traversalFile(new File(rootBytesPath));// 记录所有符合的bytes文件
        System.out.println("查找完毕，共找到 " + fileNum + " 个bytes文件，开始处理！");
        BytesThread[] bytesThreads = new BytesThread[threadNum];
        for (int i = 0; i < threadNum; i++) {
            bytesThreads[i] = new BytesThread(i);
            bytesThreads[i].start();
        }
        try {// 等待线程执行完毕
            for (int i = 0; i < threadNum; i++) {
                bytesThreads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = System.nanoTime();

        System.out.println("全部文件处理完毕，共用时" + time(startTime, endTime));
        System.out.println("bytes文件已转换完毕，感谢使用~");
    }

    private static void traversalFile(File file) {
        try {
            if (!file.isDirectory()) {// 如果是文件
                if (file.getName().endsWith(".xml.bytes")) {// 如果后缀是.xml.bytes
                    files[fileNum] = file;
                    fileNum++;
                }
            } else {// 如果是文件夹
                File[] listFiles = file.listFiles();// 为里面每个文件、目录创建对象
                if (listFiles == null) {// 如果文件夹为空，直接结束
                    return;
                }
                for (File f : listFiles) {// 遍历每个文件和目录
                    traversalFile(f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}