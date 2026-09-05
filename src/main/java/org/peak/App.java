package org.peak;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class App {

    // One list per quadrant — assignments get sorted into these
    static ArrayList<Assignment> q1 = new ArrayList<>();
    static ArrayList<Assignment> q2 = new ArrayList<>();
    static ArrayList<Assignment> q3 = new ArrayList<>();
    static ArrayList<Assignment> q4 = new ArrayList<>();

    static Scanner scanner = new Scanner(System.in);
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        System.out.println("Welcome to your Eisenhower Matrix!");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addAssignment();
                    break;
                case "2":
                    MatrixDisplay.printMatrix(q1, q2, q3, q4);
                    break;
                case "3":
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice — type 1, 2, or 3.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n----- MENU -----");
        System.out.println("1. Add assignment");
        System.out.println("2. View matrix");
        System.out.println("3. Exit");
        System.out.print("Choice: ");
    }

    private static void addAssignment() {
        System.out.print("Assignment name: ");
        String name = scanner.nextLine().trim();

        // Grade percent — keep asking until they enter a valid number
        double gradePercent = 0;
        while (true) {
            System.out.print("Grade % this is worth in your class (e.g. 25): ");
            try {
                gradePercent = Double.parseDouble(scanner.nextLine().trim());
                break;  // Input was valid, exit the loop
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number like 25 or 10.5");
            }
        }

        // Due date — keep asking until they enter a valid date
        LocalDate dueDate = null;
        while (dueDate == null) {
            System.out.print("Due date (yyyy-MM-dd, e.g. 2026-09-10): ");
            try {
                dueDate = LocalDate.parse(scanner.nextLine().trim(), formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use yyyy-MM-dd like 2026-09-10");
            }
        }

        // Assignment type
        String type = "";
        while (!type.equals("normal") && !type.equals("long")) {
            System.out.print("Assignment type — 'normal' (homework, quiz) or 'long' (project, paper): ");
            type = scanner.nextLine().trim().toLowerCase();
            if (!type.equals("normal") && !type.equals("long")) {
                System.out.println("Please type exactly: normal  or  long");
            }
        }

        // Build the assignment and sort it
        Assignment a = new Assignment(name, dueDate, gradePercent, type);
        int quadrant = MatrixSorter.getQuadrant(a);

        // Drop it into the right list
        switch (quadrant) {
            case 1: q1.add(a); break;
            case 2: q2.add(a); break;
            case 3: q3.add(a); break;
            case 4: q4.add(a); break;
        }

        System.out.println("✓ Added \"" + name + "\" → Q" + quadrant);
    }
}