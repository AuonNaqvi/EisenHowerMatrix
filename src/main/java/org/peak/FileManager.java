package org.peak;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class FileManager {

    private static final String CLASSES_FILE    = "classes.csv";
    private static final String CATEGORIES_FILE = "categories.csv";
    private static final String ACTIVE_FILE     = "assignments.csv";
    private static final String COMPLETED_FILE  = "completed.csv";

    // --- SAVE -------------------------------------------------

    public static void saveClasses(ArrayList<SchoolClass> classes) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CLASSES_FILE))) {
            for (SchoolClass sc : classes) {
                writer.println(sc.getName());
            }
        } catch (IOException e) {
            System.out.println("Warning: could not save classes. " + e.getMessage());
        }
    }

    public static void saveCategories(ArrayList<SchoolClass> classes) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CATEGORIES_FILE))) {
            for (SchoolClass sc : classes) {
                for (Category cat : sc.getCategories()) {
                    // format: categoryName,className,important,type
                    writer.println(
                            cat.getName()        + "," +
                                    cat.getClassName()   + "," +
                                    cat.isImportant()    + "," +
                                    cat.getType()
                    );
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not save categories. " + e.getMessage());
        }
    }

    public static void saveActive(
            ArrayList<Assignment> q1, ArrayList<Assignment> q2,
            ArrayList<Assignment> q3, ArrayList<Assignment> q4
    ) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ACTIVE_FILE))) {
            for (Assignment a : q1) writeAssignment(writer, a);
            for (Assignment a : q2) writeAssignment(writer, a);
            for (Assignment a : q3) writeAssignment(writer, a);
            for (Assignment a : q4) writeAssignment(writer, a);
        } catch (IOException e) {
            System.out.println("Warning: could not save assignments. " + e.getMessage());
        }
    }

    public static void saveCompleted(Assignment a) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(COMPLETED_FILE, true))) {
            writeAssignment(writer, a);
        } catch (IOException e) {
            System.out.println("Warning: could not save completed assignment. " + e.getMessage());
        }
    }

    private static void writeAssignment(PrintWriter writer, Assignment a) {
        // format: name,dueDate,className,categoryName  (type removed — lives on category now)
        writer.println(
                a.getName()         + "," +
                        a.getDueDate()      + "," +
                        a.getClassName()    + "," +
                        a.getCategoryName()
        );
    }

    // --- LOAD -------------------------------------------------

    public static ArrayList<SchoolClass> loadClasses() {
        ArrayList<SchoolClass> classes = new ArrayList<>();
        File file = new File(CLASSES_FILE);
        if (!file.exists()) return classes;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    classes.add(new SchoolClass(line.trim()));
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not load classes. " + e.getMessage());
        }
        return classes;
    }

    public static void loadCategories(ArrayList<SchoolClass> classes) {
        File file = new File(CATEGORIES_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                String catName    = parts[0];
                String className  = parts[1];
                boolean important = Boolean.parseBoolean(parts[2]);
                String type       = parts[3];

                for (SchoolClass sc : classes) {
                    if (sc.getName().equalsIgnoreCase(className)) {
                        sc.addCategory(new Category(catName, className, important, type));
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not load categories. " + e.getMessage());
        }
    }

    public static void loadActive(
            ArrayList<Assignment> q1, ArrayList<Assignment> q2,
            ArrayList<Assignment> q3, ArrayList<Assignment> q4,
            ArrayList<SchoolClass> classes
    ) {
        File file = new File(ACTIVE_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                String name         = parts[0];
                LocalDate dueDate   = LocalDate.parse(parts[1]);
                String className    = parts[2];
                String categoryName = parts[3];

                Assignment a = new Assignment(name, dueDate, className, categoryName);

                int q = MatrixSorter.getQuadrant(a, classes);
                switch (q) {
                    case 1: q1.add(a); break;
                    case 2: q2.add(a); break;
                    case 3: q3.add(a); break;
                    case 4: q4.add(a); break;
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not load assignments. " + e.getMessage());
        }
    }

    public static ArrayList<Assignment> loadCompleted() {
        ArrayList<Assignment> completed = new ArrayList<>();
        File file = new File(COMPLETED_FILE);
        if (!file.exists()) return completed;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                completed.add(new Assignment(
                        parts[0],
                        LocalDate.parse(parts[1]),
                        parts[2],
                        parts[3]
                ));
            }
        } catch (IOException e) {
            System.out.println("Warning: could not load completed assignments. " + e.getMessage());
        }
        return completed;
    }
}