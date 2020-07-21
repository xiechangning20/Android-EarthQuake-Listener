package com.example.earthquakelistener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue rq;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        rq = Volley.newRequestQueue(this);
        getEarthQuake();
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
        locationManager = (LocationManager) MapsActivity.this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
//                Log.d("newlocation:"," "+location.toString());

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else{
//          locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 0, locationListener);
//          Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//          LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
//          mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker()).title("hello"));

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
          if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
              if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) ==PackageManager.PERMISSION_GRANTED){
                  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 0, locationListener);

              }
          }

    }

    public void getEarthQuake(){
        final EarthQuake earthQuake = new EarthQuake();
        JsonObjectRequest JOR = new JsonObjectRequest(Request.Method.GET, WebInfo.url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("features");
                            for(int i=0;i<WebInfo.showLimit;i++){
//                                EarthQuake earthQuake = new EarthQuake();

                                //get Properties
                                JSONObject properties = jsonArray.getJSONObject(i).getJSONObject("properties");
                                //get Coordinates
                                JSONArray Coordinates = jsonArray.getJSONObject(i).
                                        getJSONObject("geometry").getJSONArray("coordinates");
                                  Double Lat = Coordinates.getDouble(1);
                                  Double Lng = Coordinates.getDouble(0);
                                LatLng latLng = new LatLng(Coordinates.getDouble(0),Coordinates.getDouble(1));
                                //Set Properties and coordinates
                                earthQuake.setMagnitude(properties.getDouble("mag"));
                                earthQuake.setPlace(properties.getString("place"));
                                earthQuake.setLatitude(Coordinates.getDouble(1));
                                earthQuake.setLongtitude(Coordinates.getDouble(0));
                                earthQuake.setTime(properties.getLong("time"));

                                //set time format
                                DateFormat dateFormat = DateFormat.getDateInstance();

                                String formatedDate = dateFormat.format(new Date(earthQuake.getTime()).getTime());

                                MarkerOptions mkoptions= new MarkerOptions();
                                mkoptions.title(earthQuake.getPlace());
                                mkoptions.position(new LatLng(earthQuake.getLatitude(),earthQuake.getLongtitude()));
                                mkoptions.icon(BitmapDescriptorFactory.defaultMarker());

                                Marker maker = mMap.addMarker(mkoptions);
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Lat,Lng),1));
                                maker.setSnippet("Magnitude:"+earthQuake.getMagnitude()+ "  "+"Time:"+formatedDate);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        rq.add(JOR);
    }
}