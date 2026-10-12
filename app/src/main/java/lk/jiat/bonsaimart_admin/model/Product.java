package lk.jiat.bonsaimart_admin.model;

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
public class Product implements Serializable {

    private String productId;
    private String categoryId;
    private String description;
    private List<String> images;
    private double price;
    private double rating;
    private boolean status;
    private int stockCount;
    private String title;
}