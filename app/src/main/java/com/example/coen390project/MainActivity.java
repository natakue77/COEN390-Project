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

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }
}