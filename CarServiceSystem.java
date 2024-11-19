import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class CarServiceSystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/car_service_system";
    private static final String DB_USER = "root"; // Replace with your database username
    private static final String DB_PASSWORD = ""; // Replace with your database password
    private Connection conn;

    public CarServiceSystem() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    public void addCar(int id, String model, String owner) {
        String query = "INSERT INTO Car (id, model, owner) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.setString(2, model);
            stmt.setString(3, owner);
            stmt.executeUpdate();
            System.out.println("Car added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding car: " + e.getMessage());
        }
    }

    public void scheduleService(int carId, String serviceType, LocalDateTime dateTime) {
        if (!isCarExists(carId)) {
            System.out.println("No car found with the given ID.");
            return;
        }

        if (dateTime.isBefore(LocalDateTime.now())) {
            System.out.println("Invalid date and time. You cannot schedule a service in the past.");
            return;
        }

        String query = "INSERT INTO Service (car_id, service_type, date_time) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, carId);
            stmt.setString(2, serviceType);
            stmt.setTimestamp(3, Timestamp.valueOf(dateTime));
            stmt.executeUpdate();
            System.out.println("Service scheduled successfully!");
        } catch (SQLException e) {
            System.out.println("Error scheduling service: " + e.getMessage());
        }
    }

    public void listServices() {
        String query = "SELECT s.id, c.model, c.owner, s.service_type, s.date_time " +
                       "FROM Service s JOIN Car c ON s.car_id = c.id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (!rs.isBeforeFirst()) {
                System.out.println("No services scheduled.");
                return;
            }
            while (rs.next()) {
                System.out.println("Service ID: " + rs.getInt("id") +
                        ", Car: " + rs.getString("model") + " (" + rs.getString("owner") + ")" +
                        ", Service Type: " + rs.getString("service_type") +
                        ", Date & Time: " + rs.getTimestamp("date_time").toLocalDateTime());
            }
        } catch (SQLException e) {
            System.out.println("Error listing services: " + e.getMessage());
        }
    }

    public void viewServiceDetails(int serviceId) {
        String query = "SELECT s.id, c.model, c.owner, s.service_type, s.date_time " +
                       "FROM Service s JOIN Car c ON s.car_id = c.id WHERE s.id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, serviceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Service ID: " + rs.getInt("id") +
                            ", Car: " + rs.getString("model") + " (" + rs.getString("owner") + ")" +
                            ", Service Type: " + rs.getString("service_type") +
                            ", Date & Time: " + rs.getTimestamp("date_time").toLocalDateTime());
                } else {
                    System.out.println("No service found with the given ID.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error viewing service details: " + e.getMessage());
        }
    }

    private boolean isCarExists(int id) {
        String query = "SELECT id FROM Car WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking car existence: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        CarServiceSystem system = new CarServiceSystem();
        Scanner scanner = new Scanner(System.in);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        int choice;

        do {
            System.out.println("\nCar Service Schedule System");
            System.out.println("1. Add Car");
            System.out.println("2. Schedule Service");
            System.out.println("3. List All Services");
            System.out.println("4. View Service Details");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    try {
                        System.out.print("Enter car ID (number): ");
                        int carId = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        System.out.print("Enter car model (string): ");
                        String model = scanner.nextLine();

                        System.out.print("Enter car owner name (string): ");
                        String owner = scanner.nextLine();

                        system.addCar(carId, model, owner);
                    } catch (Exception e) {
                        System.out.println("Invalid input. Please ensure the car ID is a number and model/owner are strings.");
                        scanner.nextLine(); // Clear the input buffer
                    }
                    break;
                case 2:
                    try {
                        System.out.print("Enter car ID: ");
                        int carId = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        System.out.print("Enter service type: ");
                        String serviceType = scanner.nextLine();

                        System.out.print("Enter service date and time (YYYY-MM-DD HH:MM): ");
                        String dateTimeInput = scanner.nextLine();

                        LocalDateTime dateTime = LocalDateTime.parse(dateTimeInput, formatter);
                        system.scheduleService(carId, serviceType, dateTime);
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date and time format. Please enter in the format: YYYY-MM-DD HH:MM");
                    } catch (Exception e) {
                        System.out.println("Error scheduling service.");
                    }
                    break;
                case 3:
                    system.listServices();
                    break;
                case 4:
                    System.out.print("Enter service ID to view: ");
                    int serviceId = scanner.nextInt();
                    system.viewServiceDetails(serviceId);
                    break;
                case 5:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        } while (choice != 5);

        try {
            system.conn.close();
        } catch (SQLException e) {
            System.out.println("Error closing the database connection: " + e.getMessage());
        }

        scanner.close();
    }
}
