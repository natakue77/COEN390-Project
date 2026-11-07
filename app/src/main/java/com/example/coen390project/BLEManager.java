package com.example.coen390project;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.UUID;
import android.annotation.SuppressLint;
@SuppressLint("MissingPermission")
    public class BLEManager {

        private static final UUID SERVICE_UUID =
                UUID.fromString("0000abcd-0000-1000-8000-00805f9b34fb");

        private static final UUID DATA_UUID =
                UUID.fromString("00001234-0000-1000-8000-00805f9b34fb");

        private static final UUID CCCD_UUID =
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        private static final int SAMPLE_SIZE = 48;

        // Communication with MainActivity / SensorProcessor
        public interface Listener {
            void onStatus(String message);
            void onSample(SensorSample sample);
            void onError(String message);
        }

        public static class SensorSample {
            public long timestampUs;
            public long sequence;

            public float ax, ay, az;
            public float gx, gy, gz;
            public float qw, qx, qy, qz;
        }

        private final Context context;
        private final Listener listener;
        private final Handler mainHandler =
                new Handler(Looper.getMainLooper());

        private final BluetoothAdapter adapter;

        private BluetoothLeScanner scanner;
        private BluetoothGatt gatt;
        private boolean scanning = false;

        private long lastSequence = -1;

        public BLEManager(Context context, Listener listener) {
            this.context = context.getApplicationContext();
            this.listener = listener;

            BluetoothManager manager =
                    (BluetoothManager) context.getSystemService(
                            Context.BLUETOOTH_SERVICE);

            adapter = manager == null ? null : manager.getAdapter();
        }

        private void status(String message) {
            mainHandler.post(() -> listener.onStatus(message));
        }

        private void error(String message) {
            mainHandler.post(() -> listener.onError(message));
        }

        public boolean hasPermissions() {
            if (Build.VERSION.SDK_INT >= 31) {
                return context.checkSelfPermission(
                        Manifest.permission.BLUETOOTH_SCAN)
                        == PackageManager.PERMISSION_GRANTED
                        &&
                        context.checkSelfPermission(
                                Manifest.permission.BLUETOOTH_CONNECT)
                                == PackageManager.PERMISSION_GRANTED;
            }

            return context.checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }

        // =====================================
        // SCAN FOR ESP32
        // =====================================
        @SuppressLint("MissingPermission")
        public void scanAndConnect() {

            if (!hasPermissions()) {
                error("Bluetooth permissions are missing");
                return;
            }

            if (adapter == null || !adapter.isEnabled()) {
                error("Bluetooth is disabled or unavailable");
                return;
            }

            scanner = adapter.getBluetoothLeScanner();

            if (scanner == null) {
                error("BLE scanner unavailable");
                return;
            }

            status("Scanning for Barbell Tracker...");

            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                    .build();

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            scanning = true;

            scanner.startScan(
                    Collections.singletonList(filter),
                    settings,
                    scanCallback
            );

            mainHandler.postDelayed(() -> {
                if (scanning) {
                    stopScan();
                    error("Barbell Tracker not found");
                }
            }, 10000);
        }

        private final ScanCallback scanCallback = new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {

                if (!scanning || !hasPermissions()) return;

                stopScan();

                status("ESP32 found. Connecting...");

                BluetoothDevice device = result.getDevice();

                gatt = device.connectGatt(
                        context,
                        false,
                        gattCallback,
                        BluetoothDevice.TRANSPORT_LE
                );
            }

            @Override
            public void onScanFailed(int errorCode) {
                scanning = false;
                error("BLE scan failed: " + errorCode);
            }
        };

        private void stopScan() {
            if (scanning && scanner != null && hasPermissions()) {
                scanner.stopScan(scanCallback);
            }

            scanning = false;
        }

        // =====================================
        // CONNECT AND SUBSCRIBE
        // =====================================

        private final BluetoothGattCallback gattCallback =
                new BluetoothGattCallback() {

                    @Override
                    public void onConnectionStateChange(
                            BluetoothGatt g,
                            int statusCode,
                            int newState) {

                        if (statusCode != BluetoothGatt.GATT_SUCCESS) {
                            error("BLE connection error: " + statusCode);
                            g.close();
                            if (gatt == g) gatt = null;
                            return;
                        }

                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            lastSequence = -1;
                            status("Connected to ESP32");

                            // ESP32 sends batches of up to 240 bytes
                            if (!g.requestMtu(247)) {
                                g.discoverServices();
                            }

                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            status("Disconnected");
                            g.close();
                            if (gatt == g) gatt = null;
                        }
                    }

                    @Override
                    public void onMtuChanged(
                            BluetoothGatt g,
                            int mtu,
                            int statusCode) {

                        status("BLE MTU: " + mtu);
                        g.discoverServices();
                    }

                    @Override
                    public void onServicesDiscovered(
                            BluetoothGatt g,
                            int statusCode) {

                        if (statusCode != BluetoothGatt.GATT_SUCCESS) {
                            error("Service discovery failed");
                            return;
                        }

                        BluetoothGattService service =
                                g.getService(SERVICE_UUID);

                        if (service == null) {
                            error("Barbell service not found");
                            return;
                        }

                        BluetoothGattCharacteristic characteristic =
                                service.getCharacteristic(DATA_UUID);

                        if (characteristic == null) {
                            error("Sensor characteristic not found");
                            return;
                        }

                        if (!g.setCharacteristicNotification(
                                characteristic, true)) {
                            error("Could not enable local notifications");
                            return;
                        }

                        BluetoothGattDescriptor descriptor =
                                characteristic.getDescriptor(CCCD_UUID);

                        if (descriptor == null) {
                            error("Notification descriptor not found");
                            return;
                        }

                        byte[] enable =
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;

                        if (Build.VERSION.SDK_INT >= 33) {
                            int result = g.writeDescriptor(descriptor, enable);

                            if (result != 0) {
                                error("Could not subscribe: " + result);
                            }
                        } else {
                            descriptor.setValue(enable);

                            if (!g.writeDescriptor(descriptor)) {
                                error("Could not subscribe");
                            }
                        }
                    }

                    @Override
                    public void onDescriptorWrite(
                            BluetoothGatt g,
                            BluetoothGattDescriptor descriptor,
                            int statusCode) {

                        if (statusCode == BluetoothGatt.GATT_SUCCESS) {
                            status("Receiving IMU samples");
                        } else {
                            error("Notification subscription failed");
                        }
                    }

                    // Android 13+
                    @Override
                    public void onCharacteristicChanged(
                            BluetoothGatt g,
                            BluetoothGattCharacteristic characteristic,
                            byte[] value) {

                        if (DATA_UUID.equals(characteristic.getUuid())) {
                            decodePacket(value);
                        }
                    }

                    // Android 12 and earlier
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onCharacteristicChanged(
                            BluetoothGatt g,
                            BluetoothGattCharacteristic characteristic) {

                        if (Build.VERSION.SDK_INT < 33
                                && DATA_UUID.equals(characteristic.getUuid())) {
                            decodePacket(characteristic.getValue());
                        }
                    }
                };

        // =====================================
        // DECODE IMU SAMPLES
        // =====================================

        private void decodePacket(byte[] data) {

            if (data == null || data.length == 0 ||
                    data.length % SAMPLE_SIZE != 0) {

                error("Invalid BLE packet size");
                return;
            }

            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            while (buffer.remaining() >= SAMPLE_SIZE) {

                SensorSample sample = new SensorSample();

                sample.timestampUs =
                        buffer.getInt() & 0xFFFFFFFFL;

                sample.sequence =
                        buffer.getInt() & 0xFFFFFFFFL;

                sample.ax = buffer.getFloat();
                sample.ay = buffer.getFloat();
                sample.az = buffer.getFloat();

                sample.gx = buffer.getFloat();
                sample.gy = buffer.getFloat();
                sample.gz = buffer.getFloat();

                sample.qw = buffer.getFloat();
                sample.qx = buffer.getFloat();
                sample.qy = buffer.getFloat();
                sample.qz = buffer.getFloat();

                if (lastSequence >= 0) {
                    long expected =
                            (lastSequence + 1) & 0xFFFFFFFFL;

                    if (sample.sequence != expected) {
                        error("Missing samples near sequence "
                                + expected);
                    }
                }

                lastSequence = sample.sequence;

                mainHandler.post(() -> listener.onSample(sample));
            }
        }

        // =====================================
        // DISCONNECT
        // =====================================

        public void disconnect() {
            stopScan();

            if (gatt != null) {
                gatt.disconnect();
            }
        }
    }


