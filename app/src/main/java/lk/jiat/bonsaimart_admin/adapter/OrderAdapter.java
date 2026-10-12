package lk.jiat.bonsaimart_admin.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

import lk.jiat.bonsaimart_admin.R;
import lk.jiat.bonsaimart_admin.model.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onViewDetails(Order order);
    }

    public OrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    public void updateList(List<Order> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Order order = orderList.get(position);

        // Order ID
        String orderId = order.getOrderId();
        if (orderId == null || orderId.isEmpty()) {
            orderId = "N/A";
        }
        holder.orderIdText.setText("Order #" + orderId.substring(
                Math.max(0, orderId.length() - 8)));

        // Status
        String status = order.getStatus() != null ? order.getStatus() : "UNKNOWN";
        holder.orderStatusText.setText(status);
        applyStatusColor(holder.orderStatusText, status);

        // Customer Info
        if (order.getShippingAddress() != null) {
            Order.Address addr = order.getShippingAddress();
            holder.customerName.setText(
                    addr.getName() != null ? addr.getName() : "N/A");
            holder.customerPhone.setText(
                    addr.getContact() != null ? addr.getContact() : "N/A");
        } else {
            holder.customerName.setText("No customer data");
            holder.customerPhone.setText("");
        }

        // Total
        holder.orderTotal.setText(
                String.format(Locale.US, "Rs. %,.2f", order.getTotalAmount()));

        // Date
        if (order.getOrderDate() != null) {
            String date = DateFormat.format("MMM dd, yyyy",
                    order.getOrderDate().toDate()).toString();
            holder.orderDate.setText("📅 " + date);
        } else {
            holder.orderDate.setText("📅 Unknown");
        }

        // Item Count
        int itemCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
        holder.orderItemCount.setText(itemCount + " item" + (itemCount != 1 ? "s" : ""));

        // View Button
        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(order);
            }
        });
    }

    // ============================================================
    // STATUS COLOR BASED ON ORDER STATUS
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

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView orderIdText, orderStatusText;
        TextView customerName, customerPhone;
        TextView orderTotal, orderDate, orderItemCount;
        MaterialButton btnViewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            orderIdText = itemView.findViewById(R.id.order_id_text);
            orderStatusText = itemView.findViewById(R.id.order_status_text);
            customerName = itemView.findViewById(R.id.order_customer_name);
            customerPhone = itemView.findViewById(R.id.order_customer_phone);
            orderTotal = itemView.findViewById(R.id.order_total_text);
            orderDate = itemView.findViewById(R.id.order_date_text);
            orderItemCount = itemView.findViewById(R.id.order_item_count_text);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }
}