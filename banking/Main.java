import java.sql.*;
import java.util.*;

class Account {
    private String cardNumber;
    private String pin;
    private int balance;

    protected Account() {

        Random random = new Random();
        String tempNumber = "400000" + (random.nextInt(899) + 100) +
                (random.nextInt(899) + 100) + (random.nextInt(899) + 100);

        // Applying the Luhn Algorithm

        long numberCheck = Long.parseLong(tempNumber);
        int sum = 0, checksum = 0;
        long tempSum, adder = 0;
        boolean odd = true;

        while (numberCheck > 0) {

            if (odd) {
                tempSum = (numberCheck % 10) * 2;

                if (tempSum > 9) {
                    while (tempSum > 0) {
                        adder += tempSum % 10;
                        tempSum /= 10;
                    }

                    tempSum = adder;
                }


                odd = false;

            } else {
                tempSum = numberCheck % 10;
                odd = true;
            }

            adder = 0;
            sum += tempSum;
            numberCheck /= 10;

        }

        while (sum % 10 != 0) {
            sum++;
            checksum++;
        }

        cardNumber = tempNumber + checksum;

        // Generating the PIN code

        int tempPin = random.nextInt(9999);

        if (tempPin <= 999 && tempPin >= 100) {
            pin = "0" + tempPin;
        } else if (tempPin <= 99 && tempPin >= 10) {
            pin = "00" + tempPin;
        } else if (tempPin <= 9) {
            pin = "000" + tempPin;
        } else {
            pin = "" + tempPin;
        }
        balance = 0;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPin() {
        return pin;
    }

    public int getBalance() {
        return balance;
    }
}

class InsertData {
    private String fileName;

    public InsertData(String fileName){
        this.fileName = fileName;
    }

    // Establishing the connection

    public Connection connect () {

        String url = "jdbc:sqlite:" + fileName;
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    // Function to insert the data into DB

    public void Insert (long number, int pin, int balance){
        String sql = "INSERT INTO card (number,pin,balance) VALUES(?,?,?)";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setLong(1,number);
            pstmt.setInt(2,pin);
            pstmt.setInt(3, balance);
            pstmt.executeUpdate();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
}

class RemoveData{
    private String fileName;

    public RemoveData(String fileName){
        this.fileName = fileName;
    }

    public Connection connect () {

        String url = "jdbc:sqlite:" + fileName;
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    // Function to remove row from DB

    public void Remove(long cardNumber){
        String sql = "DELETE FROM card WHERE number = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setLong(1,cardNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

class SelectData {
    private String fileName;

    public SelectData (String fileName){
        this.fileName = fileName;
    }

    public Connection connect () {

        String url = "jdbc:sqlite:" + fileName;
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    // Function to check whether the cardNumber is in data base

    public boolean checkNumber (long cardNumber) {
        boolean isCard = false;
        String sql = "SELECT number FROM card WHERE number=?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Setting parameter

            pstmt.setLong(1, cardNumber);
            ResultSet rs = pstmt.executeQuery();

            isCard = rs.next();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return isCard;
    }

    // Function to check whether the pinNumber is in data base

    public boolean checkPin(int pinNumber){
        boolean isPinValid = false;
        String sql = "SELECT pin FROM card WHERE pin=?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setLong(1,pinNumber);
            ResultSet rs = pstmt.executeQuery();
            isPinValid = rs.next();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return isPinValid;
    }

    // Function that returns a balance of specified cardNumber

    public int returnBalance (long cardNumber){
        String sql = "SELECT number, balance FROM card WHERE number=?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, cardNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("balance");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return 0;
    }
}

class UpdateData {
    private String fileName;

    public UpdateData(String fileName) {
        this.fileName = fileName;
    }

    public Connection connect () {

        String url = "jdbc:sqlite:" + fileName;
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    // Function that handles money transfers between two different card numbers

    public void Update(long sender, long receiver, int amount){
        String sql = "UPDATE card SET balance=(balance-?) WHERE number=?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1,amount);
            pstmt.setLong(2,sender);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        String nextSql = "UPDATE card SET balance=(balance+?) WHERE number=?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(nextSql)) {

            pstmt.setInt(1, amount);
            pstmt.setLong(2,receiver);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Function that adds specified amount of money to a card number

    public void addAmount(long cardNumber, int amount) {
        String sql = "UPDATE card SET balance=(balance+?) WHERE number=?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Setting parameters

            pstmt.setInt(1, amount);
            pstmt.setLong(2, cardNumber);

            // Updating

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}


public class Main {
    static String fileName;

    // Function to print generic commands when card is created

    static void printData(Account card) {
        System.out.println();
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(card.getCardNumber());
        System.out.println("Your card PIN:");
        System.out.println(card.getPin());
    }

    static void createAccount() {
        InsertData insertApp = new InsertData(fileName);
        Account account = new Account();

        // Inserting data into an SQL database

        insertApp.Insert(Long.parseLong(account.getCardNumber()), Integer.parseInt(account.getPin()),
                account.getBalance());

        printData(account);
    }

    static void logIn() {

        System.out.println();
        Scanner scanner = new Scanner(System.in);
        SelectData selectApp = new SelectData(fileName);
        boolean loggedIn = false;

        System.out.println("Enter your card number:");
        String numberInput = scanner.nextLine();
        System.out.println("Enter your PIN:");
        String pinInput = scanner.nextLine();

        // Checking correctness of card number & PIN

        if (selectApp.checkNumber(Long.parseLong(numberInput)) && selectApp.checkPin(Integer.parseInt(pinInput))) {

            loggedIn = true;
            System.out.println("You have successfully logged in!");
            menu(Long.parseLong(numberInput));

        }

        if (!loggedIn) {
            System.out.println("Wrong card number or PIN!");
        }
    }

    static void menu(long cardNumber) {
        System.out.println();
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
        Scanner scanner = new Scanner(System.in);
        SelectData selectApp = new SelectData(fileName);
        int secondChoice = scanner.nextInt();

        switch (secondChoice) {
            case 0:
                System.out.println();
                System.out.println("Bye!");
                System.exit(0);
            case 1:
                System.out.println();
                System.out.println("Balance: " + selectApp.returnBalance(cardNumber));
                break;
            case 2:
                System.out.println();
                System.out.println("Enter income:");
                int income = scanner.nextInt();

                UpdateData update = new UpdateData(fileName);
                update.addAmount(cardNumber, income);

                System.out.println("Income was added!");

                break;
            case 3:
                System.out.println();
                System.out.println("Transfer");
                System.out.println("Enter card number:");

                long cardInput = scanner.nextLong();
                boolean correct = true;

                // Checking if number passes Luhn Algorithm

                long numberCheck = cardInput;
                int sum = 0;
                long tempSum, adder = 0;
                boolean odd = false;

                while (numberCheck > 0) {

                    if (odd) {
                        tempSum = (numberCheck % 10) * 2;

                        if (tempSum > 9) {
                            while (tempSum > 0) {
                                adder += tempSum % 10;
                                tempSum /= 10;
                            }

                            tempSum = adder;
                        }

                        odd = false;

                    } else {
                        tempSum = numberCheck % 10;
                        odd = true;
                    }

                    adder = 0;
                    sum += tempSum;
                    numberCheck /= 10;
                }

                if (sum % 10 != 0) {
                    correct = false;
                }

                if (!correct) {

                    System.out.println("Probably you made mistake in the card number. Please try again!");
                    break;

                } else {

                    if (!selectApp.checkNumber(cardInput)) {

                        System.out.println("Such a card does not exist.");
                        break;

                    } else {

                        System.out.println("Enter how much money you want to transfer:");
                        int moneyTransfer = scanner.nextInt();

                        if (selectApp.returnBalance(cardNumber) < moneyTransfer) {

                            System.out.println("Not enough money!");
                            break;

                        } else {

                            UpdateData updateApp = new UpdateData(fileName);
                            updateApp.Update(cardNumber, cardInput, moneyTransfer);
                            System.out.println("Success!");

                        }

                    }

                    break;

                }
            case 4:
                RemoveData deleteApp = new RemoveData(fileName);
                deleteApp.Remove(cardNumber);

                System.out.println();
                System.out.println("The account has been closed!");
            case 5:
                System.out.println();
                System.out.println("You have successfully logged out!");
                return;
            default:
                System.out.println();
                System.out.println("Unknown command");
                break;
        }
        menu(cardNumber);
    }

    public static void createDatabase(String fileName) {

        String url = "jdbc:sqlite:" + fileName;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createTable(String fileName) {

        String url = "jdbc:sqlite:" + fileName;

        String sql = "CREATE TABLE IF NOT EXISTS card (" +
                "  `id` INTEGER NOT NULL PRIMARY KEY," +
                "  `number` TEXT," +
                "  `pin` TEXT," +
                "  `balance` INTEGER DEFAULT 0"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {


            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        fileName = "cards.db";
        Scanner scanner = new Scanner(System.in);

        int choice = -1;

        // Creating an SQL database using functions

        createDatabase(fileName);
        createTable(fileName);

        while (choice != 0) {

            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");

            choice = scanner.nextInt();

            switch (choice) {

                case 0:

                    System.out.println();
                    System.out.println("Bye!");
                    break;

                case 1:

                    createAccount();
                    break;

                case 2:

                    logIn();
                    break;

                default:

                    System.out.println();
                    System.out.println("Unknown command");
                    break;

            }
        }
    }
}