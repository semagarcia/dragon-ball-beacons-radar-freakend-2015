package everis.sema.es.dragonballradar;

import android.bluetooth.BluetoothDevice;
import java.io.UnsupportedEncodingException;

/**
 * POJO model for our simulated beacon.
 * This class could be implements Parcelable interface, but is not the purpose of that demo. This would
 * be useful in the case of we decided to pass the entire model instead of field by field through the
 * intent passed to the second activity
 *
 * José Manuel García García (Sema)
 * FreakEnd 2015, Madrid
 * Version: 1.0
 */
public class BLEBeacon /*implements Parcelable*/ {  // Parcelable feature: out of the Freakend

    private BluetoothDevice device;
    private AdvertisingStream advertisingStream;
    private int rssi;

    /**
     * Parameterized constructor: fills the POJO extracting the information of the stream read (beacon)
     * @param device Device (beacon) information
     * @param advertising Advertising stream read (containing all the ad data: UUID, major, minor, power)
     * @param rssi Received Signal Strength Indicator
     */
    BLEBeacon(BluetoothDevice device, byte[] advertising, int rssi) {
        this.device = device;
        this.advertisingStream = new AdvertisingStream(advertising);
        this.rssi = rssi;
    }

    public String getName() {
        return device.getName();
    }

    public String getAddress(){
        return device.getAddress();
    }

    public int getType() {
        return device.getType();
    }

    public int getRSSI() {
        return rssi;
    }

    public int getBluetoothClass() {
        return device.getBluetoothClass().getDeviceClass();
    }

    public int getBluetoothMajor() {
        return device.getBluetoothClass().getMajorDeviceClass();
    }

    public int getBondState() {
        return device.getBondState();
    }

    public int getDescribeContent() {
        return device.describeContents();
    }

    public int getMajor() {
        return advertisingStream.getMajor();
    }

    public int getMinor() {
        return advertisingStream.getMinor();
    }

    public int getPower() {
        return advertisingStream.getPower();
    }

    public String getAdvertiseUUID() {
        return advertisingStream.getUUID();
    }

    /**
     * Method that extract from the device its first UUID
     * @return The UUID if it could be extracted/readed or a literal to notify to the user
     */
    public String getFirstUUID() {
        if(device.getUuids() != null && device.getUuids()[0] != null) {
            return device.getUuids()[0].getUuid().toString();
        } else {
            return "No tiene";
        }
    }

    /**
     * Get the advertising stream in hexadecimal format
     * @return The advertising data read in pairs of 2 hex values: XX XX XX XX...
     */
    public String getHexAdvertisingData() {
        String data = "";
        for (byte b : advertisingStream.getAdvertisingStream())
            data += String.format("%02x ", b);
        return data;
    }

    /**
     * Get the advertising stream in decimal
     * @return The advertising data read in each decimal value: X X X X X...
     */
    public String getDecimalAdvertisingData() {
        try {
            String data = new String(advertisingStream.getAdvertisingStream(), "UTF-8");
            StringBuilder hexData = new StringBuilder(advertisingStream.getAdvertisingStream().length * 2);
            for (byte b : advertisingStream.getAdvertisingStream())
                hexData.append(b + " ");
            return hexData.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "Error en la conversión decimal";
    }

    /**
     * Formulae: d = A*(r/t) ^ B + C, where:
     *   - A, B and C are constants
     *   - r is the RSSI measured by the device
     *   - t is the reference RSSI at 1 meter
     *  (RSSI = 10n x log10(d) + A)
     * @return
     */
    public double getRange() {
        if (getRSSI() == 0 || getPower() == 0) {
            // if we cannot determine accuracy, return -1.
            return -1.0;
        }

        double result, ratio = (rssi * 1.0) / getPower();
        if (ratio < 1.0) {
            result = Math.pow(ratio, 10);
        } else {
            result = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
        }

        return result;
    }

}
