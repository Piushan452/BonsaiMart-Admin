package lk.jiat.bonsaimart_admin.fagment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.jiat.bonsaimart_admin.R;
import lk.jiat.bonsaimart_admin.adapter.NotificationAdminAdapter;

public class SendNotificationFragment extends Fragment {

    private TextInputEditText titleInput, contentInput;
    private MaterialButton sendBtn;
    private RecyclerView recyclerView;
    private NotificationAdminAdapter adapter;
    private List<Map<String, Object>> notifList = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView notifCount;
    private View emptyView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        titleInput = view.findViewById(R.id.notif_title_input);
        contentInput = view.findViewById(R.id.notif_content_input);
        sendBtn = view.findViewById(R.id.btn_send_notification);
        recyclerView = view.findViewById(R.id.notifications_recycler_view);
        notifCount = view.findViewById(R.id.tv_notif_count);
        emptyView = view.findViewById(R.id.empty_notif_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new NotificationAdminAdapter(notifList);
        recyclerView.setAdapter(adapter);

        sendBtn.setOnClickListener(v -> sendNotification());

        loadRecentNotifications();
    }

    private void sendNotification() {

        String title = titleInput.getText() != null
                ? titleInput.getText().toString().trim() : "";
        String content = contentInput.getText() != null
                ? contentInput.getText().toString().trim() : "";

        if (title.isEmpty()) {
            titleInput.setError("Title required");
            return;
        }

        if (content.isEmpty()) {
            contentInput.setError("Message required");
            return;
        }

        sendBtn.setEnabled(false);
        sendBtn.setText("Sending...");

        Map<String, Object> notif = new HashMap<>();
        notif.put("title", title);
        notif.put("content", content);
        notif.put("timestamp",
                com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("messages").add(notif)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(getContext(),
                            "✅ Notification sent to all users",
                            Toast.LENGTH_SHORT).show();
                    titleInput.setText("");
                    contentInput.setText("");
                    sendBtn.setEnabled(true);
                    sendBtn.setText("Send to All Users");
                    loadRecentNotifications();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    sendBtn.setEnabled(true);
                    sendBtn.setText("Send to All Users");
                });
    }

    private void loadRecentNotifications() {

        db.collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(qs -> {
                    if (!isAdded()) return;

                    notifList.clear();
                    for (QueryDocumentSnapshot doc : qs) {
                        notifList.add(doc.getData());
                    }

                    // Update count
                    if (notifCount != null) {
                        notifCount.setText(String.valueOf(notifList.size()));
                    }

                    // Show/hide empty state
                    if (notifList.isEmpty()) {
                        if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
                    } else {
                        if (emptyView != null) emptyView.setVisibility(View.GONE);
                        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}