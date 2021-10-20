package com.example.wifi_p2p.Adapter;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wifi_p2p.R;

import java.util.List;

public class WifiPeerListAdapter extends RecyclerView.Adapter<WifiPeerListAdapter.DeviceViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private List<WifiP2pDevice> peers;
    public AdapterClickListener adapterClickListener;

    public WifiPeerListAdapter(Context context, List<WifiP2pDevice> peers, AdapterClickListener adapterClickListener) {
        this.context = context;
        inflater = (LayoutInflater.from(context));
        this.peers = peers;
        this.adapterClickListener = adapterClickListener;

    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = inflater.inflate(R.layout.device_list_item, parent, false);
        return new DeviceViewHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull  WifiPeerListAdapter.DeviceViewHolder holder, int position) {
        WifiP2pDevice device = peers.get(position);
        holder.tv_nameResponse.setText(device.deviceName);
        holder.tv_statusResponse.setText(getDeviceStatus(device.status));

        if(holder.tv_statusResponse.getText().toString().equalsIgnoreCase(context.getString(R.string.available))){
            holder.btnConnect.setText("Connect");
        }else if(holder.tv_statusResponse.getText().toString().equalsIgnoreCase(context.getString(R.string.connected))){
            holder.btnConnect.setText("Disconnect");
        }
    }

    @Override
    public int getItemCount() {
        return peers.size();
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_nameResponse;
        private TextView tv_statusResponse;
        private Button btnConnect;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_nameResponse = itemView.findViewById(R.id.tv_nameResponse);
            tv_statusResponse = itemView.findViewById(R.id.tv_statusResponse);
            btnConnect = itemView.findViewById(R.id.btnConnect);

            btnConnect.setOnClickListener(view -> {
                if(tv_statusResponse.getText().toString().equalsIgnoreCase(context.getString(R.string.available))){
                    adapterClickListener.configConnect(peers.get(getLayoutPosition()));
                }
                else if(tv_statusResponse.getText().toString().equalsIgnoreCase(context.getString(R.string.connected))){
                    adapterClickListener.configDisconnect(peers.get(getLayoutPosition()));
                }

            });
        }
    }

    public interface AdapterClickListener{
        void configConnect(WifiP2pDevice wifiP2pDevice);
        void configDisconnect(WifiP2pDevice wifiP2pDevice);
    }

    private String getDeviceStatus(int deviceStatus) {

        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return context.getString(R.string.available);
            case WifiP2pDevice.INVITED:
                return context.getString(R.string.invited);
            case WifiP2pDevice.CONNECTED:
                return context.getString(R.string.connected);
            case WifiP2pDevice.FAILED:
                return context.getString(R.string.failed);
            case WifiP2pDevice.UNAVAILABLE:
                return context.getString(R.string.unavailable);
            default:
                return context.getString(R.string.unknown);
        }
    }
}
