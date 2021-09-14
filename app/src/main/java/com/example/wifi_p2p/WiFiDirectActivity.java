package com.example.wifi_p2p;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wifi_p2p.Adapter.WifiPeerListAdapter;
import com.example.wifi_p2p.BroadcastReceiver.WiFiDirectBroadcastReceiver;
import com.example.wifi_p2p.Model.DataModel;
import com.example.wifi_p2p.Service.FileTransferService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class WiFiDirectActivity extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener {
    public static final String TAG = "BGWifiDirect";
    public static final String SENDER_TAG = "Sender";
    public static final String RECEIVER_TAG = "Receiver";
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1001;
    private static final int PERMISSION_REQUEST_CODE = 1;
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
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
    private Button btn_discover;
    public static boolean isSender;
    private WifiP2pInfo info;
    private List<WifiP2pDevice> peers;


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

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        mProgressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);

        peers = new ArrayList<>();


        // Initializing views
        tv_header = findViewById(R.id.tv_header);
        btn_discover = findViewById(R.id.btn_discover);
        recyclerView = findViewById(R.id.recycleView);
        loadingLayout = findViewById(R.id.loadingLayout);
        pulsator = findViewById(R.id.pulsator);

        mAdapter = new WifiPeerListAdapter(getApplicationContext(), peers, new WifiPeerListAdapter.AdapterClickListener() {
            @Override
            public void configConnect() {
                connect();
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void configDisconnect() {
                disconnect();
            }
        });
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
                if (wifiManager.isWifiEnabled()) {
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
                        }
                    }, 2000);
                } else {
                    Toast.makeText(WiFiDirectActivity.this, "Turn On Wi-fi First", Toast.LENGTH_SHORT).show();
                }

            }
        });
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
    public void connect() {
        WifiP2pDevice device = peers.get(0);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
    private void disconnect() {
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
//        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
        this.info = info;

        tv_header.setText(tv_header.getText() + " [ " + ((info.isGroupOwner == true) ? "RECEIVER"
                : "SENDER") + " ]");

        if (info.groupFormed && info.isGroupOwner) {
            Toast.makeText(WiFiDirectActivity.this, "This device can only receive files", Toast.LENGTH_LONG).show();
            // Perform Async Task
            new FileServerAsyncTask(WiFiDirectActivity.this).execute();
        } else if (info.groupFormed) {
        }
    }

    /**
     * inflates the action bar menu on main activity
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    /**
     * Method to do an action when menu item button is clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_share:
                // Allow user to pick an image from Gallery or other
                // registered apps
                mGetContent.launch("*/*");
                Log.d(SENDER_TAG, "Selected file to send");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // Tell device what to do with intent result
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
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
                    startService(serviceIntent);
                }
            });

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

    public void hideProgress(ProgressDialog mProgressDialog) {

        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
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

    // Server thread
    public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {
        private Context context;

        public FileServerAsyncTask(Context context) {
            this.context = context;

        }

        @Override
        protected void onPreExecute() {
            Log.v("copied ", "----> " + "Opening a server socket");
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.d(RECEIVER_TAG, "Receiving file started");
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");
                InputStream inputstream = client.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(
                        inputstream);
                DataModel dataModel;
                dataModel = (DataModel) ois.readObject();


                String storage_state = Environment.getExternalStorageState();
                if (storage_state.equals(Environment.MEDIA_MOUNTED)) {
                    String fileName = dataModel.getFileName();
                    Long actualFileLength = dataModel.getFileLength();

                    Log.d(RECEIVER_TAG, "Creating BG folder in internal storage");

                    final File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/BabbanGona"),
                            fileName);


                    File dirs = new File(f.getParent());
                    if (!dirs.exists())
                        dirs.mkdirs();
                    f.createNewFile();

                    Log.d(RECEIVER_TAG, "Folder created in internal storage");


                    Log.d(RECEIVER_TAG, "server: copying received files " + f.toString());

                    copyReceivedFile(inputstream, new FileOutputStream(f), actualFileLength);
                    ois.close();
                    serverSocket.close();

                    return f.getAbsolutePath();
                }
            } catch (IOException | ClassNotFoundException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            }
            Log.d(RECEIVER_TAG, "server: Files copied");
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(WiFiDirectActivity.this, "File Received\n" + result, Toast.LENGTH_LONG).show();
                Intent intent=new Intent(context, SharedFilesListActivity.class);
                context.startActivity(intent);

            }
        }
    }

    public void copyReceivedFile(InputStream inputStream, OutputStream out, Long actualFileLength) {
        byte buf[] = new byte[1024];
        int len;
        long total = 0;
        int progressPercentage = 0;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                try {
                    out.write(buf, 0, len);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
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
                    // TODO: handle exception
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFileToSend(InputStream inputStream, OutputStream out, Long actualFileLength) {
        byte buf[] = new byte[1024];
        int len;
        long total = 0;
        int progressPercentage = 0;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                try {
                    out.write(buf, 0, len);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
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
                    // TODO: handle exception
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
