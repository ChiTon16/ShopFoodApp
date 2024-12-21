package com.example.shopfoodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shopfoodapp.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.Locale;

public class SelectLocationActivity extends AppCompatActivity {

    private MapView mapView;
    private GeoPoint selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình osmdroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setTileFileSystemCacheMaxBytes(1024 * 1024 * 50); // 50MB cache
        Configuration.getInstance().setTileFileSystemCacheTrimBytes(1024 * 1024 * 40); // 40MB trim cache
        Configuration.getInstance().setCacheMapTileCount((short) 9); // Cache 9 tiles
        Configuration.getInstance().setCacheMapTileOvershoot((short) 2); // Cache overshoot tiles

        setContentView(R.layout.activity_select_location);

        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);

        // Center map on default location (HCM City)
        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);
        GeoPoint defaultLocation = new GeoPoint(10.762622, 106.660172);
        mapController.setCenter(defaultLocation);

        // Add marker on map click
        mapView.setOnTouchListener((v, event) -> {
            mapView.getOverlays().clear();
            selectedLocation = new GeoPoint(defaultLocation.getLatitude(), defaultLocation.getLongitude());

            Marker marker = new Marker(mapView);
            marker.setPosition(selectedLocation);
            marker.setTitle("Selected Location");
            mapView.getOverlays().add(marker);

            return false;
        });

        // Confirm location button
        Button confirmButton = findViewById(R.id.confirmLocationButton);
        confirmButton.setOnClickListener(v -> {
            if (selectedLocation != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLocation.getLatitude());
                resultIntent.putExtra("longitude", selectedLocation.getLongitude());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select a location!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
