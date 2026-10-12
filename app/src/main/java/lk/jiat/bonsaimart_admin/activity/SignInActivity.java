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

import lk.jiat.bonsaimart_admin.R;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private EditText emailInput, passwordInput;
    private Button signInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailInput = findViewById(R.id.signin_input_email);
        passwordInput = findViewById(R.id.signin_input_password);
        signInBtn = findViewById(R.id.signin_btn_signin);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser();
            }
        });

        findViewById(R.id.signin_btn_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // ============================================================
        // FORGOT PASSWORD
        // ============================================================
        findViewById(R.id.signin_forgot_password).setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Enter valid email first");
                emailInput.requestFocus();
                return;
            }
            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> Toast.makeText(
                            SignInActivity.this,
                            "Reset link sent to your email",
                            Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(
                            SignInActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show());
        });
    }

    private void signInUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

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

        signInBtn.setEnabled(false);
        signInBtn.setText("Signing In...");

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // ============================================================
                            // ADMIN ROLE CHECK
                            // ============================================================
                            String uid = firebaseAuth.getCurrentUser().getUid();

                            db.collection("admin")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {

                                        signInBtn.setEnabled(true);
                                        signInBtn.setText("Sign In");

                                        if (documentSnapshot.exists()) {

                                            // ✅ Admin — allow access
                                            Toast.makeText(SignInActivity.this,
                                                    "Welcome, Admin!",
                                                    Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();

                                        } else {

                                            // ❌ Not an admin — deny access
                                            firebaseAuth.signOut();

                                            Toast.makeText(SignInActivity.this,
                                                    "❌ Access Denied! This is admin-only app.",
                                                    Toast.LENGTH_LONG).show();

                                            emailInput.setText("");
                                            passwordInput.setText("");
                                        }
                                    })
                                    .addOnFailureListener(e -> {

                                        signInBtn.setEnabled(true);
                                        signInBtn.setText("Sign In");

                                        firebaseAuth.signOut();

                                        Toast.makeText(SignInActivity.this,
                                                "Error checking admin status: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    });

                        } else {
                            signInBtn.setEnabled(true);
                            signInBtn.setText("Sign In");
                            String message = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                            Toast.makeText(SignInActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}