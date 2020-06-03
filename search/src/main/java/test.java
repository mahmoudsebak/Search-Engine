import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Random;

public class test {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sonoo?rewriteBatchedStatements=true", "root", "");
            // here sonoo is database name, root is username and password
            String sql = "insert into emp(name, age) values(?, ?)";
            long startTime = System.currentTimeMillis();
            PreparedStatement ps = con.prepareStatement(sql);
            for (int i = 0; i < 5000; i++) {
                Random rnd = new Random();
                int age = rnd.nextInt(60);
                byte [] name = new byte[30];
                rnd.nextBytes(name);
                ps.setString(1, name.toString());
                ps.setInt(2, age);
                ps.addBatch();
                System.out.println(i);
            }
            ps.executeBatch();
            long endTime = System.currentTimeMillis();
            System.out.println("taken time: " + (endTime - startTime));
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}