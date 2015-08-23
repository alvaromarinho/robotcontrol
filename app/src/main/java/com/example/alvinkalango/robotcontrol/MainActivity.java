package com.example.alvinkalango.robotcontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity implements OnClickListener {

    Button Forward;
    Button Backward;
    Button Left;
    Button Right;
    Button Connect;
    ToggleButton OnOff;
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

        Connect = (Button) findViewById(R.id.connect);
        OnOff = (ToggleButton) findViewById(R.id.tgOnOff);

        Forward = (Button) findViewById(R.id.buttonForward);
        Backward = (Button) findViewById(R.id.buttonBackward);
        Left = (Button) findViewById(R.id.buttonLeft);
        Right = (Button) findViewById(R.id.buttonRight);

        Connect.setOnClickListener((View.OnClickListener) this);
        OnOff.setOnClickListener((View.OnClickListener) this);

        CheckBt();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

    }

    @Override
    public void onClick(View control) {
        switch (control.getId()) {
            case R.id.connect:
                if (Connect()) {
                    Connect.setText("Conectado");
                    Connect.setEnabled(false);
                    Toast.makeText(getApplicationContext(),
                            "Conectado!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Não foi possivel conectar", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tgOnOff:
                if (OnOff.isChecked()) {
                    dataToSend = "1";
                    writeData(dataToSend);
                } else if (!OnOff.isChecked()) {
                    dataToSend = "0";
                    writeData(dataToSend);
                }
                break;
        }
    }

    private void CheckBt() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Bluetooth null !", Toast.LENGTH_SHORT).show();
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
        } catch (IOException e) {
        }

        Thread workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = inStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {

                                            if(Result.getText().toString().equals("..")) {
                                                Result.setText(data);
                                            } else {
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
