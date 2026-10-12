package lk.jiat.bonsaimart_admin.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryAdmin implements Serializable {

    private String id;           // Document ID (for Firestore operations)
    private String categoryId;   // Business ID (e.g., "cat_xxx_123")
    private String name;
    private String imageUrl;
    private int productCount;

    @Override
    public String toString() {
        return name != null ? name : "Unnamed";
    }
}