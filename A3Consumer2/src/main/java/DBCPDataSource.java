import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.dbcp2.BasicDataSource;

public class DBCPDataSource {
  private static BasicDataSource dataSource;

  // NEVER store sensitive information below in plain text!
  private static String HOST_NAME = null;
  private static String PORT = null;
  private static final String DATABASE = "LiftRides";
  private static String USERNAME = null;
  private static String PASSWORD = null;

  static {
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
    dataSource = new BasicDataSource();
    try {
//      System.out.println("loading properties");
//      InputStream is = DBCPDataSource.class.getClassLoader()
//          .getResourceAsStream("db.properties");
//      System.out.println("creating properties");
//      Properties properties = new Properties();
//      System.out.println("new properties");
//      properties.load(is);
//      System.out.println("loaded properties");
//
//      HOST_NAME = properties.getProperty("MySQL_IP_ADDRESS");
//      PORT = properties.getProperty("MySQL_PORT");
//      USERNAME = properties.getProperty("DB_USERNAME");
//      PASSWORD = properties.getProperty("DB_PASSWORD");
      HOST_NAME = "database-1.cfo46oas0pfq.us-west-2.rds.amazonaws.com";
      PORT = "3306";
      USERNAME = "admin";
      PASSWORD = "12345678";
      //System.out.println("loaded properties: hostname: " + HOST_NAME + " port: " + PORT + " username: " + USERNAME + " password: " + PASSWORD);

      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
    dataSource.setUrl(url);
    dataSource.setUsername(USERNAME);
    dataSource.setPassword(PASSWORD);
    dataSource.setInitialSize(10);
    dataSource.setMaxTotal(60);
  }

  public static BasicDataSource getDataSource() {
    return dataSource;
  }
}