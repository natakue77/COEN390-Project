package com.example.coen390project;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import android.Manifest;
import android.os.Build;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements BLEManager.Listener {

    private DrawerLayout drawerLayout;
    private BLEManager bleManager;

    private SensorProcess sensorProcess = new SensorProcess();




    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        if (bleManager.hasPermissions()) {
                            bleManager.scanAndConnect();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Bluetooth permissions denied",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bleManager = new BLEManager(this, this);


        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);

        NavigationView navigationView =
                findViewById(R.id.navigationView);

        // Creates the hamburger button and connects it to the drawer.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle the Back button when the navigation drawer is open.
        OnBackPressedCallback backCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        };

        getOnBackPressedDispatcher().addCallback(this, backCallback);

        // Enable the callback only while the drawer is open.
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {

            @Override
            public void onDrawerOpened(@NonNull android.view.View drawerView) {
                backCallback.setEnabled(true);
            }

            @Override
            public void onDrawerClosed(@NonNull android.view.View drawerView) {
                backCallback.setEnabled(false);
            }
        });

        // Handles clicks on the side menu items.
        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            if (id == R.id.nav_workouts) {
                startActivity(new Intent(this, Option1Activity.class));

            } else if (id == R.id.nav_progress) {
                startActivity(new Intent(this, Option2Activity.class));

            } else if (id == R.id.nav_analytics) {
                startActivity(new Intent(this, Option3Activity.class));

            } else if (id == R.id.nav_tips) {
                startActivity(new Intent(this, Option4Activity.class));

            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, Option5Activity.class));

            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        connectToTracker();


    }

    @Override
    public void onSample(BLEManager.SensorSample sample) {
        //android.util.Log.d("BarbellBLE", "Sample received: " + sample.sequence);

        sensorProcess.processSample(sample);

        // Print every 100th sample
        /*if (sample.sequence % 100 == 0) {
            android.util.Log.d("BarbellBLE",
                    "Seq: " + sample.sequence +
                            " | AX: " + sample.ax +
                            " | AY: " + sample.ay +
                            " | AZ: " + sample.az);
        }*/

        if (sample.sequence % 100 == 0) {

            double qw = sample.qw;
            double qx = sample.qx;
            double qy = sample.qy;
            double qz = sample.qz;

            double roll = Math.toDegrees(Math.atan2(
                    2 * (qw*qx + qy*qz),
                    1 - 2 * (qx*qx + qy*qy)
            ));

            double pitch = Math.toDegrees(Math.asin(
                    Math.max(-1, Math.min(1, 2 * (qw*qy - qz*qx)))
            ));

            //android.util.Log.d("BarbellBLE",
                    //"Roll: " + roll + " | Pitch: " + pitch);
        }
    }

    @Override
    public void onStatus(String message) {
        android.util.Log.d("BarbellBLE", message);
    }

    @Override
    public void onError(String message) {
        android.util.Log.e("BarbellBLE", message);
    }


    private void connectToTracker() {

        if (bleManager.hasPermissions()) {
            bleManager.scanAndConnect();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 and newer
            permissionLauncher.launch(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            });
        } else {
            // Android 11 and older
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            });
        }
    }
}