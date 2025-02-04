import java.sql.*;

public class Main {
    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql:fake4091");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        };
    }
}