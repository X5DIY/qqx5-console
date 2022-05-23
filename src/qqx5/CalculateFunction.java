package qqx5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Scanner;

import static org.menglei.GetInfo.getInfo;
import static qqx5.Main.time;

class CalculateFunction {

    static LinkedHashMap<File, Integer> linkedHashMap;// 所有要处理的文件
    static int threadNum = Runtime.getRuntime().availableProcessors();// 线程数目

    CalculateFunction(String rootFilePath) {
        this.rootFilePath = rootFilePath;
        linkedHashMap = new LinkedHashMap<>();
    }

    private String rootFilePath;

    void calculate() {
        long startTime;
        long endTime;
        Scanner scanner = new Scanner(System.in);

        System.out.println("选择输出模式：");
        System.out.println("1.全信息模式\t2.爆气表模式");
        int outMode = scanner.nextInt();
        if (outMode != 1 && outMode != 2) {
            System.out.println("模式有误！");
            return;
        }

        System.out.println("输入爆点个数上限（通常为10）：");
        int fireMaxNum = scanner.nextInt();
        if (fireMaxNum <= 0) {
            System.out.println("爆点个数必须为正！");
            return;
        }
        XMLInfo.FireMaxNum = fireMaxNum;

        int maxDiff = 0;
        if (outMode == 1) {
            System.out.println("输入爆点允许分差上限（0代表只输出最高分爆点）：");
            maxDiff = scanner.nextInt();
            if (maxDiff < 0) {
                System.out.println("分差不能为负！");
                return;
            }
        }

        startTime = System.nanoTime();
        new WriteFireInfo(maxDiff, outMode).writeFirst();// 写入所有文件第一行
        System.out.println("正在查找谱面文件ing");
        traversalFile(new File(rootFilePath));// 记录所有符合的xml文件
        System.out.println("查找完毕，共找到 " + linkedHashMap.size() + " 个谱面文件，开始处理！");

        CalcuThread[] calcuThreads = new CalcuThread[threadNum];
        for (int i = 0; i < threadNum; i++) {
            calcuThreads[i] = new CalcuThread(i);
            calcuThreads[i].start();
        }
        try {// 等待线程执行完毕
            for (int i = 0; i < threadNum; i++) {
                calcuThreads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = System.nanoTime();

        System.out.println("全部文件处理完毕，共用时" + time(startTime, endTime));
        System.out.println("歌曲信息已保存，感谢使用~");
    }

    /**
     * 遍历所有文件，如果是炫舞谱面文件，计算爆点
     * 确定谱面模式后，调用calculate函数，进行文件处理
     *
     * @param file 文件或文件夹
     */
    private static void traversalFile(File file) {
        try {
            if (!file.isDirectory()) {// 如果是文件
                // System.out.println("文件路径：" + file.getCanonicalPath());
                // 为了避免转化带来的问题，这里不进行bytes文件的处理
                // 如果需要转化，主界面运行转化操作即可
                if (file.getName().endsWith(".xml")) {// 如果后缀是xml
                    int mode = getMode(file);
                    if (mode != 0) {
                        linkedHashMap.put(file, mode);
                    }
                }
            } else {// 如果是文件夹
                // System.out.println("文件夹：" + file.getCanonicalPath());
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

    /**
     * 读取文件内容，判断打开的文件是4k星动、5k星动、弹珠还是泡泡
     *
     * @param xml QQX5谱面文件
     * @return 1为4k星动，2为5k星动，3为弹珠，4为泡泡，5为弦月
     */
    private static int getMode(File xml) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(xml));
            String s;
            int track = 0;
            while ((s = br.readLine()) != null) {
                if (s.contains("<Type>")) {
                    br.close();
                    return 5;
                }
                if (s.contains("TrackCount")) {
                    track = Integer.parseInt(getInfo(s, "<TrackCount>", "</TrackCount>"));
                }
                if (s.contains("note_type=\"short\"")) {
                    br.close();
                    if (track == 4) {
                        return 1;
                    } else {
                        return 2;
                    }
                } else if (s.contains("Pinball")) {
                    br.close();
                    return 3;
                } else if (s.contains("ScreenPos")) {
                    br.close();
                    return 4;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;// 都不满足说明该xml不是炫舞谱面文件
    }

}