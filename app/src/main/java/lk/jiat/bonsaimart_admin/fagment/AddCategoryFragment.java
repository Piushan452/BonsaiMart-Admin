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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lk.jiat.bonsaimart_admin.R;

public class AddCategoryFragment extends DialogFragment {

    private static final String TAG = "AddCategoryFragment";

    private ImageView categoryImg;
    private EditText categoryName;
    private MaterialButton saveBtn;
    private Uri selectedImageUri;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public AddCategoryFragment() {}

    private final ActivityResultLauncher<Intent> pickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            categoryImg.setImageURI(selectedImageUri);
                            categoryImg.setPadding(0, 0, 0, 0);
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_category, container, false);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        categoryImg = view.findViewById(R.id.category_img_select);
        categoryName = view.findViewById(R.id.category_name_input);
        saveBtn = view.findViewById(R.id.save_category_btn);

        categoryImg.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickerLauncher.launch(intent);
        });

        saveBtn.setOnClickListener(v -> uploadAndSave());

        return view;
    }

    private void uploadAndSave() {

        String name = categoryName.getText().toString().trim();

        if (name.isEmpty() || selectedImageUri == null) {
            Toast.makeText(getContext(),
                    "Select image and enter name",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        saveBtn.setEnabled(false);
        saveBtn.setText("Saving...");

        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference()
                .child("categories/" + fileName);

        ref.putFile(selectedImageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ref.getDownloadUrl().addOnSuccessListener(uri ->
                        saveToFirestore(name, uri.toString())
                );
            } else {
                saveBtn.setEnabled(true);
                saveBtn.setText("Save Category");
                Toast.makeText(getContext(),
                        "Image upload failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ============================================================
    // SAVE CATEGORY WITH UNIQUE categoryId
    // ============================================================
    private void saveToFirestore(String name, String imageUrl) {

        // Generate categoryId like "cat1", "cat2"...
        // හෝ හොඳම — timestamp-based unique ID
        String categoryId = generateCategoryId();

        Log.d(TAG, "New categoryId: " + categoryId);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("imageUrl", imageUrl);
        data.put("categoryId", categoryId);
        data.put("createdAt",
                com.google.firebase.firestore.FieldValue.serverTimestamp());

        // ✅ Use categoryId as document ID
        db.collection("categories")
                .document(categoryId)
                .set(data)
                .addOnSuccessListener(unused -> {

                    Log.d(TAG, "Category saved: " + categoryId);

                    Toast.makeText(getContext(),
                            "✅ Category Added!",
                            Toast.LENGTH_SHORT).show();

                    dismiss();
                })
                .addOnFailureListener(e -> {

                    Log.e(TAG, "Failed to save", e);

                    saveBtn.setEnabled(true);
                    saveBtn.setText("Save Category");

                    Toast.makeText(getContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ============================================================
    // GENERATE UNIQUE CATEGORY ID
    // Format: cat_<timestamp>_<random>
    // Example: cat_1728492000000_456
    // ============================================================
    private String generateCategoryId() {

        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);

        return "cat_" + timestamp + "_" + random;
    }
}