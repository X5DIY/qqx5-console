package qqx5;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

import static qqx5.Bytes.*;

public class BytesThread extends Thread {

    private static boolean PrintBytes = false;// 是否打印bytes文件本身的信息，便于调试

    private int threadNo;
    private File xml;
    private byte[] b;// 存放bytes文件信息的byte数组
    private int index;// 当前读取位置

    BytesThread(int threadNo) {
        this.threadNo = threadNo;
    }

    public void run() {
        for (int i = 0; i < fileNum; i++) {
            if (i % threadNum == threadNo) {
                process(files[i]);
            }
        }
    }

    private void process(File bytes) {
        try {
            String xmlPath = bytes.getCanonicalPath()
                    .substring(0, bytes.getCanonicalPath().lastIndexOf("."));
            // 原目录下进行转化
            xml = new File(xmlPath);
            // 先将数据用二进制流读出来
            DataInputStream dis = new DataInputStream(new FileInputStream(bytes));// 二进制文件用dis流读取
            int bytesLength = dis.available();// 读取流的长度
            b = new byte[bytesLength];// 创建等于流大小的 byte 数组
            dis.readFully(b);// 将流的内容全部写入 byte 数组中
            dis.close();
            // 再判断模式，并用自己的方式提取
            // 注意，该部分不能用 DataInputStream流 提供的读取数据方式（参考 getInt() 注释）
            if (PrintBytes) { // 用于分辨bytes文件的数据存储结构
                for (int k = 0; k < bytesLength - 4; k++) {
                    index = k;
                    System.out.printf("b[%5d] = ", k);
                    System.out.print(getHexStr(b[k]));
                    index = k;
                    int i = getInt();
                    if (i > 999999 || i < -99999) {
                        System.out.print(" int = 999999");
                    } else {
                        System.out.printf(" int = %6d", i);
                    }
                    index = k;
                    float f = getFloat();
                    if (f > 999999 || f < -99999) {
                        System.out.print(" float = 999999.00");
                    } else {
                        System.out.printf(" float = %9.2f", f);
                    }
                    index = k;
                    System.out.print(" char1 = " + new String(b, index, 1, StandardCharsets.UTF_8));
                    System.out.println(" char3 = " + new String(b, index, 3, StandardCharsets.UTF_8));
                }
            }
            index = 0;
            int mode;
            switch (getString()) {
                case "XmlIdolExtend":
                    mode = 1;
                    break;
                case "XmlPinballExtend":
                    mode = 2;
                    break;
                case "XmlBubbleExtend":
                    mode = 3;
                    break;
                case "XmlCrescentExtend":
                    mode = 4;
                    break;
                default:
                    System.out.println(bytes.getCanonicalPath() + "不是炫舞谱面bytes文件！");
                    return;
            }
            writeXml(mode);
            switch (mode) {
                case 1:
                    System.out.println("星动 " + bytes.getName() + " 转化完毕！");
                case 2:
                    System.out.println("弹珠 " + bytes.getName() + " 转化完毕！");
                case 3:
                    System.out.println("泡泡 " + bytes.getName() + " 转化完毕！");
                case 4:
                    System.out.println("弦月 " + bytes.getName() + " 转化完毕！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 仅在调试时使用，返回一个byte对应的十六进制字符串。
     * 如 (byte)0000 0000 返回 "00"（即0x00），
     * (byte)1111 1111 返回 "ff"（即0xff）
     *
     * @param b 一个byte
     * @return 对应的十六进制字符串，不足两字符的补足至两字符
     */
    private static String getHexStr(byte b) {
        StringBuilder stringBuilder = new StringBuilder();
        String hexStr = Integer.toHexString(b & 0xFF);// 避免错误的符号扩展，参考 getInt() 注释
        if (hexStr.length() < 2) {
            stringBuilder.append(0);
        }
        stringBuilder.append(hexStr);
        return stringBuilder.toString();
    }

    /**
     * 四字节转换为整型（byte认为是无符号）
     * <index>
     * 为什么不直接用 DataInputStream流 提供的 readInt() 来提取数据呢？
     * 有以下两点原因。
     * <index>
     * 一、Java以有符号识别byte的值，而bytes文件实际上都是无符号的值。
     * 在进行位扩展的时候，就需要将不正确的高位补齐去掉，方法是跟 0xff 进行与操作。
     * 1.byte的大小为8bits，而int的大小为32bits
     * 2.java的二进制采用的是补码形式（补码可以将减法转为加法）
     * 正数原码、反码、补码都相同；
     * 负数符号位为1不变，其余各位对原码取反得到反码，再加1得到补码。
     * 如果直接byte转int，Java会认为byte是有符号的，前面都会补上1.
     * 比如 -1 的原码为 1000 0001，反码为 1111 1110，补码为 1111 1111.
     * 255 的原码也为 1111 1111，bytes文件读出来应为255，而不是-1。
     * 这个补码转int为 1111 1111 1111 1111 1111 1111 1111 1111（0xffffffff，-1的补码）.
     * 这个结果是不对的，所以跟 0xff 进行与操作后，
     * 得到 0000 0000 0000 0000 0000 0000 1111 1111（0x000000ff，正数255的原码/补码）.
     * 这才是我们所要的数据。
     * 并且，这样不会出现负数，故而不需要抛出 EOFException.
     * <index>
     * 二、bytes文件为小端在前大端在后，但是Java默认大端在前小端在后
     * 比如整数4，小端在前为04 00 00 00，大端在前为00 00 00 04.
     * 如果用Java自带的方法去读数据，则会得到67108864，这显然是不对的。
     * <index>
     * 综合以上两点，必须先用 DataInputStream流 将数据读到 byte[] 中，
     * 再用自己的方式读取正确的数据。
     *
     * @return 转化后的整型数据
     */
    private int getInt() {
        int ch1 = b[index] & 0xff;
        int ch2 = b[index + 1] & 0xff;
        int ch3 = b[index + 2] & 0xff;
        int ch4 = b[index + 3] & 0xff;
        index += 4;// 读完数据后移动指针
        return (ch1 + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
    }

    /**
     * 四字节转换为浮点型
     * 在上面的 getInt() 中，已经说明了Java提取数据的特点。
     * 所以这里依然要先对byte[]进行处理，
     * 再使用 Float.intBitsToFloat(int) 得到正确结果。
     *
     * @return 转化后的浮点型数据
     */
    private float getFloat() {
        int l;
        l = b[index];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        index += 4;// 读完数据后移动指针
        return Float.intBitsToFloat(l);
    }

    /**
     * 单字节转换为布尔型
     * <index>
     * 尽管原则上讲，非零为真，零为假，
     * 但实际上 "IsFourTrack" 这个属性只会是00（假），或者01（真）。
     * <p>
     * 除此之外，Java会返回 true/false，但 xml 中一般为 "True"/"False"，
     * 所以还要进行一下转化（虽然我也不知道有没有影响就是啦）
     * （转化不在此处，在应用时转化）
     *
     * @return 转化后的布尔型数据
     */
    private boolean getBoolean() {
        boolean bool = (b[index] != 0);
        index++;// 读完数据后移动指针
        return bool;
    }

    /**
     * 单字节/多字节转换为字符串
     * <p>
     * 字符串在bytes文件中存储方式如下：
     * 1.一个int表示后面字符串的长度，假设该值为a
     * 2.字符串本身，长度为a个byte
     * 3.一个0x00作为结束
     * <p>
     * 所以在取出字符串时，应先将index调到表示字符串长度的int的位置，
     * 再使用 getString() 得到字符串。
     * getString() 中，需要先使用一次 getInt()，得到字符串长度。
     * 注意，使用 getInt() 会使 index 加4，所以前两行不能合并。
     *
     * @return 转化后的字符串
     */
    private String getString() {
        int length = getInt();// 截取str的长度
        String s = new String(b, index, length, StandardCharsets.UTF_8);
        index += (length + 1);// 读完数据后移动指针，还要跳过结尾的0x00
        return s;
    }


    private void writeXml(int mode) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(xml));
            topInfo(bw, mode);
            if (mode == 1) {
                idolNoteInfo(bw);
            } else if (mode == 2) {
                pinballNoteInfo(bw);
            } else if (mode == 3) {
                bubbleNoteInfo(bw);
            } else if (mode == 4) {
                crescentNoteInfo(bw);
            }
            bottomInfo(bw, mode);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void topInfo(BufferedWriter bw, int mode) {
        try {
            bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            bw.newLine();
            bw.write("<Level>");
            bw.newLine();
            bw.write("  <LevelInfo>");
            bw.newLine();
            bw.write("    <BPM>" + getFloat() + "</BPM>");
            // new DecimalFormat(".00").format(getFloat()) 保留两位小数
            bw.newLine();
            bw.write("    <BeatPerBar>" + getInt() + "</BeatPerBar>");
            bw.newLine();
            bw.write("    <BeatLen>" + getInt() + "</BeatLen>");
            bw.newLine();
            bw.write("    <EnterTimeAdjust>" + getInt() + "</EnterTimeAdjust>");
            bw.newLine();
            bw.write("    <NotePreShow>" + getFloat() + "</NotePreShow>");
            bw.newLine();
            bw.write("    <LevelTime>" + getInt() + "</LevelTime>");
            bw.newLine();
            bw.write("    <BarAmount>" + getInt() + "</BarAmount>");
            bw.newLine();
            bw.write("    <BeginBarLen>" + getInt() + "</BeginBarLen>");
            bw.newLine();
            boolean isFourTrack = getBoolean();
            int trackCount = getInt();
            int levelPreTime = getInt();
            if (trackCount != 0) {// 星弹泡
                bw.write("    <IsFourTrack>" + (isFourTrack ? "True" : "False") + "</IsFourTrack>");
                bw.newLine();
                bw.write("    <TrackCount>" + trackCount + "</TrackCount>");
                bw.newLine();
                bw.write("    <LevelPreTime>" + levelPreTime + "</LevelPreTime>");
                bw.newLine();
            } else {// 弦月
                bw.write("    <LevelPreTime>" + levelPreTime + "</LevelPreTime>");
                bw.newLine();
                bw.write("    <Type>Crescent</Type>");
                bw.newLine();
            }
            // int -1
            index += 4;
            bw.write("  </LevelInfo>");
            bw.newLine();
            bw.write("  <MusicInfo>");
            bw.newLine();
            bw.write("    <Title>" + getString() + "</Title>");
            bw.newLine();
            bw.write("    <Artist>" + getString() + "</Artist>");
            bw.newLine();
            bw.write("    <FilePath>" + getString() + "</FilePath>");
            // 5个0x00
            index += 5;
            bw.newLine();
            bw.write("  </MusicInfo>");
            bw.newLine();
            bw.write("  <SectionSeq>");
            bw.newLine();
            int sectionTypeNum = getInt();
            for (int i = 0; i < sectionTypeNum; i++) {
                bw.write("    <Section type=\"" + getString());
                if (i == 0) {
                    index += 4;// Section type="previous" 时，不需要 startbar
                } else {
                    bw.write("\" startbar=\"" + getInt());
                }
                bw.write("\" endbar=\"" + getInt());
                bw.write("\" mark=\"" + getString());
                bw.write("\" param1=\"" + getString());
                // 10个0x00
                index += 10;
                bw.write("\" />");
                bw.newLine();
            }
            bw.write("  </SectionSeq>");
            bw.newLine();
            if (mode != 4) {
                bw.write("  <IndicatorResetPos PosNum=\"" + getInt());
                int resetNum = getInt();
                if (resetNum == 0) {
                    bw.write("\" />");
                    bw.newLine();
                } else {
                    bw.write("\">");
                    bw.newLine();
                    for (int i = 0; i < resetNum; i++) {
                        bw.write("    <Pos Bar=\"" + getInt() + "\" BeatPos=\"" + getInt() + "\" />");
                        bw.newLine();
                    }
                    bw.write("  </IndicatorResetPos>");
                    bw.newLine();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void idolNoteInfo(BufferedWriter bw) {
        try {
            bw.write("  <NoteInfo>");
            bw.newLine();
            bw.write("    <Normal>");
            bw.newLine();
            int noteNum = getInt();
            int combineNote;// 一定存在，为 int 0（表示没有CombineNote标签）或 int 1
            int combineNum;// combineNote 为 1 时存在，表示CombineNote标签的长度
            for (int i = 0; i < noteNum; i++) {
                combineNote = getInt();
                if (combineNote == 0) {
                    oneIdolNote(bw);
                } else if (combineNote == 1) {
                    combineNum = getInt();
                    bw.write("      <CombineNote>");
                    bw.newLine();
                    for (int j = 0; j < combineNum; j++) {
                        bw.write("  ");
                        oneIdolNote(bw);
                    }
                    bw.write("      </CombineNote>");
                    bw.newLine();
                }
            }
            bw.write("    </Normal>");
            bw.newLine();
            bw.write("  </NoteInfo>");
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void oneIdolNote(BufferedWriter bw) {
        try {
            bw.write("      <Note Bar=\"" + getInt() + "\" Pos=\"" + getInt());
            String s1 = getString();
            String s2 = getString();
            String s3 = getString();
            String type = getString();
            int endBar = getInt();
            int endPos = getInt();
            switch (type) {
                case "short":
                    bw.write("\" from_track=\"" + s1 + "\" target_track=\"" + s3
                            + "\" note_type=\"" + type + "\" />");
                    break;
                case "slip":
                    bw.write("\" target_track=\"" + s3 + "\" end_track=\"" + s2
                            + "\" note_type=\"" + type + "\" />");
                    break;
                case "long":
                    bw.write("\" from_track=\"" + s1 + "\" target_track=\"" + s3
                            + "\" note_type=\"" + type + "\" EndBar=\"" + endBar
                            + "\" EndPos=\"" + endPos + "\" />");
                    break;
            }
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void pinballNoteInfo(BufferedWriter bw) {
        try {
            bw.write("  <NoteInfo>");
            bw.newLine();
            bw.write("    <Normal>");
            bw.newLine();
            int noteNum = getInt();
            for (int i = 0; i < noteNum; i++) {
                onePinballNote(bw);
            }
            bw.write("    </Normal>");
            bw.newLine();
            bw.write("  </NoteInfo>");
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onePinballNote(BufferedWriter bw) {
        try {
            bw.write("      <Note ID=\"" + getInt() + "\" note_type=\"" + getString()
                    + "\" Bar=\"" + getInt() + "\" Pos=\"" + getInt());
            int endBar = getInt();
            if (endBar != 0) {
                bw.write("\" EndBar=\"" + endBar + "\" EndPos=\"" + getInt());
            } else {
                index += 4;
            }
            bw.write("\" Son=\"");
            int sonNum = getInt();// Son状态标识，0表示Son为空，1表示后面存储Son的值
            if (sonNum != 0) {
                bw.write("" + getInt());
            }
            bw.write("\" EndArea=\"");
            int endAreaNum = getInt();// EndArea参数数目，0表示无参数（即按键在random区）
            if (endAreaNum == 1) {
                bw.write("" + (getInt() + 1));
            } else if (endAreaNum == 2) {
                bw.write((getInt() + 1) + "|" + (getInt() + 1));
            }
            bw.write("\" MoveTime=\"" + (int) getFloat() + "\" />");
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void bubbleNoteInfo(BufferedWriter bw) {
        try {
            bw.write("  <NoteInfo>");
            bw.newLine();
            bw.write("    <Normal PosNum=\"" + getInt() + "\">");
            bw.newLine();
            int noteNum = getInt();
            for (int i = 0; i < noteNum; i++) {
                oneBubbleNote(bw);
            }
            bw.write("    </Normal>");
            bw.newLine();
            bw.write("  </NoteInfo>");
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void oneBubbleNote(BufferedWriter bw) {
        try {
            bw.write("      <Note Bar=\"" + getInt()
                    + "\" BeatPos=\"" + getInt() + "\" Track=\"" + getInt());
            int type = getInt();
            if (type == 0) {
                bw.write("\" Type=\"" + type + "\">");
                // EndBar, EndBar, ID（单点不需要）
                index += 12;
                bw.newLine();
            } else {
                bw.write("\" Type=\"" + type + "\" EndBar=\"" + getInt()
                        + "\" EndPos=\"" + getInt() + "\" ID=\"" + getInt() + "\">");
                bw.newLine();
            }
            int a8 = getInt();// 最开始写这段代码时，要把所有数据列出来，然后自己拼出正确的结构
            // 单点有16个数据，非单点有18个数据，前八个数据类型相同，后面数据根据第八个而变
            // 就不改名了，当个纪念吧233（上面的type其实就是a4）
            if (a8 == 0) {
                // int 0, int 1
                index += 8;
                bw.write("        <ScreenPos x=\"" + getInt() + "\" y=\"" + getInt() + "\">");
                bw.newLine();
                // int 0, int 1
                index += 8;
                bw.write("          <FlyTrack name=\"" + getString()
                        + "\" degree=\"" + getFloat() + "\" />");
                bw.newLine();
                bw.write("        </ScreenPos>");
                bw.newLine();
                bw.write("      </Note>");
                bw.newLine();
            } else if (a8 == 1) {
                bw.write("        <MoveTrack name=\"" + getString()
                        + "\" degree=\"" + getFloat() + "\" />");
                bw.newLine();
                // int 1
                index += 4;
                bw.write("        <FlyTrack name=\"" + getString()
                        + "\" degree=\"" + getFloat() + "\" />");
                bw.newLine();
                // int 1
                index += 4;
                bw.write("        <ScreenPos x=\"" + getInt() + "\" y=\"" + getInt() + "\" />");
                bw.newLine();
                // int 0, int 0
                index += 8;
                bw.write("      </Note>");
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void crescentNoteInfo(BufferedWriter bw) {
        try {
            bw.write("  <NoteInfo>");
            bw.newLine();
            bw.write("    <Normal>");
            bw.newLine();
            // int 0, int 0
            index += 8;
            int noteNum = getInt();
            for (int i = 0; i < noteNum; i++) {
                oneCrescentNote(bw);
            }
            bw.write("    </Normal>");
            bw.newLine();
            bw.write("  </NoteInfo>");
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void oneCrescentNote(BufferedWriter bw) {
        try {
            bw.write("      <Note Bar=\"" + getInt() + "\" Pos=\"" + getInt());
            String type = getString();
            int track = getInt();
            switch (type) {
                case "short":
                case "light":
                case "long":
                    // int 0
                    index += 4;
                    bw.write("\" track=\"" + track + "\" note_type=\"" + type);
                    if (type.equals("long")) {
                        bw.write("\" length=\"" + getInt());
                    } else {
                        // int 0
                        index += 4;
                    }
                    // int 0
                    index += 16;
                    break;
                case "pair":
                    bw.write("\" track=\"" + track
                            + "\" target_track=\"" + getInt() + "\" note_type=\"" + type);
                    // 5个int 0
                    index += 20;
                    break;
                case "slip":
                    int a = getInt();// 可能是target_track，要看后面的值
                    int b = getInt();// 非零为length
                    // 2个int 0
                    index += 8;
                    if (b != 0) {// 因为target_track可能为0，所以不用a的值判断
                        bw.write("\" track=\"" + track + "\" target_track=\"" + a
                                + "\" note_type=\"" + type + "\" length=\"" + b);
                        // 2个int 0
                        index += 8;
                    } else {// a == 0 && b == 0
                        bw.write("\" track=\"" + track + "\" target_track=\"");
                        int target_trackNum = getInt();
                        for (int i = 0; i < target_trackNum; i++) {
                            bw.write(((i == 0) ? "" : ",") + getInt());
                        }
                        bw.write("\" note_type=\"" + type + "\" length=\"");
                        int lengthNum = getInt();
                        for (int i = 0; i < lengthNum; i++) {
                            bw.write(((i == 0) ? "" : ",") + getInt());
                        }
                    }
                    break;
            }
            bw.write("\" />");
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void bottomInfo(BufferedWriter bw, int mode) {
        try {
            int actionSeqType = getInt();// ActionSeq 类型，有两种，结构不同
            bw.write("  <ActionSeq type=\"" + actionSeqType + "\">");
            bw.newLine();
            int actionSeqNum = getInt();// ActionSeq 个数
            if (actionSeqType == 0) {
                for (int i = 0; i < actionSeqNum; i++) {
                    bw.write("    <ActionList start_bar=\"" + getInt());
                    // 全是0x00
                    index += 17;
                    bw.write("\" id=\"" + getString() + "\" />");
                    // 全是0x00
                    index += 5;
                    bw.newLine();
                }
                if (mode == 1) {
                    index += 4;// 星动
                }
                bw.write("  </ActionSeq>");
                bw.newLine();
                bw.write("  <CoupleActionSeq type=\"" + getInt() + "\">");
                bw.newLine();
                int coupleActionSeq = getInt();// CoupleActionSeq 个数
                for (int i = 0; i < coupleActionSeq; i++) {
                    bw.write("    <ActionList start_bar=\"" + getInt()
                            + "\" dance_len=\"" + getInt() + "\" seq_len=\"" + getInt()
                            + "\" level=\"" + getInt() + "\" type=\"" + getString() + "\" />");
                    index += 10;
                    bw.newLine();
                }
                bw.write("  </CoupleActionSeq>");
                bw.newLine();
            } else if (actionSeqType == 1) {
                for (int i = 0; i < actionSeqNum; i++) {
                    bw.write("    <ActionList start_bar=\"" + getInt()
                            + "\" dance_len=\"" + getInt() + "\" seq_len=\"" + getInt()
                            + "\" level=\"" + getInt() + "\" type=\"" + getString() + "\" />");
                    index += 10;
                    bw.newLine();
                }
                // 都是0x00
                if (mode == 1) {
                    index += 4;// 星动
                } else {
                    index += 8;// 弹珠、泡泡、弦月
                }
                bw.write("  </ActionSeq>");
                bw.newLine();
            }
            bw.write("  <CameraSeq>");
            bw.newLine();
            int cameraSeqNum = getInt();
            for (int i = 0; i < cameraSeqNum; i++) {
                bw.write("    <Camera name=\"" + getString()
                        + "\" bar=\"" + getInt() + "\" pos=\"" + getInt()
                        + "\" end_bar=\"" + getInt() + "\" end_pos=\"" + getInt() + "\" />");
                bw.newLine();
            }
            bw.write("  </CameraSeq>");
            bw.newLine();
            bw.write("  <DancerSort>");
            bw.newLine();
            int dancerSortNum = getInt();
            for (int i = 0; i < dancerSortNum; i++) {
                bw.write("    <Bar>" + getInt() + "</Bar>");
                bw.newLine();
            }
            bw.write("  </DancerSort>");
            bw.newLine();
            bw.write("  <StageEffectSeq>");
            bw.newLine();
            int stageEffectSeqNum = getInt();
            for (int i = 0; i < stageEffectSeqNum; i++) {
                bw.write("    <effect name=\"" + getString()
                        + "\" bar=\"" + getInt()
                        + "\" length=\"" + getInt() + "\" />");
                // 5个0x00
                index += 5;
                bw.newLine();
            }
            bw.write("  </StageEffectSeq>");
            bw.newLine();
            if (mode == 4) {
                bw.write("  <IndicatorResetPos PosNum=\"64\" />");// bytes文件没有这个值
                // 最后int32应该是MD5之类的东西，是个str
                bw.newLine();
            }
            bw.write("</Level>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
