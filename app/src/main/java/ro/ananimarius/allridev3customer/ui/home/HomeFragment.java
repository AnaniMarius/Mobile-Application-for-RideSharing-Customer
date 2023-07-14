package ro.ananimarius.allridev3customer.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
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
import androidx.core.content.ContextCompat;
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
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
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
import ro.ananimarius.allridev3customer.Common.RideDTO;
import ro.ananimarius.allridev3customer.Common.UnsafeOkHttpClient;
import ro.ananimarius.allridev3customer.Common.UserDTO;
import ro.ananimarius.allridev3customer.Common.WaypointDTO;
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
    String firstName;
    String lastName;
    //uri profile photo

    private PlacesClient placesClient;
    private AutocompleteSupportFragment autocompleteFragment;
    private SearchView searchBar;

    LatLng globalLatLngWaypoint = null;
    Address globalAddressWaypoint = null;
    String globalAddressWaypointString = null;

    LatLng globalLatLngUser = null;

    Boolean showDrivers = false;
    Button endRideBtn;
    Button cancelRideBtn;
    Boolean endRide = false;
    Boolean cancelRide = false;

    public void toggleRideButtons() {
        endRideBtn = getView().findViewById(R.id.end_ride_btn);
        cancelRideBtn = getView().findViewById(R.id.cancel_ride_btn);
        if (activeRide == true) {
            endRideBtn.setVisibility(View.VISIBLE);
            cancelRideBtn.setVisibility(View.VISIBLE);
        } else {
            endRideBtn.setVisibility(View.GONE);
            cancelRideBtn.setVisibility(View.GONE);
        }
    }

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
        Call<JsonObject> updateLocation(@Field("authToken") String authToken,
                                        @Field("idToken") String googleId,
                                        @Field("latitude") double latitude,
                                        @Field("longitude") double longitude);

        @FormUrlEncoded
        @POST("user/selectDriver")
        Call<List<DriverDTO>> selectDriver(@Field("authToken") String authToken,
                                           @Field("idToken") String idToken,
                                           @Field("latitude") double latitude,
                                           @Field("longitude") double longitude,
                                           @Field("destLatitude") double destLatitude,
                                           @Field("destLongitude") double destLongitude);

        @FormUrlEncoded
        @POST("user/onMapDrivers")
        Call<List<DriverDTO>> onMapDrivers(@Field("authToken") String authToken,
                                           @Field("idToken") String googleId,
                                                 @Field("latitude") double latitude,
                                                 @Field("longitude")double longitude);

        @FormUrlEncoded
        @POST("user/sendRequestToDriver")
        Call<ResponseBody> sendRequestToDriver(@Field("authToken") String authToken,
                                               @Field("idToken") String idToken,
                                               @Field("custLatitude") double custLatitude,
                                               @Field("custLongitude") double custLongitude,
                                               @Field("destLatitude") double destLatitude,
                                               @Field("destLongitude") double destLongitude,
                                               @Field("firstName") String customerFirstName,
                                               @Field("lastName") String customerLastName,
                                               @Field("driverId") String driverId);

        @FormUrlEncoded
        @POST("user/sendRequestToMatchedDriver")
        Call<ResponseBody> sendRequestToMatchedDriver(@Field("authToken") String authToken,
                                               @Field("idToken") String idToken,
                                               @Field("custLatitude") double custLatitude,
                                               @Field("custLongitude") double custLongitude,
                                               @Field("destLatitude") double destLatitude,
                                               @Field("destLongitude") double destLongitude,
                                               @Field("firstName") String customerFirstName,
                                               @Field("lastName") String customerLastName);

        @FormUrlEncoded
        @POST("user/onMapRideDriver")
        Call<DriverDTO> onMapRideDriver(@Field("authToken") String authToken,
                                        @Field("idToken") String googleId);

        @FormUrlEncoded
        @POST("user/checkCurrentRide")
        Call<RideDTO> checkCurrentRide(@Field("authToken") String authToken,
                                       @Field("driverId") String driverId,
                                       @Field("customerId") String customerId,
                                       @Field("endRide") boolean endRide,
                                       @Field("cancelRide") boolean cancelRide);

    }

    //experiment
    OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
    Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl("http://192.168.1.219:8080/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create());
    Retrofit retrofit = builder.build();

    //end experiment
//    Retrofit retrofit = new Retrofit.Builder()
//            .baseUrl("https://192.168.1.219:8080")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build();
//
    HomeFragment.APIInterface api = retrofit.create(HomeFragment.APIInterface.class);
    GoogleSignInAccount account;

    private void init() {
        //searchbar
        Places.initialize(getContext(), getString(R.string.google_maps_key));
        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setHint(getString(R.string.where_to));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Snackbar.make(getView(), "" + status.getStatusMessage(), Snackbar.LENGTH_LONG).show();
            }
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng selectedLatLng = place.getLatLng();
                mMap.clear(); // remove previous markers from the map
                mMap.addMarker(new MarkerOptions().position(selectedLatLng)); // add a marker to the selected location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 14f)); // move the camera to the selected location
            }
        });
        if (activeRide == true) {
            try {
                View bottomSheet = getLayoutInflater().inflate(R.layout.my_bottom_sheet, null);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(bottomSheet);
                // Obtain the BottomSheetBehavior
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheet.getParent());
                // Close the bottom sheet
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //onMapRideDriver();
                LatLng newPosition;
                if (mMap != null) {
                    newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 17f));
                    globalLatLngUser = newPosition;

                    latitude = newPosition.latitude;
                    longitude = newPosition.longitude;
                    Functions func = new Functions();
                    authToken = func.getAuthTokenCookie();
                    authToken = func.parseCookie(authToken);
                    //send the location to the api
                    Call<JsonObject> call = api.updateLocation(authToken, idToken, latitude, longitude);
                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response.isSuccessful()) {
                                //Toast.makeText(getContext(), response.body().toString(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "UpdateLocationError: " + response.code() + "+" + response.message(), Toast.LENGTH_SHORT).show();
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
                Snackbar.make(getView(), "" + status.getStatusMessage(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng selectedLatLng = place.getLatLng();
                mMap.clear(); // remove previous markers from the map
                mMap.addMarker(new MarkerOptions().position(selectedLatLng)); // add a marker to the selected location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 14f)); // move the camera to the selected location
                globalLatLngWaypoint = selectedLatLng;
                globalAddressWaypoint = fromLatLngToAddress(globalLatLngWaypoint);
                globalAddressWaypointString = globalAddressWaypoint.getAddressLine(0);
                displayGetDriverButtons();
            }
        });
    }

    Button pickDriverBtn;
    Button requestDriverBtn;
    Boolean toggleOnMapDrivers;

    public void displayGetDriverButtons() {
        pickDriverBtn = getView().findViewById(R.id.pick_driver_btn);
        requestDriverBtn = getView().findViewById(R.id.request_driver_btn);
//        View ride_selection_buttons=root.findViewById(R.id.ride_selection_buttons);

        //show the buttons if globalAddressWaypointString, globalAddressWaypoint, and globalLatLngWaypoint are not null


        if (activeRide == false && globalAddressWaypointString != null && globalAddressWaypoint != null && globalLatLngWaypoint != null) {
            pickDriverBtn.setVisibility(View.VISIBLE);
            requestDriverBtn.setVisibility(View.VISIBLE);
            toggleOnMapDrivers = true;
        } else {
            pickDriverBtn.setVisibility(View.GONE);
            requestDriverBtn.setVisibility(View.GONE);
            toggleOnMapDrivers = false;
        }
        onMapDrivers();
    }


    DriverDTO selectedDriver = new DriverDTO();

    public void displayRideInformation(DriverDTO driver){
        List<DriverDTO> drivers = new ArrayList<>();
        drivers.add(driver);
        View bottomSheet = getLayoutInflater().inflate(R.layout.my_bottom_sheet, null);
        RecyclerView driverList = bottomSheet.findViewById(R.id.driver_list);
        driverList.setLayoutManager(new LinearLayoutManager(getContext()));
//        DriverListAdapter driverAdapter = new DriverListAdapter(drivers, onDriverClickListener);
//        driverList.setAdapter(driverAdapter);
//        Log.d("DriverList", "Size of adapter data: " + driverAdapter.getItemCount());
//        MyBottomSheetDialogFragment bottomSheetDialogFragment = MyBottomSheetDialogFragment.newInstance(bottomSheet, driverAdapter);
//        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
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
            firstName = getArguments().getString("firstName");
            lastName = getArguments().getString("lastName");
        }


        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        init();

        try {
            endRideBtn = root.findViewById(R.id.end_ride_btn);
            endRideBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    endRide = true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            cancelRideBtn = root.findViewById(R.id.cancel_ride_btn);
            cancelRideBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelRide = true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                    Call<List<DriverDTO>> call = api.selectDriver(authToken, idToken, globalLatLngUser.latitude, globalLatLngUser.longitude, globalLatLngWaypoint.latitude, globalLatLngWaypoint.longitude);
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
                                            selectedDriver = drivers.get(position);
                                            Toast.makeText(getContext(), "Selected driver: " + selectedDriver.getFirstName(), Toast.LENGTH_SHORT).show();

                                            //send the request to the selected driver
                                            sendRequestToDriver(selectedDriver.getGoogleId());
                                        }
                                    };

                                    DriverListAdapter driverAdapter = new DriverListAdapter(drivers, onDriverClickListener);
                                    driverList.setAdapter(driverAdapter);
                                    Log.d("DriverList", "Size of adapter data: " + driverAdapter.getItemCount());
                                    MyBottomSheetDialogFragment bottomSheetDialogFragment = MyBottomSheetDialogFragment.newInstance(bottomSheet, driverAdapter);
                                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                                } else {
                                    Toast.makeText(getContext(), "No drivers available in your area", Toast.LENGTH_SHORT).show();
                                }

                                //Toast.makeText(getContext(), "SelectDriverMessage: " + response.code() + "+" + response.message(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "There are no available drivers around!", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getContext(), "There are no available drivers around!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            requestDriverBtn=root.findViewById(R.id.request_driver_btn);
            requestDriverBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequestToMatchedDriver();
                }
            });
        } catch (Exception e) {
            //fdsg
        }
        return root;
    }

    private void sendRequestToDriver(String selectedDriver) {
        if (activeRide == false) {
            Functions func = new Functions();
            authToken = func.getAuthTokenCookie();
            authToken = func.parseCookie(authToken);
            handler = new Handler();
            Call<ResponseBody> call = api.sendRequestToDriver(authToken, idToken, globalLatLngUser.latitude, globalLatLngUser.longitude, globalLatLngWaypoint.latitude, globalLatLngWaypoint.longitude, firstName, lastName, selectedDriver);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        String result = null;
                        try {
                            result = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();

                        // Add a 7-second delay before calling the endpoint to check if the request has been accepted
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Call the endpoint here
                                //toggleRideDriver=true;
                                //onMapRideDriver();
                            }
                        }, 7000); // Delay in milliseconds (7 seconds)
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
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
                    Toast.makeText(getContext(), "sendRequestToDriver: " + statusCode + ", Message: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendRequestToMatchedDriver() {
        if (activeRide == false) {
            Functions func = new Functions();
            authToken = func.getAuthTokenCookie();
            authToken = func.parseCookie(authToken);
            handler = new Handler();
            Call<ResponseBody> call = api.sendRequestToMatchedDriver(authToken, idToken, globalLatLngUser.latitude, globalLatLngUser.longitude, globalLatLngWaypoint.latitude, globalLatLngWaypoint.longitude, firstName, lastName);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        String result = null;
                        try {
                            result = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();

                        // Add a 7-second delay before calling the endpoint to check if the request has been accepted
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Call the endpoint here
                                //toggleRideDriver=true;
                                //onMapRideDriver();
                            }
                        }, 7000); // Delay in milliseconds (7 seconds)
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
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
                    Toast.makeText(getContext(), "sendRequestToMatchedDriver: " + statusCode + ", Message: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void setMarker() {
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
                        globalAddressWaypointString = addressString;
                        globalAddressWaypoint = address;
                        globalLatLngWaypoint = latLng;
                        displayGetDriverButtons();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() { //remove the marker if map is tapped
                    @Override
                    public void onMapClick(LatLng latLng) {
                        mMap.clear();
                        driverOverlays.clear();
                        toggleOnMapDrivers=false;
                        globalAddressWaypointString = null;
                        globalAddressWaypoint = null;
                        globalLatLngWaypoint = null;
                        displayGetDriverButtons();
                    }
                });
            }
        });
    }

    public Address fromLatLngToAddress(LatLng latLng) {
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

    private Bitmap getBitmapFromDrawable(int drawableResId) {
        Drawable drawable = ContextCompat.getDrawable(requireContext(), drawableResId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            return getBitmapFromVectorDrawable((VectorDrawable) drawable);
        }
        return null;
    }

    private Bitmap getBitmapFromVectorDrawable(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }


    //list to store ground overlays for each driver
    private List<GroundOverlay> driverOverlays = new ArrayList<>();

    public void onMapDrivers() {
        if (toggleOnMapDrivers == true && activeRide == false) {
            try {
                Functions func = new Functions();
                authToken = func.getAuthTokenCookie();
                authToken = func.parseCookie(authToken);
                handler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        //call the onMapDrivers API to get the current locations of all online drivers
                        Call<List<DriverDTO>> call = api.onMapDrivers(authToken, idToken, latitude, longitude);
                        call.enqueue(new Callback<List<DriverDTO>>() {
                            @Override
                            public void onResponse(Call<List<DriverDTO>> call, Response<List<DriverDTO>> response) {
                                if (response.isSuccessful()) {
                                    //clear previous ground overlays
                                    for (GroundOverlay overlay : driverOverlays) {
                                        overlay.remove();
                                    }
                                    driverOverlays.clear();

                                    //add a ground overlay for each online user
                                    for (DriverDTO user : response.body()) {
                                        LatLng position = new LatLng(user.getLatitude(), user.getLongitude());
                                        BitmapDescriptor carIcon = BitmapDescriptorFactory.fromResource(R.drawable.car1); // Replace 'car_image' with your car icon's resource name
                                        GroundOverlayOptions overlayOptions = new GroundOverlayOptions()
                                                .position(position, 50) // Set the width of the ground overlay to 50 meters. Adjust the value as needed.
                                                .image(carIcon)
                                                .anchor(0.5f, 0.5f); // Center the anchor of the ground overlay

                                        GroundOverlay overlay = mMap.addGroundOverlay(overlayOptions);
                                        driverOverlays.add(overlay);
                                    }
                                    //resize the icon along with the map zoom
//                                    mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
//                                        @Override
//                                        public void onCameraIdle() {
//                                            float currentZoom = mMap.getCameraPosition().zoom;
//
//                                            //calculate the new width of the ground overlays based on the current zoom level
//                                            float newWidth = 50 * currentZoom / 15; //adjust the value '15' as needed
//
//                                            //update the size of each ground overlay
//                                            for (GroundOverlay overlay : driverOverlays) {
//                                                overlay.setDimensions(newWidth);
//                                            }
//                                        }
//                                    });
                                } else {
                                    //Toast.makeText(getContext(), "DriverOnMapError: " + response.code() + "+" + response.message(), Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getContext(), "No available driver around!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<List<DriverDTO>> call, Throwable t) {
                                int statusCode = 0;
                                String errorMessage = "";

                                if (isAdded()) {
                                    if (t instanceof HttpException) {
                                        HttpException httpException = (HttpException) t;
                                        Response response = httpException.response();
                                        statusCode = response.code();
                                        errorMessage = response.message();
                                    } else {
                                        errorMessage = t.getMessage();
                                    }
                                    Toast.makeText(getContext(), "No available driver around!", Toast.LENGTH_SHORT).show();
                                    //Toast.makeText(getContext(), "DriverOnMapError, " + statusCode + ", " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        handler.postDelayed(runnable, 5000); //schedule the next request after 1 second
                    }
                };
                handler.postDelayed(runnable, 0); //start the request immediately
            } catch (Exception e) {
            }
        } else {
            handler.removeCallbacks(runnable);
        }
    }

    boolean activeRide = false;
    boolean toggleRideDriver;
    private List<GroundOverlay> rideDriverOverlays = new ArrayList<>();

    public void onMapRideDriver() {
        //if(toggleRideDriver==true) {
        //toggleRideDriver=false;
        try {
            Functions func = new Functions();
            authToken = func.getAuthTokenCookie();
            authToken = func.parseCookie(authToken);
            //call the onMapDrivers API to get the current locations of all online drivers
            Call<DriverDTO> call = api.onMapRideDriver(authToken, idToken);
            call.enqueue(new Callback<DriverDTO>() {
                @Override
                public void onResponse(Call<DriverDTO> call, Response<DriverDTO> response) {
                    if (response.isSuccessful()) {
                        //clear previous ground overlays
                        for (GroundOverlay overlay : rideDriverOverlays) {
                            overlay.remove();
                        }
                        rideDriverOverlays.clear();

                        //add a ground overlay for each online user
                        if (response.body() != null) {
                            if (activeRide == false) {
                                driverOverlays.clear();
                                mMap.clear();
                                Toast.makeText(getContext(), "Request accepted!", Toast.LENGTH_SHORT).show();
                            }
                            activeRide = true;


                            Toast.makeText(getContext(), "Request in progress!", Toast.LENGTH_SHORT).show();
                            DriverDTO user = response.body();
                            LatLng position = new LatLng(user.getLatitude(), user.getLongitude());
                            BitmapDescriptor carIcon = BitmapDescriptorFactory.fromResource(R.drawable.car1); // Replace 'car_image' with your car icon's resource name
                            GroundOverlayOptions overlayOptions = new GroundOverlayOptions()
                                    .position(position, 50) // Set the width of the ground overlay to 50 meters. Adjust the value as needed.
                                    .image(carIcon)
                                    .anchor(0.5f, 0.5f); // Center the anchor of the ground overlay

                            GroundOverlay overlay = mMap.addGroundOverlay(overlayOptions);
                            rideDriverOverlays.add(overlay);
                        } else {
                            activeRide = false;
                            Toast.makeText(getContext(), "Request Ended!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        activeRide = false;
                        Toast.makeText(getContext(), "No available driver around!", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getContext(), "RideDriverOnMapError: " + response.code() + "+" + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<DriverDTO> call, Throwable t) {
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
                    Toast.makeText(getContext(), "No available driver around!", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getContext(), "RideDriverOnMapError, " + statusCode + ", " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Set<WaypointDTO> route;
    WaypointDTO waypointDTO = new WaypointDTO();
    RideDTO ride = new RideDTO();

    private void checkCurrentRide() {
        Call<RideDTO> call = api.checkCurrentRide(authToken, null, idToken, endRide, cancelRide);
        try {
            Functions func = new Functions();
            authToken = func.getAuthTokenCookie();
            authToken = func.parseCookie(authToken);
            call.enqueue(new Callback<RideDTO>() {
                @Override
                public void onResponse(Call<RideDTO> call, Response<RideDTO> response) {
                    if (response.isSuccessful()) {
                        //clear previous ground overlays
                        for (GroundOverlay overlay : rideDriverOverlays) {
                            overlay.remove();
                        }
                        rideDriverOverlays.clear();
                        if (response.body() != null) {
                            if (activeRide == false) {
                                //AICI VOI ACTIVA BUTOANELE SA FIE VIZIBILE
                                if (isAdded()) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(requireContext(), "The ride is in progress!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

//                                ride.setCost(response.body().getCost());
//                                ride.setCurrency(response.body().getCurrency());
//                                ride.setId(response.body().getId());
                                ride.setDriver(response.body().getDriver());
                                ride.setCurrentRidePrice(response.body().getCurrentRidePrice());
                                ride.setCurrentRideTotalTime(response.body().getCurrentRideTotalTime());
                                ride.setCurrentRideTotalDistance(response.body().getCurrentRideTotalDistance());
//                                ride.setPassenger(response.body().getPassenger());
//                                ride.setRoute(response.body().getRoute());

//changethestatusofthereuqest
//matched
//selectdriver


                                DriverDTO displayInfo=new DriverDTO();
                                displayInfo.setFirstName(ride.getDriver().getFirstName());
                                displayInfo.setLastName(ride.getDriver().getLastName());
                                displayInfo.setCurrentRating(ride.getDriver().getCurrentRating());
                                displayInfo.setCurrentRidePrice(ride.getCurrentRidePrice());
                                displayInfo.setCurrentRideTotalTime(ride.getCurrentRideTotalTime());
                                displayInfo.setCurrentRideTotalDistance(ride.getCurrentRideTotalDistance());
                                displayRideInformation(displayInfo);
                                driverOverlays.clear();
                                mMap.clear();
                                if (isAdded()) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(requireContext(), "Request accepted!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                //route = ride.getRoute();
                            }
                            ride.setCost(response.body().getCost());
                            ride.setCurrency(response.body().getCurrency());
                            ride.setId(response.body().getId());
                            ride.setDriver(response.body().getDriver());
                            ride.setPassenger(response.body().getPassenger());
                            ride.setRoute(response.body().getRoute());
                            ride.setCustomerCancelsRide(response.body().isCustomerCancelsRide());
                            ride.setDriverCancelsRide(response.body().isDriverCancelsRide());
                            ride.setCustomerEndsRide(response.body().isCustomerEndsRide());
                            ride.setDriverEndsRide(response.body().isDriverEndsRide());
                            ride.setNearCustomer(response.body().isNearCustomer());
                            ride.setNearDestination(response.body().isNearDestination());
                            route = ride.getRoute();

                            activeRide = true;
                            try {
                                toggleRideButtons();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                pickDriverBtn.setVisibility(View.GONE);
                                requestDriverBtn.setVisibility(View.GONE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (route != null && !route.isEmpty()) {
                                waypointDTO = route.iterator().next();
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(requireContext(), "Ride in progress!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                UserDTO user = ride.getDriver();
                                LatLng position = new LatLng(user.getLatitude(), user.getLongitude());
                                BitmapDescriptor carIcon = BitmapDescriptorFactory.fromResource(R.drawable.car1); // Replace 'car_image' with your car icon's resource name
                                GroundOverlayOptions overlayOptions = new GroundOverlayOptions()
                                        .position(position, 50) // Set the width of the ground overlay to 50 meters. Adjust the value as needed.
                                        .image(carIcon)
                                        .anchor(0.5f, 0.5f); // Center the anchor of the ground overlay

                                GroundOverlay overlay = mMap.addGroundOverlay(overlayOptions);
                                rideDriverOverlays.add(overlay);
                            }
                            if ((ride.isCustomerEndsRide() == true && ride.isDriverEndsRide() == true) || (ride.isCustomerCancelsRide() == true) || (ride.isDriverCancelsRide() == true)) {
                                activeRide = false;
                                try {
                                    endRide = false;
                                    cancelRide = false;
                                    toggleRideButtons();
                                    mMap.clear();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                //inseamna ca el a anulat, urmeaza un mesaj de cofnrimare ca a fost anulat si i-au fost trasi banii.
                            }
                        } else {
                            //selectedDriver=new DriverDTO();
                            activeRide = false;
                            try {
                                endRide = false;
                                cancelRide = false;
                                toggleRideButtons();
                                mMap.clear();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getContext(), "Request rejected!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //selectedDriver=new DriverDTO();
                        activeRide = false;
                        endRide = false;
                        cancelRide = false;
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "checkCurrentRide error: " + response.code() + "+" + response.message(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }

                @Override
                public void onFailure(Call<RideDTO> call, Throwable t) {
                    activeRide = false;
                    try {
                        endRide = false;
                        cancelRide = false;
                        toggleRideButtons();
                        mMap.clear();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

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
                    //Toast.makeText(getContext(), "checkCurrentRide, Status code: " + statusCode + ", Message: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        //selectedDriver=new DriverDTO();
//        cancelRide=false;
//        endRide=false;
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
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show())
                                    .addOnSuccessListener(location -> {
                                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f));

                                    });
                            return true;
                        });
                        //set location button
                        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1"))
                                .getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        //place it to right bottom
//                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
//                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
//                        params.setMargins(0,0,0,50);
                        setMarker();
                        //check the buttons

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getContext(), "Permission " + permissionDeniedResponse.getPermissionName() + "" +
                                " was denied!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();

        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.uber_maps_style));
            if (!success) {
                Log.e("ERROR", "Style parsing error");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("ERROR", e.getMessage());
        }

        //continuously calling the onmapridedriver endpoint to check if there is a ride active
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                //onMapRideDriver();
                checkCurrentRide();
                handler.postDelayed(this, 5000); // Schedule the Runnable to run again after 1 second
            }
        };

        // Start the continuous execution of onMapRideDriver()
        handler.postDelayed(runnable, 0); // Start immediately
    }

    private void iniViews(View root) {
        ButterKnife.bind(this, root);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}