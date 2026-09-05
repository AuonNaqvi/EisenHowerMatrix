package org.peak;

import java.util.ArrayList;

public class MatrixSorter {

    public static int getQuadrant(Assignment a, ArrayList<SchoolClass> classes) {
        boolean urgent    = isUrgent(a, classes);
        boolean important = isImportant(a, classes);

        if (urgent && important)   return 1;
        if (urgent && !important)  return 2;
        if (!urgent && important)  return 3;
        return 4;
    }

    private static boolean isUrgent(Assignment a, ArrayList<SchoolClass> classes) {
        long days = a.daysUntilDue();

        // Look up the category to get its type
        String type = getCategoryType(a, classes);

        if (type.equals("long")) {
            return days <= 7;
        } else {
            return days <= 3; // "normal" is the default
        }
    }

    private static boolean isImportant(Assignment a, ArrayList<SchoolClass> classes) {
        long days = a.daysUntilDue();

        // Override: due today or tomorrow = always important regardless of type
        if (days <= 1) return true;
        for (SchoolClass sc : classes) {
            if (sc.getName().equalsIgnoreCase(a.getClassName())) {
                Category cat = sc.findCategory(a.getCategoryName());
                if (cat != null) return cat.isImportant();
            }
        }
        return false;
    }

    // Looks up the type from the category — defaults to "normal" if not found
    private static String getCategoryType(Assignment a, ArrayList<SchoolClass> classes) {
        for (SchoolClass sc : classes) {
            if (sc.getName().equalsIgnoreCase(a.getClassName())) {
                Category cat = sc.findCategory(a.getCategoryName());
                if (cat != null) return cat.getType();
            }
        }
        return "normal";
    }
}