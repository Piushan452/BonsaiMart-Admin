package lk.jiat.bonsaimart_admin.fagment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lk.jiat.bonsaimart_admin.R;
import lk.jiat.bonsaimart_admin.model.Category;
import lk.jiat.bonsaimart_admin.model.Product;

public class AddProductFragment extends Fragment {

    private ImageView img1, img2, img3;
    private EditText titleInput, descriptionInput, priceInput, quantityInput;
    private Spinner categorySpinner;
    private MaterialButton addProductBtn, addCategoryBtn;
    private TextView headerTitle;

    private Uri[] imageUris = new Uri[3];
    private String[] existingImageUrls = new String[3];
    private int currentImageIndex = -1;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private List<Category> categories = new ArrayList<>();
    private ArrayAdapter<Category> categoryAdapter;

    private Product updateProduct;
    private boolean isUpdate = false;

    public AddProductFragment() {
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (currentImageIndex != -1) {
                        imageUris[currentImageIndex] = uri;
                        ImageView target = (currentImageIndex == 0) ? img1 : (currentImageIndex == 1) ? img2 : img3;
                        if (target != null) {
                            target.setImageURI(uri);
                            target.setPadding(0, 0, 0, 0);
                        }
                    }
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        headerTitle = view.findViewById(R.id.add_product_header);
        img1 = view.findViewById(R.id.add_product_img1);
        img2 = view.findViewById(R.id.add_product_img2);
        img3 = view.findViewById(R.id.add_product_img3);
        titleInput = view.findViewById(R.id.add_product_title);
        descriptionInput = view.findViewById(R.id.add_product_description);
        categorySpinner = view.findViewById(R.id.add_product_category_spinner);
        priceInput = view.findViewById(R.id.add_product_price);
        quantityInput = view.findViewById(R.id.add_product_quantity);
        addProductBtn = view.findViewById(R.id.add_product_btn);
        addCategoryBtn = view.findViewById(R.id.btn_add_category);

        img1.setOnClickListener(v -> pickImage(0));
        img2.setOnClickListener(v -> pickImage(1));
        img3.setOnClickListener(v -> pickImage(2));

        addProductBtn.setOnClickListener(v -> validateAndSave());

        addCategoryBtn.setOnClickListener(v -> {
            AddCategoryFragment dialog = new AddCategoryFragment();
            dialog.show(getChildFragmentManager(), "AddCategoryDialog");
        });

        setupCategorySpinner();

        if (getArguments() != null && getArguments().containsKey("product_data")) {
            isUpdate = true;
            updateProduct = (Product) getArguments().getSerializable("product_data");
            prepareUpdateMode();
        }
    }

    private void prepareUpdateMode() {
        if (headerTitle != null) headerTitle.setText("Update Product");
        addProductBtn.setText("Update Product");

        titleInput.setText(updateProduct.getTitle());
        descriptionInput.setText(updateProduct.getDescription());
        priceInput.setText(String.valueOf(updateProduct.getPrice()));
        quantityInput.setText(String.valueOf(updateProduct.getStockCount()));

        if (updateProduct.getImages() != null) {
            List<String> images = updateProduct.getImages();
            for (int i = 0; i < images.size() && i < 3; i++) {
                existingImageUrls[i] = images.get(i);
                ImageView target = (i == 0) ? img1 : (i == 1) ? img2 : img3;
                if (target != null) {
                    Glide.with(this).load(images.get(i)).into(target);
                    target.setPadding(0, 0, 0, 0);
                }
            }
        }
    }

    private void setupCategorySpinner() {
        categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        loadCategories();
    }

    private void loadCategories() {
        db.collection("categories").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                categories.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Category category = new Category();
                    category.setId(document.getId());
                    category.setName(document.getString("name"));
                    categories.add(category);
                }
                categoryAdapter.notifyDataSetChanged();

                if (isUpdate && updateProduct != null) {
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).getId().equals(updateProduct.getCategoryId())) {
                            categorySpinner.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void pickImage(int index) {
        currentImageIndex = index;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void validateAndSave() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        Category selectedCategory = (Category) categorySpinner.getSelectedItem();
        String priceStr = priceInput.getText().toString().trim();
        String quantityStr = quantityInput.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || selectedCategory == null || priceStr.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasImage = false;
        for (Uri uri : imageUris) if (uri != null) hasImage = true;
        for (String url : existingImageUrls) if (url != null) hasImage = true;

        if (!hasImage) {
            Toast.makeText(getContext(), "Select at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        addProductBtn.setEnabled(false);
        addProductBtn.setText(isUpdate ? "Updating..." : "Adding...");

        uploadImages(title, description, selectedCategory.getId(), Double.parseDouble(priceStr), Integer.parseInt(quantityStr));
    }

    private void uploadImages(String title, String description, String catId, double price, int qty) {
        List<String> finalUrls = new ArrayList<>();
        List<Integer> toUploadIndices = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            if (imageUris[i] != null) toUploadIndices.add(i);
            else if (existingImageUrls[i] != null) finalUrls.add(existingImageUrls[i]);
        }

        if (toUploadIndices.isEmpty()) {
            saveToFirestore(title, description, catId, price, qty, finalUrls);
            return;
        }

        final int totalToUpload = toUploadIndices.size();
        final int[] uploaded = {0};

        for (int index : toUploadIndices) {
            String name = UUID.randomUUID().toString();
            StorageReference ref = storage.getReference().child("products/" + name);
            ref.putFile(imageUris[index]).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                finalUrls.add(uri.toString());
                uploaded[0]++;
                if (uploaded[0] == totalToUpload) {
                    saveToFirestore(title, description, catId, price, qty, finalUrls);
                }
            })).addOnFailureListener(e -> {
                addProductBtn.setEnabled(true);
                addProductBtn.setText(isUpdate ? "Update Product" : "Add Product");
                Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    // ============================================================
    // SAVE TO FIRESTORE
    // ✅ FIXED: "Id" → "productId" (matches user app model)
    // ============================================================
    private void saveToFirestore(String title, String description,
                                 String catId, double price, int qty,
                                 List<String> images) {

        String id = isUpdate ? updateProduct.getProductId()
                : db.collection("products").document().getId();

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);
        data.put("categoryId", catId);  // ✅ Category ID save වෙනවා
        data.put("price", price);
        data.put("stockCount", qty);
        data.put("images", images);
        data.put("productId", id);
        data.put("status", true);
        data.put("rating", isUpdate ? updateProduct.getRating() : 0.0);

        db.collection("products").document(id).set(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(),
                            isUpdate ? "Product Updated" : "Product Added",
                            Toast.LENGTH_SHORT).show();
                    if (!isUpdate) clearFields();
                    addProductBtn.setEnabled(true);
                    addProductBtn.setText(isUpdate ? "Update Product" : "Add Product");
                })
                .addOnFailureListener(e -> {
                    addProductBtn.setEnabled(true);
                    addProductBtn.setText(isUpdate ? "Update Product" : "Add Product");
                    Toast.makeText(getContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void clearFields() {
        titleInput.setText("");
        descriptionInput.setText("");
        priceInput.setText("");
        quantityInput.setText("");
        img1.setImageResource(android.R.drawable.ic_menu_camera);
        img2.setImageResource(android.R.drawable.ic_menu_camera);
        img3.setImageResource(android.R.drawable.ic_menu_camera);
        img1.setPadding(40, 40, 40, 40);
        img2.setPadding(40, 40, 40, 40);
        img3.setPadding(40, 40, 40, 40);
        imageUris = new Uri[3];
        existingImageUrls = new String[3];
    }
}