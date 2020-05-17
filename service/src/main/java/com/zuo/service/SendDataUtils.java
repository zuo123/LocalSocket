package com.zuo.service;

import java.io.BufferedInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import androidx.annotation.NonNull;

/**
 * LocalSocket 传输数据（封装、解析）工具类
 *
 * @author zuo
 * @date 2020/5/14 19:20
 */
public class SendDataUtils {
    /**
     * 对应数据的 size ，7 位 （9.5M）
     */
    private static final int infoSize = 7;
    private static final int dataSize = 7;

    /**
     * 封装 LocalSocket 发送的数据
     *
     * @param info -- 需要发送的字符串数据
     * @param data -- 需要发送的字节流数据
     * @return 封装后的字节流数据
     */
    public static byte[] makeSendData(@NonNull String info, byte[] data) throws Exception {
        //文本信息
        Charset charset_utf8 = Charset.forName("utf-8");
        ByteBuffer buff = charset_utf8.encode(info);
        byte[] infoBytes = buff.array();
        int infoLength = infoBytes.length;
        byte[] headSizeBytes = String.valueOf(infoLength).getBytes();
        int dataLength = data == null ? 0 : data.length;
        byte[] dataSizeBytes = String.valueOf(dataLength).getBytes();
        int totalSize = infoSize + dataSize + infoLength + dataLength;
        byte[] output = new byte[totalSize];
        //1、头部信息（info size）
        System.arraycopy(headSizeBytes, 0, output, 0, headSizeBytes.length);
        //2、头部信息（data size）
        System.arraycopy(dataSizeBytes, 0, output, infoSize, dataSizeBytes.length);
        //2、info 信息
        System.arraycopy(infoBytes, 0, output, infoSize + dataSize, infoLength);
        if (dataLength > 0) {
            //拷贝 data 信息
            System.arraycopy(data, 0, output, infoSize + dataSize + infoLength, dataLength);
        }
        return output;
    }

    /**
     * 解析 LocalSocket 接收到的数据
     *
     * @param is -- 待解析的输入流
     * @return 解析后的数据
     * @throws Exception
     */
    public static SocketParseBean parseSendData(BufferedInputStream is) throws Exception {
        if (null == is||is.available() <= 0) return null;
        //拿到info信息的size
        byte[] infoSizeByte = new byte[infoSize];
        is.read(infoSizeByte);
        String infoLength = new String(infoSizeByte);
        String infoSizeStr = infoLength.trim();
        Integer infoSize = Integer.valueOf(infoSizeStr);
        //拿到data的size
        byte[] dataSizeByte = new byte[dataSize];
        is.read(dataSizeByte);
        String dataLength = new String(dataSizeByte);
        String dataSizeStr = dataLength.trim();
        Integer dataSize = Integer.valueOf(dataSizeStr);
        //读取info
        byte[] infoByte = new byte[infoSize];
        is.read(infoByte, 0, infoSize);
        SocketParseBean parseBean = new SocketParseBean();
        String s = new String(infoByte, "utf-8");
        parseBean.setInfo(s.trim());
        //读取data
        if (dataSize > 0) {
            byte[] buffer = new byte[dataSize];
            is.read(buffer, 0, dataSize);
            parseBean.setData(buffer);
        }
        return parseBean;
    }

}