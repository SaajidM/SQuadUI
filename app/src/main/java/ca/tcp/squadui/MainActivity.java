package ca.tcp.squadui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    Handler handler;
    static final int WRITEERROR = 10;
    static final int WRITESTATUS = 20;
    static final int WRITECORDS = 30;
    static final int WRITEANGLES = 40;
    static final int PHASECHANGES = 50;

    //Popular uuid for bluetooth taken from http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice quadBluetooth = null;
    BluetoothSocket bluetoothSocket;

    SQuadUIBlueInThread inT;
    boolean inRunning = false;
    SQuadUIBlueOutThread outT;
    boolean outRunning = false;

    ThumbStickView pitchRollView;
    RisingLevelView throttleView;
    QuadView quadView;
    AngleView angleView;
    int throttleMin = 750;
    int throttleMax = 2000;
    int throttleRange = throttleMax-throttleMin;

    short currentThrottle = 700, currentPitch=0, currentRoll=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        pitchRollView = (ThumbStickView) this.findViewById(R.id.pitchRollView);
        pitchRollView.setOnTouchListener(new View.OnTouchListener() {
                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            if (pitchRollView.onTouch(event)) {
                                                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                                                    currentPitch = (short)(((pitchRollView.getCanvasCenterY() - event.getY()) / (pitchRollView.getCanvasCenterY())) * -20);
                                                    currentRoll = (short)(((event.getX() - pitchRollView.getCanvasCenterX()) / (pitchRollView.getCanvasCenterX())) * -20);
                                                    ((TextView) findViewById(R.id.pitchText)).setText(String.valueOf(currentPitch));
                                                    ((TextView) findViewById(R.id.rollText)).setText(String.valueOf(-currentRoll));
                                                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                                                    ((TextView) findViewById(R.id.pitchText)).setText("0");
                                                    ((TextView) findViewById(R.id.rollText)).setText("0");
                                                    currentPitch = 0;
                                                    currentRoll = 0;
                                                }
                                                return true;
                                            }
                                            return false;
                                        }
        });

        throttleView = (RisingLevelView) this.findViewById(R.id.throttleView);
        throttleView.setOnTouchListener(new View.OnTouchListener() {
                                            @Override
                                            public boolean onTouch(View v, MotionEvent event) {
                                                if (throttleView.onTouch(event)) {
                                                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                                                        currentThrottle = (short)(throttleMax - (event.getY()/throttleView.getCanvasHeight()) * throttleRange);
                                                        if (currentThrottle > throttleMax) { currentThrottle = (short)throttleMax;}
                                                        ((TextView)findViewById(R.id.throttleText)).setText(String.valueOf(currentThrottle));
                                                    }
                                                    return true;
                                                }
                                                return false;
                                            }
                                        }
        );

        quadView = (QuadView) findViewById(R.id.quadView);
        angleView = (AngleView) findViewById(R.id.angleView);

        findViewById(R.id.phase1Button).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.phase1Button).setEnabled(false);
                writeOut('S');
            }
        });
        findViewById(R.id.phase2Button).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.phase2Button).setEnabled(false);
                writeOut('T');
            }
        });
        findViewById(R.id.phase3Button).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.phase3Button).setEnabled(false);
                writeOut('R');
            }
        });

        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WRITEERROR: {
                        String toWrite = msg.getData().getString("Error");
                        ((TextView) findViewById(R.id.systemMessagesTxt)).setText(toWrite);
                        break;
                    }
                    case WRITESTATUS: {
                        String toWrite = msg.getData().getString("Status");
                        ((TextView)findViewById(R.id.statusMessageTxt)).setText(toWrite);
                        if (toWrite.contains("PID")) {
                            ((TextView)findViewById(R.id.phase1Text)).setText("Check");
                            findViewById(R.id.phase2Button).setEnabled(true);
                        } else if (toWrite.contains("Calibration")) {
                            ((TextView)findViewById(R.id.phase2Text)).setText("Check");
                            findViewById(R.id.phase3Button).setEnabled(true);
                        } else if (toWrite.contains("Motors")) {
                            ((TextView)findViewById(R.id.phase3Text)).setText("Check");
                        }
                        break;
                    }
                    case WRITECORDS: {
                        int[] cords = msg.getData().getIntArray("Cords");
                        quadView.setFourCordsValues(cords[0], cords[1], cords[2], cords[3]);
                        break;
                    }
                    case WRITEANGLES: {
                        int[] angles = msg.getData().getIntArray("Angles");
                        angleView.setPitchRollValues(angles[0], angles[1]);
                        break;
                    }
                }
            }
        };
    }

    public void writeOut(String toWrite) { if (outRunning) {outT.writeOut(toWrite.getBytes());} }

    public void writeOut(short toWrite) {
        if (outRunning) {outT.writeOut((byte)(toWrite >> 8));outT.writeOut((byte)(toWrite & 0xFF));}
    }

    public void writeOut(char toWrite) {
        if (outRunning) {outT.writeOut((byte)toWrite);}
    }

    public void emgStop(View v) {
        writeOut('E');
    }

    public void yawTrimL(View v) {
        writeOut('I');
    }

    public void yawTrimR(View v) {
        writeOut('M');
    }

    protected void onResume() {
        super.onResume();

        if (!bluetoothAdapter.isEnabled()) {bluetoothAdapter.enable();}

        Set<BluetoothDevice> bdSet = bluetoothAdapter.getBondedDevices(); //get paired device list
        if (!bdSet.isEmpty()) {
            for (BluetoothDevice bd : bdSet) { //search list for quad copter bluetooth
                if (bd.getName().equals("HC-05")) { //if found set as device
                    quadBluetooth = bd;
                    break;
                }
            }
            if (quadBluetooth != null) {
                try {
                    bluetoothSocket = quadBluetooth.createRfcommSocketToServiceRecord(uuid); //Create connection
                    //Thread used to push connect lag to the background inorder to keep UI functioning
                    new Thread() {
                        public void run() {
                            try {
                                bluetoothSocket.connect();
                                if (bluetoothSocket.isConnected()) { // Copter can connect
                                    inT = new SQuadUIBlueInThread(bluetoothSocket); //Create threads to handle receiving
                                    outT = new SQuadUIBlueOutThread(bluetoothSocket); // and sending to copter
                                    outT.start();
                                    inT.start();
                                    Message msg = new Message();
                                    msg.what = WRITEERROR;
                                    Bundle bdl = new Bundle();
                                    bdl.putString("Error", "Quad Found");
                                    msg.setData(bdl);
                                    handler.sendMessage(msg);
                                    findViewById(R.id.phase1Button).setEnabled(true);
                                    ((TextView)findViewById(R.id.phase1Text)).setText("");
                                    findViewById(R.id.phase2Button).setEnabled(false);
                                    ((TextView)findViewById(R.id.phase2Text)).setText("");
                                    findViewById(R.id.phase3Button).setEnabled(false);
                                    ((TextView)findViewById(R.id.phase3Text)).setText("");
                                } else {
                                    Message msg = new Message();
                                    msg.what = WRITEERROR;
                                    Bundle bdl = new Bundle();
                                    bdl.putString("Error", "Quad Not Found");
                                    msg.setData(bdl);
                                    handler.sendMessage(msg);
                                }
                            } catch (Exception e) {}
                        }
                    }.start();
                } catch (Exception e) {}
            } else {
                Message msg = new Message();
                msg.what = WRITEERROR;
                Bundle bdl = new Bundle();
                bdl.putString("Error", "Quad Not Found");
                msg.setData(bdl);
                handler.sendMessage(msg);
            }
        } else {
            Message msg = new Message();
            msg.what = WRITEERROR;
            Bundle bdl = new Bundle();
            bdl.putString("Error", "No Bluetooth Devices Found");
            msg.setData(bdl);
            handler.sendMessage(msg);
        }
    }

    public void onPause() {
        writeOut('E');
        inRunning = false;
        outRunning = false;
        quadBluetooth = null;
        ((TextView)findViewById(R.id.statusMessageTxt)).setText("");
        ((TextView)findViewById(R.id.systemMessagesTxt)).setText("Looking For Quad ...");
        ((TextView)findViewById(R.id.phase1Text)).setText("");
        ((TextView)findViewById(R.id.phase2Text)).setText("");
        ((TextView)findViewById(R.id.phase3Text)).setText("");
        try {
            bluetoothSocket.close();
        } catch (Exception e) {}
        super.onPause();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class SQuadUIBlueInThread extends Thread{
        BufferedReader in;
        SQuadUIBlueInThread(BluetoothSocket bs) {
            try {
                in = new BufferedReader(new InputStreamReader(bs.getInputStream()));
                inRunning = true;
            } catch (Exception e) {}
        }
        public void run () {
            while (inRunning) {
                try {
                    String fromQuad = in.readLine();
                    if (fromQuad.contains("NW")) {
                        Message msg = new Message();
                        msg.what = WRITECORDS;
                        Bundle bdl = new Bundle();
                        int tempS = fromQuad.indexOf(':') + 1;
                        int tempE = fromQuad.indexOf(':', tempS) + 1;
                        int cords[] = new int[4];
                        cords[0] = (int)Double.parseDouble(fromQuad.substring(tempS, tempE - 3));
                        tempS = fromQuad.indexOf(':', tempE) + 1;
                        cords[1] = (int)Double.parseDouble(fromQuad.substring(tempE, tempS - 3));
                        tempE = fromQuad.indexOf(':', tempS) + 1;
                        cords[2] = (int)Double.parseDouble(fromQuad.substring(tempS, tempE - 3));
                        cords[3] = (int)Double.parseDouble(fromQuad.substring(tempE));
                        bdl.putIntArray("Cords", cords);
                        msg.setData(bdl);
                        handler.sendMessage(msg);
                    } else if (fromQuad.contains("CmbP")) {
                        int tempS = 5;
                        int tempE = fromQuad.indexOf(':',tempS) + 1;
                        int angles[] = new int[2];
                        angles[0] = (int)Double.parseDouble(fromQuad.substring(tempS, tempE-5));
                        angles[1] = (int)Double.parseDouble(fromQuad.substring(tempE));
                        Message msg = new Message();
                        msg.what = WRITEANGLES;
                        Bundle bdl = new Bundle();
                        bdl.putIntArray("Angles", angles);
                        msg.setData(bdl);
                        handler.sendMessage(msg);
                    } else if (fromQuad.contains("Req")) {
                        writeOut(currentPitch);
                        writeOut(currentRoll);
                        writeOut(currentThrottle);
                    } else {
                        Message msg = new Message();
                        msg.what = WRITESTATUS;
                        Bundle bdl = new Bundle();
                        bdl.putString("Status", fromQuad);
                        msg.setData(bdl);
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {}
            }
        }
    }

    class SQuadUIBlueOutThread extends Thread{
        OutputStream out;
        SQuadUIBlueOutThread(BluetoothSocket bs) {
            try {
                out = bs.getOutputStream();
                outRunning = true;
            } catch (Exception e) {}
        }

        public void run() {
            while (outRunning) {}
        }

        public void writeOut(byte[] toWrite) {
            try {
                out.write(toWrite);
                out.flush();
            } catch (Exception e) {}
        }

        public void writeOut(byte toWrite) {
            try {
                out.write(toWrite);
                out.flush();
            } catch (Exception e) {}
        }
    }

}