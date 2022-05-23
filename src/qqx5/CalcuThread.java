package qqx5;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static qqx5.CalculateFunction.linkedHashMap;
import static qqx5.CalculateFunction.threadNum;

public class CalcuThread extends Thread {

    private final int threadNo;
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    CalcuThread(int threadNo) {
        this.threadNo = threadNo;
    }

    public void run() {
        int i = 0;
        for (Map.Entry<File, Integer> entry : linkedHashMap.entrySet()) {
            process(entry.getKey() , entry.getValue());
            i++;
        }
    }

    private void process(File xml, int mode) {
        XMLInfo a = switch (mode) {
            case 1, 2 -> a = new XMLInfo(1, xml.getName());
            case 3 -> a = new XMLInfo(2, xml.getName());
            case 4 -> a = new XMLInfo(3, xml.getName());
            case 5 -> a = new XMLInfo(4, xml.getName());
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };

        try {
            new SetBasicInfo(xml, mode).set(a);// 读取 xml 并录入有用的信息
        } catch (SetInfoException e) {
            e.warnMess();
        }

        new Calculate(a).calculate(a);// 计算爆点

        try {
            lock.writeLock().lock();
            new WriteFireInfo().write(a);// 输出信息
        } finally {
            lock.writeLock().unlock();
        }

        switch (mode) {
            case 1:
            case 2:
                System.out.println("星动 " + a.title + " 处理完毕");
                break;
            case 3:
                System.out.println("弹珠 " + a.title + " 处理完毕");
                break;
            case 4:
                System.out.println("泡泡 " + a.title + " 处理完毕");
                break;
            case 5:
                System.out.println("弦月 " + a.title + " 处理完毕");
                break;
        }
    }

}
