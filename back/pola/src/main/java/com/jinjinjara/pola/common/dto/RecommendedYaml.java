package com.jinjinjara.pola.common.dto;

import lombok.Getter; import lombok.Setter;
import java.util.List;

@Getter @Setter
public class RecommendedYaml {
    private int version;
    private String locale;
    private List<Category> categories;

    @Getter @Setter
    public static class Category {
        private String name;
        private List<String> tags;
    }
}
