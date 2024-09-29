package com.example.agencetun.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.agencetun.R;
import com.example.agencetun.utils.GoogleMapController;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity {

    private Timer _timer = new Timer();
    private FloatingActionButton _fab;
    private GoogleMap mMap;


    private double latitud = 0;
    private double longitud = 0;



    private MapView mapview1;
    private GoogleMapController _mapview1_controller;

    private TextView title_address;
    private TextView latlng;
    private TextView textviewspeed;

    private LocationManager locate;
    private LocationListener _locate_location_listener;
    private TimerTask t;
    private Location previousLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        initialize(savedInstanceState);
        getSupportActionBar().hide();

        requestLocationPermissions();
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            initializeLogic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeLogic();
        } else {

            showPermissionDeniedMessage();
        }
    }

    private void initialize(Bundle savedInstanceState) {
        _fab = findViewById(R.id._fab);

        mapview1 = findViewById(R.id.mapview1);
        mapview1.onCreate(savedInstanceState);
        title_address = findViewById(R.id.title_address);
        latlng = findViewById(R.id.latlng);
        textviewspeed = findViewById(R.id.textviewspeed);
        locate = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getWindow().setStatusBarColor(Color.parseColor("#008000"));


        initializeMap();
        setupFAB();
        setupLocationListener();
    }

    private void initializeMap() {
        _mapview1_controller = new GoogleMapController(mapview1, new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                _mapview1_controller.setGoogleMap(googleMap);
                mMap = googleMap;
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    _mapview1_controller.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    _mapview1_controller.addMarker("pin", latitud, longitud);
                    _mapview1_controller.setMarkerIcon("pin", R.drawable.gps2);
                }
            }
        });

        _mapview1_controller.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                title_address.setText(marker.getTag().toString());
                return false;
            }
        });
    }

    private void setupFAB() {
        _fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locate.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, _locate_location_listener);
                }
            }
        });
    }

    private void setupLocationListener() {
        _locate_location_listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitud = location.getLatitude();
                longitud = location.getLongitude();

               // _mapview1_controller.zoomTo(15);
                _mapview1_controller.moveCamera(latitud, longitud);
                _mapview1_controller.setMarkerPosition("pin", latitud, longitud);
                latlng.setText(String.format(Locale.getDefault(), "%.6f, %.6f", latitud, longitud));

                if (previousLocation != null) {

                    float distanceInMeters = previousLocation.distanceTo(location);
                    long timeDifferenceInSeconds = (location.getTime() - previousLocation.getTime()) / 1000;


                    if (timeDifferenceInSeconds > 0) {
                        float speedInMetersPerMinute = (distanceInMeters / timeDifferenceInSeconds) * 60;
                        textviewspeed.setText(String.format(Locale.getDefault(), " %.2f m/min", speedInMetersPerMinute));
                    }
                } else {

                    textviewspeed.setText(" Not available");
                }

                previousLocation = location;

                _getLocation(latitud, longitud);
            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };
    }

    private void initializeLogic() {
        getWindow().setNavigationBarColor(Color.parseColor("#ffffff"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        t = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            locate.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 1, _locate_location_listener);
                        }
                    }
                });
            }
        };
        _timer.scheduleAtFixedRate(t, 0, 1000);

        _getLocation(latitud, longitud);
    }

    private void showPermissionDeniedMessage() {
        Toast.makeText(this, "Location permission is required to use this feature", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapview1.onDestroy();
        _timer.cancel();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapview1.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapview1.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapview1.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapview1.onStop();
    }

    public void _getLocation(final double latitude, final double longitude) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                title_address.setText(strReturnedAddress.toString());
            } else {
                title_address.setText("No Address found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            title_address.setText("Can't get Address");
        }
    }
}

