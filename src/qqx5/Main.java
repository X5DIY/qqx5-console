package qqx5;

import org.jetbrains.annotations.NotNull;

import java.util.Scanner;

/**
 * 这是一个有关 QQ炫舞 的爆点计算程序.
 * 输入：谱面文件或其所在文件夹
 * 输出：各类情况下的最高分爆点位置、准确分数以及爆点描述
 * 准确是首位的！！！
 */

class Main {

    public static void main(String[] args) {
        while (true) {
            System.out.println("——————菜单——————");
            System.out.println("1.计算歌曲爆点");// 计算星弹泡弦谱面爆点
            System.out.println("2.文件提取与转换");// 提取安装包的bytes及bgm文件，bytes转xml
            System.out.println("3.解压安装包内所有文件");// 提取安装包的bytes及bgm文件，bytes转xml
            System.out.println("0.退出程序");
            Scanner scanner = new Scanner(System.in).useDelimiter("\n");
            switch (scanner.next()) {
                case "1":
                    System.out.println("输入谱面文件路径或其所在文件夹，连接符用 \"/\"");
                    new CalculateFunction(scanner.next()).calculate();
                    continue;
                case "2":
                    System.out.println("输入apk文件路径或bytes文件所在文件夹，连接符用 \"/\"");
                    String path = scanner.next();
                    if (path.endsWith(".apk")) {
                        new X5APK(path).getAll();
                    } else {
                        new Bytes(path).toXML();
                    }
                    continue;
                case "3":
                    new X5APK("apkZips").f3();
                    continue;
                case "0":
                    System.out.println("感谢使用！");
                    return;
                default:
                    System.out.println("输入有误！");
            }
        }
    }

    /**
     * 将以 ns 为单位的时间进行拆分
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间字符串
     */
    @NotNull
    static String time(long startTime, long endTime) {
        long nanosecond = endTime - startTime;
        StringBuilder time = new StringBuilder(" ");
        if (nanosecond > 60000000000L) {
            long minute = nanosecond / 60000000000L;
            nanosecond %= 60000000000L;
            time.append(minute).append(" min ");
        }
        if (nanosecond > 1000000000) {
            long second = nanosecond / 1000000000;
            nanosecond %= 1000000000;
            time.append(second).append(" s ");
        }
        if (nanosecond > 1000000) {
            long millisecond = nanosecond / 1000000;
            nanosecond %= 1000000;
            time.append(millisecond).append(" ms ");
        }
        if (nanosecond > 1000) {
            long microsecond = nanosecond / 1000;
            nanosecond %= 1000;
            time.append(microsecond).append(" us ");
        }
        time.append(nanosecond).append(" ns");
        return time.toString();
    }

}
