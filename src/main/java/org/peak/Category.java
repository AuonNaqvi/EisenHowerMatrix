package org.peak;

public class Category {

    private String name;
    private String className;
    private boolean important;
    private String type;        // "normal" or "long" — set once, inherited by all assignments

    public Category(String name, String className, boolean important, String type) {
        this.name = name;
        this.className = className;
        this.important = important;
        this.type = type;
    }

    public String getName()             { return name; }
    public String getClassName()        { return className; }
    public boolean isImportant()        { return important; }
    public String getType()             { return type; }

    public String getImportanceLabel() {
        return important ? "Important" : "Not Important";
    }
}