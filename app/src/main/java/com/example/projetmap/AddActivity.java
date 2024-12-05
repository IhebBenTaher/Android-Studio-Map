package com.example.projetmap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final int FINE_PERMISSION_CODE=1;
    private GoogleMap myMap;
    private Handler handler;
    private long refreshtime=60000;
    private Runnable runnable;
    private Marker movableMarker,currentPositionMarker;
    private EditText latitude,longitude,numero,pseudo;
    private Button button;
    Location currentLocation;
    private ImageView backButton;
    FusedLocationProviderClient fusedLocationProviderClient;
    @SuppressLint({"MissingInflatedId", "WrongViewCast"})


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(AddActivity.this);

        handler = new Handler();

        // Get last location initially
        getLastLocation();

        // Rafraîchit la position chaque 1min
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                getLastLocation();
                handler.postDelayed(runnable, refreshtime);
            }

        }, refreshtime);



        // Initialisation des champs
        latitude = findViewById(R.id.latitudeadd);
        longitude = findViewById(R.id.longitudeadd);
        pseudo = findViewById(R.id.pseudoadd);
        numero = findViewById(R.id.numeroadd);
        button = findViewById(R.id.buttonadd);
        backButton = findViewById(R.id.imageViewback);

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(AddActivity.this, MainActivity.class);
            startActivity(intent);
        });

        button.setOnClickListener(view -> {

            // Vérifie si tous les champs sont remplis avant d'envoyer la requête
            if (latitude.getText().toString().isEmpty() || longitude.getText().toString().isEmpty() ||
                    numero.getText().toString().isEmpty() || pseudo.getText().toString().isEmpty()) {
                Toast.makeText(AddActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lance la tâche pour envoyer les données au serveur
            new PostRequestTask().execute();
            Intent intent = new Intent(AddActivity.this, MainActivity.class);
            startActivity(intent);

        });

    }


    private void getLastLocation() {
        // Check if the app has location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // If not, ask for the necessary permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(AddActivity.this);// This triggers onMapReady() callback
                    }
                } else {
                    // Handle the case when location is null (could be due to no available location data)
                    Log.e("currentlocation", "Location is null");
                }
            }
        });
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        LatLng myLocation = new LatLng(currentLocation.getLatitude()-1.6094136, currentLocation.getLongitude()+132.722302);
        myMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    1001);
            return;
        }

        // Enable the user's current location to be displayed on the map
        myMap.setMyLocationEnabled(true);

        myMap.getUiSettings().setCompassEnabled(true);

        MarkerOptions markerOptions=new MarkerOptions().position(myLocation).icon(getBitmapDescriptorFromVector(getApplicationContext(), R.drawable.baseline_my_location_24));
        if (currentPositionMarker != null) {
            currentPositionMarker.remove();
            currentPositionMarker = null; // Optional: Clear the reference.
        }
        currentPositionMarker=myMap.addMarker(markerOptions);
        if (movableMarker == null) {
            movableMarker = myMap.addMarker(new MarkerOptions().position(myLocation).draggable(true));
        }
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,17));
        myMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }
            @Override
            public void onMarkerDrag(Marker marker) {
            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                 // Handle drag end (e.g., save the new position)
                 LatLng newPosition = marker.getPosition();
                 latitude.setText(newPosition.latitude+"");
                 longitude.setText(newPosition.longitude+"");
            }
        });
    }


    private class PostRequestTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("http://10.0.2.2/servicephp/add_position.php");  // URL du serveur local
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url.toString(),
                        response -> Log.i("res", response),
                        error -> Log.i("err", error.toString())
                ) {
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("latitude", latitude.getText().toString());
                        params.put("longitude", longitude.getText().toString());
                        params.put("numero", numero.getText().toString());
                        params.put("pseudo", pseudo.getText().toString());
                        return params;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(AddActivity.this);
                requestQueue.add(stringRequest);  // Ajoute la requête à la file d'attente
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }
    }


    //Crée une icône personnalisée à partir d'un drawable vectoriel pour le Marker.
    private BitmapDescriptor getBitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        int height = 50;
        int width = 50;
        vectorDrawable.setBounds(0, 0, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}