import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.dbcp2.BasicDataSource;

public class LiftRideDao implements Runnable {
  private static BasicDataSource dataSource;
  private BlockingQueue<int[]> buffer;
  private static final int batchSize = 2000;
  private static final int interval = 10000; //10 seconds interval to send batch data to db

  public LiftRideDao(BlockingQueue<int[]> buffer) {
    dataSource = DBCPDataSource.getDataSource();
    this.buffer = buffer;
  }

  public void createLiftRide(List<int[]> batchData) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String insertQueryStatement = "INSERT INTO LiftRides (skierId, resortId, seasonId, dayId, time, liftId) " +
        "VALUES (?,?,?,?,?,?)";
    try {
      //[217, 10, 1, 2024, 1, 123]
      //[time, liftID, resortID, seasonID, dayID, skierID]
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement);

      // transaction block start
      conn.setAutoCommit(false);
      for (int[] newLiftRide : batchData) {
        preparedStatement.setInt(1, newLiftRide[5]);
        preparedStatement.setInt(2, newLiftRide[2]);
        preparedStatement.setInt(3, newLiftRide[3]);
        preparedStatement.setInt(4, newLiftRide[4]);
        preparedStatement.setInt(5, newLiftRide[0]);
        preparedStatement.setInt(6, newLiftRide[1]);
        preparedStatement.addBatch();
      }
      // execute insert SQL statement
      preparedStatement.executeBatch();
      // transaction block end
      conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
  }

  @Override
  public void run() {

    long preTime = System.currentTimeMillis();
    List<int[]> batchData = new ArrayList<>();

    while (true) {
      try {
        if (!batchData.isEmpty() && (batchData.size() == batchSize || System.currentTimeMillis() - preTime >= interval)) {
          System.out.println("writing to db...");
          createLiftRide(batchData);
          batchData.clear();
          preTime = System.currentTimeMillis();
        }
        //cannot use buffer.take() because it will block the thread
        int[] newLiftRide = buffer.poll(5, TimeUnit.SECONDS);
        if (newLiftRide != null) batchData.add(newLiftRide);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}