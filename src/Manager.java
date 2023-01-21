import java.util.Objects;
import java.util.Scanner;

public class Manager {

    static final String keyword = "pswrd";
    public static String getCommand(boolean isExiting) {
        String command;
        Scanner scan = new Scanner(System.in);
        command = scan.nextLine();
        if (isExiting) {
            scan.close();
        }
        return command;
    }

    public static void wrongCommand(String command, char type) {
        switch (type) {
            case 'a':
                System.err.println("Action [" + command + "] not found.");
            case 'c':
                System.err.println("Command [" + command + "] not found.");
        }
    }


    public static boolean checkParent(String command) {
        int keyLength = keyword.length();
        if (command.length() > keyLength) {
            String parentComm = command.substring(0, keyLength);
            return parentComm.equals(keyword);
        }
        else {
            System.err.println("Command is too short!");
            return false;
        }
    }

    public static String detectAction(String command) {
        String[] splitArr = command.split(" ");
        return splitArr[1];
    }

    public static void executeAction(String action) {
        switch (action) {
            case "add" -> System.out.println("adding");
            case "del" -> System.out.println("deleting");
            default -> wrongCommand(action, 'a');
        }
    }

    public static void getAction() {
        String command = null;
        while (!Objects.equals(command, "exit")) {
            command = getCommand(false);
            if (checkParent(command)) {
                String action = detectAction(command);
                executeAction(action);
            } else {
                wrongCommand(command, 'c');
            }
        }
        if (Objects.equals(command, "exit")) {
            getCommand(true);
        }
    }
    public static void main(String[] args) {
        getAction();
    }
}