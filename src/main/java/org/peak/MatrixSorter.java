package org.peak;

public class MatrixSorter {

    // Sorts assignments by time due vs importance
    // Q1 = Urgent + Important (Do Now)
    // Q2 = Urgent + Not Important (Do later?)
    // Q3 = Not Urgent + Important (Figure out when to do or move it into another quadrant)
    // Q4 = Not Urgent + Not Important ("Do last")

    //Important is anything that's over 5% of the grade
    //Urgent assignements are due in 3 days or less

    //Special Rules:
    //If a long assignment is due in a week, it's urgent
    //If a regular assignment is due tomorrow or the same day, it's important

    private static boolean isUrgent(Assignment a) {
        long days = a.daysUntilDue();

        //Type based rules
        if (a.getName().equals("long")) {
            return days <= 7;
        } else {
            return days <= 3; //Normal assignments (not projects or long assignments)
        }
    }

    private static boolean isImportant(Assignment a) {
        //Override rules: If due today or due tomorrow, automatically put in important
        long days = a.daysUntilDue();
        if (days <= 1) return true;

        return a.getGradePercent() > 5.0;
    }

    public static int getQuadrant(Assignment a) {
        boolean urgent = isUrgent(a);
        boolean important = isImportant(a);

        if (urgent && important) return 1;
        if (urgent && !important) return 2;
        if (!urgent && important) return 3;
        return 4;
    }
}
