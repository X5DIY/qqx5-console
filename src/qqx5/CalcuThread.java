package qqx5;

import java.io.File;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static qqx5.CalculateFunction.*;

public class CalcuThread extends Thread {

    private int threadNo;
    private static ReadWriteLock lock = new ReentrantReadWriteLock();

    CalcuThread(int threadNo) {
        this.threadNo = threadNo;
    }

    public void run() {
        for (int i = 0; i < fileNum; i++) {
            if (i % threadNum == threadNo) {
                process(files[i], fileMode[i]);
            }
        }
    }

    private void process(File xml, int mode) {
        XMLInfo a;
        switch (mode) {
            case 1:
            case 2:
                a = new XMLInfo(1);
                break;
            case 3:
                a = new XMLInfo(2);
                break;
            case 4:
                a = new XMLInfo(3);
                break;
            case 5:
                a = new XMLInfo(4);
                break;
            default:
                System.out.println("Mode in XML is not correct.");
                return;
        }

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
