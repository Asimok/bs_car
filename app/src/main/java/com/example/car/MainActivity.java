package com.example.car;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.car.tools.Codes;
import com.example.car.tools.LongClickButton;
import com.example.car.tools.bluetooth_Pref;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private boolean shortPress = false;
    private final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private OutputStream os;
    private ConnectedThread thread;
    boolean connected = true;
    // private TextView  tvBandBluetooth;
    private TextView havaPeople, frontDistance;
    private bluetooth_Pref blue_sp;
    // 获取到蓝牙适配器
    public BluetoothAdapter mBluetoothAdapter;
    private Button openCarLamp, openCarBeep, connectCar, openCarLampLeft, openCarLampRight;
    private Button autoDrive, headDrive, one, two, three, zero,free_run;
    private LongClickButton btn_back, btn_front, btn_Left, btn_Right;
    BluetoothDevice lvDevice = null;
    private boolean connectedCar = false;
    BluetoothSocket lvSocket = null;
    private boolean boolopenTable3 = false, boolopenTable4 = false, boolopenTable2 = false, boolopenTable1 = false;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //tvBandBluetooth =   findViewById(R.id.tvBandBluetooth);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        blue_sp = bluetooth_Pref.getInstance(this);
        setTitle(String.format("已绑定设备：  %s  %s", blue_sp.getBluetoothName(), blue_sp.getBluetoothAd()));
//        havaPeople = findViewById(R.id.havaPeople);
//        frontDistance = findViewById(R.id.frontDistance);

        openCarLamp = findViewById(R.id.table2id);
        connectCar = findViewById(R.id.connectCar);
        autoDrive = findViewById(R.id.autoDrive);
        headDrive = findViewById(R.id.headDrive);
        openCarBeep = findViewById(R.id.table1id);
        openCarLampLeft = findViewById(R.id.table3id);
        openCarLampRight = findViewById(R.id.table4id);
        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);
        zero = findViewById(R.id.zero);
        free_run  = findViewById(R.id.bizhang);

        btn_back = (LongClickButton) findViewById(R.id.btn_back);
        btn_front = (LongClickButton) findViewById(R.id.btn_front);
        btn_Left = (LongClickButton) findViewById(R.id.btn_left);
        btn_Right = (LongClickButton) findViewById(R.id.btn_right);

//连续点击
        btn_back.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
            @Override
            public void repeatAction() {
                try {
                    send(blue_sp.getBluetoothAd(), Codes.car_back);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 50);
        btn_front.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
            @Override
            public void repeatAction() {
                try {
                    send(blue_sp.getBluetoothAd(), Codes.car_forward);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 50);

        btn_Left.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
            @Override
            public void repeatAction() {
                try {
                    send(blue_sp.getBluetoothAd(), Codes.car_turn_left);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 50);
        btn_Right.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
            @Override
            public void repeatAction() {
                try {
                    send(blue_sp.getBluetoothAd(), Codes.car_turn_right);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 50);


        //单次点击
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    send(blue_sp.getBluetoothAd(), Codes.car_back);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_front.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    send(blue_sp.getBluetoothAd(), Codes.car_forward);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_Left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    send(blue_sp.getBluetoothAd(), Codes.car_turn_left);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_Right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    send(blue_sp.getBluetoothAd(), Codes.car_turn_right);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        openCarBeep.setOnClickListener(this);
        connectCar.setOnClickListener(this);
        headDrive.setOnClickListener(this);
        openCarLamp.setOnClickListener(this);
        openCarLampLeft.setOnClickListener(this);
        openCarLampRight.setOnClickListener(this);
        autoDrive.setOnClickListener(this);
        headDrive.setOnClickListener(this);
        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        zero.setOnClickListener(this);
        free_run.setOnClickListener(this);
    }


    // 创建handler，因为我们接收是采用线程来接收的，在线程中无法操作UI，所以需要handler
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
//TODO 处理接收到的信息
            super.handleMessage(msg);
            Log.e("cc1", "收到 " + (String) msg.obj);
            String message = (String) msg.obj;
            if (message.contains("cm") && message.contains("人")) {
                frontDistance.setText("前方障碍物:" + message.substring(0, message.indexOf("c")));
                havaPeople.setText("是否有人:" + message.substring(message.indexOf("m") + 1));
            }

        }
    };

    public void Stop(View view) throws IOException {
        send(blue_sp.getBluetoothAd(), Codes.car_stop);
    }

    public void left_place(View view) throws IOException {
        send(blue_sp.getBluetoothAd(), Codes.car_turn_left_place);
    }

    public void right_place(View view) throws IOException {
        send(blue_sp.getBluetoothAd(), Codes.car_turn_right_place);
    }

    @Override
    public void onClick(View view) {
        //TODO 按钮点击
        switch (view.getId()) {
            case R.id.connectCar:
                if (connectCar.getText().toString().equals("连接小车")) {
                    if (blue_sp.getBluetoothAd().equals("null")) {
                        Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                    } else {
                        try {

                            send(blue_sp.getBluetoothAd(), Codes.beep1);
                            connectCar.setBackgroundResource(R.drawable.btn_close);
                            headDrive.setBackgroundResource(R.drawable.btn_close);
                            autoDrive.setBackgroundResource(R.drawable.btn_open);
                            connectCar.setText("断开连接");

                            //延时函数
                            try {
                                Thread.sleep(1000);//单位：毫秒
                                send(blue_sp.getBluetoothAd(), Codes.head_drive);

                                connectedCar = true;
                            } catch (Exception ignored) {
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                } else {
                    connectCar.setText("连接小车");
                    try {
                        connectCar.setBackgroundResource(R.drawable.btn_open);
                        send(blue_sp.getBluetoothAd(), Codes.car_stop);

                        try {
                            Thread.sleep(1000);//单位：毫秒
                            send(blue_sp.getBluetoothAd(), Codes.beep3);
                            connectedCar = true;
                        } catch (Exception ignored) {
                        }


                        connectedCar = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                break;
            case R.id.table3id:
                try {
                    if (!boolopenTable3) {
                        if (blue_sp.getBluetoothAd().equals("null")) {
                            Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            send(blue_sp.getBluetoothAd(), Codes.table_3);
                            openCarLampLeft.setBackgroundResource(R.drawable.btn_close);
                            boolopenTable3 = !boolopenTable3;
                        }
                    } else {
                        send(blue_sp.getBluetoothAd(), Codes.car_stop);
                        openCarLampLeft.setBackgroundResource(R.drawable.btn_open);
                        boolopenTable3 = !boolopenTable3;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.table4id:
                try {
                    if (!boolopenTable4) {
                        if (blue_sp.getBluetoothAd().equals("null")) {
                            Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            send(blue_sp.getBluetoothAd(), Codes.table_4);
                            openCarLampRight.setBackgroundResource(R.drawable.btn_close);
                            boolopenTable4 = !boolopenTable4;

                        }
                    } else {
                        send(blue_sp.getBluetoothAd(), Codes.car_stop);
                        openCarLampRight.setBackgroundResource(R.drawable.btn_open);
                        boolopenTable4 = !boolopenTable4;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.table2id:
                try {
                    if (!boolopenTable2) {
                        if (blue_sp.getBluetoothAd().equals("null")) {
                            Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            send(blue_sp.getBluetoothAd(), Codes.table_2);
                            openCarLamp.setBackgroundResource(R.drawable.btn_close);
                            boolopenTable2 = !boolopenTable2;
                        }
                    } else {
                        send(blue_sp.getBluetoothAd(), Codes.car_stop);
                        openCarLamp.setBackgroundResource(R.drawable.btn_open);
                        boolopenTable2 = !boolopenTable2;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.table1id:
                try {
                    if (!boolopenTable1) {
                        if (blue_sp.getBluetoothAd().equals("null")) {
                            Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            send(blue_sp.getBluetoothAd(), Codes.table_1);
                            openCarBeep.setBackgroundResource(R.drawable.btn_close);
                            boolopenTable1 = !boolopenTable1;
                        }
                    } else {
                        send(blue_sp.getBluetoothAd(), Codes.car_stop);
                        openCarBeep.setBackgroundResource(R.drawable.btn_open);
                        boolopenTable1 = !boolopenTable1;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.autoDrive:
                try {

                    if (blue_sp.getBluetoothAd().equals("null")) {
                        Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                    } else {
                        send(blue_sp.getBluetoothAd(), Codes.auto_drive);
                        headDrive.setBackgroundResource(R.drawable.btn_open);
                        autoDrive.setBackgroundResource(R.drawable.btn_close);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.headDrive:
                try {

                    if (blue_sp.getBluetoothAd().equals("null")) {
                        Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                    } else {
                        send(blue_sp.getBluetoothAd(), Codes.head_drive);
                        autoDrive.setBackgroundResource(R.drawable.btn_open);
                        headDrive.setBackgroundResource(R.drawable.btn_close);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.one:

                if (blue_sp.getBluetoothAd().equals("null")) {
                    Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        send(blue_sp.getBluetoothAd(), Codes.one);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    zero.setBackgroundResource(R.drawable.btn_band);
                    one.setBackgroundResource(R.drawable.btn_close);
                    two.setBackgroundResource(R.drawable.btn_band);
                    three.setBackgroundResource(R.drawable.btn_band);
                }
                break;
            case R.id.two:

                if (blue_sp.getBluetoothAd().equals("null")) {
                    Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        send(blue_sp.getBluetoothAd(), Codes.two);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    zero.setBackgroundResource(R.drawable.btn_band);
                    two.setBackgroundResource(R.drawable.btn_close);
                    one.setBackgroundResource(R.drawable.btn_band);
                    three.setBackgroundResource(R.drawable.btn_band);
                }
                break;
            case R.id.three:

                if (blue_sp.getBluetoothAd().equals("null")) {
                    Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        send(blue_sp.getBluetoothAd(), Codes.three);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    zero.setBackgroundResource(R.drawable.btn_band);
                    three.setBackgroundResource(R.drawable.btn_close);
                    one.setBackgroundResource(R.drawable.btn_band);
                    two.setBackgroundResource(R.drawable.btn_band);
                }
                break;
            case R.id.zero:

                if (blue_sp.getBluetoothAd().equals("null")) {
                    Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        send(blue_sp.getBluetoothAd(), Codes.zero);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    zero.setBackgroundResource(R.drawable.btn_close);
                    three.setBackgroundResource(R.drawable.btn_band);
                    one.setBackgroundResource(R.drawable.btn_band);
                    two.setBackgroundResource(R.drawable.btn_band);
                }
                break;
            case R.id.bizhang:

                if (free_run.getText().toString().equals("自由")) {
                    if (blue_sp.getBluetoothAd().equals("null")) {
                        Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            //关闭避障
                            send(blue_sp.getBluetoothAd(), Codes.free_run_open);
                            free_run.setBackgroundResource(R.drawable.btn_close);
                            free_run.setText("避障");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                } else {
                    free_run.setText("自由");
                    try {
                        //打开避障
                        free_run.setBackgroundResource(R.drawable.btn_open);
                        send(blue_sp.getBluetoothAd(), Codes.free_run_close);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }

    }


    //右上角三个点
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /***
     * 向指定的蓝牙设备发送数据
     */
    public void send(String pvsMac, byte[] pvsContent) throws IOException {

        // 如果选择设备为空则代表还没有选择设备
        if (lvDevice == null) {
            //通过地址获取到该设备
            lvDevice = mBluetoothAdapter.getRemoteDevice(pvsMac);
        }
        // 这里需要try catch一下，以防异常抛出
        try {
            // 判断客户端接口是否为空
            if (lvSocket == null) {
                // 获取到客户端接口
                lvSocket = lvDevice
                        .createRfcommSocketToServiceRecord(MY_UUID);
                // 向服务端发送连接
                lvSocket.connect();

                // 获取到输出流，向外写数据
                os = lvSocket.getOutputStream();
                if (connected) {
                    connected = false;
                    // 实例接收客户端传过来的数据线程
                    thread = new ConnectedThread(lvSocket);
                    // 线程开始
                    thread.start();
                }
            }
            // 判断是否拿到输出流
            if (os != null) {
                // 需要发送的信息
                // 以utf-8的格式发送出去
                os.write(pvsContent);
            }
            // 吐司一下，告诉用户发送成功
            Toast.makeText(this, "发送信息成功，请查收", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // 如果发生异常则告诉用户发送失败
            Toast.makeText(this, "发送信息失败", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_1:
                Toast.makeText(this, "已经断开连接", Toast.LENGTH_SHORT).show();
                os = null;
                lvSocket = null;
                lvDevice = null;
                connected = true;
                thread.cancel();


                break;
            case R.id.menu_3:
                try {
                    lvDevice = null;
                    os = null;
                    lvSocket = null;
                    connected = true;
                    thread.cancel();

                    send(blue_sp.getBluetoothAd(), Codes.car_stop);

                    Toast.makeText(this, "已重新连接", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                break;
            case R.id.menu_2:
                Toast.makeText(this, "绑定蓝牙", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(MainActivity.this, Bluetooth_band.class);
                startActivity(intent1);
                break;

        }
        return true;
    }

    private boolean isNeedRequestPermissions(List<String> permissions) {
        // 定位精确位置
        addPermission(permissions, Manifest.permission.ACCESS_FINE_LOCATION);
        // 存储权限
        addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // 读取手机状态
        addPermission(permissions, Manifest.permission.READ_PHONE_STATE);
        return permissions.size() > 0;
    }

    private void addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 适配android M，检查权限
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNeedRequestPermissions(permissions)) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
        }
    }

    @Override
    protected void onResume() {
//        tvBandBluetooth.setText(String.format("已绑定设备：  %s  %s", blue_sp.getBluetoothName(), blue_sp.getBluetoothAd()));
        setTitle(String.format("已绑定设备：  %s  %s", blue_sp.getBluetoothName(), blue_sp.getBluetoothAd()));
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        connected = false;
        thread.cancel();
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d("aa", "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d("aa", "temp sockets not created" + e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            if (Thread.interrupted()) {
                Log.d("aa", "return");
                return;
            }
            Log.d("aa", "BEGIN mConnectedThread");
            byte[] buffer = new byte[256];
            int bytes;


            while (true) {
                synchronized (this) {

                    try {
                        while (mmInStream.available() == 0) {
                        }
                        try {
                            Thread.sleep(100);  //当有数据流入时，线程休眠一段时间，默认100ms
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bytes = mmInStream.read(buffer);  //从字节流中读取数据填充到字节数组，返回读取数据的长度

                        Log.d("aa", "count   " + bytes);
                        // 创建Message类，向handler发送数据
                        Message msg = new Message();
                        // 发送一个String的数据，让他向上转型为obj类型
                        msg.obj = new String(buffer, 0, bytes, "utf-8");
                        // 发送数据
                        Log.d("aa", "data   " + msg.obj);

                        handler.sendMessage(msg);
                    } catch (IOException e) {
                        Log.e("aa", "disconnected", e);

                        break;
                    }
                }


            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e("aa", "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("aa", "close() of connect socket failed", e);
            }
        }
    }
}