package com.example.rvakash.coc_testing;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Third_Activity extends ActionBarActivity {

    Button homebutton, templatesbutton, settingsbutton;
    ImageButton bluetooth;


    // UI elements
    private TextView messages;
   //a private EditText input;

    // BTLE state
    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;

    // UUIDs for UAT service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    // UUID for the BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String prevStateExtra = BluetoothAdapter.EXTRA_PREVIOUS_STATE;
            String stateExtra = BluetoothAdapter.EXTRA_STATE;
            int state = intent.getIntExtra(stateExtra,-1);
            //int prevstate = intent.getIntExtra(prevStateExtra,-1);
            //Toast toast = new Toast(getApplicationContext());

               while ( !adapter.isEnabled() )
                        {
                           // Toast.makeText(Third_Activity.this, "Bluetooth turning on", toast.LENGTH_LONG).show();

                    }
            switch(state) {
                case (BluetoothAdapter.STATE_ON): {
                    //Toast.makeText(Third_Activity.this, "Bluetooth on", toast.LENGTH_LONG).show();
                    Intent settings = new Intent(Third_Activity.this, Third_Activity.class);
                    startActivity(settings);
                    break;
                }

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_);
        templatesbutton = (Button) findViewById(R.id.templatesbutton);
        homebutton = (Button) findViewById(R.id.homebutton);
        settingsbutton = (Button) findViewById(R.id.settingsbutton);
        bluetooth = (ImageButton) findViewById(R.id.imageButton);
        // Grab references to UI elements.
        messages = (TextView) findViewById(R.id.messages);
        //input = (EditText) findViewById(R.id.input);

        //Check if bluetooth is enabled. If enabled display bluetooth_col and "Bluetooth is already turned on!". Else ask user to "turn it on."
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.isEnabled()) {
            //To change the image
            bluetooth = (ImageButton) findViewById(R.id.imageButton);
            bluetooth.setImageResource(R.drawable.bluetooth_col);
            Toast toast = new Toast(getApplicationContext());
            Toast.makeText(Third_Activity.this, "Bluetooth is turned on! You are now connected to the display", toast.LENGTH_LONG).show();
            OnResume();
            OnStop();
        }
        else {
            Toast toast = new Toast(getApplicationContext());
            Toast.makeText(Third_Activity.this, "Bluetooth is not on. Click on Bluetooth to turn it on", toast.LENGTH_LONG).show();

        }
        ////When home button is clicked open a new activity home
        homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent home = new Intent(Third_Activity.this, MainActivity.class);
                startActivity(home);
            }
        });
        ////When templates button is clicked open a new activity templates
        templatesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent templates = new Intent(Third_Activity.this, Second_Activity.class);
                startActivity(templates);
            }
        });
        //When bluetooth ImageButton is clicked, IF bluetooth is already enabled then turn it off ELSE turn on bluetooth and change image to bluetooth_col.
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.isEnabled()) {
                    adapter.disable();
                    Toast toast = new Toast(getApplicationContext());
                    Toast.makeText(Third_Activity.this, "Bluetooth is turned off! You are now disconnected", toast.LENGTH_LONG).show();
                    bluetooth.setImageResource(R.drawable.bluetooth_grey);
                }
                else {
                    //Intent settings = new Intent(Third_Activity.this, Third_Activity.class);
                    //startActivity(settings);
                    String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
                    String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;
                    IntentFilter filter = new IntentFilter(actionStateChanged);
                    registerReceiver(bluetoothState, filter);
                    startActivityForResult(new Intent(actionRequestEnable), 0);
                    //String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
                    //IntentFilter filter = new IntentFilter(actionStateChanged);
                    //bluetooth.setImageResource(R.drawable.bluetooth_col);
                    //Toast toast = new Toast(getApplicationContext());
                    //Toast.makeText(Third_Activity.this, "Bluetooth is turned on!", toast.LENGTH_LONG).show();
                    //OnResume();
                    //OnStop();
                }
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////TDICOLA/////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////

        // Main BTLE device callback where much of the logic occurs.
        private BluetoothGattCallback callback = new BluetoothGattCallback() {
            // Called whenever the device connection state changes, i.e. from disconnected to connected.
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    writeLine("Connected!");
                    // Discover services.
                    if (!gatt.discoverServices()) {
                        writeLine("Failed to start discovering services!");
                    }
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    writeLine("Disconnected!");
                } else {
                    writeLine("Connection state changed.  New state: " + newState);
                }
            }

            // Called when services have been discovered on the remote device.
            // It seems to be necessary to wait for this discovery to occur before
            // manipulating any services or characteristics.
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    writeLine("Service discovery completed!");
                } else {
                    writeLine("Service discovery failed with status: " + status);
                }
                // Save reference to each characteristic.
                tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
                rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
                // Setup notifications on RX characteristic changes (i.e. data received).
                // First call setCharacteristicNotification to enable notification.
                if (!gatt.setCharacteristicNotification(rx, true)) {
                    writeLine("Couldn't set notifications for RX characteristic!");
                }
                // Next update the RX characteristic's client descriptor to enable notifications.
                if (rx.getDescriptor(CLIENT_UUID) != null) {
                    BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
                    desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    if (!gatt.writeDescriptor(desc)) {
                        writeLine("Couldn't write RX client descriptor value!");
                    }
                } else {
                    writeLine("Couldn't get RX client descriptor!");
                }
            }

            // Called when a remote characteristic changes (like the RX characteristic).
       /* @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            writeLine("Received: " + characteristic.getStringValue(0));
        }*/
        };
        // BTLE device scanning callback.
        private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
            // Called when a device is found.
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                writeLine("Found device: " + bluetoothDevice.getAddress());
                // Check if the device has the UART service.
                if (parseUUIDs(bytes).contains(UART_UUID)) {
                    // Found a device, stop the scan.
                    adapter.stopLeScan(scanCallback);
                    writeLine("Found UART service!");
                    // Connect to the device.
                    // Control flow will now go to the callback functions when BTLE events occur.
                    gatt = bluetoothDevice.connectGatt(getApplicationContext(), false, callback);
                }
            }
        };

///////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////TDICOLA/////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
   // adapter = BluetoothAdapter.getDefaultAdapter();
    // OnResume, called right before UI is displayed.  Start the BTLE connection.
    //@Override
     private void OnResume() {
        super.onResume();
        // Scan for all BTLE devices.
        // The first one with the UART service will be chosen--see the code in the scanCallback.
        //writeLine("Scanning for devices...");
        adapter.startLeScan(scanCallback);
    }

    // OnStop, called right before the activity loses foreground focus.  Close the BTLE connection.
   // @Override
    private void OnStop() {
        super.onStop();
        if (gatt != null) {
            // For better reliability be careful to disconnect and close the connection.
            gatt.disconnect();
            gatt.close();
            gatt = null;
            tx = null;
            rx = null;
        }

    }

/*
    // Handler for mouse click on the send button.
    public void sendClick(View view) {
        String message = input.getText().toString();
        if (tx == null || message == null || message.isEmpty()) {
            // Do nothing if there is no device or message to send.
            return;
        }
        // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
        tx.setValue(message.getBytes(Charset.forName("UTF-8")));
        if (gatt.writeCharacteristic(tx)) {
            writeLine("Sent: " + message);
        }
        else {
            writeLine("Couldn't write TX characteristic!");
        }
    }
*/
    // Write some text to the messages text view.
    // Care is taken to do this on the main UI thread so writeLine can be called
    // from any thread (like the BTLE callback).
    private void writeLine(final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.append(text);
                messages.append("\n");
            }
        });
    }

    // Filtering by custom UUID is broken in Android 4.3 and 4.4, see:
    //   http://stackoverflow.com/questions/18019161/startlescan-with-128-bit-uuids-doesnt-work-on-native-android-ble-implementation?noredirect=1#comment27879874_18019161
    // This is a workaround function from the SO thread to manually parse advertisement data.
    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData, offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            //Log.e(LOG_TAG, e.toString());
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
    }

    // Boilerplate code from the activity creation:

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
