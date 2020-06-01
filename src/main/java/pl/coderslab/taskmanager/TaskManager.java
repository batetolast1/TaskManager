package pl.coderslab.taskmanager;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class TaskManager {

    private static String[][] tasks = new String[0][];
    private static Path path;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println(ConsoleColors.PURPLE + "Task Manager 1.1\n");
        System.out.println(ConsoleColors.BLUE + "Please type csv file name to open: ");

        // loading data from file
        while (scanner.hasNextLine()) {
            String filename = scanner.nextLine(); // getting filename
            if (isValidFilename(filename)) { // validating filename (*.csv)
                path = Paths.get(filename);
                if (Files.exists(path)) { // checking if file exists
                    if (getTasks()) { // trying to get tasks from existing file
                        break;
                    }
                } else {
                    if (askToCreateNewFile()) { // asking if user wants to create new file
                        break;
                    }
                }
            }
            System.out.println(ConsoleColors.BLUE + "Please type csv file name to open: ");
        }

        // managing tasks
        printOptions(); // printing available options
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().toLowerCase();
            switch (input) {
                case "add":
                    addTask(); // adding taks
                    break;
                case "remove":
                    removeTask(); // removing task
                    break;
                case "list":
                    listTasks(); // listing all tasks
                    break;
                case "exit":
                    if (saveTasks()) { // trying to save tasks to file
                        scanner.close();
                        System.exit(0); // quitting
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
        if (filename.matches(".+.csv")) {
            return true;
        } else {
            System.out.println(ConsoleColors.RED + "Filename is invalid!");
            return false;
        }
    }

    private static boolean getTasks() {
        // reading lines from file into ArrayList
        ArrayList<String> taskList;
        try {
            taskList = new ArrayList<>(Files.readAllLines(path));
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED + "I/O error, try again.");
            return false;
        }

        // validating imported lines
        int validTasksCounter = 0;
        for (int i = 0; i < taskList.size(); i++) {
            String task = taskList.get(i).replaceAll("\"\"", "\"").replaceAll("\",\"", ","); // parsing line (changing "" into " and "," into ')
            String[] taskArray = task.split(",");
            int length = taskArray.length;
            if ((length >= 3) && (isValidDate(taskArray[length - 2])) && isBoolean(taskArray[length - 1])) { // validating data
                String[] validTask = new String[3]; // creating array with valid task data
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j <= length - 3; j++) { // connecting description parts into one string
                    if (j != length - 3) {
                        stringBuilder.append(taskArray[j]).append(",");
                    } else {
                        stringBuilder.append(taskArray[j]);
                    }
                }
                validTask[0] = stringBuilder.toString(); // adding description
                validTask[1] = taskArray[length - 2]; // adding data
                validTask[2] = taskArray[length - 1]; // adding task importance
                tasks = Arrays.copyOf(tasks, tasks.length + 1);
                tasks[validTasksCounter] = validTask; // different data storage
                validTasksCounter++;
            } else {
                System.out.println(ConsoleColors.RED + "Error while parsing line" + i + ": " + task + ". Line has invalid data!");
            }
        }
        System.out.println(ConsoleColors.GREEN + "File successfully loaded!");
        return true;
    }

    private static boolean isValidDate(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        try {
            dateTimeFormatter.parse(date);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    private static boolean isBoolean(String str) {
        return ((str.toLowerCase().equals("true")) || (str.toLowerCase().equals("false")));
    }

    private static boolean askToCreateNewFile() {
        System.out.println(ConsoleColors.YELLOW + "File not found! Do you want to create new file: " + path + " (yes/no)?");
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().toLowerCase();
            //noinspection EnhancedSwitchMigration
            switch (input) {
                case "yes":
                    try {
                        Files.createFile(path);
                    } catch (IOException e) {
                        System.out.println(ConsoleColors.RED + "Unexpected error: can't create new file!");
                        return false;
                    }
                    System.out.println(ConsoleColors.GREEN + "File successfully created!");
                    return true;
                case "no":
                    System.out.println(ConsoleColors.YELLOW + "File wasn't created.");
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

    private static void listTasks() {
        if (tasks.length == 0) {
            System.out.println(ConsoleColors.YELLOW + "Task list is empty! Please add new task.");
        } else {
            for (int i = 0; i < tasks.length; i++) {
                System.out.println(i + " : " + tasks[i][0] + " " + tasks[i][1] + " " + tasks[i][2]);
            }
        }
    }

    private static void addTask() {
        tasks = Arrays.copyOf(tasks, tasks.length + 1);
        String[] newTask = new String[3];

        // description
        System.out.println(ConsoleColors.RESET + "Please add task description:");
        while (scanner.hasNextLine()) {
            String desc = scanner.nextLine();
            if (!desc.matches("\\s*")) {
                newTask[0] = desc;
                break;
            } else {
                System.out.println(ConsoleColors.YELLOW + "Please add taks description!");
            }
        }

        // date
        System.out.println(ConsoleColors.RESET + "Please add task due date:");
        while (scanner.hasNextLine()) {
            String date = scanner.nextLine();
            if (isValidDate(date)) {
                newTask[1] = date;
                break;
            } else {
                System.out.println(ConsoleColors.YELLOW + "Please add proper task due date (YYYY-MM-DD):");
            }
        }

        // true/false
        System.out.println(ConsoleColors.RESET + "Is your task important:");
        while (scanner.hasNextLine()) {
            String str = scanner.nextLine();
            if (isBoolean(str)) {
                newTask[2] = str;
                break;
            }
            System.out.println(ConsoleColors.YELLOW + "Is your task important (true/false):");
        }

        // adding new task to tasks list
        tasks[tasks.length - 1] = newTask;
        System.out.println(ConsoleColors.GREEN + "Task was successfully added.");
    }

    private static void removeTask() {
        if (tasks.length == 0) {
            System.out.println(ConsoleColors.YELLOW + "Task list is empty! Please add new task.");
        } else if (tasks.length == 1) {
            tasks = new String[0][];
            System.out.println(ConsoleColors.GREEN + "Task 0 was successfully deleted.");
        } else {
            System.out.println(ConsoleColors.RESET + "Please select task's number to remove.");
            int index = 0;
            while (scanner.hasNextLine()) {
                String str = scanner.nextLine();
                try {
                    index = Integer.parseInt(str);
                    if ((index >= 0) && (index < tasks.length)) {
                        break;
                    } else {
                        System.out.println(ConsoleColors.YELLOW + "Please select proper task's number to remove! (0-" + (tasks.length - 1) + ")");
                    }
                } catch (NumberFormatException e) {
                    System.out.print(ConsoleColors.YELLOW + "Please select proper task's number to remove!");
                }
            }
            tasks = ArrayUtils.remove(tasks, index);
            System.out.println(ConsoleColors.GREEN + "Task " + index + " was successfully deleted.");
        }
    }

    private static boolean saveTasks() {
        ArrayList<String> taskList = new ArrayList<>();

        // parsing tasks into lines
        for (String[] task : tasks) {
            String desc = task[0].replaceAll(",", "\",\"").replaceAll("\"", "\"\"");
            String parsedLine = desc + "," + task[1] + "," + task[2];
            taskList.add(parsedLine);
        }

        //saving parsed lines to file
        try {
            Files.write(path, taskList);
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED + "Unexpected error: can't save tasks to file!");
            return false;
        }
        System.out.println(ConsoleColors.GREEN + "File successfully saved! See you soon! :)");
        return true;
    }
}