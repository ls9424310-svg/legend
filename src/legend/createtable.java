/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package legend;
import java.sql.Connection;
import java.sql.Statement;


/**
 *
 * @author SHARMA
 */
public class createtable {
     public createtable() {

        createStudentTable();
       

    }
    
    public void createStudentTable() {
        
         try {

            Connection con = Gamechanger.getConnection();

            Statement st = con.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS student("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "sr_no INTEGER,"
                    + "name TEXT,"
                    + "roll_no INTEGER UNIQUE"
                    + ")";

            st.executeUpdate(sql);

            System.out.println("Table Created Successfully");

            st.close();
            con.close();

        } catch (Exception e) {
            System.out.println(e);
        }
        
    }
    
}
