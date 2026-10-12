package lk.jiat.bonsaimart_admin.fagment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.jiat.bonsaimart_admin.R;
import lk.jiat.bonsaimart_admin.activity.SignInActivity;

public class AdminProfileFragment extends Fragment {

    private static final String TAG = "AdminProfileFragment";

    private EditText inputName, inputEmail, inputOldPassword, inputNewPassword, inputConfirmPassword;
    private TextView avatar;
    private com.google.android.material.button.MaterialButton btnUpdate, btnLogout;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        avatar = view.findViewById(R.id.admin_profile_avatar);
        inputName = view.findViewById(R.id.admin_profile_input_name);
        inputEmail = view.findViewById(R.id.admin_profile_input_email);
        inputOldPassword = view.findViewById(R.id.admin_profile_input_old_password);
        inputNewPassword = view.findViewById(R.id.admin_profile_input_new_password);
        inputConfirmPassword = view.findViewById(R.id.admin_profile_input_confirm_password);
        btnUpdate = view.findViewById(R.id.admin_profile_btn_update);
        btnLogout = view.findViewById(R.id.admin_profile_btn_logout);

        // Load admin data
        loadAdminData();

        // Button listeners
        btnUpdate.setOnClickListener(v -> processUpdates());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // ============================================================
    // LOAD ADMIN DATA FROM FIRESTORE
    // ============================================================
    private void loadAdminData() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) return;

        db.collection("admin")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) return;

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");

                        if (name != null) {
                            inputName.setText(name);
                            // Set avatar letter
                            if (!name.isEmpty()) {
                                avatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                            }
                        }

                        if (email != null) {
                            inputEmail.setText(email);
                        }

                        Log.d(TAG, "Admin data loaded");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load admin data", e);
                    if (isAdded()) {
                        Toast.makeText(getContext(),
                                "Failed to load profile",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ============================================================
    // PROCESS PROFILE UPDATES
    // ============================================================
    private void processUpdates() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        String newName = inputName.getText().toString().trim();
        String newEmail = inputEmail.getText().toString().trim();
        String oldPassword = inputOldPassword.getText().toString();
        String newPassword = inputNewPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        if (newName.isEmpty()) {
            inputName.setError("Name is required");
            return;
        }

        // 1. Update Name in Firestore (No re-auth needed)
        updateNameInFirestore(user.getUid(), newName);

        // 2. Handle Email or Password Change (Requires Re-auth)
        if (!newEmail.equals(user.getEmail()) || !newPassword.isEmpty()) {

            if (oldPassword.isEmpty()) {
                inputOldPassword.setError("Required for security changes");
                return;
            }

            btnUpdate.setEnabled(false);
            btnUpdate.setText("Verifying...");

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
            user.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {

                if (!isAdded()) return;

                btnUpdate.setEnabled(true);
                btnUpdate.setText("Save Changes");

                if (reAuthTask.isSuccessful()) {

                    // Handle Email Change
                    if (!newEmail.equals(user.getEmail())) {
                        updateEmail(user, newEmail);
                    }

                    // Handle Password Change
                    if (!newPassword.isEmpty()) {
                        if (newPassword.equals(confirmPassword)) {
                            if (newPassword.length() >= 6) {
                                updatePassword(user, newPassword);
                            } else {
                                inputNewPassword.setError("Minimum 6 characters");
                            }
                        } else {
                            inputConfirmPassword.setError("Passwords do not match");
                        }
                    }

                    // Clear password fields
                    inputOldPassword.setText("");
                    inputNewPassword.setText("");
                    inputConfirmPassword.setText("");

                } else {
                    Toast.makeText(getContext(),
                            "❌ Old password is incorrect",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // ============================================================
    // UPDATE NAME
    // ============================================================
    private void updateNameInFirestore(String uid, String name) {
        db.collection("admin").document(uid)
                .update("name", name)
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(),
                            "✅ Name updated successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Update avatar
                    if (!name.isEmpty()) {
                        avatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(),
                            "Failed to update name: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ============================================================
    // UPDATE EMAIL
    // ============================================================
    private void updateEmail(FirebaseUser user, String newEmail) {
        user.verifyBeforeUpdateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (!isAdded()) return;

                    if (task.isSuccessful()) {
                        db.collection("admin").document(user.getUid())
                                .update("email", newEmail);
                        Toast.makeText(getContext(),
                                "✅ Verification email sent to " + newEmail,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(),
                                "Email update failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ============================================================
    // UPDATE PASSWORD
    // ============================================================
    private void updatePassword(FirebaseUser user, String newPassword) {
        user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (!isAdded()) return;

                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "✅ Password updated successfully!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),
                                "Password update failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ============================================================
    // LOGOUT CONFIRMATION
    // ============================================================
    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout from Admin Panel?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("LOGOUT", (dialog, which) -> performLogout())
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void performLogout() {
        firebaseAuth.signOut();

        Toast.makeText(getContext(),
                "Logged out successfully",
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}