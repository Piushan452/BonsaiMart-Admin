package lk.jiat.bonsaimart_admin.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import lk.jiat.bonsaimart_admin.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);

            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }

        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        setContentView(R.layout.activity_splash);

        ImageView imageView = findViewById(R.id.splashLogo);

        Glide.with(this)
                .asBitmap()
                .load(R.drawable.app_icon)
                .override(300)
                .into(imageView);


        // ============================================================
        // NAVIGATE AFTER SPLASH
        // ============================================================
        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.splashProgressBar).setVisibility(View.INVISIBLE);

                        // ============================================================
                        // CHECK LOGIN STATUS + ADMIN ROLE
                        // ============================================================
                        com.google.firebase.auth.FirebaseAuth auth =
                                com.google.firebase.auth.FirebaseAuth.getInstance();

                        if (auth.getCurrentUser() != null) {

                            // User is logged in — verify admin
                            String uid = auth.getCurrentUser().getUid();

                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection("admin")
                                    .document(uid)
                                    .get()
                                    .addOnCompleteListener(task -> {

                                        Intent intent;

                                        if (task.isSuccessful() && task.getResult().exists()) {
                                            // ✅ Admin — go to MainActivity
                                            intent = new Intent(SplashActivity.this, MainActivity.class);
                                        } else {
                                            // ❌ Not admin — sign out and go to SignIn
                                            auth.signOut();
                                            intent = new Intent(SplashActivity.this, SignInActivity.class);
                                        }

                                        startActivity(intent);
                                        finish();
                                    });

                        } else {

                            // Not logged in
                            Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }, 3000);


        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.splashProgressBar).setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 5000);

    }
}