package lk.jiat.bonsaimart_admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    private String id;
    private String name;

    @Override
    public String toString() {
        return name;
    }
}