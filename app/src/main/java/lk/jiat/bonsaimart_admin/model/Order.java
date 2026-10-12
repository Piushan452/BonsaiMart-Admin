package lk.jiat.bonsaimart_admin.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Serializable {

    private String orderId;
    private String userId;
    private double totalAmount;
    private String status;  // PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
    private Timestamp orderDate;
    private Timestamp deliveryDate;
    private List<OrderItem> orderItems;
    private Address shippingAddress;
    private Address billingAddress;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItem implements Serializable {
        private String productId;
        private String productName;
        private String productImage;
        private double unitPrice;
        private int quantity;
        private List<Attribute> attributes;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Attribute implements Serializable {
            private String name;
            private String value;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Address implements Serializable {
        private String name;
        private String email;
        private String contact;
        private String address1;
        private String address2;
        private String city;
        private String postcode;
    }
}