package ro.ananimarius.allridev3customer.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import ro.ananimarius.allridev3customer.Common.DriverDTO;
//import ro.ananimarius.allridev3customer.DriverAdapter;
import ro.ananimarius.allridev3customer.Functions;
import ro.ananimarius.allridev3customer.R;
import ro.ananimarius.allridev3customer.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    @BindView(R.id.activity_main)
    SlidingPaneLayout slidingPaneLayout;
    @BindView(R.id.txt_welcome)
    TextView txt_welcome;

    private AutocompleteSupportFragment autocompleteSupportFragment;

    private GoogleMap mMap;
    private FragmentHomeBinding binding;

    //Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    SupportMapFragment mapFragment;
    Double latitude;
    Double longitude;
    String authToken;
    String idToken;
    String email;

    private PlacesClient placesClient;
    private AutocompleteSupportFragment autocompleteFragment;
    private SearchView searchBar;

    LatLng globalLatLngWaypoint=null;
    Address globalAddressWaypoint=null;
    String globalAddressWaypointString=null;

    LatLng globalLatLngUser=null;

    Boolean showDrivers=false;

    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
//        mMap.clear(); //crashes
        globalAddressWaypointString = null;
        globalAddressWaypoint = null;
        globalLatLngWaypoint = null;
        super.onDestroy();
    }

    public interface APIInterface {
        @FormUrlEncoded
        @POST("user/updateLocation")
        Call<JsonObject> updateLocation(   @Field("authToken") String authToken,
                                           @Field("idToken") String googleId,
                                           @Field("latitude") double latitude,
                                           @Field("longitude")double longitude);
        @FormUrlEncoded
        @POST("user/selectDriver")
        Call<List<DriverDTO>> selectDriver(@Field("authToken") String authToken,
                                     @Field("idToken") String idToken,
                                     @Field("latitude") double latitude,
                                     @Field("longitude") double longitude);
        @FormUrlEncoded
        @POST("user/onMapDrivers")
        Call<List<DriverDTO>> onMapDrivers(   @Field("authToken") String authToken,
                                                 @Field("idToken") String googleId/*,
                                                 @Field("latitude") double latitude,
                                                 @Field("longitude")double longitude*/);
    }
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    HomeFragment.APIInterface api = retrofit.create(HomeFragment.APIInterface.class);
    GoogleSignInAccount account;

    private void init() {

        //searchbar
        Places.initialize(getContext(),getString(R.string.google_maps_key));
        autocompleteFragment=(AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setHint(getString(R.string.where_to));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Snackbar.make(getView(),""+status.getStatusMessage(),Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng selectedLatLng = place.getLatLng();
                mMap.clear(); // remove previous markers from the map
                mMap.addMarker(new MarkerOptions().position(selectedLatLng)); // add a marker to the selected location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 14f)); // move the camera to the selected location
            }
        });


        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 14f));
                globalLatLngUser=newPosition;

                latitude=newPosition.latitude;
                longitude=newPosition.longitude;
                Functions func=new Functions();
                authToken=func.getAuthTokenCookie();
                authToken=func.parseCookie(authToken);
                //send the location to the api
                Call<JsonObject> call = api.updateLocation(authToken, idToken, latitude, longitude);
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            //finish();
                        } else {
                            Toast.makeText(getContext(), "UpdateLocationError: " + response.code()+"+"+response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        int statusCode = 0;
                        String errorMessage = "";

                        if (t instanceof HttpException) {
                            HttpException httpException = (HttpException) t;
                            Response response = httpException.response();
                            statusCode = response.code();
                            errorMessage = response.message();
                        } else {
                            errorMessage = t.getMessage();
                        }
                        Toast.makeText(getContext(), "Error, Status code: " + statusCode + ", Message: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //google Places API client initialization
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.google_maps_key));
        }
        //autocomplete fragment initialization
        placesClient = Places.createClient(getContext());
        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setHint("Enter an address");
        //add a listener to handle the place selection
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Snackbar.make(getView(),""+status.getStatusMessage(),Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng selectedLatLng = place.getLatLng();
                mMap.clear(); // remove previous markers from the map
                mMap.addMarker(new MarkerOptions().position(selectedLatLng)); // add a marker to the selected location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 14f)); // move the camera to the selected location
                globalLatLngWaypoint=selectedLatLng;
                globalAddressWaypoint=fromLatLngToAddress(globalLatLngWaypoint);
                globalAddressWaypointString = globalAddressWaypoint.getAddressLine(0);
            }
        });

    }

    Button pickDriverBtn;
    Button requestDriverBtn;
    public void displayGetDriverButtons(){
        pickDriverBtn = getView().findViewById(R.id.pick_driver_btn);
        requestDriverBtn = getView().findViewById(R.id.request_driver_btn);
//        View ride_selection_buttons=root.findViewById(R.id.ride_selection_buttons);

        //show the buttons if globalAddressWaypointString, globalAddressWaypoint, and globalLatLngWaypoint are not null
        if (globalAddressWaypointString != null && globalAddressWaypoint != null && globalLatLngWaypoint != null) {
            pickDriverBtn.setVisibility(View.VISIBLE);
            requestDriverBtn.setVisibility(View.VISIBLE);
        }
        else{
            pickDriverBtn.setVisibility(View.GONE);
            requestDriverBtn.setVisibility(View.GONE);
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //receive the google account
        if (getArguments() != null) {
            idToken = getArguments().getString("googleId");
            email = getArguments().getString("email");
            if (idToken != null && email != null) {
                //use the Google account information
            }
        }


        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        init();

        //searchbar message
        iniViews(root);

        //pasted from mapActivity
        //obtain the SupportMapFragment and get notified when the map is ready to be used
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //displayGetDriverButtons();

        List<DriverDTO> drivers = new ArrayList<>();
        try {
//                            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            pickDriverBtn = root.findViewById(R.id.pick_driver_btn);
            pickDriverBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Call<List<DriverDTO>> call = api.selectDriver(authToken, idToken, globalLatLngUser.latitude, globalLatLngUser.longitude);
                    call.enqueue(new Callback<List<DriverDTO>>() {
                        @Override
                        public void onResponse(Call<List<DriverDTO>> call, Response<List<DriverDTO>> response) {
                            if (response.isSuccessful()) {
                                for (DriverDTO driver : response.body()) {
                                    drivers.add(driver);
                                }
                                if (drivers.size() > 0) {
                                    // Show the sliding panel
                                    View bottomSheet = getLayoutInflater().inflate(R.layout.my_bottom_sheet, null);
                                    RecyclerView driverList = bottomSheet.findViewById(R.id.driver_list);
                                    driverList.setLayoutManager(new LinearLayoutManager(getContext()));

                                    DriverListAdapter.OnDriverClickListener onDriverClickListener = new DriverListAdapter.OnDriverClickListener() {
                                        @Override
                                        public void onDriverClick(int position) {
                                            DriverDTO selectedDriver = drivers.get(position);
                                            Toast.makeText(getContext(), "Selected driver: " + selectedDriver.getFirstName(), Toast.LENGTH_SHORT).show();
                                        }
                                    };

                                    DriverListAdapter driverAdapter = new DriverListAdapter(drivers, onDriverClickListener);
                                    driverList.setAdapter(driverAdapter);
                                    Log.d("DriverList", "Size of adapter data: " + driverAdapter.getItemCount());
                                    MyBottomSheetDialogFragment bottomSheetDialogFragment = MyBottomSheetDialogFragment.newInstance(bottomSheet, driverAdapter);
                                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                                }


                                else{
                                    Toast.makeText(getContext(), "No drivers available in your area", Toast.LENGTH_SHORT).show();
                                }

                                //inflate the fragment's layout XML file, which should contain a RecyclerView element
//                                View rootView = inflater.inflate(R.layout.drivers_sliding_panel, container, false);
//                                RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
//                                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//                                DriverAdapter adapter = new DriverAdapter(drivers);
//                                recyclerView.setAdapter(adapter);

//                                if (drivers.size() > 0) {
//                                    View slidingPanel = getView().findViewById(R.id.driver_recycler_view);
//                                    slidingPanel.setVisibility(View.VISIBLE);
//                                    RecyclerView recyclerView = getView().findViewById(R.id.driver_recycler_view);
//                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//                                    DriverAdapter adapter = new DriverAdapter(drivers);
//                                    recyclerView.setAdapter(adapter);
//                                }

                                Toast.makeText(getContext(), "SelectDriverMessage: " + response.code() + "+" + response.message(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "SelectDriverError: " + response.code() + "+" + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<DriverDTO>> call, Throwable t) {
                            int statusCode = 0;
                            String errorMessage = "";

                            if (t instanceof HttpException) {
                                HttpException httpException = (HttpException) t;
                                Response response = httpException.response();
                                statusCode = response.code();
                                errorMessage = response.message();
                            } else {
                                errorMessage = t.getMessage();
                            }
                            Toast.makeText(getContext(), "SelectDriverError: " + statusCode + ", Message: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e){
            //fdsg
        }
        return root;
    }

    private void iniViews(View root) {
        ButterKnife.bind(this,root);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void setMarker(){
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            private Marker currentMarker;
            @Override
            public void onMapLongClick(LatLng latLng) {
                //Reverse-geocode the coordinates to an address to display in the waypoint title
                mMap.clear();
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String addressString = address.getAddressLine(0);
                        mMap.addMarker(new MarkerOptions().position(latLng).title(addressString));
                        globalAddressWaypointString=addressString;
                        globalAddressWaypoint=address;
                        globalLatLngWaypoint=latLng;
                        displayGetDriverButtons();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() { //remove the marker if map is tapped
                    @Override
                    public void onMapClick(LatLng latLng) {
                        mMap.clear();
                        globalAddressWaypointString = null;
                        globalAddressWaypoint = null;
                        globalLatLngWaypoint = null;
                        displayGetDriverButtons();
                    }
                });
            }
        });
    }
    public Address fromLatLngToAddress(LatLng latLng){
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String addressText = address.getAddressLine(0);
            return address;
        }
        return null;
    }

    private Handler handler;
    private Runnable runnable;
    public void onMapDrivers(){
        try {
            Functions func = new Functions();
            authToken = func.getAuthTokenCookie();
            authToken = func.parseCookie(authToken);
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    //call the onMapDrivers API to get the current locations of all online drivers
                    Call<List<DriverDTO>> call = api.onMapDrivers(authToken, idToken/*, latitude, longitude*/);
                    call.enqueue(new Callback<List<DriverDTO>>() {
                        @Override
                        public void onResponse(Call<List<DriverDTO>> call, Response<List<DriverDTO>> response) {
                            if (response.isSuccessful()) {
                                //clear the map
                                mMap.clear();

                                //add a marker for each online user
                                for (DriverDTO user : response.body()) {
                                    LatLng position = new LatLng(user.getLatitude(), user.getLongitude());
                                    //BitmapDescriptor carIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_directions_car_24);
                                    //mMap.addMarker(new MarkerOptions().position(position).icon(carIcon));
                                    mMap.addMarker(new MarkerOptions().position(position));
                                }
                            } else {
                                Toast.makeText(getContext(), "DriverOnMapError: " + response.code() + "+" + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<DriverDTO>> call, Throwable t) {
                            int statusCode = 0;
                            String errorMessage = "";

                            if (t instanceof HttpException) {
                                HttpException httpException = (HttpException) t;
                                Response response = httpException.response();
                                statusCode = response.code();
                                errorMessage = response.message();
                            } else {
                                errorMessage = t.getMessage();
                            }
                            Toast.makeText(getContext(), "DriverOnMapError, " + statusCode + ", " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });

                    handler.postDelayed(runnable, 1000); //schedule the next request after 1 second
                }
            };
            handler.postDelayed(runnable, 0); //start the request immediately
        }catch (Exception e){

        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        //check permission
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.getUiSettings().setZoomControlsEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(() -> {
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnFailureListener(e -> Toast.makeText(getContext(),"Error to get location: "+e.getMessage(),Toast.LENGTH_SHORT).show())
                                    .addOnSuccessListener(location -> {
                                        LatLng userLatLng=new LatLng(location.getLatitude(),location.getLongitude());
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,18f));

                                    });
                            return true;
                        });
                        //set location button
                        View locationButton=((View) mapFragment.getView().findViewById(Integer.parseInt("1"))
                                .getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        //place it to right bottom
//                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
//                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
//                        params.setMargins(0,0,0,50);
                        setMarker();
                        //onMapDrivers();
                        //check the buttons

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getContext(),"Permission "+permissionDeniedResponse.getPermissionName()+""+
                                " was denied!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();

        try{
            boolean success=googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.uber_maps_style));
            if(!success){
                Log.e("ERROR","Style parsing error");
            }
        }catch(Resources.NotFoundException e){
            Log.e("ERROR",e.getMessage());
        }
    }
}