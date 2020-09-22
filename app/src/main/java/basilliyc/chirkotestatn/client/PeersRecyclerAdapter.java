package basilliyc.chirkotestatn.client;

import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import basilliyc.chirkotestatn.R;

public class PeersRecyclerAdapter extends RecyclerView.Adapter<PeersRecyclerAdapter.ViewHolder> {
    public List<WifiP2pDevice> data = new ArrayList<>();
    public OnClickItemListener onClickItemListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_peer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(final WifiP2pDevice item) {

            String builder = item.deviceAddress + "\n" + item.deviceName;
            ((TextView) itemView.findViewById(R.id.item_peer_title)).setText(builder);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickItemListener.onClickItem(item);
                }
            });
        }
    }

    public interface OnClickItemListener {
        void onClickItem(WifiP2pDevice item);
    }

}
