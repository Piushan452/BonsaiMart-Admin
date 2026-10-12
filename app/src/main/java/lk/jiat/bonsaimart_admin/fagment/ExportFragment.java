package lk.jiat.bonsaimart_admin.fagment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lk.jiat.bonsaimart_admin.R;

public class ExportFragment extends Fragment {

    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_export, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        MaterialButton btnOrders = view.findViewById(R.id.btn_export_orders);
        MaterialButton btnProducts = view.findViewById(R.id.btn_export_products);

        btnOrders.setOnClickListener(v -> exportOrders());
        btnProducts.setOnClickListener(v -> exportProducts());
    }

    private void exportOrders() {
        db.collection("orders").get().addOnSuccessListener(qs -> {
            try {
                StringBuilder csv = new StringBuilder();
                csv.append("Order ID,Date,Status,Total,Customer,Email,Phone,City\n");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                for (DocumentSnapshot doc : qs.getDocuments()) {
                    String orderId = doc.getString("orderId");
                    String status = doc.getString("status");
                    Double total = doc.getDouble("totalAmount");
                    com.google.firebase.Timestamp ts = doc.getTimestamp("orderDate");

                    String date = ts != null ? sdf.format(ts.toDate()) : "N/A";
                    String customer = "N/A", email = "N/A", phone = "N/A", city = "N/A";

                    if (doc.contains("shippingAddress")) {
                        java.util.Map<String, Object> addr =
                                (java.util.Map<String, Object>) doc.get("shippingAddress");
                        if (addr != null) {
                            customer = String.valueOf(addr.getOrDefault("name", "N/A"));
                            email = String.valueOf(addr.getOrDefault("email", "N/A"));
                            phone = String.valueOf(addr.getOrDefault("contact", "N/A"));
                            city = String.valueOf(addr.getOrDefault("city", "N/A"));
                        }
                    }

                    csv.append(escape(orderId)).append(",")
                            .append(date).append(",")
                            .append(status).append(",")
                            .append(total != null ? total : 0).append(",")
                            .append(escape(customer)).append(",")
                            .append(escape(email)).append(",")
                            .append(escape(phone)).append(",")
                            .append(escape(city)).append("\n");
                }

                saveAndShare(csv.toString(), "orders_report.csv");

            } catch (Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exportProducts() {
        db.collection("products").get().addOnSuccessListener(qs -> {
            try {
                StringBuilder csv = new StringBuilder();
                csv.append("Product ID,Title,Category ID,Price,Stock,Rating\n");

                for (DocumentSnapshot doc : qs.getDocuments()) {
                    csv.append(escape(doc.getString("productId"))).append(",")
                            .append(escape(doc.getString("title"))).append(",")
                            .append(escape(doc.getString("categoryId"))).append(",")
                            .append(doc.getDouble("price")).append(",")
                            .append(doc.getLong("stockCount")).append(",")
                            .append(doc.getDouble("rating")).append("\n");
                }

                saveAndShare(csv.toString(), "products_report.csv");

            } catch (Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAndShare(String csvContent, String fileName) {
        try {
            File dir = getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(dir, fileName);

            FileWriter writer = new FileWriter(file);
            writer.write(csvContent);
            writer.close();

            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Share Report"));
            Toast.makeText(getContext(), "Report exported!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String escape(String s) {
        if (s == null) return "N/A";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}