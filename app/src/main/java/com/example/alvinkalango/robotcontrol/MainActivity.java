package com.example.alvinkalango.robotcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ImageButton Forward;
    ImageButton Backward;
    ImageButton Left;
    ImageButton Right;

    Button Buzzer;
    TextView BuzzerText;

    Switch Connect;
    Switch Navigate;
    Switch Led;

    ImageView Bt_icon;
    ImageView Nav_icon;
    ImageView Led_icon;
    ImageView Buz_icon;

    TextView Result;
    private String dataToSend;

    private static final String TAG = "Jon";
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private static String address = "98:D3:31:B4:35:0E";
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private InputStream inStream = null;
    Handler handler = new Handler();
    byte delimiter = 10;
    boolean stopWorker = false;
    int readBufferPosition = 0;
    byte[] readBuffer = new byte[1024];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Connect = (Switch) findViewById(R.id.connect);
        Navigate = (Switch) findViewById(R.id.navigate);
        Led = (Switch) findViewById(R.id.led);
        Buzzer = (Button) findViewById(R.id.buzzer);
        BuzzerText = (TextView) findViewById(R.id.textView);

        Forward = (ImageButton) findViewById(R.id.buttonForward);
        Backward = (ImageButton) findViewById(R.id.buttonBackward);
        Left = (ImageButton) findViewById(R.id.buttonLeft);
        Right = (ImageButton) findViewById(R.id.buttonRight);

        Bt_icon = (ImageView) findViewById(R.id.bt_icon);
        Nav_icon = (ImageView) findViewById(R.id.nav_icon);
        Led_icon = (ImageView) findViewById(R.id.led_icon);
        Buz_icon = (ImageView) findViewById(R.id.buz_icon);

        Navigate.setEnabled(false);
        Led.setEnabled(false);
        Buzzer.setEnabled(false);
        BuzzerText.setTextColor(Color.parseColor("#C3C3C3"));
        Nav_icon.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
        Led_icon.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
        Buz_icon.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);

        Forward.setEnabled(false);
        Backward.setEnabled(false);
        Left.setEnabled(false);
        Right.setEnabled(false);

        Forward.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
        Backward.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
        Left.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
        Right.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);

        Connect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Connect()) {
                        Connect.setEnabled(false);
                        Bt_icon.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
                        Toast.makeText(getApplicationContext(),
                                "Connected!", Toast.LENGTH_SHORT).show();

                        Navigate.setEnabled(true);
                        Led.setEnabled(true);
                        Buzzer.setEnabled(true);
                        BuzzerText.setTextColor(Color.parseColor("#000000"));
                        Nav_icon.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
                        Led_icon.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
                        Buz_icon.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);

                        Forward.setEnabled(true);
                        Backward.setEnabled(true);
                        Left.setEnabled(true);
                        Right.setEnabled(true);

                        Forward.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
                        Backward.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
                        Left.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
                        Right.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);

                    } else {
                        Connect.setChecked(false);
                        Toast.makeText(getApplicationContext(),
                                "Could not connect!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        Navigate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    dataToSend = "N";
                    writeData(dataToSend);
                    Toast.makeText(getApplicationContext(),
                            "Auto mode activated!", Toast.LENGTH_SHORT).show();

                    Forward.setEnabled(false);
                    Backward.setEnabled(false);
                    Left.setEnabled(false);
                    Right.setEnabled(false);

                    Forward.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
                    Backward.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
                    Left.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);
                    Right.setColorFilter(0xffC3C3C3, PorterDuff.Mode.SRC_IN);

                }
                else {
                    dataToSend = "S";
                    writeData(dataToSend);
                    Toast.makeText(getApplicationContext(),
                            "Auto mode deactivated", Toast.LENGTH_SHORT).show();

                    Forward.setEnabled(true);
                    Backward.setEnabled(true);
                    Left.setEnabled(true);
                    Right.setEnabled(true);

                    Forward.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
                    Backward.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
                    Left.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
                    Right.setColorFilter(0xff000000, PorterDuff.Mode.SRC_IN);
                }
            }
        });

        Led.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Led.isChecked()) {
                    dataToSend = "1";
                    writeData(dataToSend);
                } else if (!Led.isChecked()) {
                    dataToSend = "0";
                    writeData(dataToSend);
                }
            }
        });

        Buzzer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dataToSend = "Z";
                writeData(dataToSend);
            }
        });

        Forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dataToSend = "F";
                        writeData(dataToSend);
                        break;
                    case MotionEvent.ACTION_UP:
                        dataToSend = "S";
                        writeData(dataToSend);
                        break;
                }
                return false;
            }
        });

        Backward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dataToSend = "B";
                        writeData(dataToSend);
                        break;
                    case MotionEvent.ACTION_UP:
                        dataToSend = "S";
                        writeData(dataToSend);
                        break;
                }
                return false;
            }
        });
        Left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dataToSend = "L";
                        writeData(dataToSend);
                        break;
                    case MotionEvent.ACTION_UP:
                        dataToSend = "S";
                        writeData(dataToSend);
                        break;
                }
                return false;
            }
        });
        Right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dataToSend = "R";
                        writeData(dataToSend);
                        break;
                    case MotionEvent.ACTION_UP:
                        dataToSend = "S";
                        writeData(dataToSend);
                        break;
                }
                return false;
            }
        });

        CheckBt();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

    }

    private void CheckBt() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Bluetooth null!", Toast.LENGTH_SHORT).show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    public boolean Connect() {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mBluetoothAdapter.cancelDiscovery();
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            btSocket.connect();
        }
        catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                return false;
            }
            return false;
        }

        beginListenForData();
        return true;
    }

    private void writeData(String data) {
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Bug BEFORE Sending stuff",
                    Toast.LENGTH_SHORT).show();
        }

        String message = data;
        byte[] msgBuffer = message.getBytes();

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Bug while sending stuff",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            btSocket.close();
        } catch (IOException e) {
        }
    }

    public void beginListenForData()   {
        try {
            inStream = btSocket.getInputStream();
        }
        catch (IOException e) {
        }

        Thread workerThread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = inStream.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        public void run() {
                                            if(Result.getText().toString().equals("..")) {
                                                Result.setText(data);
                                            }
                                            else {
                                                Result.append("\n"+data);
                                            }
	                                        	/* You also can use Result.setText(data); it won't display multilines
	                                        	*/
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
