package com.example.wifi_p2p;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wifi_p2p.Adapter.WifiPeerListAdapter;
import com.example.wifi_p2p.AsyncTask.FileServerAsyncTask;
import com.example.wifi_p2p.BroadcastReceiver.WiFiDirectBroadcastReceiver;
import com.example.wifi_p2p.Data.SharedPrefs;
import com.example.wifi_p2p.Enum.FileType;
import com.example.wifi_p2p.Service.FileTransferService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

import static androidx.core.content.FileProvider.getUriForFile;

public class WiFiDirectActivity extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener {
    public static final String TAG = "BGWifiDirect";
    public static final String SENDER_TAG = "Sender";
    public static final String RECEIVER_TAG = "Receiver";
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1001;
    private static final int PERMISSION_REQUEST_CODE = 1;
    public static final String EXTRA_DATA_PATH = " com.example.wifi_p2p";
    private WifiP2pManager manager;
    private WifiManager wifiManager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private static ProgressDialog mProgressDialog;
    private RecyclerView recyclerView;
    private WifiPeerListAdapter mAdapter;
    private TextView tv_header;
    private LinearLayout loadingLayout;
    private PulsatorLayout pulsator;
    private Button btn_discover, btn_shareFile;
    public static boolean isSender;
    private WifiP2pInfo info;
    private LocationManager lm;
    public static boolean gps_enabled = false;
    private List<WifiP2pDevice> peers;
    private static final String MESSAGE_TAG = "com.example.wifi_p2p";
    private AlertDialog.Builder myAlertBuilder;

    private static WiFiDirectActivity activity;
    private Context context;
    private SharedPrefs sharedPrefs;



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(WiFiDirectActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(WiFiDirectActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(WiFiDirectActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(WiFiDirectActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean initP2p() {
        // Device capability definition check
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Log.e(TAG, "Wi-Fi Direct is not supported by this device.");
            return false;
        }
        // Hardware capability check
        if (wifiManager == null) {
            Log.e(TAG, "Cannot get Wi-Fi system service.");
            return false;
        }
        if (!wifiManager.isP2pSupported()) {
            Log.e(TAG, "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.");
            return false;
        }
        if (manager == null) {
            Log.e(TAG, "Cannot get Wi-Fi Direct system service.");
            return false;
        }
        channel = manager.initialize(this, getMainLooper(), null);
        if (channel == null) {
            Log.e(TAG, "Cannot initialize Wi-Fi Direct.");
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        mProgressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
        sharedPrefs = new SharedPrefs(getApplicationContext());
        peers = new ArrayList<>();
        context = WiFiDirectActivity.this;


        // Initializing views
        tv_header = findViewById(R.id.tv_header);
        btn_discover = findViewById(R.id.btn_discover);
        btn_shareFile = findViewById(R.id.btn_shareFile);
        recyclerView = findViewById(R.id.TestAppRecyclerView);
        loadingLayout = findViewById(R.id.loadingLayout);
        pulsator = findViewById(R.id.pulsator);
        activity = new WiFiDirectActivity();


        mAdapter = new WifiPeerListAdapter(this, peers, new WifiPeerListAdapter.AdapterClickListener() {
            @Override
            public void configConnect(WifiP2pDevice wifiP2pDevice) {
                connect(wifiP2pDevice);
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void configDisconnect(WifiP2pDevice wifiP2pDevice) {
                alertBuilder(wifiP2pDevice);
            }
        });
        Log.d(TAG, "onCreate: " + mAdapter);
        // Connect the adapter with the RecyclerView.
        recyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        if (!initP2p()) {
            finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    WiFiDirectActivity.PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
            // After this point you wait for callback in
            // onRequestPermissionsResult(int, String[], int[]) overridden method
        }

        // Discover button action
        btn_discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: Check if wifi and GPS are enabled, else display a dialog prompting the user to put them on

                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ex) {
                }
                if (!wifiManager.isWifiEnabled() && !gps_enabled) {
                    Toast.makeText(WiFiDirectActivity.this, "Turn On Wi-Fi and Location", Toast.LENGTH_SHORT).show();
                } else if (!gps_enabled) {
                    Toast.makeText(WiFiDirectActivity.this, "Turn On Location", Toast.LENGTH_SHORT).show();
                } else if (!wifiManager.isWifiEnabled()) {
                    Toast.makeText(WiFiDirectActivity.this, "Turn On Wi-fi", Toast.LENGTH_SHORT).show();
                } else {
                    loadingLayout.setVisibility(View.VISIBLE);
                    pulsator.start();
                    recyclerView.setVisibility(View.GONE);

                    //this time delay for just see animation
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            discoverPeers();
                            recyclerView.setVisibility(View.VISIBLE);
                            loadingLayout.setVisibility(View.GONE);
                        }
                    }, 2000);
                }
            }
        });

        // Share button action
        btn_shareFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("*/*");
                Log.d(SENDER_TAG, "Selected file to send");
            }
        });
    }

    private void alertBuilder(WifiP2pDevice wifiP2pDevice) {
        myAlertBuilder = new AlertDialog.Builder(WiFiDirectActivity.this);
        myAlertBuilder.setTitle("BG WIFI DIRECT");
        myAlertBuilder.setMessage("Do another transfer");
        myAlertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        myAlertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                disconnect(wifiP2pDevice);
                btn_shareFile.setVisibility(View.GONE);
                finish();
            }
        });
        myAlertBuilder.show();
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
            } else {
                requestPermission(); // Code for permission
            }
        } else {

            // Code for Below 23 API Oriented Device
            // Do next code
        }
        registerReceiver(receiver, intentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    // Update device name
    public void updateConnectedDevice(WifiP2pDevice device) {
        tv_header.setText(device.deviceName);
        if (peers.size() != 0) {
            peers.set(0, device);
            mAdapter.notifyDataSetChanged();
        }
    }


    // Initiate peer discovery
    public void discoverPeers() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Discovery Failed" + reason, Toast.LENGTH_SHORT).show();

            }
        });
    }


    // Initiate connection between devices
    public void connect(WifiP2pDevice wifiP2pDevice) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiP2pDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        // sets device that clicks connect button first as SENDER
        config.groupOwnerIntent = 0;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connection failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Disconnect devices
    private void disconnect(WifiP2pDevice wifiP2pDevice) {
        deletePersistentGroups();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // set recycler view to gone
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Disconnect failed. Reason :" + reason);
            }
        });
    }

    // Delete persistent group after disconnection
    private void deletePersistentGroups() {
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(manager, channel, netid, null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ConnectionInfoListener
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        this.info = info;

        tv_header.setText(tv_header.getText() + " [ " + ((info.isGroupOwner == true) ? "RECEIVER"
                : "SENDER") + " ]");

        if (info.groupFormed && info.isGroupOwner) {
            //The receiver block
            Toast.makeText(WiFiDirectActivity.this, "This device can only receive files", Toast.LENGTH_LONG).show();
            // Perform Async Task
            FileServerAsyncTask asyncTask = (FileServerAsyncTask) new FileServerAsyncTask(WiFiDirectActivity.this, new FileServerAsyncTask.AsyncResponse() {

                @Override
                public void processFinish(String result) {
                    //Here you will receive the result fired from async class
                    //of onPostExecute(result) method.
                    Intent filePathIntent = new Intent();
                    filePathIntent.putExtra(EXTRA_DATA_PATH, result);
                    setResult(RESULT_OK, filePathIntent);
                    finish();
                }
            }).execute();

        }
        else if (info.groupFormed) {
            //The sender block
            // create instance of Shared preference to receive stored data
            SharedPrefs sharedPrefs = new SharedPrefs(WiFiDirectActivity.this);

            // Save stored preferences to variables
            Log.d(SENDER_TAG, "build: Receiving Shared preference 'FILE PATH'");
            String receivedContentUri = sharedPrefs.getKeyFilePath();
            String getFileType = sharedPrefs.getKeyFileType();
            Log.d(SENDER_TAG, "build: Shared preference received... Content Uri: " + receivedContentUri);
            Log.d(SENDER_TAG, "build: Shared preference received... FileType: " + getFileType);


            if (getFileType.equals(FileType.TABLE.toString())) {
                Log.d(SENDER_TAG, "build: Selected File Type is Database");
                Log.d(SENDER_TAG, "build: Converting File Path: " + receivedContentUri + " to Uri");


                // Write received filepath to a File to convert from file Uri to a content Uri
                Uri contentUri = Uri.parse(receivedContentUri);

                Log.d(SENDER_TAG, "onConnectionInfoAvailable: Received Content Uri of file Path: " + contentUri.toString());
                Log.d(SENDER_TAG, "build: Starting intent Service.");
                beginTransfer(contentUri);
            } else if(getFileType.equals(FileType.FILE.toString())) {
                btn_shareFile.setVisibility(View.VISIBLE);

            }
        }

    }

    // Tell device what to do with intent result
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    if (uri == null) {
                        Log.d(SENDER_TAG, "beginTransfer: uri is null ");
                    } else {
                        Log.d(SENDER_TAG, "onActivityResult: Uri from selection " + uri.toString());
                        beginTransfer(uri);
                    }
                }
            });


    private void beginTransfer(Uri uri) {
        String Extension = "";
        String type = "";
        String actualFileLength = "";

        /*
         * Get the file's content URI from the incoming Intent,
         * then query the server app to get the file's display name
         * and size.
         */
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        /*
         * Get the column indexes of the data in the Cursor,
         * move to the first row in the Cursor, get the data,
         * and display it.
         */
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();

        Extension = cursor.getString(nameIndex);
        type = getContentResolver().getType(uri);
        actualFileLength = cursor.getString(sizeIndex);


        // Transfer data using Intent Service
        Log.d(SENDER_TAG, "Intent----------- " + uri);
        Intent serviceIntent = new Intent(WiFiDirectActivity.this, FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(FileTransferService.Extension, Extension);
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
        serviceIntent.putExtra(FileTransferService.TYPE, type);
        serviceIntent.putExtra(FileTransferService.FileLength, actualFileLength);
        Log.d(SENDER_TAG, "Attempting to start Intent");
        Log.d(SENDER_TAG, "File path selected: " + uri.toString() + "\n Host Address: " + info.groupOwnerAddress.getHostAddress());
        Log.d(SENDER_TAG, "Extension: " + Extension + "\n File Type: " + type + "\n File length: " + actualFileLength);
        Log.d(SENDER_TAG, "Now we start the service");
        startService(serviceIntent);
    }


    public static void showProgressBar(final String title) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.setMessage(title);
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgressNumberFormat(null);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.show();
                //}
            }
        });
    }

    // PeerListListener
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peersList) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peersList.getDeviceList());
        Toast.makeText(WiFiDirectActivity.this, "Updating Devices", Toast.LENGTH_SHORT).show();
        recyclerView.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
        if (peers.size() == 0) {
            isSender = false;
        }
        mAdapter.notifyDataSetChanged();
        if (peers.size() == 0) {
            Toast.makeText(WiFiDirectActivity.this, "No devices found", Toast.LENGTH_SHORT).show();
        }
    }


    public void copyReceivedFile(InputStream inputStream, OutputStream out, Long actualFileLength) {
        Log.d(RECEIVER_TAG, "Inside copyReceivedFile function");

        byte buf[] = new byte[1024];
        int len;
        long total = 0;
        int progressPercentage = 0;

        try {
            Log.d(RECEIVER_TAG, "About to begin writing file. receiver");
            while ((len = inputStream.read(buf)) != -1) {
                try {
                    out.write(buf, 0, len);
                    Log.d(RECEIVER_TAG, "File write successful. receiver");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                try {
                    total += len;
                    if (actualFileLength > 0) {
                        progressPercentage = (int) ((total * 100) / actualFileLength);
                    }
                    showProgressBar("Receiving File....");
                    mProgressDialog.setProgress(progressPercentage);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mProgressDialog != null) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                }

            }
            // dismiss progress after sending
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            out.close();
            inputStream.close();
            Log.d(RECEIVER_TAG, "Receiving should be done here, closing out all streams");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFileToSend(InputStream inputStream, OutputStream out, Long actualFileLength) {
        Log.d(SENDER_TAG, "Inside copyFileToSend function");

        byte buf[] = new byte[1024];
        int len;
        long total = 0;
        int progressPercentage = 0;
        try {
            Log.d(SENDER_TAG, "About to begin writing file");
            while ((len = inputStream.read(buf)) != -1) {
                try {
                    out.write(buf, 0, len);
                    Log.d(SENDER_TAG, "File write successful");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                try {
                    total += len;
                    if (actualFileLength > 0) {
                        progressPercentage = (int) ((total * 100) / actualFileLength);
                    }
                    showProgressBar("Sending File....");
                    mProgressDialog.setProgress(progressPercentage);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mProgressDialog != null) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                }
            }

            // dismiss progress after sending
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            out.close();
            inputStream.close();
            Log.d(SENDER_TAG, "Sending should be done here, closing out all streams");

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}
