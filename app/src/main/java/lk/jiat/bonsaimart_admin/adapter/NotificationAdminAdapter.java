package lk.jiat.bonsaimart_admin.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.Map;

import lk.jiat.bonsaimart_admin.R;

public class NotificationAdminAdapter extends
        RecyclerView.Adapter<NotificationAdminAdapter.VH> {

    private List<Map<String, Object>> list;

    public NotificationAdminAdapter(List<Map<String, Object>> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_admin, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Map<String, Object> notif = list.get(position);

        h.title.setText(String.valueOf(notif.get("title")));
        h.content.setText(String.valueOf(notif.get("content")));

        Object ts = notif.get("timestamp");
        if (ts instanceof Timestamp) {
            String timeAgo = (String) DateUtils.getRelativeTimeSpanString(
                    ((Timestamp) ts).toDate().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS);
            h.time.setText(timeAgo);
        } else {
            h.time.setText("Just now");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, content, time;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notif_item_title);
            content = itemView.findViewById(R.id.notif_item_content);
            time = itemView.findViewById(R.id.notif_item_time);
        }
    }
}