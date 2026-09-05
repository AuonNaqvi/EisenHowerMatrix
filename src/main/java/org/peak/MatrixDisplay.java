package org.peak;

import java.util.ArrayList;

public class MatrixDisplay {

    public static void printMatrix(
            ArrayList<Assignment> q1, ArrayList<Assignment> q2,
            ArrayList<Assignment> q3, ArrayList<Assignment> q4
    ) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║           EISENHOWER MATRIX              ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        printQuadrant("Q1 — DO FIRST   (Urgent + Important)",         q1);
        printQuadrant("Q2 — DELEGATE   (Urgent + Not Important)",     q2);
        printQuadrant("Q3 — SCHEDULE   (Not Urgent + Important)",     q3);
        printQuadrant("Q4 — DO LAST    (Not Urgent + Not Important)", q4);
    }

    private static void printQuadrant(String title, ArrayList<Assignment> assignments) {
        System.out.println("┌- " + title);
        if (assignments.isEmpty()) {
            System.out.println("│   (nothing here)");
        } else {
            for (Assignment a : assignments) {
                System.out.println("│   " + formatAssignment(a));
            }
        }
        System.out.println("│");
    }

    public static ArrayList<Assignment> printNumberedList(
            ArrayList<Assignment> q1, ArrayList<Assignment> q2,
            ArrayList<Assignment> q3, ArrayList<Assignment> q4
    ) {
        ArrayList<Assignment> flat = new ArrayList<>();
        flat.addAll(q1);
        flat.addAll(q2);
        flat.addAll(q3);
        flat.addAll(q4);

        if (flat.isEmpty()) {
            System.out.println("No active assignments.");
            return flat;
        }

        System.out.println("\nActive assignments:");
        for (int i = 0; i < flat.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, formatAssignment(flat.get(i)));
        }
        return flat;
    }

    public static void printCompleted(ArrayList<Assignment> completed) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║         COMPLETED ASSIGNMENTS            ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        if (completed.isEmpty()) {
            System.out.println("  Nothing completed yet — get to work! 📚");
        } else {
            for (Assignment a : completed) {
                System.out.println("  ✓ " + formatAssignment(a));
            }
        }
        System.out.println();
    }

    // e.g.  "HW 4  [Calculus > Homework]  due in 3 days"
    private static String formatAssignment(Assignment a) {
        long days = a.daysUntilDue();

        String daysLabel;
        if (days < 0)       daysLabel = "OVERDUE by " + Math.abs(days) + " day(s)";
        else if (days == 0) daysLabel = "due TODAY";
        else if (days == 1) daysLabel = "due TOMORROW";
        else                daysLabel = "due in " + days + " days";

        return String.format("%-28s [%s > %s] | %s",
                a.getName(),
                a.getClassName(),
                a.getCategoryName(),
                daysLabel
        );
    }
}