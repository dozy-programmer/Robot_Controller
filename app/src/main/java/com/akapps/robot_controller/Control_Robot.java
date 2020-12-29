package com.akapps.robot_controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.UUID;

public class Control_Robot extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ProgressDialog progress;
    private boolean isBtConnected = false;
    private BluetoothSocket btSocket = null;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Context context;
    private Devices found_Device;
    private String found_Device_Address;
    private ImageView forward_Btn, down_Btn, left_Btn, right_Btn;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control__robot);
        context = this;

        // Changes color of title to black
        setTitle(Html.fromHtml("<font color='#000000'> Robot Controller </font>"));

        initialize_Layout(); // initializes layout

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); // Any number

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);

        forward_Btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    forward();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stop();
                }
                return true;
            }
        });

        down_Btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    back();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stop();
                }
                return true;
            }
        });

        left_Btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    left();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stop();
                }
                return true;
            }
        });

        right_Btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    right();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stop();
                }
                return true;
            }
        });

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
        if (id == R.id.action_Connect_Device) {

            if(isBtConnected == false) {
                if (bluetoothAdapter!=null && !bluetoothAdapter.isEnabled()) {
                    // enables bluetooth if not enabled
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }
                else {
                    if(bluetoothAdapter==null)
                        message_User_Via_Snackbar("No bluetooth");
                    else if (bluetoothAdapter.isEnabled()) {
                        // if bluetooth enabled, then it starts looking for bluetooth devices
                        if (!bluetoothAdapter.isDiscovering()) {
                            message_User_Via_Snackbar("Attempting to Connect");
                            bluetoothAdapter.startDiscovery();
                            registerReceiver(discover, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                        }
                        else {
                            // if button is pressed again, then it cancels the discovery
                            bluetoothAdapter.cancelDiscovery();
                            message_User_Via_Snackbar("Discovery canceled");
                        }
                    }
                }
            }
            else{
                message_User_Via_Snackbar("Already connected");
            }

            return true;
        }

        // disconnects device from robot
        else if (id == R.id.action_Disconnect_Device) {
            if (btSocket!=null)
            {
                try
                {
                    if(isBtConnected == false)
                        message_User_Via_Snackbar("Already disconnected");
                    else {
                        btSocket.close(); //close connection
                        isBtConnected = false;
                    }
                }
                catch (IOException e)
                { message_User_Via_Snackbar("Error in disconnecting device");}
            }
            else{
                if (bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                    message_User_Via_Snackbar("Discovery canceled");
                }
                else
                    message_User_Via_Snackbar("No connected device");
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initialize_Layout(){
        forward_Btn = (ImageView) findViewById(R.id.forward_Btn);
        down_Btn = (ImageView) findViewById(R.id.back_Btn);
        left_Btn = (ImageView) findViewById(R.id.left_Btn);
        right_Btn = (ImageView) findViewById(R.id.right_Btn);
    }

        /**
         * Broadcast Receiver for listing devices that are not yet paired
         * -Executed by btnDiscover() method.
         */
        private BroadcastReceiver discover = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Toast.makeText(context, "Started", Toast.LENGTH_SHORT).show();
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                }
                else if (action.equals(BluetoothDevice.ACTION_FOUND)){
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Devices current_Found_Device = new Devices(device.getName(), device.getAddress());
                    // looks for one bluetooth device in particular, which is the robot
                    // bluetooth module named HC-06, but it looks only for device name
                    // containing "HC" and connects to that device
                    if(current_Found_Device!=null && current_Found_Device.getName()!=null){
                        if(current_Found_Device.getName().contains("HC")) {
                            unregisterReceiver(discover);
                            found_Device = current_Found_Device;
                            found_Device_Address = found_Device.getAddress();
                            new ConnectBT().execute();
                        }
                    }
                }
            }
        };

    // BroadcastReceiver that listens for bluetooth broadcasts that are disconnected
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                message_User_Via_Snackbar("Disconnected");
                isBtConnected = false;
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                message_User_Via_Snackbar("Disconnected");
                isBtConnected= false;
            }
        }
    };

        @Override
        protected void onDestroy() {
            super.onDestroy();

            try {
                unregisterReceiver(discover);
            } catch(IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK){
                message_User_Via_Snackbar("Bluetooth has been enabled since it was off");
            }
            if(resultCode == RESULT_CANCELED){
                message_User_Via_Snackbar("Blutooth not enabled");
            }
        }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  //  UI thread
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(context, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    // connects to the device's address and checks if it's available
                    BluetoothDevice this_Device = bluetoothAdapter.getRemoteDevice(found_Device_Address);
                    // creates a RFCOMM (SPP) connection
                    btSocket = this_Device.createInsecureRfcommSocketToServiceRecord(myUUID);
                    // cancels discovery since it is trying to connect to a device
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect(); //starts connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                message_User_Via_Snackbar("Connection Failed. Try again.");
                isBtConnected = false;
                finish();
            }
            else
            {
                message_User_Via_Snackbar("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    // notifies user via message
    public void message_User_Via_Snackbar(String message){
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT
                    ).setAction("Action", null);
            View snackbarView = snackbar.getView();
            TextView snack_Text = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            snack_Text.setTextColor(getResources().getColor(R.color.actually_dark_black));
            snack_Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            snack_Text.setTypeface(null, Typeface.BOLD);
            snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
            params.gravity = Gravity.TOP;
            snackbarView.setLayoutParams(params);
            snackbar.show();
    }

    private void forward()
    {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write(1);
            }
            catch (IOException e) {
                //Log.d("Main_Activity", "Error in moving forward");
            }
        }
    }
    private void back()
    {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write(2);
            }
            catch (IOException e) {
                //Log.d("Main_Activity","Error in moving back");
            }
        }
    }
    private void left()
    {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write(4);
            }
            catch (IOException e) {
                //Log.d("Main_Activity","Error in moving left");
            }
        }
    }
    private void right()
    {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write(3);
            }
            catch (IOException e) {
                //Log.d("Main_Activity","Error in moving right");
            }
        }
    }
    private void stop()
    {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write(0);
            }
            catch (IOException e) {
                //Log.d("Main_Activity","Error in stopping");
            }
        }
    }
}
