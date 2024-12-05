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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class EditActivity extends AppCompatActivity implements OnMapReadyCallback {
    private EditText latitude,longitude,numero,pseudo;
    private double longitudev,latitudev;
    private int idposv;
    private GoogleMap myMap;
    private Button btnEdit;
    private ImageView backButton;
    private LatLng sousse;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit);
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(EditActivity.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map3);
        mapFragment.getMapAsync(EditActivity.this);
        latitude=findViewById(R.id.latitudeedit);
        longitude=findViewById(R.id.longitudeedit);
        pseudo=findViewById(R.id.pseudoedit);
        numero=findViewById(R.id.numeroedit);
        btnEdit=findViewById(R.id.buttonedit);
        backButton = findViewById(R.id.imback);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        idposv = getIntent().getIntExtra("idPosition",0);
        String pseudov = getIntent().getStringExtra("pseudo");
        String numerov = getIntent().getStringExtra("numero");
        longitudev = getIntent().getDoubleExtra("longitude",0);
        latitudev = getIntent().getDoubleExtra("latitude",0);
        pseudo.setText(pseudov);
        numero.setText(numerov);
        longitude.setText(longitudev+"");
        latitude.setText(latitudev+"");
        getLastLocation();
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(latitude.getText().toString().isEmpty()||longitude.getText().toString().isEmpty()||numero.getText().toString().isEmpty()||pseudo.getText().toString().isEmpty()){
                    Toast.makeText(EditActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }
                new EditActivity.PostRequestTask().execute();
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    1);
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                currentLocation.setLatitude(currentLocation.getLatitude()-1.6094136);
                currentLocation.setLongitude(currentLocation.getLongitude()+132.722302);
                if (myMap != null) {
                    LatLng cur = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(cur)
                            .icon(getBitmapDescriptorFromVector(this, R.drawable.baseline_my_location_24));
                    myMap.addMarker(markerOptions);

                }
            } else {
                Log.e("curlocation", "Failed to retrieve current location");
            }
        });
    }
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
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        sousse = new LatLng(latitudev, longitudev);
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setCompassEnabled(true);
        MarkerOptions markerOptions=new MarkerOptions().position(sousse).icon(BitmapDescriptorFactory.defaultMarker()).draggable(true);
        myMap.addMarker(markerOptions);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sousse,17));
        myMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }
            @Override
            public void onMarkerDrag(Marker marker) {
                // Handle the marker while dragging (e.g., show a temporary location)
            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng newPosition = marker.getPosition();
                latitude.setText(newPosition.latitude+"");
                longitude.setText(newPosition.longitude+"");
                LatLng cuur=new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
            }
        });
    }
    private class PostRequestTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("http://10.0.2.2/servicephp/edit_position.php");
                StringRequest stringRequest=new StringRequest(Request.Method.POST, url.toString(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i("res", response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("err", error.toString());
                            }
                        }
                ){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String>params=new HashMap<>();
                        params.put("latitude",latitude.getText().toString());
                        params.put("idPosition",""+idposv);
                        params.put("longitude",longitude.getText().toString());
                        params.put("numero",numero.getText().toString());
                        params.put("pseudo",pseudo.getText().toString());
                        return params;
                    }
                };
                RequestQueue requestQueue= Volley.newRequestQueue(EditActivity.this);
                requestQueue.add(stringRequest);
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }
    }
}