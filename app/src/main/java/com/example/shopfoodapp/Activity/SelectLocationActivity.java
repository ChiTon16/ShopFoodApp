package com.example.shopfoodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shopfoodapp.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;



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

        setContentView(R.layout.activity_select_location);

        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);

        // Center map on default location (HCM City)
        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);
        GeoPoint defaultLocation = new GeoPoint(10.762622, 106.660172);
        mapController.setCenter(defaultLocation);

        // Add event overlay for map click
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                // Xóa chỉ các marker, giữ lại MapEventsOverlay
                mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker);

                // Cập nhật vị trí đã chọn
                selectedLocation = geoPoint;

                // Thêm marker mới
                Marker marker = new Marker(mapView);
                marker.setPosition(selectedLocation);
                marker.setTitle(String.format(Locale.US, "Lat: %.6f, Lon: %.6f", geoPoint.getLatitude(), geoPoint.getLongitude()));
                mapView.getOverlays().add(marker);

                // Hiển thị lại bản đồ
                mapView.invalidate();
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint geoPoint) {
                // Không xử lý nhấn giữ
                return false;
            }
        };


        MapEventsOverlay eventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapView.getOverlays().add(eventsOverlay);

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
