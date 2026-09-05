package org.peak;

import java.util.ArrayList;

public class MatrixDisplay {


    private static void printQuadrant(String title, ArrayList<Assignment> assignments) {
        System.out.println("┌─ " + title);

        if (assignments.isEmpty()) {
            System.out.println("│   (nothing here)");
        } else {
            for (Assignment a : assignments) {
                long days = a.daysUntilDue();

                // Build the "days" label
                String daysLabel;
                if (days < 0) {
                    daysLabel = "OVERDUE by " + Math.abs(days) + " day(s)";
                } else if (days == 0) {
                    daysLabel = "due TODAY";
                } else if (days == 1) {
                    daysLabel = "due TOMORROW";
                } else {
                    daysLabel = "due in " + days + " days";
                }

                System.out.printf("│   • %-30s | %5.1f%% | %s%n",
                        a.getName(), a.getGradePercent(), daysLabel);
            }
        }

        System.out.println("│");
    }

    public static void printMatrix(
            ArrayList<Assignment> q1, ArrayList<Assignment> q2,
            ArrayList<Assignment> q3, ArrayList<Assignment> q4
    ) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║         EISENHOWER MATRIX                ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        printQuadrant("Q1 - DO FIRST   (Urgent + Important)", q1);
        printQuadrant("Q2 - DELEGATE   (Urgent + Not Important)", q2);
        printQuadrant("Q3 - SCHEDULE   (Not Urgent + Important)", q3);
        printQuadrant("Q4 - DO LAST    (Not Urgent + Not Important)", q4);
    }

}
