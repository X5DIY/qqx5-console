package qqx5;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class X5APK {

    private String apkPath;

    private static String zipsPath = "apkZips";
    private static String bgmPath = "bgm";
    private static String bytesPath = "rank";
    private static String idolPath = bytesPath + File.separator + "idol";
    private static String pinballPath = bytesPath + File.separator + "pinball";
    private static String bubblePath = bytesPath + File.separator + "bubble";
    private static String crescentPath = bytesPath + File.separator + "crescent";

    X5APK(String apkPath) {
        this.apkPath = apkPath;
        File zipsDir = new File(zipsPath);
        if (!zipsDir.exists()) {
            zipsDir.mkdirs();
        }
        File bgmDir = new File(bgmPath);
        if (!bgmDir.exists()) {
            bgmDir.mkdirs();
        }
        File idolDir = new File(idolPath);
        if (!idolDir.exists()) {
            idolDir.mkdirs();
        }
        File pinballDir = new File(pinballPath);
        if (!pinballDir.exists()) {
            pinballDir.mkdirs();
        }
        File bubbleDir = new File(bubblePath);
        if (!bubbleDir.exists()) {
            bubbleDir.mkdirs();
        }
        File crescentDir = new File(crescentPath);
        if (!crescentDir.exists()) {
            crescentDir.mkdirs();
        }
    }

    void getAll() {
        getFile(apkPath);
        // delete(new File(zipsPath));
        // System.out.println("所有zip文件已删除！");
        System.out.println("全部bgm文件、bytes文件解压完毕！");
        new Bytes(bytesPath).toXML();// 转化bytes文件
    }

    private void getFile(String zipPath) {
        try {
            ZipFile zipFile = new ZipFile(new File(zipPath));
            Enumeration<? extends ZipEntry> files = zipFile.entries();
            ZipEntry entry;// entry 是每个压缩文件的入口
            while (files.hasMoreElements()) {// 遍历所有文件
                entry = files.nextElement();
                // entry.getName() 就是文件在压缩包中的路径，比如 assets/zips/123
                String parentPath;
                if (entry.getName().contains("assets/zips/")) {
                    parentPath = zipsPath;
                } else if (entry.getName().contains("assets/assetbundles/audio/bgm/")) {
                    parentPath = bgmPath;
                } else if (entry.getName().contains("assetbundles/level/idol")) {
                    parentPath = idolPath;
                } else if (entry.getName().contains("assetbundles/level/pinball")) {
                    parentPath = pinballPath;
                } else if (entry.getName().contains("assetbundles/level/bubble")) {
                    parentPath = bubblePath;
                } else if (entry.getName().contains("assetbundles/level/crescent")) {
                    parentPath = crescentPath;
                } else {
                    continue;
                }
                File outFile;
                String fileName = entry.getName().substring(entry.getName().lastIndexOf("/") + 1);
                System.out.println("解压 " + fileName + " 至 " + parentPath);
                outFile = new File(parentPath + File.separator + fileName);
                if (outFile.exists()) {
                    outFile.delete();
                }
                outFile.createNewFile();

                BufferedInputStream bin = new BufferedInputStream(zipFile.getInputStream(entry));
                BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(outFile));
                byte[] buffer = new byte[1024];
                int readCount;
                while ((readCount = bin.read(buffer)) != -1) {
                    bout.write(buffer, 0, readCount);
                }
                bin.close();
                bout.close();
                if (parentPath.equals(zipsPath)) {
                    getFile(outFile.getCanonicalPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void f3() {
        traversalFile(new File(apkPath));
    }


    private void traversalFile(File file) {
        try {
            if (!file.isDirectory()) {// 如果是文件
                // System.out.println("文件路径：" + file.getCanonicalPath());
                // 为了避免转化带来的问题，这里不进行bytes文件的处理
                // 如果需要转化，主界面运行转化操作即可
                getFile2(file);
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


    private void getFile2(File zips) {
        try {
            ZipFile zipFile = new ZipFile(zips);
            Enumeration<? extends ZipEntry> files = zipFile.entries();
            ZipEntry entry;// entry 是每个压缩文件的入口
            while (files.hasMoreElements()) {// 遍历所有文件
                entry = files.nextElement();
                // entry.getName() 就是文件在压缩包中的路径，比如 assets/zips/123
                File outFile;

                String parentPath = "aaa/" +
                        entry.getName().substring(0, entry.getName().lastIndexOf('/'));
                String fileName = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
                System.out.println("解压 " + fileName + " 至 " + parentPath);
                outFile = new File(parentPath + File.separator + fileName);
                new File(parentPath).mkdirs();


                System.out.println("解压 " + fileName);
                if (outFile.exists()) {
                    outFile.delete();
                }
                outFile.createNewFile();

                BufferedInputStream bin = new BufferedInputStream(zipFile.getInputStream(entry));
                BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(outFile));
                byte[] buffer = new byte[1024];
                int readCount;
                while ((readCount = bin.read(buffer)) != -1) {
                    bout.write(buffer, 0, readCount);
                }
                bin.close();
                bout.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void delete(File file) {
        // 如果是目录，先删除里面所有的东西
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File f : children) {
                delete(f);
            }
        }
        // 文件或空目录可以直接删除
        try {
            int deleteTimes = 0;
            while (true) {// 重复删除文件，直到删掉为止
                if (file.delete()) {
                    System.out.println("已删除 " + file.getCanonicalPath());
                    break;
                } else {
                    if (deleteTimes == 0) {
                        System.out.println("未删除 " + file.getCanonicalPath() + "，尝试不断删除……");
                        deleteTimes++;
                    } else if (deleteTimes == 1000) {
                        System.out.println("未删除 " + file.getCanonicalPath() + "，请手动删除");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}