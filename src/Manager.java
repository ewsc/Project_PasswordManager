import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

public class Manager {

    static final String keyword = "pswrd";
    static private final ArrayList <PasswordRecord> passwordRecordAL = new ArrayList<>();
    static private final String passwordFilePath = "data.dat";
    static private String mainPassword;

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
        switch (type) {
            case 'k' -> System.out.print("Input new password keyword: ");
            case 'p' -> System.out.print("Input new password: ");
            case 'm' -> System.out.print("Input main password: ");
            case 'n' -> System.out.print("Input new main password: ");
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

    public static String getSaveVal(String password, String encPassword) {
        StringBuilder resultPass = new StringBuilder(password);
        for (int i = 0; i < resultPass.length(); i++) {
            int charA = encPassword.charAt(i);
            int charB = resultPass.charAt(i);
            resultPass.setCharAt(i, (char) (charA ^ charB));
        }
        return String.valueOf(resultPass);
    }

    public static void addNewRecord() {
        PasswordRecord newALElem = new PasswordRecord();
        newALElem.keyword = inputPassword('k');
        String password = inputPassword('p');
        System.out.println("Encoding... Complete.");
        newALElem.encValue = encodePassword(password);
        newALElem.saveValue = getSaveVal(password, newALElem.encValue);
        passwordRecordAL.add(newALElem);
    }

    public static String getDef(String encVal, String svvVal) {
        StringBuilder resVal = new StringBuilder();
        resVal.setLength(svvVal.length());
        for (int i = 0; i < svvVal.length(); i++) {
            int charA = encVal.charAt(i);
            int charB = svvVal.charAt(i);
            resVal.setCharAt(i, (char) (charA ^ charB));
        }
        return String.valueOf(resVal);
    }

    public static void showPass() {
        for (PasswordRecord passwordRecord : passwordRecordAL) {
            System.out.println(passwordRecord.keyword + " -> " + getDef(passwordRecord.encValue, passwordRecord.saveValue) + " (enc: " + passwordRecord.encValue + "), (svv: " + passwordRecord.saveValue + ");");
        }
        System.out.println();
    }


    public static void checkPasswordFile() {
        try {
            File mainPassFile = new File(passwordFilePath);
            if (mainPassFile.createNewFile()) {
                System.out.println("New data file created at " + mainPassFile.getAbsolutePath() + ";");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pleaseSomebodyHelp() {
        System.out.println("\nHere are some useful commands: ");
        System.out.println(keyword + " add -> add new password record.");
        System.out.println(keyword + " show -> show list of passwords.");
        System.out.println(keyword + " help -> show this menu.\n");
    }

    public static void executeAction(String action) {
        switch (action) {
            case "add" -> addNewRecord();
            case "del" -> System.out.println("nonexistent btw");
            case "show" -> showPass();
            case "help" -> pleaseSomebodyHelp();
            default -> wrongCommand(action, 'a');
        }
    }

    public static void createNewPass() {
        String newPass;
        try {
            File mainPassFile = new File(passwordFilePath);
            FileWriter writer = new FileWriter(mainPassFile);
            newPass = inputPassword('n');
            String encNewPass = encodePassword(newPass);
            newPass = getSaveVal(newPass, encNewPass);
            writer.write("pass=" + newPass);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mainPassword = newPass;
    }

    public static String returnFilePassword(String passwordLine) {
        String[] passLine = passwordLine.split("=");
        return passLine[1];
    }

    public static void checkForPasswordLine() {
        String passwordLine = null;
        try {
            File mainPassFile = new File(passwordFilePath);
            Scanner fileScanner  = new Scanner(mainPassFile);
            if (fileScanner.hasNextLine()) {
                passwordLine = fileScanner.nextLine();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (passwordLine != null) {
            String tempPass = inputPassword('m');
            String envPass = encodePassword(tempPass);
            mainPassword = getSaveVal(tempPass, envPass);
        }
        else {
            createNewPass();
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

    public static boolean passwordsMatch() {
        String filePassword;
        try {
            File passFile = new File(passwordFilePath);
            Scanner fileScanner = new Scanner(passFile);
            filePassword = returnFilePassword(fileScanner.nextLine());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Objects.equals(filePassword, mainPassword);
    }

    public static void main(String[] args) {
        do {
            checkPasswordFile();
            checkForPasswordLine();
        } while (!passwordsMatch());
        System.out.println("Welcome! You're in! Use [" + keyword + " help] to get info.");
        getAction();
    }
}