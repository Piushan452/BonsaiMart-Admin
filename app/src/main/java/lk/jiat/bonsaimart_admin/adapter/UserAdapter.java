package lk.jiat.bonsaimart_admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import lk.jiat.bonsaimart_admin.R;
import lk.jiat.bonsaimart_admin.model.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;

    public UserAdapter(List<User> users) { this.users = users; }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());

        holder.deleteBtn.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete " + user.getName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deleteUserFromFirestore(user.getUid(), position, holder.itemView.getContext());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        Glide.with(holder.itemView.getContext())
                .load(user.getProfilePicUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.userImg);
    }

    @Override
    public int getItemCount() { return users.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        ImageView userImg;
        ImageButton deleteBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name_text);
            email = itemView.findViewById(R.id.user_email_text);
            userImg = itemView.findViewById(R.id.user_image_view);
            deleteBtn = itemView.findViewById(R.id.delete_user_btn);
        }
    }

    private void deleteUserFromFirestore(String uid, int position, android.content.Context context) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    users.remove(position);
                    notifyItemRemoved(position);
                    android.widget.Toast.makeText(context, "User deleted successfully!", android.widget.Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(context, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                });
    }

}