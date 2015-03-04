package everis.sema.es.dragonballradar;

/**
 * When looking for an iBeacon, we skip the first 2 – 5 bytes as these are a company identifier.
 * We then check for a pattern in the next few bytes.
 * <company identifier (2 bytes)> <type (1 byte)> <data length (1 byte)>
 *     <uuid (16 bytes)> <major (2 bytes)> <minor (2 bytes)> <RSSI @ 1m>
 *
 * José Manuel García García (Sema)
 * FreakEnd 2015, Madrid
 * Version: 1.0
 */
public class AdvertisingStream {

    private byte [] advertisingStream;
    private String uuid;
    private int major;
    private int minor;
    private int power;
    private double distance;
    private boolean isValidBeacon;
    private int bytePointerStartCounter;
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Parameterized constructor: extract all the particular data from a stream read (beacon)
     * @param advertisingPackage The information read from the beacon device (advertising data)
     */
    AdvertisingStream(byte [] advertisingPackage) {
        this.advertisingStream = advertisingPackage;
        validateIBeaconPattern(advertisingPackage);
        parseAdvertiseData(advertisingPackage);
    }

    public byte [] getAdvertisingStream() {
        return advertisingStream;
    }

    public String getUUID() {
        return uuid;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPower() {
        return power;
    }

    /**
     * Check the beacon and length bytes
     * @param advertisingPackage The stream read from a beacon
     */
    private void validateIBeaconPattern(byte [] advertisingPackage) {
        bytePointerStartCounter = 2;
        while(bytePointerStartCounter <= 5) {  // Read from 2nd to 5th byte
            if( ((int) advertisingPackage[bytePointerStartCounter + 2] & 0xff) == 0x02 && // Identifies an iBeacon
                    ((int) advertisingPackage[bytePointerStartCounter + 3] & 0xff) == 0x15 ) { // Identifies correct data length
                isValidBeacon = true;
                break;
            }
            bytePointerStartCounter++;
        }
    }

    /**
     * Decode the advertising stream in the individual values (uuid, major, minor, power) extracting
     * them from the byte returned by the callback in the "DragonBallRadar.java" activity
     * @param advertisingPackage
     */
    private void parseAdvertiseData(byte[] advertisingPackage) {
        byte[] uuidBytes = new byte[16];  // 16 bytes * 8 bits/byte = 128 bits

        // Parsing the UUID
        System.arraycopy(advertisingPackage, bytePointerStartCounter + 4, uuidBytes, 0, 16);
        String hexString = bytesToHex(uuidBytes);
        // UUID pattern format: AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE
        uuid = hexString.substring(0,8) + "-" +     // AAAAAAAA fragment (8 bytes)
               hexString.substring(8,12) + "-" +    // BBBB fragment (4 bytes)
               hexString.substring(12,16) + "-" +   // CCCC fragment (4 bytes)
               hexString.substring(16,20) + "-" +   // DDDD fragment (4 bytes)
               hexString.substring(20,32);          // EEEEEEEEEEEE fragment (12 bytes)

        // We take the first byte and multiply it by 100. Then add the second byte and therefore creating a 2 byte int
        major = (advertisingPackage[bytePointerStartCounter + 20] & 0xff) * 0x100 + (advertisingPackage[bytePointerStartCounter + 21] & 0xff); // [26-27]
        minor = (advertisingPackage[bytePointerStartCounter + 22] & 0xff) * 0x100 + (advertisingPackage[bytePointerStartCounter + 23] & 0xff); // [28-29]
        power = (advertisingPackage[bytePointerStartCounter + 24] & 0xff) * 0x100 ;    // [30-31]
    }

    /**
     * Helper function to convert a byte array into a hex string values
     * @param bytes The array of byte values
     * @return The hex string
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
