package lk.jiat.bonsaimart_admin.fagment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.jiat.bonsaimart_admin.R;
import lk.jiat.bonsaimart_admin.activity.MainActivity;

public class HomeFragment extends Fragment {

    // ============================================================
    // DASHBOARD COUNTERS
    // ============================================================
    private TextView tvProductCount;
    private TextView tvCategoryCount;
    private TextView tvUserCount;
    private TextView tvSoldCount;
    private TextView tvTotalSales;
    private TextView tvChartYear;

    // ============================================================
    // SALES CHART BARS
    // ============================================================
    private View barJan, barFeb, barMar, barApr, barMay, barJun;
    private View barJul, barAug, barSep, barOct, barNov, barDec;

    // ============================================================
    // QUICK ACTION CARDS
    // ============================================================
    private View cardAddProduct;
    private View cardViewProducts;

    // ============================================================
    // PROFILE ICON (TOP RIGHT)
    // ============================================================
    private View cardProfileIcon;

    // ============================================================
    // FIREBASE
    // ============================================================
    private FirebaseFirestore db;

    // ============================================================
    // ON CREATE VIEW
    // ============================================================
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    // ============================================================
    // ON VIEW CREATED
    // ============================================================
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setDefaultValues();
        setupClickListeners();
        loadDashboardData();
        loadCategoryPieChart();
    }

    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================
    private void initializeViews(View view) {

        // Counters
        tvProductCount = view.findViewById(R.id.tv_product_count);
        tvCategoryCount = view.findViewById(R.id.tv_category_count);
        tvUserCount = view.findViewById(R.id.tv_user_count);
        tvSoldCount = view.findViewById(R.id.tv_sold_count);
        tvTotalSales = view.findViewById(R.id.tv_total_sales);
        tvChartYear = view.findViewById(R.id.tv_chart_year);

        // Chart bars
        barJan = view.findViewById(R.id.bar_jan);
        barFeb = view.findViewById(R.id.bar_feb);
        barMar = view.findViewById(R.id.bar_mar);
        barApr = view.findViewById(R.id.bar_apr);
        barMay = view.findViewById(R.id.bar_may);
        barJun = view.findViewById(R.id.bar_jun);
        barJul = view.findViewById(R.id.bar_jul);
        barAug = view.findViewById(R.id.bar_aug);
        barSep = view.findViewById(R.id.bar_sep);
        barOct = view.findViewById(R.id.bar_oct);
        barNov = view.findViewById(R.id.bar_nov);
        barDec = view.findViewById(R.id.bar_dec);

        // Quick actions
        cardAddProduct = view.findViewById(R.id.card_add_product);
        cardViewProducts = view.findViewById(R.id.card_view_products);

        // Profile icon
        cardProfileIcon = view.findViewById(R.id.card_profile_icon);
    }

    // ============================================================
    // DEFAULT VALUES
    // ============================================================
    private void setDefaultValues() {

        if (tvProductCount != null) tvProductCount.setText("0");
        if (tvCategoryCount != null) tvCategoryCount.setText("0");
        if (tvUserCount != null) tvUserCount.setText("0");
        if (tvSoldCount != null) tvSoldCount.setText("0");
        if (tvTotalSales != null) tvTotalSales.setText("Rs. 0.00");

        if (tvChartYear != null) {
            Calendar calendar = Calendar.getInstance();
            tvChartYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        }

        resetChartBars();
    }

    // ============================================================
    // RESET CHART
    // ============================================================
    private void resetChartBars() {

        View[] bars = {
                barJan, barFeb, barMar, barApr, barMay, barJun,
                barJul, barAug, barSep, barOct, barNov, barDec
        };

        int minHeight = (int) (2 * getResources().getDisplayMetrics().density);

        for (View bar : bars) {
            if (bar != null) {
                ViewGroup.LayoutParams lp = bar.getLayoutParams();
                if (lp != null) {
                    lp.height = minHeight;
                    bar.setLayoutParams(lp);
                }
            }
        }
    }

    // ============================================================
    // LOAD CATEGORY PIE CHART
    // ============================================================
    private void loadCategoryPieChart() {

        if (db == null) return;

        db.collection("categories").get().addOnSuccessListener(catSnap -> {

            if (!isAdded() || getView() == null) return;

            List<String> catIds = new ArrayList<>();
            Map<String, String> catNames = new HashMap<>();

            for (DocumentSnapshot doc : catSnap.getDocuments()) {
                String id = doc.getId();
                catIds.add(id);
                catNames.put(id, doc.getString("name"));
            }

            if (catIds.isEmpty()) return;

            db.collection("products").get().addOnSuccessListener(prodSnap -> {

                if (!isAdded() || getView() == null) return;

                Map<String, Integer> counts = new HashMap<>();
                for (String id : catIds) counts.put(id, 0);

                for (DocumentSnapshot doc : prodSnap.getDocuments()) {
                    String catId = doc.getString("categoryId");
                    if (catId != null && counts.containsKey(catId)) {
                        counts.put(catId, counts.get(catId) + 1);
                    }
                }

                // ====================================================
                // ✅ FIXED: Use actual int values instead of parseColor
                // ====================================================
                int[] colors = {
                        Color.rgb(76, 175, 80),     // Green
                        Color.rgb(33, 150, 243),    // Blue
                        Color.rgb(255, 152, 0),     // Orange
                        Color.rgb(156, 39, 176),    // Purple
                        Color.rgb(244, 67, 54),     // Red
                        Color.rgb(0, 188, 212),     // Cyan
                        Color.rgb(121, 85, 72),     // Brown
                        Color.rgb(96, 125, 139)     // Blue Grey
                };

                List<lk.jiat.bonsaimart_admin.widget.PieChartView.Slice> slices = new ArrayList<>();

                LinearLayout legend = getView().findViewById(R.id.pie_legend);
                lk.jiat.bonsaimart_admin.widget.PieChartView pie =
                        getView().findViewById(R.id.pie_chart);

                if (legend == null || pie == null) return;

                legend.removeAllViews();

                int colorIndex = 0;
                for (String id : catIds) {
                    int count = counts.get(id);
                    if (count == 0) continue;

                    int color = colors[colorIndex % colors.length];

                    String catName = catNames.get(id);
                    if (catName == null) catName = "Unknown";

                    slices.add(new lk.jiat.bonsaimart_admin.widget.PieChartView.Slice(
                            catName, count, color));

                    // Legend row
                    LinearLayout row = new LinearLayout(getContext());
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    row.setPadding(0, 4, 0, 4);

                    View dot = new View(getContext());
                    LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(12, 12);
                    dot.setLayoutParams(dotParams);
                    dot.setBackgroundColor(color);
                    row.addView(dot);

                    TextView tv = new TextView(getContext());
                    tv.setText("  " + catName + " (" + count + ")");
                    tv.setTextSize(12);
                    tv.setTextColor(Color.rgb(51, 51, 51));  // ✅ Safe dark gray
                    row.addView(tv);

                    legend.addView(row);
                    colorIndex++;
                }

                pie.setData(slices);
            });
        });
    }
    // ============================================================
    // QUICK ACTION LISTENERS
    // ============================================================
    private void setupClickListeners() {

        // Add Product
        if (cardAddProduct != null) {
            cardAddProduct.setOnClickListener(v -> {
                if (requireActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.openAddProduct();
                }
            });
        }

        // View Products
        if (cardViewProducts != null) {
            cardViewProducts.setOnClickListener(v -> {
                if (requireActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.openProducts();
                }
            });
        }

        // Profile icon
        if (cardProfileIcon != null) {
            cardProfileIcon.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.fragment_container,
                                    new AdminProfileFragment()
                            )
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }

    // ============================================================
    // LOAD DASHBOARD DATA
    // ============================================================
    private void loadDashboardData() {

        loadCount("products", tvProductCount);
        loadCount("categories", tvCategoryCount);
        loadCount("users", tvUserCount);
        loadOrdersAndChart();
    }

    // ============================================================
    // LOAD COLLECTION COUNT
    // ============================================================
    private void loadCount(String collection, TextView target) {

        if (db == null) return;

        db.collection(collection)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;
                    if (target != null) {
                        target.setText(String.valueOf(querySnapshot.size()));
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    if (target != null) target.setText("0");
                });
    }

    // ============================================================
    // LOAD ORDERS + SALES CHART
    // ============================================================
    private void loadOrdersAndChart() {

        if (db == null) return;

        db.collection("orders")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (!isAdded() || querySnapshot == null) return;

                    double grandTotal = 0.0;

                    Map<Integer, Double> monthlyData = new HashMap<>();
                    for (int i = 0; i < 12; i++) monthlyData.put(i, 0.0);

                    Calendar calendar = Calendar.getInstance();
                    int currentYear = calendar.get(Calendar.YEAR);

                    if (tvChartYear != null) {
                        tvChartYear.setText(String.valueOf(currentYear));
                    }

                    int currentYearOrderCount = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {

                        double amount = extractAmount(doc);
                        grandTotal += amount;

                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        if (timestamp == null) timestamp = doc.getTimestamp("date");

                        if (timestamp != null) {

                            calendar.setTime(timestamp.toDate());

                            int orderYear = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH);

                            if (orderYear == currentYear) {
                                currentYearOrderCount++;

                                Double oldValue = monthlyData.get(month);
                                if (oldValue == null) oldValue = 0.0;

                                monthlyData.put(month, oldValue + amount);
                            }
                        }
                    }

                    if (tvSoldCount != null) {
                        tvSoldCount.setText(String.valueOf(currentYearOrderCount));
                    }

                    if (tvTotalSales != null) {
                        tvTotalSales.setText(String.format(Locale.US, "Rs. %,.2f", grandTotal));
                    }

                    updateChartUI(monthlyData);

                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),
                            "Error loading sales data", Toast.LENGTH_SHORT).show();
                });
    }

    // ============================================================
    // UPDATE CHART UI
    // ============================================================
    private void updateChartUI(Map<Integer, Double> monthlyData) {

        double max = 0.0;
        for (int i = 0; i < 12; i++) {
            Double value = monthlyData.get(i);
            if (value != null && value > max) max = value;
        }

        if (max <= 0) {
            resetChartBars();
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        int maxBarHeightPx = (int) (145 * density);
        int minHeightPx = (int) (3 * density);

        View[] bars = {
                barJan, barFeb, barMar, barApr, barMay, barJun,
                barJul, barAug, barSep, barOct, barNov, barDec
        };

        for (int i = 0; i < 12; i++) {

            View bar = bars[i];
            if (bar == null) continue;

            Double value = monthlyData.get(i);
            if (value == null) value = 0.0;

            int height = (int) ((value / max) * maxBarHeightPx);
            if (height < minHeightPx) height = minHeightPx;

            ViewGroup.LayoutParams params = bar.getLayoutParams();
            params.height = height;
            bar.setLayoutParams(params);
        }
    }

    // ============================================================
    // EXTRACT ORDER AMOUNT
    // ============================================================
    private double extractAmount(DocumentSnapshot doc) {

        String[] keys = {"total", "totalAmount", "amount", "grandTotal", "price"};

        for (String key : keys) {
            Object value = doc.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return 0.0;
    }

    // ============================================================
    // ON RESUME
    // ============================================================
    @Override
    public void onResume() {
        super.onResume();
        if (db != null) loadDashboardData();
    }
}