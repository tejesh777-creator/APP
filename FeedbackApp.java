import java.sql.*;
import java.util.Scanner;

public class FeedbackApp {

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/company";
        String user = "root";
        String password = "your_password";

        try {
            con = DriverManager.getConnection(url, user, password);

            int choice;
            do {
                System.out.println("\n--- Customer Feedback ---");
                System.out.println("1. Insert feedback");
                System.out.println("2. Average rating per service");
                System.out.println("3. Feedback rated below 3");
                System.out.println("4. View all feedback");
                System.out.println("5. Exit");
                System.out.print("Enter choice: ");
                choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1: insertFeedback(); break;
                    case 2: averagePerService(); break;
                    case 3: lowRated(); break;
                    case 4: viewAll(); break;
                    case 5: System.out.println("Goodbye."); break;
                    default: System.out.println("Invalid choice.");
                }
            } while (choice != 5);

            con.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void insertFeedback() throws SQLException {
        System.out.print("Customer name: ");
        String name = sc.nextLine().trim();
        System.out.print("Service name: ");
        String service = sc.nextLine().trim();

        int rating;
        try {
            System.out.print("Rating (1-5): ");
            rating = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Rating must be a number.");
            return;
        }
        if (rating < 1 || rating > 5) {
            System.out.println("Rating must be between 1 and 5.");
            return;
        }

        System.out.print("Comments: ");
        String comments = sc.nextLine().trim();

        if (name.isEmpty() || service.isEmpty()) {
            System.out.println("Customer name and service name cannot be empty.");
            return;
        }

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO feedback (customer_name, service_name, rating, comments) " +
            "VALUES (?, ?, ?, ?)");
        ps.setString(1, name);
        ps.setString(2, service);
        ps.setInt(3, rating);
        ps.setString(4, comments);
        ps.executeUpdate();
        System.out.println("Feedback saved.");
    }

    static void averagePerService() throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(
            "SELECT service_name, AVG(rating) AS avg_rating, COUNT(*) AS total " +
            "FROM feedback GROUP BY service_name ORDER BY avg_rating DESC");

        System.out.printf("%-20s %-12s %-8s%n", "Service", "Avg Rating", "Reviews");
        int count = 0;
        while (rs.next()) {
            System.out.printf("%-20s %-12.2f %-8d%n",
                rs.getString("service_name"),
                rs.getDouble("avg_rating"),
                rs.getInt("total"));
            count++;
        }
        if (count == 0) System.out.println("No feedback yet.");
    }

    static void lowRated() throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(
            "SELECT * FROM feedback WHERE rating < 3 ORDER BY rating");
        print(rs);
    }

    static void viewAll() throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(
            "SELECT * FROM feedback ORDER BY feedback_id");
        print(rs);
    }

    static void print(ResultSet rs) throws SQLException {
        System.out.printf("%-5s %-12s %-18s %-7s %s%n",
            "ID", "Customer", "Service", "Rating", "Comments");
        int count = 0;
        while (rs.next()) {
            System.out.printf("%-5d %-12s %-18s %-7d %s%n",
                rs.getInt("feedback_id"),
                rs.getString("customer_name"),
                rs.getString("service_name"),
                rs.getInt("rating"),
                rs.getString("comments"));
            count++;
        }
        if (count == 0) System.out.println("No records found.");
    }
}