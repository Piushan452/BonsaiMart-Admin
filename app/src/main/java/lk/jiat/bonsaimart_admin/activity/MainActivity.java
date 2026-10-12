package lk.jiat.bonsaimart_admin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import lk.jiat.bonsaimart_admin.R;
import lk.jiat.bonsaimart_admin.fagment.AddProductFragment;
import lk.jiat.bonsaimart_admin.fagment.AdminProfileFragment;
import lk.jiat.bonsaimart_admin.fagment.CategoryManagementFragment;
import lk.jiat.bonsaimart_admin.fagment.ExportFragment;
import lk.jiat.bonsaimart_admin.fagment.HomeFragment;
import lk.jiat.bonsaimart_admin.fagment.OrdersFragment;
import lk.jiat.bonsaimart_admin.fagment.ProductViewFragment;
import lk.jiat.bonsaimart_admin.fagment.SendNotificationFragment;
import lk.jiat.bonsaimart_admin.fagment.UsersFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        // ============================================================
        // BOTTOM NAVIGATION
        // ============================================================
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(this);
        }

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    // ============================================================
    // NAVIGATION
    // ============================================================
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;
        int id = item.getItemId();

        if (id == R.id.bottom_nav_home) {
            fragment = new HomeFragment();

        } else if (id == R.id.bottom_nav_orders) {
            fragment = new OrdersFragment();

        } else if (id == R.id.bottom_nav_category) {
            fragment = new CategoryManagementFragment();

        } else if (id == R.id.bottom_nav_notif) {
            fragment = new SendNotificationFragment();

        } else if (id == R.id.bottom_nav_more) {
            showMoreOptionsDialog();
            return true;
        }

        if (fragment != null) {
            loadFragment(fragment);
            return true;
        }
        return false;
    }

    // ============================================================
    // MORE OPTIONS
    // ============================================================
    private void showMoreOptionsDialog() {

        String[] options = {
                "Add Product",
                "View Products",
                "Users",
                "Export Reports",
                "Admin Profile",
                "Logout"
        };

        new AlertDialog.Builder(this)
                .setTitle("More Options")
                .setItems(options, (dialog, which) -> {

                    Fragment fragment = null;

                    if (which == 0) {
                        fragment = new AddProductFragment();
                    } else if (which == 1) {
                        fragment = new ProductViewFragment();
                    } else if (which == 2) {
                        fragment = new UsersFragment();
                    } else if (which == 3) {
                        fragment = new ExportFragment();
                    } else if (which == 4) {
                        fragment = new AdminProfileFragment();
                    } else if (which == 5) {
                        showLogoutDialog();
                        return;
                    }

                    if (fragment != null) loadFragment(fragment);
                })
                .show();
    }

    // ============================================================
    // LOGOUT
    // ============================================================
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("LOGOUT", (d, w) -> {
                    firebaseAuth.signOut();
                    Intent i = new Intent(MainActivity.this, SignInActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    // ============================================================
    // QUICK ACTIONS
    // ============================================================
    public void openAddProduct() {
        loadFragment(new AddProductFragment());
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);
        }
    }

    public void openProducts() {
        loadFragment(new ProductViewFragment());
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);
        }
    }

    // ============================================================
    // LOAD FRAGMENT
    // ============================================================
    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.fragment_container, fragment);
        tx.commit();
    }
}