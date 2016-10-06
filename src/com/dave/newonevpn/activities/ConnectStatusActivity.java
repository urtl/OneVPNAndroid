package com.dave.newonevpn.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dave.newonevpn.api.CurrentIPAPI;
import com.dave.newonevpn.model.Global;
import com.dave.newonevpn.netutil.CommonAsyncTask;
import com.dave.newonevpn.netutil.WebServiceClient;
import com.dave.onevpnfresh.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.newonevpn.vpn.ConfigConverter;
import com.newonevpn.vpn.LaunchVPNManager;
import com.newonevpn.vpn.core.OpenVpnService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by gustaf on 11/2/15.
 */
public class ConnectStatusActivity extends Activity implements View.OnClickListener, OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    boolean showPassword = false;
    private CommonAsyncTask mAsyncTask = null;
    private JSONObject objRes;
    GoogleMap mMap;
    ImageView imgMenu;
    Location l;
    TextView txtIpAddress, txtConnectStatus;
    ProgressBar progress;
    LinearLayout linearConnect;
    ImageView imgConnectStatus;
    TextView txtConnectLabel;
    PopupWindow popupMenu, popupLogin, popupExit, popupForgot;
    private final int interval = 50; // 1 Second
    private int nPos = 0;
    private boolean bConnecting = false;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {
            if (bConnecting) {
                if (nPos > 99) nPos = 0;
                nPos++;
                progress.setProgress(nPos);
                handler.postAtTime(runnable, System.currentTimeMillis() + interval);
                handler.postDelayed(runnable, interval);
            }
        }
    };
    private ConfigConverter pConverter;
    private LaunchVPNManager pLaunchVPN;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_status);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        imgMenu = (ImageView) findViewById(R.id.imgMenu);
        txtIpAddress = (TextView) findViewById(R.id.txtIpAddress);
        txtConnectStatus = (TextView) findViewById(R.id.txtConnectStatus);
        progress = (ProgressBar) findViewById(R.id.progress);
        linearConnect = (LinearLayout) findViewById(R.id.linearConnect);
        imgConnectStatus = (ImageView) findViewById(R.id.imgConnectStatus);
        txtConnectLabel = (TextView) findViewById(R.id.txtConnectLabel);
        progress.setProgress(0);

        imgMenu.setOnClickListener(this);
        linearConnect.setOnClickListener(this);

        pLaunchVPN = new LaunchVPNManager(this);
        pConverter = new ConfigConverter(this, pLaunchVPN);

        getIpAddress();

        if (OpenVpnService.getManagement() == null) {
            Global.ed.putBoolean("Connect", false);
            Global.ed.commit();

            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey("auto")) {
                clickconnect();
            }
        } else {
            if (Global.sp.getBoolean("Connect", false)) {
                txtConnectStatus.setText("Connected!");
                progress.setProgress(100);
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_disconnect));
                txtConnectLabel.setText("TAP TO DISCONNECT");
                imgMenu.setVisibility(View.GONE);
            } else {
                txtConnectStatus.setText("Not Connected");
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_connect));
                txtConnectLabel.setText("TAP TO CONNECT");
                imgMenu.setVisibility(View.VISIBLE);
            }
        }
        setResult(RESULT_CANCELED);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


    }
    @Override
    public void onResume() {
        super.onResume();
        getIpAddress();
    }
    @Override
    public void onClick(View v) {
        if (v == imgMenu) {
            showMenuPopup();
        } else if (v == linearConnect) {
            clickconnect();
        }
    }

    public void showLoginPopup() {
        View parent = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_credential, null);
        final EditText edtEmail = (EditText) parent.findViewById(R.id.edtEmail);
        final EditText edtPassword = (EditText) parent.findViewById(R.id.edtPassword);

        edtPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    float f1 = event.getRawX() - 25;
                    float f2 = (edtPassword.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width());
                    if (f1 <= f2) {
                        // your action here
                        if (!showPassword) {
                            showPassword = true;
                            edtPassword.setTransformationMethod(null);
                        } else {
                            showPassword = false;
                            edtPassword.setTransformationMethod(new PasswordTransformationMethod());
                        }

                    }
                }
                return false;
            }
        });

        LinearLayout linearLogin = (LinearLayout) parent.findViewById(R.id.linearLogin);
        final CheckBox chkRememberMe = (CheckBox) parent.findViewById(R.id.chkRememberMe);
        TextView txtForgetPassword = (TextView) parent.findViewById(R.id.txtForgetPassword);
        TextView txtSignUp = (TextView) parent.findViewById(R.id.txtSignUp);

        if (Global.sp.getBoolean("chkRememberMe", false)) {
            edtEmail.setText(Global.sp.getString("email", ""));
            edtPassword.setText(Global.sp.getString("password", ""));
            chkRememberMe.setChecked(true);
        } else {
            chkRememberMe.setChecked(false);
        }

        linearLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtEmail.getText().length() == 0) {
                    Toast.makeText(ConnectStatusActivity.this, "Input Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edtPassword.getText().length() == 0) {
                    Toast.makeText(ConnectStatusActivity.this, "Input Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (chkRememberMe.isChecked()) {
                    Global.ed.putBoolean("chkRememberMe", true);
                } else {
                    Global.ed.putBoolean("chkRememberMe", false);
                }
                Global.ed.putString("email", edtEmail.getText().toString());
                Global.ed.putString("password", edtPassword.getText().toString());
                Global.ed.commit();


                popupLogin.dismiss();


                bConnecting = true;
                nPos = 0;
                progress.setProgress(nPos);
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_cancel));
                txtConnectLabel.setText("TAP TO CANCEL");
                txtConnectStatus.setText("Connecting...");
                handler.postAtTime(runnable, System.currentTimeMillis() + interval);
                handler.postDelayed(runnable, interval);

                imgMenu.setVisibility(View.GONE);
                Lock(false);

                ReceiveBroadCast();
                tryConnect();


            }
        });

        chkRememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (chkRememberMe.isChecked()) {
                    Global.ed.putBoolean("chkRememberMe", true);
                    Global.ed.putString("email", edtEmail.getText().toString());
                    Global.ed.putString("password", edtPassword.getText().toString());
                    Global.ed.commit();
                } else {
                    Global.ed.putBoolean("chkRememberMe", false);
                    Global.ed.commit();
                }
            }
        });

        txtForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupLogin.dismiss();
                showForgetDialog(false);
            }
        });

        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupLogin.dismiss();
                showSignUpDialog();
            }
        });


        popupLogin = new PopupWindow(parent, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
        popupLogin.setOutsideTouchable(true);
        popupLogin.setTouchable(true);
        popupLogin.setBackgroundDrawable(new BitmapDrawable());
        popupLogin.setAnimationStyle(R.style.PopupAnimation);
        popupLogin.update();
        popupLogin.showAtLocation(imgMenu, Gravity.CENTER, 0, 0);
    }

    public void showForgetDialog(boolean bChange) {
        View parent = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_forgot, null);
        TextView txtDesc = (TextView) parent.findViewById(R.id.txtDesc);
        if (bChange) {
            txtDesc.setText("You've Already Claimed Your Free Account. For further details contact support at www.onevpn.com");
        }
        Button btnOK = (Button) parent.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupForgot.dismiss();
            }
        });


        popupForgot = new PopupWindow(parent, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
        popupForgot.setOutsideTouchable(true);
        popupForgot.setTouchable(true);
        popupForgot.setBackgroundDrawable(new BitmapDrawable());
        popupForgot.setAnimationStyle(R.style.PopupAnimation);
        popupForgot.update();
        popupForgot.showAtLocation(imgMenu, Gravity.CENTER, 0, 0);
    }
    public void showSignUpDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set title
        alertDialogBuilder.setTitle("Alert");
        // set dialog message
        alertDialogBuilder.setMessage("You can claim FREE Account only once. Do you wish to continue?").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                dialog.dismiss();
                                checkSignUp();
                                // current activity
                            }
                        }
                )
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                dialog.dismiss();
                                // current activity
                            }
                        }
                );

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    public void checkSignUp() {
        mAsyncTask = new CommonAsyncTask(this, true, new CommonAsyncTask.asyncTaskListener() {
            @Override
            public Boolean onTaskExecuting() {
                try {

                    ArrayList<NameValuePair> postParameters = null;
                    postParameters = new ArrayList<NameValuePair>();

                    postParameters.add(new BasicNameValuePair("ieme", Global.deviceId));
                    WebServiceClient wsClient = new WebServiceClient(ConnectStatusActivity.this);
                    String str = wsClient.sendDataToServer(Global.SERVER_URL + "check_ieme.php", postParameters);
                    objRes = new JSONObject(wsClient.sendDataToServer(Global.SERVER_URL + "check_ieme.php", postParameters));
                    if (objRes == null) return false;
                } catch (Exception e) {
                    return false;
                }
                return true;
            }

            @Override
            public void onTaskFinish(Boolean result) {
                if (result == true) {
                    try {
                        if (objRes.getString("status").equalsIgnoreCase("1")) {

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ConnectStatusActivity.this);
                            alertDialogBuilder.setTitle("Alert");
                            alertDialogBuilder.setMessage("You already have claimed your free trial, please upgrade!!!").setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    // if this button is clicked, close
                                                    dialog.dismiss();
                                                }
                                            }
                                    );

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                        } else {
                            Intent intent = new Intent(ConnectStatusActivity.this, SignUpActivity.class);
                            startActivityForResult(intent, 2);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(ConnectStatusActivity.this, "JSON Parse Error!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(ConnectStatusActivity.this, "Server Connection Timeout!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAsyncTask.execute();
    }

    public void showMenuPopup() {
        View parent = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_menu, null);
        LinearLayout linearMenuUniqueID = (LinearLayout) parent.findViewById(R.id.linearMenuUniqueID);
        linearMenuUniqueID.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      popupMenu.dismiss();
                                                      Global.alertDialog(ConnectStatusActivity.this, Global.deviceId);
                                                  }
                                              }
        );
        LinearLayout linearChooseServer = (LinearLayout) parent.findViewById(R.id.linearMenuChooseServer);
        LinearLayout linearAbout = (LinearLayout) parent.findViewById(R.id.linearMenuAbout);
        LinearLayout linearProfile = (LinearLayout) parent.findViewById(R.id.linearMenuProfile);
        LinearLayout linearFAQ = (LinearLayout) parent.findViewById(R.id.linearMenuFAQ);
        LinearLayout linearShare = (LinearLayout) parent.findViewById(R.id.linearMenuShare);
        LinearLayout linearUpgrade = (LinearLayout) parent.findViewById(R.id.linearMenuUpgrade);
        LinearLayout linearSetting = (LinearLayout) parent.findViewById(R.id.linearMenuSetting);
        LinearLayout linearExit = (LinearLayout) parent.findViewById(R.id.linearMenuExit);

        linearChooseServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                setResult(RESULT_CANCELED);
                ConnectStatusActivity.this.finish();
            }
        });
        linearSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConnectStatusActivity.this, SettingActivity.class);
                startActivityForResult(intent, 1);

                popupMenu.dismiss();
            }
        });
        linearAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                Intent intent = new Intent(ConnectStatusActivity.this, AboutActivity.class);
                startActivityForResult(intent, 0);

            }
        });
        linearProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://profile.onevpn.com"));
                startActivity(i);
            }
        });
        linearFAQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://faqs.onevpn.com"));
                startActivity(i);
            }
        });
        linearShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                String shareText = "http://www.facebook.com/TheOneVPN/?fref=ts";

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_SUBJECT, "OneVPN");
                share.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(share, "Share link!"));
            }
        });
        linearUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://upgrade.onevpn.com"));
                startActivity(i);
            }
        });
        linearExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                showExitPopup();
            }
        });

        popupMenu = new PopupWindow(parent, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
        popupMenu.setOutsideTouchable(true);
        popupMenu.setTouchable(true);
        popupMenu.setBackgroundDrawable(new BitmapDrawable());
        popupMenu.setAnimationStyle(R.style.PopupAnimation);
        popupMenu.update();
        popupMenu.setWidth(Global.dpToPx(150));
        popupMenu.setHeight(Global.dpToPx(300));
        popupMenu.showAtLocation(imgMenu, Gravity.RIGHT | Gravity.TOP, Global.dpToPx(2), Global.dpToPx(100));
    }

    public void showExitPopup() {
        View parent = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_exit, null);
        Button btnExitYes = (Button) parent.findViewById(R.id.btnExitYes);
        Button btnExitNo = (Button) parent.findViewById(R.id.btnExitNo);
        btnExitYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (OpenVpnService.getManagement() != null)
                    OpenVpnService.getManagement().stopVPN();
                if (OpenVpnService.mProcessThread != null)
                    OpenVpnService.mProcessThread.interrupt();

                popupExit.dismiss();
                setResult(RESULT_OK);
                ConnectStatusActivity.this.finish();
            }
        });

        btnExitNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupExit.dismiss();
            }
        });

        popupExit = new PopupWindow(parent, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
        popupExit.setOutsideTouchable(true);
        popupExit.setTouchable(true);
        popupExit.setBackgroundDrawable(new BitmapDrawable());
        popupExit.setAnimationStyle(R.style.PopupAnimation);
        popupExit.update();
        popupExit.showAtLocation(imgMenu, Gravity.CENTER, 0, 0);
    }

    public String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    public void getIpAddress() {
        String ip = getLocalIpAddress();
        txtIpAddress.setText(ip);
        getCurrentIP();
    }

    private void getCurrentIP() {

        CurrentIPAPI api = new CurrentIPAPI(this, new CurrentIPAPI.OnTaskCompleted() {

            @Override
            public void onTaskCompleted(String response) {
                txtIpAddress.setText(response);
            }
        });
        api.setURL("http://ifcfg.me/ip");
        api.execute();


    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    System.out.println("ip1--:" + inetAddress);
                    // for getting IPV4 format
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {

                        String ip = inetAddress.getHostAddress().toString();
                        return ip;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }

    public void ReceiveBroadCast() {
        IntentFilter filter = new IntentFilter("com.newonevpn.vpn.VPN_STATUS");
        if (filter != null)
            this.registerReceiver(new Receiver(), filter);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        List<Address> addresses = null;
        List<LatLng> ll = null;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 4);
        } else {
            googleMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            final double[] longitudeeee = {myLocation.getLongitude()};
            final double[] latitudeeee = {myLocation.getLatitude()};
            final LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    longitudeeee[0] = location.getLongitude();
                    latitudeeee[0] = location.getLatitude();
                }
            };
            LatLng latLng = new LatLng(latitudeeee[0], longitudeeee[0]);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(4));
        }
    }

    public void showMarkerOntheServer() {
        List<Address> addresses = null;
        List<LatLng> ll = null;
        if (Geocoder.isPresent()) {
            try {
                Geocoder gc = new Geocoder(getBaseContext());
                if (Global.g_selectedServerInfo.city != null) {
                    addresses = gc.getFromLocationName(Global.g_selectedServerInfo.city + "," + Global.g_selectedServerInfo.country, 1); // get the found Address Objects
                } else {
                    addresses = gc.getFromLocationName(Global.g_selectedServerInfo.country, 1);
                }
                ll = new ArrayList<LatLng>(addresses.size());
                for (Address a : addresses) {
                    if (a.hasLatitude() && a.hasLongitude()) {
                        ll.add(new LatLng(a.getLatitude(), a.getLongitude()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (ll.size() > 0) {
                mMap.addMarker(new MarkerOptions().position(ll.get(0)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(ll.get(0)));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(4));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void returnToCurrentLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 4);
        } else {
            mMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            final double[] longitudeeee = {myLocation.getLongitude()};
            final double[] latitudeeee = {myLocation.getLatitude()};
            final LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    longitudeeee[0] = location.getLongitude();
                    latitudeeee[0] = location.getLatitude();
                }
            };
            LatLng latLng = new LatLng(latitudeeee[0], longitudeeee[0]);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(4));
    }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {


    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("ConnectStatus Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private class Receiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1 == null) return;
            if (arg1.getExtras() == null) return;

            String strStatus = arg1.getExtras().getString("status");
            String detailStatus = arg1.getExtras().getString("detailstatus");

            if (strStatus == null || detailStatus == null) return;

            if (detailStatus.equals("CONNECTING")) detailStatus = "connection in progress";
            else if (detailStatus.equals("WAIT")) detailStatus = "waiting for server reply";
            else if (detailStatus.equals("RECONNECTING")) detailStatus = "reconnecting";
            else if (detailStatus.equals("ASSIGN_IP")) detailStatus = "assigning ip addresses";
            else if (detailStatus.equals("RESOLVE")) detailStatus = "detecting server address";
            else if (detailStatus.equals("TCP_CONNECT"))
                detailStatus = "connection in progress(TCP)";
            else if (detailStatus.equals("AUTH")) detailStatus = "authentication";
            else if (detailStatus.equals("GET_CONFIG")) detailStatus = "loading sever parameters";
            else if (detailStatus.equals("ASSIGN_UP")) detailStatus = "setting up new connection";
            else if (detailStatus.equals("ADD_ROUTES")) detailStatus = "setting up secured routes";
            else if (detailStatus.equals("NETWORK")) detailStatus = "waiting for usable network";
            else if (detailStatus.equals("SCREENOFF")) detailStatus = "paused - screen off";
            else if (detailStatus.equals("USERPAUSE")) detailStatus = "pause requested by user";
            else if (strStatus.equals("LEVEL_CONNECTING_NO_SERVER_REPLY_YET"))
                detailStatus = "waiting for server reply";
            else if (strStatus.equals("LEVEL_CONNECTING_SERVER_REPLIED"))
                detailStatus = "secure connect";
            else if (strStatus.equals("EXITING"))
                detailStatus = "exiting";
            else
                detailStatus = "waiting";

            if (strStatus.equals("LEVEL_CONNECTED")) {
                bConnecting = false;
                Global.ed.putBoolean("Connect", true);
                Global.ed.commit();
                progress.setProgress(100);
                txtConnectStatus.setText("Connected!");

                try {
                    showMarkerOntheServer();
                }
                catch (NullPointerException e){
                    //Toast.makeText(ConnectStatusActivity.this,"Service Not Available",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_disconnect));
                txtConnectLabel.setText("TAP TO DISCONNECT");
                Lock(true);
                getIpAddress();
                imgMenu.setVisibility(View.GONE);
                ConnectStatusActivity.this.setResult(RESULT_CANCELED);

            } else if (strStatus.equals("LEVEL_NOTCONNECTED")) {
                bConnecting = false;
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_connect));
                txtConnectLabel.setText("TAP TO CONNECT");
                txtConnectStatus.setText("Not Connected");
                returnToCurrentLocation();
                imgMenu.setVisibility(View.VISIBLE);

                progress.setProgress(0);
            } else if (strStatus.equals("connection aborted")) {
                bConnecting = false;
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_connect));
                txtConnectLabel.setText("TAP TO CONNECT");
                txtConnectStatus.setText("Not Connected");
                returnToCurrentLocation();
                imgMenu.setVisibility(View.VISIBLE);

                progress.setProgress(0);
            } else if (strStatus.equals("DISCONNECTED")) {
                bConnecting = false;
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_connect));
                txtConnectLabel.setText("TAP TO CONNECT");
                txtConnectStatus.setText("Not Connected");
                returnToCurrentLocation();
                imgMenu.setVisibility(View.VISIBLE);
                progress.setProgress(0);
            } else if (arg1.getExtras().getString("detailstatus").equals("NEXTOVPNTRY")) {

            }
        }
    }

    public void tryConnect() {
        pConverter.StartConfigConverter();
    }

    public void clickconnect() {
        if (txtConnectLabel.getText().toString().equals("TAP TO CONNECT") == false) {
            bConnecting = false;
            nPos = 0;
            progress.setProgress(nPos);
            imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_connect));
            txtConnectLabel.setText("TAP TO CONNECT");
            txtConnectStatus.setText("Not Connected");

            imgMenu.setVisibility(View.VISIBLE);
            Global.ed.putBoolean("Connect", false);
            Global.ed.commit();
            if (OpenVpnService.getManagement() != null)
                OpenVpnService.getManagement().stopVPN();
            if (OpenVpnService.mProcessThread != null)
                OpenVpnService.mProcessThread.interrupt();
            getIpAddress();
        } else {
            showLoginPopup();
        }
    }

    public void Lock(boolean bLock) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        } else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                // this will change the status of registerd device to 1 from 0
                String result = data.getStringExtra("result");
                if (result.equalsIgnoreCase("success"))
                    changeStatus();
            }
        } else {
            if (resultCode == RESULT_OK) {
                tryConnect();
                //   ReceiveBroadCast();
            }
        }
    }
    private void changeStatus() {
        mAsyncTask = new CommonAsyncTask(this, true, new CommonAsyncTask.asyncTaskListener() {
            @Override
            public Boolean onTaskExecuting() {
                try {
                    ArrayList<NameValuePair> postParameters = null;
                    postParameters = new ArrayList<NameValuePair>();
                    postParameters.add(new BasicNameValuePair("imei", Global.deviceId));
                    postParameters.add(new BasicNameValuePair("status", "1"));
                    WebServiceClient wsClient = new WebServiceClient(ConnectStatusActivity.this);
                    String response = wsClient.sendDataToServer("http://webservice.onevpn.com/onevpn/webservice/update_imei.php", postParameters);
                    objRes = new JSONObject(response);
                    if (objRes == null) return false;
                } catch (Exception e) {
                    return false;
                }
                return true;
            }

            @Override
            public void onTaskFinish(Boolean result) {
                if (result == true) {
                    try {
//                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SignUpActivity.this);
                        if (objRes.getBoolean("message")) {
                        } else {
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }
        });
        mAsyncTask.execute();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 4:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                else
                {
                    Toast.makeText(ConnectStatusActivity.this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
