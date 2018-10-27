package s1640402.coinzgame.nishtha_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;


//Geo Json imports
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.style.light.Position;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;


//Mapbox markers and icon imports
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PlayGame extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener,
        PermissionsListener {

    private MapView mapView;
    private String tag = "PlayGame";
    private MapboxMap map;

    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private String geojsonstring;
    private List<Feature> features;
    private ArrayList<String> removemarkers = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get map data from mainview
        Mapbox.getInstance(this, "pk.eyJ1IjoibmlzaHRoYWt1bWFyIiwiYSI6ImNqbW5rbXdlaDBzYmYza254eGE1aXJkN2wifQ.Y2hUSRk2rGB45RKqgycCXQ");
        setContentView(R.layout.activity_play_game);
        mapView = (MapView) findViewById(R.id.mapboxMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //send downloaded data to map view
        Bundle bundle = getIntent().getExtras();
        geojsonstring = bundle.getString("strMapData");

       // if(removemarkers.size() == 0 || removemarkers !=null)
       // {
       //     getremovedmarkers();
       // }

    }

    private void enableLocation()
    {
        if (PermissionsManager.areLocationPermissionsGranted(this))
        {
            Log.d(tag,"Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        }
        else
        {
            Log.d(tag, "Permissions are not granted");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap)
    {
        if (mapboxMap == null)
        {
            Log.d(tag, "[onMapReady] mapBox is null");
        }
        else
        {
            map = mapboxMap;
            // Set user interface options
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            // Make location information available
            enableLocation();

            //take downloaded geojson string and make into feature collection
            FeatureCollection featureCollection = FeatureCollection.fromJson(geojsonstring);
            features = featureCollection.features();
            IconArraylist iconArraylist = new IconArraylist();
            IconFactory iconFactory = IconFactory.getInstance(PlayGame.this);

            //setup markers using loop and relation information from slides
            for (Feature f : features) {
                if (f.geometry() instanceof Point) {

                    //get marker icon based on currency and symbol
                    int marker = iconArraylist.geticonmarker(f.properties().get("currency").getAsString(),
                                                         f.properties().get("marker-symbol").getAsString());

                    map.addMarker(
                            new MarkerOptions().setPosition(new LatLng(
                                    ((Point) f.geometry()).latitude(),
                                    ((Point) f.geometry()).longitude()))
                            .setTitle(f.properties().get("currency").getAsString())
                            .setSnippet("value: " + f.properties().get("value").getAsString() +
                                        "\nid: " + f.properties().get("id").getAsString()))
                            .setIcon(iconFactory.fromResource(marker));
                }

            }

        }
    }


    private void setCameraPosition(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this)
                .obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000); // preferably every 5 seconds
        locationEngine.setFastestInterval(1000); // at most every second
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }


    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer()
    {
        if (mapView == null)
        {
            Log.d(tag, "mapView is null");
        }
        else
        {
            if (map == null)
            {
                Log.d(tag, "map is null");
            }
            else
            {
                locationLayerPlugin = new LocationLayerPlugin(mapView,
                        map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (location == null)
        {
            Log.d(tag, "[onLocationChanged] location is null");
        }
        else
        {
            Log.d(tag, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);

            float distance = 0;

            for (int i = 0; i<map.getMarkers().size(); i++) {

                Point g = (Point) (features.get(i)).geometry();

                Location point = new Location("");
                point.setLatitude(map.getMarkers().get(i).getPosition().getLatitude());
                point.setLongitude(map.getMarkers().get(i).getPosition().getLongitude());

                distance = location.distanceTo(point);

                if(distance <=25) {

                    Marker marker = map.getMarkers().get(i);
                   // removemarkers.add(features.get(i).getProperty("id").getAsString());
                    map.removeMarker(marker);
                }
            }

        }
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(tag, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain)
    {
        Log.d(tag, "Permissions: " + permissionsToExplain.toString());
        // Present toast or dialog.
    }

    @Override
    public void onPermissionResult(boolean granted)
    {
        Log.d(tag, "[onPermissionResult] granted == " + granted);
        if (granted)
        {
            enableLocation();
        }
        else
        {
           // Open a dialogue with the user

        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}