package org.peak;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class App {

    static ArrayList<SchoolClass> classes   = new ArrayList<>();
    static ArrayList<Assignment>  q1        = new ArrayList<>();
    static ArrayList<Assignment>  q2        = new ArrayList<>();
    static ArrayList<Assignment>  q3        = new ArrayList<>();
    static ArrayList<Assignment>  q4        = new ArrayList<>();
    static ArrayList<Assignment>  completed = new ArrayList<>();

    static Scanner scanner = new Scanner(System.in);
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {

        // Pull latest CSVs from GitHub before loading anything
        GitHubSync.pull();

        // Load everything on startup
        classes = FileManager.loadClasses();
        FileManager.loadCategories(classes);
        FileManager.loadActive(q1, q2, q3, q4, classes);
        completed = FileManager.loadCompleted();

        int activeCount = q1.size() + q2.size() + q3.size() + q4.size();
        System.out.println("Welcome to your Eisenhower Matrix!");
        System.out.println("Loaded " + classes.size() + " class(es), "
                + activeCount + " active assignment(s), "
                + completed.size() + " completed assignment(s).");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": manageClassesMenu(); break;
                case "2": addAssignment();     break;
                case "3": MatrixDisplay.printMatrix(q1, q2, q3, q4); break;
                case "4": completeAssignment(); break;
                case "5": MatrixDisplay.printCompleted(completed); break;
                case "6":
                    // Push updated CSVs to GitHub before closing
                    GitHubSync.push();
                    System.out.println("Goodbye! Good luck with your assignments.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice — type a number 1 through 6.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n----- MENU -----");
        System.out.println("1. Manage classes & categories");
        System.out.println("2. Add assignment");
        System.out.println("3. View matrix");
        System.out.println("4. Complete an assignment");
        System.out.println("5. View completed assignments");
        System.out.println("6. Exit");
        System.out.print("Choice: ");
    }

    // --- CLASSES & CATEGORIES SUBMENU -------------------------

    private static void manageClassesMenu() {
        boolean inSubmenu = true;
        while (inSubmenu) {
            System.out.println("\n----- CLASSES & CATEGORIES -----");
            System.out.println("1. Add a class");
            System.out.println("2. Add a category to a class");
            System.out.println("3. View all classes & categories");
            System.out.println("4. Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1": addClass();                break;
                case "2": addCategory();             break;
                case "3": viewClassesAndCategories(); break;
                case "4": inSubmenu = false;         break;
                default: System.out.println("Type 1, 2, 3, or 4.");
            }
        }
    }

    private static void addClass() {
        System.out.print("Class name: ");
        String name = scanner.nextLine().trim();

        if (findClass(name) != null) {
            System.out.println("\"" + name + "\" already exists - using the existing one.");
            return;
        }

        classes.add(new SchoolClass(name));
        FileManager.saveClasses(classes);
        System.out.println("✓ Class \"" + name + "\" added.");
    }

    private static void addCategory() {
        if (classes.isEmpty()) {
            System.out.println("No classes yet - add a class first.");
            return;
        }

        SchoolClass sc = pickClass("Which class is this category for?");
        if (sc == null) return;

        System.out.print("Category name: ");
        String catName = scanner.nextLine().trim();

        if (sc.findCategory(catName) != null) {
            System.out.println("\"" + catName + "\" already exists in " + sc.getName() + ".");
            return;
        }

        // Ask for both importance and type together
        boolean important = pickImportance();
        String type = pickType();

        Category cat = new Category(catName, sc.getName(), important, type);
        sc.addCategory(cat);
        FileManager.saveCategories(classes);

        System.out.println("✓ Category \"" + catName + "\" ["
                + cat.getImportanceLabel() + " / " + type + "] added to " + sc.getName() + ".");
    }

    private static void viewClassesAndCategories() {
        if (classes.isEmpty()) {
            System.out.println("No classes set up yet.");
            return;
        }
        System.out.println();
        for (SchoolClass sc : classes) {
            System.out.println("  📚 " + sc.getName());
            if (sc.getCategories().isEmpty()) {
                System.out.println("      (no categories yet)");
            } else {
                for (Category cat : sc.getCategories()) {
                    System.out.println("      • " + cat.getName()
                            + " [" + cat.getImportanceLabel() + " / " + cat.getType() + "]");
                }
            }
        }
    }

    // --- ADD ASSIGNMENT ---------------------------------------

    private static void addAssignment() {
        SchoolClass sc = pickOrCreateClass();
        if (sc == null) return;

        Category cat = pickOrCreateCategory(sc);
        if (cat == null) return;

        System.out.print("Assignment name: ");
        String name = scanner.nextLine().trim();

        LocalDate dueDate = null;
        while (dueDate == null) {
            System.out.print("Due date (yyyy-MM-dd, e.g. 2026-09-10): ");
            try {
                dueDate = LocalDate.parse(scanner.nextLine().trim(), formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format. Use yyyy-MM-dd like 2026-09-10");
            }
        }

        // No type prompt here - inherited from category
        Assignment a = new Assignment(name, dueDate, sc.getName(), cat.getName());
        int quadrant = MatrixSorter.getQuadrant(a, classes);

        switch (quadrant) {
            case 1: q1.add(a); break;
            case 2: q2.add(a); break;
            case 3: q3.add(a); break;
            case 4: q4.add(a); break;
        }

        FileManager.saveActive(q1, q2, q3, q4);
        System.out.println("✓ Added \"" + name + "\" → "
                + sc.getName() + " > " + cat.getName()
                + " → Q" + quadrant);
    }

    // --- COMPLETE ASSIGNMENT ----------------------------------

    private static void completeAssignment() {
        ArrayList<Assignment> flat = MatrixDisplay.printNumberedList(q1, q2, q3, q4);
        if (flat.isEmpty()) return;

        System.out.print("\nEnter the number to mark complete (0 to cancel): ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input - returning to menu.");
            return;
        }

        if (choice == 0) return;
        if (choice < 1 || choice > flat.size()) {
            System.out.println("Number out of range - returning to menu.");
            return;
        }

        Assignment done = flat.get(choice - 1);
        q1.remove(done);
        q2.remove(done);
        q3.remove(done);
        q4.remove(done);
        completed.add(done);

        FileManager.saveActive(q1, q2, q3, q4);
        FileManager.saveCompleted(done);

        System.out.println("✓ \"" + done.getName() + "\" marked complete");
    }

    // --- HELPER: PICK / CREATE CLASS --------------------------

    private static SchoolClass pickOrCreateClass() {
        System.out.println("\nClasses:");
        if (classes.isEmpty()) {
            System.out.println("  (none yet)");
        } else {
            for (int i = 0; i < classes.size(); i++) {
                System.out.printf("  %2d. %s%n", i + 1, classes.get(i).getName());
            }
        }
        System.out.print("Pick a number, or type a new class name: ");
        String input = scanner.nextLine().trim();

        try {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= classes.size()) return classes.get(idx - 1);
            System.out.println("Number out of range.");
            return null;
        } catch (NumberFormatException e) {
            SchoolClass existing = findClass(input);
            if (existing != null) {
                System.out.println("Using existing class \"" + existing.getName() + "\".");
                return existing;
            }
            SchoolClass newClass = new SchoolClass(input);
            classes.add(newClass);
            FileManager.saveClasses(classes);
            System.out.println("✓ New class \"" + input + "\" created.");
            return newClass;
        }
    }

    private static SchoolClass pickClass(String prompt) {
        System.out.println("\n" + prompt);
        for (int i = 0; i < classes.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, classes.get(i).getName());
        }
        System.out.print("Pick a number: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx >= 1 && idx <= classes.size()) return classes.get(idx - 1);
            System.out.println("Number out of range.");
        } catch (NumberFormatException e) {
            System.out.println("Please enter a number.");
        }
        return null;
    }

    // --- HELPER: PICK / CREATE CATEGORY ----------------------

    private static Category pickOrCreateCategory(SchoolClass sc) {
        ArrayList<Category> cats = sc.getCategories();

        System.out.println("\nCategories in " + sc.getName() + ":");
        if (cats.isEmpty()) {
            System.out.println("  (none yet)");
        } else {
            for (int i = 0; i < cats.size(); i++) {
                System.out.printf("  %2d. %-20s [%s / %s]%n",
                        i + 1,
                        cats.get(i).getName(),
                        cats.get(i).getImportanceLabel(),
                        cats.get(i).getType());
            }
        }
        System.out.print("Pick a number, or type a new category name: ");
        String input = scanner.nextLine().trim();

        try {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= cats.size()) return cats.get(idx - 1);
            System.out.println("Number out of range.");
            return null;
        } catch (NumberFormatException e) {
            Category existing = sc.findCategory(input);
            if (existing != null) {
                System.out.println("Using existing category \"" + existing.getName() + "\".");
                return existing;
            }
            // New category - ask for both importance and type
            boolean important = pickImportance();
            String type = pickType();
            Category newCat = new Category(input, sc.getName(), important, type);
            sc.addCategory(newCat);
            FileManager.saveCategories(classes);
            System.out.println("✓ New category \"" + input + "\" ["
                    + newCat.getImportanceLabel() + " / " + type + "] created.");
            return newCat;
        }
    }

    // --- HELPER: IMPORTANCE & TYPE PROMPTS --------------------

    private static boolean pickImportance() {
        while (true) {
            System.out.print("Important? (y/n): ");
            String ans = scanner.nextLine().trim().toLowerCase();
            if (ans.equals("y") || ans.equals("yes")) return true;
            if (ans.equals("n") || ans.equals("no"))  return false;
            System.out.println("Please type y or n.");
        }
    }

    private static String pickType() {
        while (true) {
            System.out.print("Assignment length - 'normal' (homework, quiz) or 'long' (project, paper): ");
            String ans = scanner.nextLine().trim().toLowerCase();
            if (ans.equals("normal") || ans.equals("long")) return ans;
            System.out.println("Please type exactly: normal  or  long");
        }
    }

    // --- HELPER: FIND CLASS BY NAME ---------------------------

    private static SchoolClass findClass(String name) {
        for (SchoolClass sc : classes) {
            if (sc.getName().equalsIgnoreCase(name)) return sc;
        }
        return null;
    }
}