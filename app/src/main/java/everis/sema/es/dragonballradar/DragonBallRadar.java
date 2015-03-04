package everis.sema.es.dragonballradar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point. This class represents to the main screen where the radar is shown
 *
 * José Manuel García García (Sema)
 * FreakEnd 2015, Madrid
 * Version: 1.0
 */
public class DragonBallRadar extends Activity {

    private TextView tvDevicesFound;
    private int counterDevicesFound;
    private boolean isDeviceScanning;
    private Handler handlerToScanDevice;
    private List<BLEBeacon> devicesFound;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dragon_ball_radar);

        // Bind variables (objects) with UI elements
        tvDevicesFound = (TextView) findViewById(R.id.tv_devices_found);
        registerForContextMenu(tvDevicesFound);

        // Other inits
        counterDevicesFound = 0;
        handlerToScanDevice = new Handler();
        devicesFound = new ArrayList<BLEBeacon>();

        checkBTConditions();
    }

    /**
     * This method checks the initial conditions for the app in order to it could be executed successfully
     */
    private void checkBTConditions() {
        // BLE supported on the device?
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dragon_ball_radar, menu);
        if (!isDeviceScanning) {
            menu.findItem(R.id.menu_scan_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_scan_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.scanning_entry_menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_scan:
                counterDevicesFound = 0;
                tvDevicesFound.setText(R.string.beacons_founds);
                scanLeDevice(true);
                break;
            case R.id.menu_more:
                break;
            case R.id.menu_scan_stop:
                scanLeDevice(false);
                break;
            case R.id.menu_discovery_mode:
                Toast.makeText(getApplicationContext(), "Not implemented yet", Toast.LENGTH_LONG).show();
                break;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(contextMenu, v, menuInfo);

        // Remove the previous entries and re-build the menu to add entries dynamically according to the
        // number of the beacons detected/found
        contextMenu.removeGroup(0);
        contextMenu.setHeaderTitle((counterDevicesFound == 0) ? "No se encontró nada :-(" : ("¡Bolas de Dragón encontradas!"));
        for(int i=0; i<counterDevicesFound; i++) {
            contextMenu.add(0, i, i+1, devicesFound.get(i).getAddress());
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        startActivity(fillIntent(item.getItemId()));
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device or throw an intent to enable it
        if (!bluetoothAdapter.isEnabled()) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            }
        }

        tvDevicesFound.setText(R.string.beacons_founds);
        counterDevicesFound = 0;
        devicesFound.clear();
        scanLeDevice(true);  // Scanning
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // Only for the FreakEnd2015: I've decided to not to exit in case of the user press
            // the "cancel" button. In that way, I can show the app
            // finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        tvDevicesFound.setText(R.string.beacons_founds);
        counterDevicesFound = 0;
        devicesFound.clear();
    }

    /**
     * Method to decide, according to the flag parameter, if the scanning process should be started
     * or not periodically
     * @param enable flag to decide if the scanning should be performed or not. True to start, false to stop it
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) { // Start it
            handlerToScanDevice.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isDeviceScanning = false;
                    bluetoothAdapter.stopLeScan(scanCallback);
                    invalidateOptionsMenu();
                }
            }, Constants.SCAN_PERIOD);
            isDeviceScanning = true;
            bluetoothAdapter.startLeScan(scanCallback);
        } else {  // Stop it
            isDeviceScanning = false;
            bluetoothAdapter.stopLeScan(scanCallback);
        }
        invalidateOptionsMenu();
    }

    /**
     * CallBack: function to be executed after a LE device is found during a scan previously
     * initiated by a startLeScan(callBack) call
     */
    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            // Create our own Beacon object and add it to a specific list
            devicesFound.add(new BLEBeacon(device, scanRecord, rssi));
            counterDevicesFound++;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Drawn the beacons found (result) in the main UI Thread
                    tvDevicesFound.setText("Beacons detectados: " + counterDevicesFound);
                }
            });
        }
    };

    /**
     * Method to insert into an intent the info passed to the second activity (which will draw that
     * fields related to a beacon selected)
     * @param index The n-th beacon found
     * @return An intent containing all the information related to the beacon found
     */
    private Intent fillIntent(int index) {
        Intent beaconDetails = new Intent(getBaseContext(), BeaconDetails.class);
        beaconDetails.putExtra(Constants.BLE_BEACON_ADDRESS, devicesFound.get(index).getAddress());
        beaconDetails.putExtra(Constants.BLE_BEACON_NAME, devicesFound.get(index).getName());
        beaconDetails.putExtra(Constants.BLE_BEACON_MAJOR, devicesFound.get(index).getMajor());
        beaconDetails.putExtra(Constants.BLE_BEACON_MINOR, devicesFound.get(index).getMinor());
        beaconDetails.putExtra(Constants.BLE_BEACON_POWER, devicesFound.get(index).getPower());
        beaconDetails.putExtra(Constants.BLE_BEACON_RSSI, devicesFound.get(index).getRSSI());
        beaconDetails.putExtra(Constants.BLE_BEACON_UUID, devicesFound.get(index).getAdvertiseUUID());
        beaconDetails.putExtra(Constants.BLE_BEACON_DISTANCE, devicesFound.get(index).getRange());
        return beaconDetails;
    }

}
