package com.zuo.localsocket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.zuo.localsocket.databinding.ActivityMainBinding;

import java.io.BufferedInputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

/**
 * @author zuo
 * @date 2020/5/18 11:29
 */
public class MainActivity extends AppCompatActivity {

    //    private SocketClientImpl socketClient;
    private SocketTextImpl socketClient;
    private ActivityMainBinding binding;

    //持续接收服务端反馈信息
    private StringBuilder buffer = new StringBuilder();
    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.arg1 == 1) {
                SocketParseBean bean = null;
                try {
                    bean = SendDataUtils.parseSendData((BufferedInputStream) msg.obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (null == bean || TextUtils.isEmpty(bean.getInfo())) {
                    return false;
                }
                buffer.append(bean.getInfo());
                buffer.append("\r\n");
                showSocketMsg(bean.getData());
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setPresenter(new Presenter());
        startSocketClient();
        observerData();
    }

    private void observerData() {
        ThreadFactoryImpl threadFactory = new ThreadFactoryImpl();
        threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String data = LiveStreamRepository.getInstance().getData();
                    if (!TextUtils.isEmpty(data)) {
                        buffer.append(data);
                        buffer.append("\r\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showSocketMsg(null);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private void showSocketMsg(final byte[] data) {
        if (null != binding) {
            binding.backMsgShow.setText(buffer.toString());
        }
        showImg(data);
    }

    private void startSocketClient() {
//        socketClient = new SocketClientImpl(handler);
        socketClient = new SocketTextImpl();
        new Thread(socketClient).start();
    }

    private void showImg(byte[] data) {
        if (null == data) {
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        binding.imgShow.setImageBitmap(bitmap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != socketClient) {
            socketClient.close();
        }
    }

    public void sendData2Server(final String hint) {
        ThreadFactoryImpl threadFactory = new ThreadFactoryImpl();
        threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                if (null != socketClient) {
                    try {
                        socketClient.send(hint);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /*public void sendData2Server(final String hint, final Bitmap bmp) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(new Runnable() {
            @Override
            public void run() {
                if (null != socketClient) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] array = null;
                    try {
                        if (null != bmp) {
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                            array = baos.toByteArray();
                        }
                        byte[] bytes = SendDataUtils.makeSendData(hint, array);
                        socketClient.send(bytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }*/

    public class Presenter {

        public void sendData(View view) {
            String text = binding.clientInput.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(MainActivity.this, "消息内容不能为空！", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                sendData2Server(text);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "消息发送失败！", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
