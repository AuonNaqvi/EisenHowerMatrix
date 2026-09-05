package org.peak;

import java.util.ArrayList;

public class SchoolClass {

    private String name;
    private ArrayList<Category> categories;

    public SchoolClass(String name) {
        this.name = name;
        this.categories = new ArrayList<>();
    }

    public String getName() { return name; }
    public ArrayList<Category> getCategories() { return categories; }

    public void addCategory(Category c) {
        categories.add(c);
    }

    // Returns a category by name (case-insensitive), or null if not found
    public Category findCategory(String categoryName) {
        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(categoryName)) return c;
        }
        return null;
    }
}