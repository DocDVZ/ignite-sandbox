package ru.iitdgroup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
//import org.apache.ignite.examples.ExampleNodeStartup;

/**
 * Created by DocDVZ on 10.08.2017.
 */
public class MainTest {

    public static void main(String[] args) throws Exception {
        print("JDBC example started.");
        Class.forName("org.apache.ignite.IgniteJdbcThinDriver");
        // Open JDBC connection
        try (Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/")) {
            print("Connected to server.");
//            System.out.println( DriverManager.getDriver(conn.getMetaData().getURL()).getClass());

            // Create database objects.
            try (Statement stmt = conn.createStatement()) {
                // Create reference City table based on REPLICATED template.
                stmt.executeUpdate("CREATE TABLE city (id LONG PRIMARY KEY, name VARCHAR) " +
                        "WITH \"template=replicated\"");

                // Create table based on PARTITIONED template with one backup.
                stmt.executeUpdate("CREATE TABLE person (id LONG, name VARCHAR, city_id LONG, " +
                        "PRIMARY KEY (id, city_id)) WITH \"backups=1, affinityKey=city_id\"");

                // Create an index.
                stmt.executeUpdate("CREATE INDEX on Person (city_id)");
            }

            print("Created database objects.");

            // Populate City table with PreparedStatement.
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO city (id, name) VALUES (?, ?)")) {
                stmt.setLong(1, 1L);
                stmt.setString(2, "Forest Hill");
                stmt.executeUpdate();

                stmt.setLong(1, 2L);
                stmt.setString(2, "Denver");
                stmt.executeUpdate();

                stmt.setLong(1, 3L);
                stmt.setString(2, "St. Petersburg");
                stmt.executeUpdate();
            }

            // Populate Person table with PreparedStatement.
            try (PreparedStatement stmt =
                         conn.prepareStatement("INSERT INTO person (id, name, city_id) values (?, ?, ?)")) {
                stmt.setLong(1, 1L);
                stmt.setString(2, "John Doe");
                stmt.setLong(3, 3L);
                stmt.executeUpdate();

                stmt.setLong(1, 2L);
                stmt.setString(2, "Jane Roe");
                stmt.setLong(3, 2L);
                stmt.executeUpdate();

                stmt.setLong(1, 3L);
                stmt.setString(2, "Mary Major");
                stmt.setLong(3, 1L);
                stmt.executeUpdate();

                stmt.setLong(1, 4L);
                stmt.setString(2, "Richard Miles");
                stmt.setLong(3, 2L);
                stmt.executeUpdate();
            }

            print("Populated data.");

            // Get data.
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs =
                             stmt.executeQuery("SELECT p.name, c.name FROM Person p INNER JOIN City c on c.id = p.city_id")) {
                    print("Query results:");

                    while (rs.next())
                        System.out.println(">>>    " + rs.getString(1) + ", " + rs.getString(2));
                }
            }

            // Drop database objects.
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP TABLE Person");
                stmt.executeUpdate("DROP TABLE City");
            }

            print("Dropped database objects.");
        }

        print("JDBC example finished.");
    }

    /**
     * Prints message.
     *
     * @param msg Message to print before all objects are printed.
     */
    private static void print(String msg) {
        System.out.println();
        System.out.println(">>> " + msg);
    }

}
