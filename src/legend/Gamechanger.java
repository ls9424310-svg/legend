/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package legend;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



/**
 *
 * @author SHARMA
 */
public class Gamechanger {
    private static final String URL = "jdbc:sqlite:student.db";

    public static Connection getConnection() {

        Connection con = null;

        try {

            con = DriverManager.getConnection(URL);
            System.out.println("Database Connected Successfully!");

        } catch (SQLException e) {

            System.out.println("Connection Error: " + e.getMessage());

        }

        return con;
    }
    
}
