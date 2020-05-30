package pl.coderslab.taskmanager;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/*Simple task - very important, 2020-03-09, true
Second task not so important, 2020-05-10, false
Throw away trash, 2020-03-09, false*/

public class TaskManager {

    public static void main(String[] args) {
        Path path = null; // file path
        Scanner scanner = new Scanner(System.in);
        String[][] tasks = new String[0][]; // array with tasks

        // loading data

        System.out.println(ConsoleColors.BLUE + "Task Manager 1.0\nPlease type csv file name to open: ");
        while (scanner.hasNext()) {
            String filename = scanner.nextLine();
            if (isValidFilename(filename)) { // validating filename
                path = Paths.get(filename);
                if (Files.exists(path)) { // if file exists, trying to get tasks from file
                    try {
                        tasks = getTasks(path);
                        System.out.println(ConsoleColors.GREEN + "File successfully loaded!");
                        break;
                    } catch (IOException e) {
                        System.out.println(ConsoleColors.RED + "I/O error, try again.");
                    }
                } else {
                    if (askToCreateNewFile(path)) { // if file doesn't exist, asking if user wants to create new file
                        break;
                    }
                }
            }
            System.out.println(ConsoleColors.YELLOW + "Please type proper csv file name to open: ");
        }

        // main program

        printOptions();
        while (scanner.hasNext()) {
            String input = scanner.nextLine().toLowerCase();
            switch (input) {
                case "add":
                    tasks = addTask(tasks);
                    break;
                case "remove":
                    tasks = removeTask(tasks);
                    break;
                case "list":
                    list(tasks);
                    break;
                case "exit":
                    if (saveTasks(tasks, path)) { // trying to save file
                        scanner.close();
                        System.exit(0); // quit
                    } else {
                        break;
                    }
                default:
                    System.out.println(ConsoleColors.YELLOW + "Not a valid option!");
            }
            printOptions();
        }
    }

    private static boolean isValidFilename(String filename) {
        return (filename.matches(".+.csv"));
    }

    private static String[][] getTasks(Path path) throws IOException {
        ArrayList<String> array = new ArrayList<>(Files.readAllLines(path)); // IntelliJ suggestion
        String[][] data = new String[array.size()][];
        for (int i = 0; i < data.length; i++) {
            String str = array.get(i);
            data[i] = str.split(", ");
        }
        return data;
    }

    private static boolean askToCreateNewFile(Path path) {
        System.out.println(ConsoleColors.YELLOW + "File not found! Do you want to create new file: " + path + " (yes/no)?");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String input = scanner.nextLine().toLowerCase();
            switch (input) {
                case "yes":
                    try {
                        Files.createFile(path);
                        System.out.println(ConsoleColors.GREEN + "File successfully created!");
                    } catch (IOException e) {
                        System.out.println(ConsoleColors.RED + "Unexpected error: can't create new file!");
                    }
                    return true;
                case "no":
                    return false;
                default:
                    System.out.println(ConsoleColors.YELLOW + "Please select a correct option. Do you want to create new file: " + path + " (yes/no)?");
            }
        }
        return false;
    }

    private static void printOptions() {
        System.out.println(ConsoleColors.BLUE + "Please select an option:");
        System.out.println(ConsoleColors.RESET + "add\nremove\nlist\nexit");
    }

    private static void list(String[][] tasks) {
        if (tasks.length == 0) {
            System.out.println(ConsoleColors.YELLOW + "Task list is empty! Please add new task.");
            return;
        }
        for (int i = 0; i < tasks.length; i++) {
            System.out.println(i + " : " + tasks[i][0] + " " + tasks[i][1] + " " + tasks[i][2]);
        }
    }

    private static String[][] addTask(String[][] tasks) {
        tasks = Arrays.copyOf(tasks, tasks.length + 1);
        String[] newTask = new String[3];
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please add task description:");
        while (scanner.hasNext()) {

            String desc = scanner.nextLine();
            if (!desc.contains(",")) {
                newTask[0] = desc;
                break;
            }
            System.out.println(ConsoleColors.YELLOW + "Please add proper task description (without comma):");
        }

        System.out.println(ConsoleColors.RESET + "Please add task due date:");
        while (scanner.hasNext()) {
            String date = scanner.nextLine();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                dateFormat.parse(date);
                newTask[1] = date;
                break;
            } catch (ParseException e) {
                System.out.println(ConsoleColors.YELLOW + "Please add proper task due date (YYYY-MM-DD):");
            }
        }

        System.out.println(ConsoleColors.RESET + "Is your task important:");
        while (scanner.hasNext()) {
            String str = scanner.nextLine().toLowerCase();
            if ((str.equals("true")) || str.equals("false")) {
                newTask[2] = str;
                break;
            }
            System.out.println(ConsoleColors.YELLOW + "Is your task important (true/false):");
        }

        tasks[tasks.length - 1] = newTask;
        System.out.println(ConsoleColors.GREEN + "Task was successfully added.");
        return tasks;
    }

    private static String[][] removeTask(String[][] tasks) {
        if (tasks.length == 0) {
            System.out.println(ConsoleColors.YELLOW + "Task list is empty! Please add new task.");
            return tasks;
        }

        Scanner scanner = new Scanner(System.in);
        int index = 0;
        System.out.println("Please select task's number to remove.");

        while (scanner.hasNext()) {
            String str = scanner.nextLine();
            try {
                index = Integer.parseInt(str);
                int length = tasks.length;
                if ((index >= 0) && (index < length)) {
                    break;
                } else {
                    System.out.print(ConsoleColors.YELLOW + "Please select proper task's number to remove (");
                    System.out.println((length != 1) ? ("0 - " + (length - 1) + "):") : "0):");
                }
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.YELLOW + "Please select proper task's number to remove.");
            }
        }

        tasks = ArrayUtils.remove(tasks, index);
        System.out.println(ConsoleColors.GREEN + "Task " + index + " was successfully deleted.");
        return tasks;
    }

    private static boolean saveTasks(String[][] tasks, Path path) {
        ArrayList<String> data = new ArrayList<>();
        for (String[] task : tasks) {
            data.add(task[0] + ", " + task[1] + ", " + task[2]);
        }
        try {
            Files.write(path, data);
            System.out.println(ConsoleColors.GREEN + "File successfully saved! See you soon! :)");
            return true;
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED + "Unexpected error: can't save tasks to file! :(");
            return false;
        }
    }
}