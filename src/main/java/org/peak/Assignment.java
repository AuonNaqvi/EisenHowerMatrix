package org.peak;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Assignment {
    private String name;
    private LocalDate dueDate;
    private double gradePercent;
    private String type;

    //Constructor
    public Assignment(String name, LocalDate dueDate, double gradePercent, String type) {
        this.name = name;
        this.dueDate = dueDate;
        this.gradePercent = gradePercent;
        this.type = type;
    }

    // Getters
    public String getName() { return name; }
    public LocalDate getDueDate() { return dueDate; }
    public double getGradePercent() { return gradePercent; }
    public String getType() { return type; }

    // Helpers: Time till due
    public long daysUntilDue() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }


}
