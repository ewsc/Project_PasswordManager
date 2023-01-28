import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.io.*;

public class Manager {

    static final String keyword = "pswrd";
    static private final ArrayList <PasswordRecord> passwordRecordAL = new ArrayList<>();
    static private final String passwordFilePath = System.getProperty("user.dir") + "\\file";

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

    public static boolean getMainPassword(boolean passwordExists) throws IOException {
        String mainPassword;
        if (!passwordExists) {
            mainPassword = inputPassword('n');
        }
        else {
            mainPassword = inputPassword('m');
        }
        String encPassVal = encodePassword(mainPassword);
        String svvPassVal = getSaveVal(mainPassword, encPassVal);
        File mainPassFile = new File(passwordFilePath);
        if (passwordExists) {
            Scanner reader = new Scanner(mainPassFile);
            String data = reader.nextLine();
            reader.close();
            return Objects.equals(svvPassVal, data);
        }
        else {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(passwordFilePath));
                writer.write(svvPassVal + "\n");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    public static boolean createPasswordFile() {
        boolean fileExists = true;
        File mainPassFile = new File(passwordFilePath);
        try {
            if (mainPassFile.createNewFile()) {
                fileExists = false;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return fileExists;
    }

    public static void executeAction(String action) {
        switch (action) {
            case "add" -> addNewRecord();
            case "del" -> System.out.println("nonexistent btw");
            case "show" -> showPass();
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

    public static void main(String[] args) throws IOException {
        boolean fileExists = createPasswordFile();
        if (getMainPassword(fileExists)) {
            getAction();
        }
    }
}