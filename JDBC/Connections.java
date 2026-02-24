import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Connections {
    
    public static void main(String[] args) {
        
        String Query ="SELECT * FROM student ORDER BY age DESC;";
        String url = "jdbc:postgresql://localhost:5433/testdb";
        String user ="postgres";
        String Password ="1234";
        Connection con ;
        try {

           con = DriverManager.getConnection(url, user, Password);
           PreparedStatement st = con.prepareStatement(Query);
           ResultSet rs = st.executeQuery();
            while (rs.next()) {
                System.out.println(
                        rs.getInt("id") + " " +
                        rs.getString("name") + " " +
                        rs.getInt("age") + " " +
                        rs.getString("email")
                );
            }

            con.close();
        }
        catch (Exception err) {
            System.out.println(err.getMessage());
        }
    }
}

// java -cp ".:lib/*" Connections  