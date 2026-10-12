package lk.jiat.bonsaimart_admin.fagment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lk.jiat.bonsaimart_admin.R;
import lk.jiat.bonsaimart_admin.adapter.OrderAdapter;
import lk.jiat.bonsaimart_admin.model.Order;

public class OrdersFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private static final String TAG = "OrdersFragment";

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private EditText searchInput;
    private ChipGroup statusFilterGroup;
    private LinearLayout emptyView;

    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentStatusFilter = "ALL";

    // Available order statuses
    private static final String[] STATUS_OPTIONS = {
            "PENDING", "PAID", "SHIPPED", "DELIVERED", "CANCELLED"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Initialize views
        recyclerView = view.findViewById(R.id.orders_recycler_view);
        searchInput = view.findViewById(R.id.orders_search_input);
        statusFilterGroup = view.findViewById(R.id.status_filter_group);
        emptyView = view.findViewById(R.id.empty_orders_view);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdapter(filteredOrders, this);
        recyclerView.setAdapter(adapter);

        // Setup search
        setupSearch();

        // Setup filter chips
        setupFilterChips();

        // Load orders
        loadOrders();
    }

    // ============================================================
    // SETUP SEARCH
    // ============================================================
    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ============================================================
    // SETUP FILTER CHIPS
    // ============================================================
    private void setupFilterChips() {
        statusFilterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {

            if (checkedIds.isEmpty()) {
                currentStatusFilter = "ALL";
            } else {
                int checkedId = checkedIds.get(0);

                if (checkedId == R.id.chip_all) currentStatusFilter = "ALL";
                else if (checkedId == R.id.chip_pending) currentStatusFilter = "PENDING";
                else if (checkedId == R.id.chip_paid) currentStatusFilter = "PAID";
                else if (checkedId == R.id.chip_shipped) currentStatusFilter = "SHIPPED";
                else if (checkedId == R.id.chip_delivered) currentStatusFilter = "DELIVERED";
                else if (checkedId == R.id.chip_cancelled) currentStatusFilter = "CANCELLED";
            }

            applyFilters();
        });
    }

    // ============================================================
    // LOAD ORDERS FROM FIRESTORE
    // ============================================================
    private void loadOrders() {

        db.collection("orders")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (!isAdded()) return;

                    allOrders.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            Order order = doc.toObject(Order.class);

                            if (order != null) {
                                // Set orderId from document ID if missing
                                if (order.getOrderId() == null) {
                                    order.setOrderId(doc.getId());
                                }

                                allOrders.add(order);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse order: " + doc.getId(), e);
                        }
                    }

                    // Sort by date descending
                    allOrders.sort((a, b) -> {
                        if (a.getOrderDate() == null || b.getOrderDate() == null) return 0;
                        return b.getOrderDate().compareTo(a.getOrderDate());
                    });

                    Log.d(TAG, "Loaded " + allOrders.size() + " orders");

                    applyFilters();

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load orders", e);
                    if (isAdded()) {
                        Toast.makeText(getContext(),
                                "Failed to load orders: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ============================================================
    // APPLY SEARCH + FILTER
    // ============================================================
    private void applyFilters() {

        String query = searchInput.getText().toString().trim().toLowerCase();

        filteredOrders.clear();

        for (Order order : allOrders) {

            // Status filter
            if (!currentStatusFilter.equals("ALL")) {
                String status = order.getStatus();
                if (status == null || !status.equalsIgnoreCase(currentStatusFilter)) {
                    continue;
                }
            }

            // Search filter
            if (!query.isEmpty()) {
                boolean matches = false;

                // Order ID match
                if (order.getOrderId() != null
                        && order.getOrderId().toLowerCase().contains(query)) {
                    matches = true;
                }

                // Customer name match
                if (!matches && order.getShippingAddress() != null
                        && order.getShippingAddress().getName() != null
                        && order.getShippingAddress().getName().toLowerCase().contains(query)) {
                    matches = true;
                }

                // Customer phone match
                if (!matches && order.getShippingAddress() != null
                        && order.getShippingAddress().getContact() != null
                        && order.getShippingAddress().getContact().toLowerCase().contains(query)) {
                    matches = true;
                }

                if (!matches) continue;
            }

            filteredOrders.add(order);
        }

        // Show/hide empty state
        if (filteredOrders.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    // ============================================================
    // VIEW DETAILS CLICK
    // ============================================================
    @Override
    public void onViewDetails(Order order) {
        showOrderDetailsDialog(order);
    }

    // ============================================================
    // SHOW ORDER DETAILS DIALOG
    // ============================================================
    private void showOrderDetailsDialog(Order order) {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_order_details, null);

        // Order ID
        TextView orderIdText = dialogView.findViewById(R.id.detail_order_id);
        String orderId = order.getOrderId() != null ? order.getOrderId() : "N/A";
        orderIdText.setText("Order #" + orderId);

        // Status
        TextView orderStatusText = dialogView.findViewById(R.id.detail_order_status);
        String status = order.getStatus() != null ? order.getStatus() : "UNKNOWN";
        orderStatusText.setText(status);
        applyStatusColor(orderStatusText, status);

        // Status Spinner
        Spinner statusSpinner = dialogView.findViewById(R.id.detail_status_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                STATUS_OPTIONS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(spinnerAdapter);

        // Set current status as selection
        for (int i = 0; i < STATUS_OPTIONS.length; i++) {
            if (STATUS_OPTIONS[i].equalsIgnoreCase(status)) {
                statusSpinner.setSelection(i);
                break;
            }
        }

        // Customer Details
        if (order.getShippingAddress() != null) {
            Order.Address addr = order.getShippingAddress();

            ((TextView) dialogView.findViewById(R.id.detail_customer_name))
                    .setText("👤 " + (addr.getName() != null ? addr.getName() : "N/A"));

            ((TextView) dialogView.findViewById(R.id.detail_customer_email))
                    .setText("📧 " + (addr.getEmail() != null ? addr.getEmail() : "N/A"));

            ((TextView) dialogView.findViewById(R.id.detail_customer_phone))
                    .setText("📞 " + (addr.getContact() != null ? addr.getContact() : "N/A"));

            String fullAddress = "";
            if (addr.getAddress1() != null) fullAddress += addr.getAddress1();
            if (addr.getAddress2() != null && !addr.getAddress2().isEmpty())
                fullAddress += ", " + addr.getAddress2();
            if (addr.getCity() != null) fullAddress += ", " + addr.getCity();
            if (addr.getPostcode() != null) fullAddress += " " + addr.getPostcode();

            ((TextView) dialogView.findViewById(R.id.detail_customer_address))
                    .setText("📍 " + (fullAddress.isEmpty() ? "N/A" : fullAddress));
        }

        // Order Items
        LinearLayout itemsContainer = dialogView.findViewById(R.id.detail_items_container);
        itemsContainer.removeAllViews();

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {

            for (Order.OrderItem item : order.getOrderItems()) {

                View itemView = LayoutInflater.from(requireContext())
                        .inflate(android.R.layout.simple_list_item_2, itemsContainer, false);

                TextView text1 = itemView.findViewById(android.R.id.text1);
                TextView text2 = itemView.findViewById(android.R.id.text2);

                text1.setText("• " + (item.getProductName() != null
                        ? item.getProductName() : "Product"));

                String details = "  Qty: " + item.getQuantity()
                        + " × Rs. " + String.format(Locale.US, "%,.2f", item.getUnitPrice())
                        + " = Rs. " + String.format(Locale.US, "%,.2f",
                        item.getQuantity() * item.getUnitPrice());

                text2.setText(details);

                text1.setTextColor(Color.parseColor("#1A1A2E"));
                text2.setTextColor(Color.parseColor("#666666"));

                itemsContainer.addView(itemView);
            }
        } else {
            TextView noItems = new TextView(requireContext());
            noItems.setText("No items");
            noItems.setTextColor(Color.parseColor("#888888"));
            itemsContainer.addView(noItems);
        }

        // Total
        TextView totalText = dialogView.findViewById(R.id.detail_order_total);
        totalText.setText(String.format(Locale.US, "Rs. %,.2f", order.getTotalAmount()));

        // ============================================================
        // SHOW DIALOG
        // ============================================================
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Update Status", null)  // Override later
                .setNegativeButton("Close", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override positive button to update status
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String newStatus = (String) statusSpinner.getSelectedItem();

            if (newStatus == null || newStatus.equalsIgnoreCase(status)) {
                Toast.makeText(getContext(),
                        "No changes made",
                        Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            updateOrderStatus(order, newStatus, dialog);
        });
    }

    // ============================================================
    // UPDATE ORDER STATUS
    // ============================================================
    private void updateOrderStatus(Order order, String newStatus, AlertDialog dialog) {

        String orderId = order.getOrderId();
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(getContext(),
                    "Invalid order ID",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("orders")
                .document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {

                    if (!isAdded()) return;

                    order.setStatus(newStatus);
                    applyFilters();

                    Toast.makeText(getContext(),
                            "✅ Order status updated to " + newStatus,
                            Toast.LENGTH_SHORT).show();

                    dialog.dismiss();

                    // Create notification for user
                    createStatusNotification(order, newStatus);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Failed to update: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ============================================================
    // CREATE NOTIFICATION FOR USER
    // ============================================================
    private void createStatusNotification(Order order, String newStatus) {

        java.util.Map<String, Object> notification = new java.util.HashMap<>();
        notification.put("title", "Order Update");
        notification.put("content", "Your order #" +
                (order.getOrderId() != null ? order.getOrderId() : "")
                + " is now " + newStatus);
        notification.put("timestamp",
                com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("messages")
                .add(notification)
                .addOnSuccessListener(ref -> Log.d(TAG, "Notification created"))
                .addOnFailureListener(e -> Log.e(TAG, "Notification failed", e));
    }

    // ============================================================
    // STATUS COLOR
    // ============================================================
    private void applyStatusColor(TextView textView, String status) {

        int bgColor;
        int textColor;

        switch (status.toUpperCase()) {
            case "PENDING":
                bgColor = Color.parseColor("#FFF4E5");
                textColor = Color.parseColor("#E65100");
                break;
            case "PAID":
                bgColor = Color.parseColor("#E3F2FD");
                textColor = Color.parseColor("#1565C0");
                break;
            case "SHIPPED":
                bgColor = Color.parseColor("#F3E5F5");
                textColor = Color.parseColor("#6A1B9A");
                break;
            case "DELIVERED":
                bgColor = Color.parseColor("#E8F5E9");
                textColor = Color.parseColor("#2E7D32");
                break;
            case "CANCELLED":
                bgColor = Color.parseColor("#FFEBEE");
                textColor = Color.parseColor("#C62828");
                break;
            default:
                bgColor = Color.parseColor("#F5F5F5");
                textColor = Color.parseColor("#616161");
                break;
        }

        textView.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        textView.setTextColor(textColor);
    }
}