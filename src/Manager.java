import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

public class Manager {

    static final String keyword = "pswrd";
    static private final ArrayList <PasswordRecord> passwordRecordAL = new ArrayList<>();
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

    public static String inputPassword(char type) {
        if (type == 'k') {
            System.out.print("Input new password keyword:");
        }
        else if (type == 'p') {
            System.out.print("Input new password:");
        }
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    public static String encodePassword(String password) {
        String newPassword = null;
        try
        {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(password.getBytes());
            byte[] bytes = m.digest();
            StringBuilder s = new StringBuilder();
            for (byte aByte : bytes) {
                s.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            newPassword = s.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return newPassword;
    }

    public static void addNewRecord() {
        PasswordRecord newALElem = new PasswordRecord();
        newALElem.keyword = inputPassword('k');
        String password = inputPassword('p');
        System.out.println("Encoding...");
        newALElem.encValue = encodePassword(password);
        newALElem.defValue = password;
        passwordRecordAL.add(newALElem);
    }

    public static void executeAction(String action) {
        switch (action) {
            case "add" -> addNewRecord();
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