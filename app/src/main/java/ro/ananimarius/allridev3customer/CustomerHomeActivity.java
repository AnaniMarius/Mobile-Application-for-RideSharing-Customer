package ro.ananimarius.allridev3customer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import ro.ananimarius.allridev3customer.Common.UnsafeOkHttpClient;
import ro.ananimarius.allridev3customer.databinding.ActivityDriverHomeBinding;

public class CustomerHomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDriverHomeBinding binding;
    String phoneNumber = null;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverHomeBinding.inflate(getLayoutInflater());
        if (binding != null) {
            setContentView(binding.getRoot());
        }
        setSupportActionBar(binding.appBarDriverHome.toolbar);

        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setOpenableLayout(drawer)
                .build();

        //here is used the HOMEFRAGMENT.JAVA
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        if (account != null) {
//            TextView nameTxt = (TextView)findViewById(R.id.nameText);
//            nameTxt.setText(account.getGivenName()); //GIVES RUNTIME ERROR
//        }


        Bundle bundle = new Bundle();
        bundle.putString("googleId", account.getId());
        bundle.putString("email", account.getEmail());
        bundle.putString("firstName", account.getFamilyName());
        bundle.putString("lastName", account.getGivenName());
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_driver_home);
        navController.setGraph(R.navigation.mobile_navigation, bundle);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item selection here
                return true;
            }
        });
        View header= navigationView.getHeaderView(0);
        TextView txtName = header.findViewById(R.id.txt_name);
        TextView txtStar = header.findViewById(R.id.txt_star);
        TextView txtPhone = header.findViewById(R.id.txt_phone);

        txtStar.setText("3.5"); // Replace with the actual value
        txtName.setText(account.getFamilyName()+" "+account.getGivenName()); // Replace with the actual value
        txtPhone.setText("0730657538");


        init();
    }

    private String getAuthTokenCookie(){ //SEARCH FOR THE COOKIE TO BE SENT TO THE API
        CookieManager cookieManagerCheck = CookieManager.getInstance();
        String cookie = cookieManagerCheck.getCookie("http://192.168.1.219:8080");
        if (cookie != null) {
            //the cookie exists
            Log.d("COOKIE_GET", "authToken value: " + cookie);
            //Toast.makeText(getApplicationContext(), "Cookie found ", Toast.LENGTH_SHORT).show();
        } else {
            //the cookie does not exist
            Log.d("COOKIE_GET", "authToken cookie not found");
            //Toast.makeText(getApplicationContext(), "Cookie not found ", Toast.LENGTH_SHORT).show();
        }
        return cookie;
    }

    private void deleteCookie(){ //delete cookie from the client
        CookieManager cookieManagerCheck = CookieManager.getInstance();
        String cookie = cookieManagerCheck.getCookie("http://192.168.1.219:8080");
        if (cookie != null) {
            // The cookie exists
            Log.d("COOKIE_DELETED", "authToken value: " + cookie);
            //Toast.makeText(getApplicationContext(), "Cookie found has been deleted ", Toast.LENGTH_SHORT).show();

            //delete the cookie
            cookieManagerCheck.setCookie("http://192.168.1.219:8080", "authToken=;expires=Thu, 01 Jan 1970 00:00:00 GMT");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManagerCheck.flush();
            } else {
                CookieSyncManager.createInstance(this);
                CookieSyncManager.getInstance().sync();
            }
        } else {
            //the cookie does not exist
            Log.d("COOKIE_DELETED", "authToken cookie not found");
            //Toast.makeText(getApplicationContext(), "Cookie not found ", Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        //here we initialize the signout button
        navigationView.setNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_sign_out) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomerHomeActivity.this);
                builder.setTitle("Sign out")
                        .setMessage("Confirm to sign out")
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                        .setPositiveButton("Sign Out", (dialogInterface, i) -> {
                            //revoke access token before signing out
                            GoogleSignInClient signInClient = GoogleSignIn.getClient(CustomerHomeActivity.this,
                                    GoogleSignInOptions.DEFAULT_SIGN_IN);
                            signInClient.revokeAccess().addOnCompleteListener(task -> {
                                //make an HTTP request to the signout endpoint of the API
                                new SignOutTask().execute();
                            });
                        })
                        .setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(dialogInterface -> {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(getResources().getColor(R.color.colorAccent));
                });
                dialog.show();
            }
            return true;
        });
    }

    public interface APIInterface {
        @POST("user/signout")
        Call<JsonObject> signout(@Body String authToken);
    }
    class SignOutTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            String authToken = getAuthTokenCookie();
            // try to parse
            String authTokenParsed = null;
            try {
                JSONObject jsonObject = new JSONObject(authToken.substring(9));
                authTokenParsed = jsonObject.getString("authToken");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl("http://192.168.1.219:8080/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create());
            Retrofit retrofit = builder.build();
            APIInterface api = retrofit.create(APIInterface.class);
            Call<JsonObject> call = api.signout(authTokenParsed); // Assuming your APIInterface has a "signout" method

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    int responseCode = response.code();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        deleteCookie();
                        onPostExecute(true);
                    } else {
                        onPostExecute(false);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    t.printStackTrace();
                    onPostExecute(false);
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // handle the result of the API request
            if (result != null && result.booleanValue()) {
                Intent intent = new Intent(CustomerHomeActivity.this, SplashScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                // handle unsuccessful response
            }
        }

    }

//    public void getPhoneNumber(){
//        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        if (telephonyManager != null) {
//            Dexter.withContext(getApplicationContext())
//                    .withPermission(Manifest.permission.READ_PHONE_STATE).withListener(new PermissionListener() {
//                        @Override
//                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
//                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                                return;
//                            }
//                            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
//                            //String phoneNumber = telephonyManager.getLine1Number();
//
//                            phoneNumber = telephonyManager.getLine1Number();
//                            if (phoneNumber != null && !phoneNumber.isEmpty()) {
//                                Log.d("Phone Number", phoneNumber);
//                            }
//                        }
//
//                        @Override
//                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
//                            Toast.makeText(getApplicationContext(),"Permission "+permissionDeniedResponse.getPermissionName()+""+
//                                    " was denied!", Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
//
//                        }
//                    }).check();
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//                //phoneNumber = telephonyManager.getLine1Number();
//                telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
//                phoneNumber = telephonyManager.getLine1Number();
//                if (phoneNumber != null && !phoneNumber.isEmpty()) {
//                    Log.d("Phone Number", phoneNumber);
//                } else {
//                    Log.d("Phone Number", "Not available");
//                }
//            } else {
//                Log.d("Phone Number", "Permission not granted");
//            }
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_driver_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    protected void onDestroy() {
        new SignOutTask().execute();
        super.onDestroy();
    }
    @Override
    protected void onStop() {
        new SignOutTask().execute();
        super.onStop();
    }
}