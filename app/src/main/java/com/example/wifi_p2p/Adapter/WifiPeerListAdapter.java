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

        if(holder.tv_statusResponse.getText().toString().equalsIgnoreCase("Available")){
            holder.btnConnect.setText("Connect");
        }else if(holder.tv_statusResponse.getText().toString().equalsIgnoreCase("Connected")){
            holder.btnConnect.setText("Disconnect");
        }

        holder.btnConnect.setOnClickListener(view -> {
            if(holder.tv_statusResponse.getText().toString().equalsIgnoreCase("Available")){
                adapterClickListener.configConnect();
            }
            else if(holder.tv_statusResponse.getText().toString().equalsIgnoreCase("Connected")){
                adapterClickListener.configDisconnect();
            }

        });
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
        }
    }

    public interface AdapterClickListener{
        void configConnect();
        void configDisconnect();
    }

    private String getDeviceStatus(int deviceStatus) {

        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }
}
