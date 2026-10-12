package lk.jiat.bonsaimart_admin.adapter;

import android.util.Log;
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
import lk.jiat.bonsaimart_admin.model.CategoryAdmin;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    private static final String TAG = "CategoryAdapter";

    public interface Listener {
        void onEdit(CategoryAdmin cat);
        void onDelete(CategoryAdmin cat);
    }

    private List<CategoryAdmin> list;
    private Listener listener;

    public CategoryAdapter(List<CategoryAdmin> list, Listener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<CategoryAdmin> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_admin, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CategoryAdmin cat = list.get(position);

        h.name.setText(cat.getName() != null ? cat.getName() : "N/A");
        h.productCount.setText(cat.getProductCount() + " products");

        // ============================================================
        // ✅ FIXED: Use getImageUrl()
        // ============================================================
        String imageUrl = cat.getImageUrl();

        Log.d(TAG, "Category: " + cat.getName() + " | URL: " + imageUrl);

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(h.image);
        } else {
            h.image.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(cat);
        });

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(cat);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, productCount;
        ImageButton btnEdit, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cat_image);
            name = itemView.findViewById(R.id.cat_name);
            productCount = itemView.findViewById(R.id.cat_product_count);
            btnEdit = itemView.findViewById(R.id.cat_btn_edit);
            btnDelete = itemView.findViewById(R.id.cat_btn_delete);
        }
    }
}