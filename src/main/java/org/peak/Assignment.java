package org.peak;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Assignment {

    private String name;
    private LocalDate dueDate;
    private String className;
    private String categoryName;

    public Assignment(String name, LocalDate dueDate, String className, String categoryName) {
        this.name = name;
        this.dueDate = dueDate;
        this.className = className;
        this.categoryName = categoryName;
    }

    public String getName()         { return name; }
    public LocalDate getDueDate()   { return dueDate; }
    public String getClassName()    { return className; }
    public String getCategoryName() { return categoryName; }

    public long daysUntilDue() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }
}