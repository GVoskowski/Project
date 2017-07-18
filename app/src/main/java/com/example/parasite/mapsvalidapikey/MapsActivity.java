package com.example.parasite.mapsvalidapikey;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private static final int FINE_LOCATION_PERMISSION_REQUEST = 1;
    private static final int COARSE_LOCATION_PERMISSION_REQUEST = 1;
    private Marker userMarker;
    private LocationManager locationManager;
    Location userLocation;
    private GoogleMap mMap;

    String provider;
    private static final String TAG = MapsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        checkIfNetworkLocationAvailable(this);

        provider = locationManager.getBestProvider(new Criteria(), false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_REQUEST);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    COARSE_LOCATION_PERMISSION_REQUEST);
            return;
        }
        userLocation = locationManager.getLastKnownLocation(provider);

        if (userLocation != null) {
            Toast.makeText(this, "Achieved", Toast.LENGTH_LONG).show();
            Log.i("Location", "Achieved");

        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show();
            Log.i("Location", "failed");
        }


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_map));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Add a marker in Sydney and move the camera

        LatLng sydney = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        // Set marker and properties
        userMarker = mMap.addMarker(new MarkerOptions()
                .position(sydney)
                //Make the marker Draggable for user to Change location manually
                .title("You are here").draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    @Override
    protected void onResume() {
        checkIfNetworkLocationAvailable(this);
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        checkIfNetworkLocationAvailable(this);
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        LatLng userLoc = new LatLng(lat, lng);
        userMarker.remove();
        userMarker = mMap.addMarker(new MarkerOptions()
                .position(userLoc)
                //Make the marker Draggable for user to Change location manually
                .title("Marker in Sydney").draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLoc));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    // Checks if network Loca
    private void checkIfNetworkLocationAvailable(Context context) {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean networkLocationEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!networkLocationEnabled) {
            //show dialog to allow user to enable location settings
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("Tip");
            dialog.setMessage("Disabled");

            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    //nothing to do
                }
            });

            dialog.show();
        }

    }
}
