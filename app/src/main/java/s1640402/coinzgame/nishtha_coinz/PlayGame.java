package s1640402.coinzgame.nishtha_coinz;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
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
import com.mapbox.geojson.Point;


//Mapbox markers and icon imports
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;

import java.util.List;

//firebase
import com.google.firebase.firestore.FirebaseFirestore;

import javax.annotation.Nullable;


public class PlayGame extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener,
        PermissionsListener {
    private String tag = "PlayGame";

    //location and map variables
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private String geojsonstring;
    private List<Feature> features;
    private MapView mapView;
    private MapboxMap map;

    //firebase variables
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private String curruser;
    private Button walletbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get map data from main menu
        Mapbox.getInstance(this, "pk.eyJ1IjoibmlzaHRoYWt1bWFyIiwiYSI6ImNqbW5rbXdlaDBzYmYza254eGE1aXJkN2wifQ.Y2hUSRk2rGB45RKqgycCXQ");
        setContentView(R.layout.activity_play_game);
        mapView = (MapView) findViewById(R.id.mapboxMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //send downloaded data to map view
        Bundle bundle = getIntent().getExtras();
        geojsonstring = bundle.getString("strMapData");

        //intitalise firebase/firestore variables
        mAuth = FirebaseAuth.getInstance();
        curruser = mAuth.getCurrentUser().getEmail();
        walletbutton = findViewById(R.id.wallet);


    }

    //if location tracking permission is granted intialize all the relevant engines and layers
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

                    //check if current marker hasnt already been collected by the user by comparing to the removedcoins list on the database
                    CollectionReference collectionReference = db.collection("users").document(curruser).collection("removedcoins");

                    collectionReference.document(f.properties().get("id").getAsString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(@Nullable DocumentSnapshot documentSnapshot) {
                            if (!documentSnapshot.exists()) {
                                //get marker icon based on currency and symbol
                                int marker = iconArraylist.geticonmarker(f.properties().get("currency").getAsString(),
                                                                     f.properties().get("marker-symbol").getAsString());

                                map.addMarker(new MarkerOptions().setPosition(new LatLng(((Point) f.geometry()).latitude(),
                                                                                         ((Point) f.geometry()).longitude()))
                                                                             .setTitle(f.properties().get("id").getAsString())
                                                                             .setSnippet("" + f.properties().get("currency").getAsString() +
                                                                                         "\n" + f.properties().get("value").getAsString()))
                                                                              .setIcon(iconFactory.fromResource(marker));
                            }
                        }
                    });

                }

            }
        }
    }

    //center camera around the users location
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

    // when location is changed we will check if there are any coins near by that can be banked
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

            //when the users location changed we loop through all the markers on the map to see
            //if they are near any of the coins
            for (int i = 0; i<map.getMarkers().size(); i++) {

                Point g = (Point) (features.get(i)).geometry();

                //get coordinates of this specific iterations coin
                Location point = new Location("");
                point.setLatitude(map.getMarkers().get(i).getPosition().getLatitude());
                point.setLongitude(map.getMarkers().get(i).getPosition().getLongitude());

                //calculate the distance in meters from the coin and the current location
                distance = location.distanceTo(point);
                //if the distance is 25 meters it means the user can pick up the coin
                if(distance <=25) {

                    //remove coin from markers list so it is not visble on the map anymore
                    Marker marker = map.getMarkers().get(i);
                    map.removeMarker(marker);

                    //use the snippets of that coin along with it's id to create a new coin object for storing in wallet
                    String[] data = marker.getSnippet().split("\n");
                    Coin collected = new Coin(marker.getTitle(), data[1], data[0]);

                    //check if the coin goes into the wallet or spare change
                    db.collection("users").document(curruser).collection("wallet").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {

                            //if the wallet has space
                            if( queryDocumentSnapshots.size() < 50) {
                                //put coin in wallet
                                db.collection("users").document(curruser).collection("wallet").document(collected.getId()).set(collected);
                                //update the wallet size display to show the new wallet size
                                db.collection("users").document(curruser).collection("wallet").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshot) {
                                        Integer walletcount = queryDocumentSnapshot.size();
                                        walletbutton.setText(walletcount.toString());
                                    }
                                });
                            }
                            //if wallet is full so has 50 coins, this coin goes into spare change
                            else {
                                db.collection("users").document(curruser).collection("sparechange").document(collected.getId()).set(collected);
                            }
                        }
                    });
                    //add the coin to the removed coin list so that next time this coin isnt re added to the map
                    db.collection("users").document(curruser).collection("removedcoins").document(collected.getId()).set(collected);
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

        new ConverterandDialogs().OKdialog("To play the game you will have to enable location! " +
                " Please go to your settings to enable location services","Location Services",PlayGame.this);
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
            new ConverterandDialogs().OKdialog("To play the game you will have to enable location! " +
                    " Please go to your settings to enable location services","Location Services",PlayGame.this);
        }
    }

    //goes to wallet
    public void gotowallet(View view) {
        Intent intent = new Intent(this, ViewWallet.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        walletbutton = findViewById(R.id.wallet);

        //load the wallet size for the wallet size button
        db.collection("users").document(curruser).collection("wallet").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                Integer walletcount = queryDocumentSnapshots.size();
                walletbutton.setText(walletcount.toString());
            }
        });

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
        //stop tracking location when user leaves this activity
        if(locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if(locationLayerPlugin !=null) {
            locationLayerPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        if(mapView != null) {
            mapView.onDestroy();
        }
        super.onDestroy();
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void gotomain(View view){
        Intent intent = new Intent(this, MainView.class);
        startActivity(intent);
    }


}