package basilliyc.chirkotestatn.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.entity.StoredMediaInfo;
import basilliyc.chirkotestatn.utils.Utils;

public class StoredMediaAdapter extends RecyclerView.Adapter<StoredMediaAdapter.ViewHolder> {
    public List<StoredMediaInfo> data = new ArrayList<>();
    public OnClickItemListener onClickItemListener;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stored_media, parent, false));
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

        private TextView fileName;
        private TextView fileSize;
        private TextView fileTime;
        private TextView fileStoredType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            fileSize = (TextView) itemView.findViewById(R.id.file_size);
            fileTime = (TextView) itemView.findViewById(R.id.file_time);
            fileStoredType = (TextView) itemView.findViewById(R.id.file_store_type);
        }

        public void bind(final StoredMediaInfo item) {

            fileName.setText(item.getFileName());
            fileSize.setText(Utils.humanReadableByteCountBin(item.getFileLength()));
            fileTime.setText(simpleDateFormat.format(new Date(item.getTime())));

            if (item.isReceived()) {
                fileStoredType.setText(R.string.stored_receive);
            } else {
                fileStoredType.setText(R.string.stored_sent);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickItemListener.onClickItem(item);
                }
            });
        }
    }

    public interface OnClickItemListener {
        void onClickItem(StoredMediaInfo item);
    }

}
