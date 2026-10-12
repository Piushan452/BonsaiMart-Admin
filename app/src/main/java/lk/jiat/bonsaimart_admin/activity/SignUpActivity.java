package lk.jiat.bonsaimart_admin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import lk.jiat.bonsaimart_admin.R;

public class SignUpActivity extends AppCompatActivity {

    // ============================================================
    // ADMIN SECRET CODE - Change this to something secure!
    // ============================================================
    private static final String ADMIN_SECRET_CODE = "BONSAI2026";

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private EditText nameInput, emailInput, passwordInput, retypePasswordInput, adminCodeInput;
    private Button signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameInput = findViewById(R.id.signup_input_name);
        emailInput = findViewById(R.id.signup_input_email);
        passwordInput = findViewById(R.id.signup_input_password);
        retypePasswordInput = findViewById(R.id.signup_input_retype_password);
        adminCodeInput = findViewById(R.id.signup_input_admin_code);  // ✅ NEW FIELD
        signUpBtn = findViewById(R.id.signup_btn_signup);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        findViewById(R.id.signup_btn_signin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String retypePassword = retypePasswordInput.getText().toString().trim();
        String adminCode = adminCodeInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Full name is required");
            nameInput.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        if (!password.equals(retypePassword)) {
            retypePasswordInput.setError("Passwords do not match");
            retypePasswordInput.requestFocus();
            return;
        }

        // ============================================================
        // ADMIN CODE VALIDATION
        // ============================================================
        if (adminCode.isEmpty()) {
            adminCodeInput.setError("Admin code is required");
            adminCodeInput.requestFocus();
            return;
        }

        if (!adminCode.equals(ADMIN_SECRET_CODE)) {
            adminCodeInput.setError("❌ Invalid admin code. Access denied.");
            adminCodeInput.requestFocus();
            Toast.makeText(SignUpActivity.this,
                    "You are not authorized to create admin accounts",
                    Toast.LENGTH_LONG).show();
            return;
        }

        signUpBtn.setEnabled(false);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = firebaseAuth.getCurrentUser().getUid();

                            // Store admin details in Firestore "admin" collection
                            Map<String, Object> admin = new HashMap<>();
                            admin.put("name", name);
                            admin.put("email", email);
                            admin.put("role", "admin");
                            admin.put("createdAt", com.google.firebase.Timestamp.now());

                            db.collection("admin").document(userId)
                                    .set(admin)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            signUpBtn.setEnabled(true);
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SignUpActivity.this,
                                                        "✅ Admin Registration Successful",
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                String message = task.getException() != null ? task.getException().getMessage() : "Failed to store admin data";
                                                Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            signUpBtn.setEnabled(true);
                            String message = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                            Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}