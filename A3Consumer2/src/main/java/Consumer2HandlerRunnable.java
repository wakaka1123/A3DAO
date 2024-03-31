import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;

public class Consumer2HandlerRunnable implements Runnable {

  private final Connection connection;
  private final String queueName;
  private final String exchangeName;
  private BlockingDeque<int[]> buffer;

  public Consumer2HandlerRunnable(Connection connection, String exchangeName, String queueName, BlockingDeque<int[]> buffer) {
    this.connection = connection;
    this.exchangeName = exchangeName;
    this.queueName = queueName;
    this.buffer = buffer;
  }

  @Override
  public void run() {
    try {
      Channel channel = connection.createChannel();
      channel.exchangeDeclare(exchangeName, "fanout");
      channel.queueDeclare(queueName, false, false, false, null);
      channel.queueBind(queueName, exchangeName, "");
      System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

      channel.basicQos(1);

      //fair dispatch
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String msg = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [x] Received '" + msg + "'");
        try {
          //add to the blocking queue first
          //liftRideDao = new LiftRideDao();
          String[] parts = msg.split(",");
          int[] liftRideMsg = {Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
              Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]),
              Integer.parseInt(parts[5])};
          buffer.put(liftRideMsg);
          System.out.println("size of buffer after adding is: " + buffer.size());

          System.out.println(" [x] Done");
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        } catch (Exception e) {
          e.printStackTrace();
        }
      };
      channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private void updateDB(String msg, LiftRideDao dao) {
    //[217, 10, 1, 2024, 1, 123]
    //[time, liftID, resortID, seasonID, dayID, skierID]
    String[] parts = msg.split(",");
    int[] liftRideMsg = {Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
        Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]),
        Integer.parseInt(parts[5])};
    //dao.createLiftRide(liftRideMsg);
  }
}
