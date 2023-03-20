package com.wildcard.flooke;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothServerSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public int REQUEST_ENABLE_BT = 1;
    Button listen, listDevices;
    ListView listView;
    TextView status;
    BluetoothAdapter instBlueAdapter;
    BluetoothDevice[] devices;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;
            }
            return true;
        }
    });

    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");

    private void findViewByIds() {
        listen = (Button) findViewById(R.id.listen);
        listView = (ListView) findViewById(R.id.listview);
        listDevices = (Button) findViewById(R.id.listDevices);
        status = (TextView) findViewById(R.id.status);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewByIds();
        instBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if(instBlueAdapter == null) {
            status.setText("Adapter null");
            // Device does not support bluetooth
            return;
        }
        if(!instBlueAdapter.isEnabled()) {
            // Enable Bluetooth
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        }
        else {
            status.setText("Bluetooth is already enabled");
        }
        implementListeners();
    }

    private void implementListeners() {
        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });
        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> deviceSet = instBlueAdapter.getBondedDevices();
                String[] strings = new String[deviceSet.size()];
                devices = new BluetoothDevice[deviceSet.size()];
                int index = 0;

                if(deviceSet.size()>0)
                {
                    for(BluetoothDevice device : deviceSet)
                    {
                        devices[index] = device;
                        strings[index] = device.getName();
                        index++;
                    }
                    ArrayAdapter<String> instArrayAdapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            android.R.layout.simple_list_item_1,
                            strings
                    );
                    listView.setAdapter(instArrayAdapter);
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClientClass clientClass = new ClientClass(devices[i]);
                clientClass.start();

                status.setText("Connecting");
            }
        });
    }

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass(){
            try {
                serverSocket = instBlueAdapter.listenUsingRfcommWithServiceRecord(
                    APP_NAME, MY_UUID
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        public void run() {
//            BluetoothSocket socket = null;
//
//            while (socket==null) {
//                try {
//                    Message message = Message.obtain();
//                    message.what = STATE_CONNECTING;
//                    handler.sendMessage(message);
//
//                    socket=serverSocket.accept();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Message message = Message.obtain();
//                    message.what = STATE_CONNECTION_FAILED;
//                    handler.sendMessage(message);
//                }
//                if(socket!=null) {
//                    Message message = Message.obtain();
//                    message.what = STATE_CONNECTED;
//                    handler.sendMessage(message);
//                    sendReceive = new SendReceive(socket);
//                    sendReceive.start();
//                    break;
//                }
//            }
//        }
    }

    private class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1) {
            device = device1;
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        public void run() {
//            try {
//                socket.connect();
//                Message message = Message.obtain();
//                message.what = STATE_CONNECTED;
//                handler.sendMessage(message);
//
//                sendReceive = new SendReceive(socket);
//                sendReceive.start();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                Message message = Message.obtain();
//                message.what = STATE_CONNECTION_FAILED;
//                handler.sendMessage(message);
//            }
//        }
    }
}
