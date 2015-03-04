package everis.sema.es.dragonballradar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;

public class BeaconDetails extends Activity {

    private String address, name, uuid;
    private int major, minor, power, rssi;
    private double distance;
    private TextView tvAddress, tvName, tvUUID, tvMajor, tvMinor, tvRSSI, tvDistance;
    private static final String NOT_AVAILABLE = "No disponible";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_details);

        // Bind
        tvAddress = (TextView) findViewById(R.id.tv_address);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvUUID = (TextView) findViewById(R.id.tv_uuid);
        tvMajor = (TextView) findViewById(R.id.tv_major);
        tvMinor = (TextView) findViewById(R.id.tv_minor);
        tvRSSI = (TextView) findViewById(R.id.tv_rssi);
        tvDistance = (TextView) findViewById(R.id.tv_distance);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            address = extras.getString(Constants.BLE_BEACON_ADDRESS, NOT_AVAILABLE);
            name = extras.getString(Constants.BLE_BEACON_NAME, NOT_AVAILABLE);
            uuid = extras.getString(Constants.BLE_BEACON_UUID, NOT_AVAILABLE);
            major = extras.getInt(Constants.BLE_BEACON_MAJOR);
            minor = extras.getInt(Constants.BLE_BEACON_MINOR);
            rssi = extras.getInt(Constants.BLE_BEACON_RSSI);
            distance = extras.getDouble(Constants.BLE_BEACON_DISTANCE, -1);
        }

        tvAddress.setText(address);
        tvName.setText("Nombre: " + name);
        tvUUID.setText("UUID: " + uuid);
        tvMajor.setText("Major: " + major);
        tvMinor.setText("Minor: " + minor);
        tvRSSI.setText("RSSI: " + rssi + " dBm");
        tvDistance.setText("Distancia: " + obtainAndPrettifyDistance());
    }

    private String obtainAndPrettifyDistance() {
        if(distance <= -1) {
            return NOT_AVAILABLE;
        } else {
            //DecimalFormat df = new DecimalFormat("#0.##");
            //return df.format(distance) + "m";
            return String.valueOf(distance).substring(0, 5) + "m";
        }
    }

}
