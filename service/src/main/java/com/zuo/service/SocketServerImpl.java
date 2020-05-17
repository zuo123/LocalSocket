package com.zuo.service;

import android.net.Credentials;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * 和客户端进行数据收发
 * <p>
 * 传递的数据为 二进制数组 byte[]
 *
 * @author zuo
 * @date 2020/5/14 15:08
 */
public class SocketServerImpl implements Runnable {
    private static final String TAG = "SocketServerImpl";
    private String localSocketAddress = "com.zuo.service";
    private BufferedOutputStream os;
    private BufferedInputStream is;
    public static final int bufferSizeOutput = 1024 * 1024;
    LocalServerSocket server;
    LocalSocket client;
    Handler handler;

    public SocketServerImpl(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        Log.i(TAG, "Server isOpen");
        try {
            if (null == server) {
                server = new LocalServerSocket(localSocketAddress);
            }
            if (null == client) {
                client = server.accept();
                Log.i(TAG, "Client Connected");
            }
            Credentials cre = client.getPeerCredentials();
            Log.i(TAG, "ClientID:" + cre.getUid());
            os = new BufferedOutputStream(client.getOutputStream(), bufferSizeOutput);
            is = new BufferedInputStream(client.getInputStream(), bufferSizeOutput);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        while (null != is) {
            try {
                if (is.available() <= 0) continue;
                Message msg = handler.obtainMessage();
                msg.obj = is;
                msg.arg1 = 1;
                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送数据
     *
     * @param data
     */
    public void send(byte[] data) throws Exception {
        if (null != os) {
            os.write(data);
            os.flush();
        }
    }

    /**
     * 关闭监听
     */
    public void close() {
        try {
            if (null != os) {
                os.close();
                os = null;
            }
            if (null != is) {
                is.close();
                is = null;
            }
            if (null != client) {
                client.close();
                client = null;
            }
            if (null != server) {
                server.close();
                server = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}