import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Manager {

    static final String mngrKeyword = "pswrd";
    static private final ArrayList <PasswordRecord> passwordRecords = new ArrayList<>();
    static private final String mainFilePath = "data.dat";
    static private String mainPassword;

    public static Query getCommand(boolean isExiting) {
        String line;
        Scanner scan = new Scanner(System.in);
        line = scan.nextLine();
        if (isExiting) {
            scan.close();
        }
        Query resQuery = new Query();
        String[] resQuerySplit = line.split(" ");
        resQuery.command = resQuerySplit[0];
        if (resQuerySplit.length > 1) {
            resQuery.action = resQuerySplit[1];
        }
        if (resQuerySplit.length > 2) {
            resQuery.paramUno = resQuerySplit[2];
        }
        if (resQuerySplit.length > 3) {
            resQuery.paramDos = resQuerySplit[3];
        }
        return resQuery;
    }

    public static void wrongCommand(String command, char type) {
        switch (type) {
            case 'a' -> System.err.println("action [" + command + "] not found.");
            case 'c' -> System.err.println("command [" + command + "] not found.");
        }
    }

    public static boolean checkParent(String command) {
        int keyLength = mngrKeyword.length();
        if (command.length() >= keyLength) {
            String parentComm = command.substring(0, keyLength);
            return parentComm.equals(mngrKeyword);
        }
        else {
            return false;
        }
    }

    public static String inputPassword(char type) {
        switch (type) {
            case 'm' -> System.out.print("input main password: ");
            case 'n' -> System.out.print("input new main password: ");
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

    public static void updateFile() throws IOException {
        File mainFile = new File(mainFilePath);
        FileWriter writer = new FileWriter(mainFile);
        writer.write("pass=" + mainPassword + "\n");
        for (PasswordRecord passwordRecord : passwordRecords) {
            writer.write("=KEY=" + passwordRecord.keyword + "=" + passwordRecord.encValue + "=" + passwordRecord.saveValue);
        }
        writer.close();
    }

    public static boolean isGoodPassword(String password) {
        if (password.length() < 8) {
            System.out.println("password must be at least 8 characters long.");
            return false;
        }
        if (!password.matches("^[a-zA-Z0-9_]+")) {
            System.out.println("password could contain only English letters, digits and underscores '_'");
            return false;
        }
        return true;
    }

    public static boolean isUniqueKeyword(String keyword) {
        for (PasswordRecord passwordRecord : passwordRecords) {
            if (Objects.equals(keyword, passwordRecord.keyword)) {
                System.err.println("this keyword already exists.");
                return false;
            }
        }
        return true;
    }

    public static void addNewRecord(String keyword, String password) throws IOException {
        if (password != null && keyword != null) {
            if (isUniqueKeyword(keyword) && isGoodPassword(password)) {
                PasswordRecord newALElem = new PasswordRecord();
                newALElem.keyword = keyword;
                newALElem.encValue = encodePassword(password);
                newALElem.saveValue = getSaveVal(password, newALElem.encValue);
                System.out.println("password added.");
                passwordRecords.add(newALElem);
                updateFile();
            }
        }
        else {
            System.err.println("input keyword and password.");
        }
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
        if (passwordRecords.size() > 0) {
            for (int i = 0; i < passwordRecords.size(); i++) {
                PasswordRecord passwordRecord = passwordRecords.get(i);
                System.out.println("[" + (i + 1) + "] " + passwordRecord.keyword + " -> " + getDef(passwordRecord.encValue, passwordRecord.saveValue) + " (enc: " + passwordRecord.encValue + "), (svv: " + passwordRecord.saveValue + ");");
            }
        }
        else {
            System.out.println("nothing to see here.");
        }
    }


    public static void checkPasswordFile() {
        try {
            File mainPassFile = new File(mainFilePath);
            if (mainPassFile.createNewFile()) {
                System.out.println("new data file created at " + mainPassFile.getAbsolutePath() + ".");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pleaseSomebodyHelp() {
        String version = "1.0";
        System.out.println("password manager by gthanksg v." + version + ".");
        System.out.println(mngrKeyword + " add [unique keyword] [password] - adds password. password should be at least 8 chars long.");
        System.out.println(mngrKeyword + " show - shows list of all passwords.");
        System.out.println(mngrKeyword + " delete [password record id] - deletes password from passwords list. check password list at [" + mngrKeyword + " show].");
        System.out.println(mngrKeyword + " help - show this menu (wow).");
    }

    public static void deleteRecord(int deleting) throws IOException {
        if (deleting < passwordRecords.size()) {
            passwordRecords.remove(deleting);
            System.out.println("deleted successfully.");
            updateFile();
        }
        else {
            System.err.println("error at finding index [" + deleting + "].");
        }
    }

    public static void executeAction(Query currentQuery) throws IOException {
        switch (currentQuery.action) {
            case "add" -> addNewRecord(currentQuery.paramUno, currentQuery.paramDos);
            case "del" -> {
                try {
                    deleteRecord(Integer.parseInt(currentQuery.paramUno) - 1);
                }
                catch (Exception e) {
                    System.err.println("error at checking index [" + currentQuery.paramUno + "]");
                }
            }
            case "show" -> showPass();
            case "help" -> pleaseSomebodyHelp();
            default -> wrongCommand(currentQuery.action, 'a');
        }
    }

    public static void createNewPass() {
        String newPass;
        try {
            File mainPassFile = new File(mainFilePath);
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
            File mainPassFile = new File(mainFilePath);
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

    public static void getAction() throws IOException {
        do {
            Query currentQuery = getCommand(false);
            String command = currentQuery.command;
            if (command.equals("exit")) {
                System.err.println("cya.");
                break;
            }
            else if (checkParent(command)) {
                executeAction(currentQuery);
            } else {
                wrongCommand(command, 'c');
            }
        } while (true);
    }

    public static boolean passwordsMatch() {
        String filePassword;
        try {
            File passFile = new File(mainFilePath);
            Scanner fileScanner = new Scanner(passFile);
            filePassword = returnFilePassword(fileScanner.nextLine());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Objects.equals(filePassword, mainPassword);
    }

    public static void loadFileData() throws IOException {
        File dataFile = new File(mainFilePath);
        Scanner scanner = new Scanner(dataFile);
        String ignore = scanner.nextLine();
        StringBuilder fileSData = new StringBuilder();
        while (scanner.hasNextLine()) {
            fileSData.append(scanner.nextLine());
        }
        String[] fileRecords = fileSData.toString().split("=KEY=");
        if (fileRecords.length > 1) {
            for (String fileRecord : fileRecords) {
                if (fileRecord.length() > 1) {
                    String[] fileRecString = fileRecord.split("=");
                    PasswordRecord fileRec = new PasswordRecord();
                    fileRec.keyword = fileRecString[0];
                    fileRec.encValue = fileRecString[1];
                    fileRec.saveValue = fileRecString[2];
                    passwordRecords.add(fileRec);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("""

                ██████╗  █████╗ ███████╗███████╗██╗    ██╗ ██████╗ ██████╗ ██████╗     ███╗   ███╗ █████╗ ███╗   ██╗ █████╗  ██████╗ ███████╗██████╗\s
                ██╔══██╗██╔══██╗██╔════╝██╔════╝██║    ██║██╔═══██╗██╔══██╗██╔══██╗    ████╗ ████║██╔══██╗████╗  ██║██╔══██╗██╔════╝ ██╔════╝██╔══██╗
                ██████╔╝███████║███████╗███████╗██║ █╗ ██║██║   ██║██████╔╝██║  ██║    ██╔████╔██║███████║██╔██╗ ██║███████║██║  ███╗█████╗  ██████╔╝
                ██╔═══╝ ██╔══██║╚════██║╚════██║██║███╗██║██║   ██║██╔══██╗██║  ██║    ██║╚██╔╝██║██╔══██║██║╚██╗██║██╔══██║██║   ██║██╔══╝  ██╔══██╗
                ██║     ██║  ██║███████║███████║╚███╔███╔╝╚██████╔╝██║  ██║██████╔╝    ██║ ╚═╝ ██║██║  ██║██║ ╚████║██║  ██║╚██████╔╝███████╗██║  ██║
                ╚═╝     ╚═╝  ╚═╝╚══════╝╚══════╝ ╚══╝╚══╝  ╚═════╝ ╚═╝  ╚═╝╚═════╝     ╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝
                """);
        do {
            checkPasswordFile();
            checkForPasswordLine();
        } while (!passwordsMatch());
        loadFileData();
        System.out.println("login successful.");
        System.out.println("use [" + mngrKeyword + " help] to get info.");
        getAction();
    }
}