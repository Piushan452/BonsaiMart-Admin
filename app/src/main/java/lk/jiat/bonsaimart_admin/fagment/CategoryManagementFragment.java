package lk.jiat.bonsaimart_admin.fagment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lk.jiat.bonsaimart_admin.R;
import lk.jiat.bonsaimart_admin.adapter.CategoryAdapter;
import lk.jiat.bonsaimart_admin.model.CategoryAdmin;

public class CategoryManagementFragment extends Fragment
        implements CategoryAdapter.Listener {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<CategoryAdmin> categoryList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // ============================================================
    // ✅ ACTIVITY RESULT LAUNCHER - Class field (register ONCE)
    // ============================================================
    private Uri selectedImageUri;
    private ImageView currentImageView;
    private EditText currentNameInput;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {

                            selectedImageUri = result.getData().getData();

                            if (currentImageView != null) {
                                currentImageView.setImageURI(selectedImageUri);
                                currentImageView.setPadding(0, 0, 0, 0);
                            }
                        }
                    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        recyclerView = view.findViewById(R.id.categories_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CategoryAdapter(categoryList, this);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btn_add_new_category)
                .setOnClickListener(v -> showCategoryDialog(null));

        loadCategories();
    }

    // ============================================================
    // LOAD CATEGORIES
    // ============================================================
    private void loadCategories() {

        db.collection("categories").get().addOnSuccessListener(qs -> {

            if (!isAdded()) return;

            categoryList.clear();

            for (QueryDocumentSnapshot doc : qs) {

                CategoryAdmin cat = new CategoryAdmin();

                // ✅ Document ID (delete/update වලට)
                cat.setId(doc.getId());

                // ✅ Business categoryId (products map වෙන්න)
                String businessId = doc.getString("categoryId");
                if (businessId == null || businessId.isEmpty()) {
                    // Fallback: use document ID
                    businessId = doc.getId();
                }
                cat.setCategoryId(businessId);

                cat.setName(doc.getString("name"));
                cat.setImageUrl(doc.getString("imageUrl"));

                categoryList.add(cat);
            }

            adapter.notifyDataSetChanged();
            loadProductCounts();
        });
    }

    private void loadProductCounts() {
        for (int i = 0; i < categoryList.size(); i++) {
            final int index = i;
            CategoryAdmin cat = categoryList.get(i);

            db.collection("products")
                    .whereEqualTo("categoryId", cat.getId())
                    .get()
                    .addOnSuccessListener(qs -> {
                        if (!isAdded()) return;
                        cat.setProductCount(qs.size());
                        adapter.notifyItemChanged(index);
                    });
        }
    }

    // ============================================================
    // EDIT CATEGORY
    // ============================================================
    @Override
    public void onEdit(CategoryAdmin cat) {
        showCategoryDialog(cat);
    }

    // ============================================================
    // DELETE CATEGORY
    // ============================================================
    @Override
    public void onDelete(CategoryAdmin cat) {

        db.collection("products")
                .whereEqualTo("categoryId", cat.getId())
                .get()
                .addOnSuccessListener(qs -> {

                    if (!isAdded()) return;

                    if (qs.size() > 0) {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Cannot Delete")
                                .setMessage("This category has " + qs.size()
                                        + " products. Delete them first.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Delete Category")
                            .setMessage("Delete \"" + cat.getName() + "\"?")
                            .setPositiveButton("DELETE", (d, w) -> {
                                db.collection("categories").document(cat.getId())
                                        .delete()
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(getContext(),
                                                    "Category deleted",
                                                    Toast.LENGTH_SHORT).show();
                                            loadCategories();
                                        });
                            })
                            .setNegativeButton("CANCEL", null)
                            .show();
                });
    }

    // ============================================================
    // SHOW ADD/EDIT DIALOG  ✅ FIXED
    // ============================================================
    private void showCategoryDialog(@Nullable CategoryAdmin existing) {

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_category, null);

        EditText nameInput = view.findViewById(R.id.dialog_cat_name);
        ImageView imageView = view.findViewById(R.id.dialog_cat_image);
        MaterialButton btnSelect = view.findViewById(R.id.dialog_cat_pick_btn);

        // Reset
        selectedImageUri = null;
        currentImageView = imageView;
        currentNameInput = nameInput;

        if (existing != null) {
            nameInput.setText(existing.getName());
            if (existing.getImageUrl() != null && !existing.getImageUrl().isEmpty()) {
                Glide.with(this).load(existing.getImageUrl()).into(imageView);
                imageView.setPadding(0, 0, 0, 0);
            }
        }

        // ✅ Use the class-level launcher (not register here!)
        btnSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add Category" : "Edit Category")
                .setView(view)
                .setPositiveButton("SAVE", null)
                .setNegativeButton("CANCEL", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String name = nameInput.getText().toString().trim();

            if (name.isEmpty()) {
                nameInput.setError("Name required");
                return;
            }

            if (existing == null && selectedImageUri == null) {
                Toast.makeText(getContext(),
                        "Select an image", Toast.LENGTH_SHORT).show();
                return;
            }

            // If new image selected — upload
            if (selectedImageUri != null) {
                String fileName = UUID.randomUUID().toString();
                StorageReference ref = storage.getReference()
                        .child("categories/" + fileName);

                ref.putFile(selectedImageUri)
                        .addOnSuccessListener(snap -> ref.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    saveCategory(existing, name, uri.toString());
                                    dialog.dismiss();
                                }))
                        .addOnFailureListener(e -> Toast.makeText(getContext(),
                                "Upload failed", Toast.LENGTH_SHORT).show());
            } else {
                // Update without new image
                saveCategory(existing, name, existing.getImageUrl());
                dialog.dismiss();
            }
        });
    }

    // ============================================================
    // SAVE CATEGORY
    // ============================================================
    private void saveCategory(@Nullable CategoryAdmin existing, String name, String imageUrl) {

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("image", imageUrl);

        if (existing == null) {
            db.collection("categories").add(data)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(getContext(),
                                "Category added", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    });
        } else {
            db.collection("categories").document(existing.getId())
                    .update(data)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(),
                                "Category updated", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    });
        }
    }
}