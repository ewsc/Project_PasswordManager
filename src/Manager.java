import java.util.Objects;
import java.util.Scanner;

public class Manager {

    static final String keyword = "pswrd";
    public static String getCommand() {
        String command;
        Scanner scan = new Scanner(System.in);
        command = scan.nextLine();
        //scan.close();
        return command;
    }

    public static void wrongCommand(String command) {
        System.err.println("Action [" + command + "] not found.");
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

    public static void getAction() {
        String command = null;
        while (!Objects.equals(command, "exit")) {
            command = getCommand();
            if (checkParent(command)) {
                String action = detectAction(command);
                System.out.println(action);
            } else {
                wrongCommand(command);
            }
        }
    }
    public static void main(String[] args) {
        getAction();
    }
}