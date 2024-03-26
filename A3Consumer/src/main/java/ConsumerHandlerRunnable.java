import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ConsumerHandlerRunnable implements Runnable {

  private final Connection connection;
  private final Map<String, int[]> mp;
  private final String queueName;
  private final String exchangeName;
  private final JedisPool pool;

  public ConsumerHandlerRunnable(Connection connection,
      Map<String, int[]> mp, String exchangeName, String queueName, JedisPool pool) {
    this.connection = connection;
    this.mp = mp;
    this.exchangeName = exchangeName;
    this.queueName = queueName;
    this.pool = pool;
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
          //updateMap(msg);
          storeMsg(msg);
        //} finally {
          System.out.println(" [x] Done");
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          //latch.countDown();
        } catch (Exception e) {
          e.printStackTrace();
        }
      };
      channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private void updateMap(String msg) {
    //[217, 10, 1, 2024, 1, 123]
    //[time, liftID, resortID, seasonID, dayID, skierID]
    String[] parts = msg.split(",");
    int[] arr = {Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
        Integer.parseInt(parts[2]),
        Integer.parseInt(parts[3]), Integer.parseInt(parts[4])};
    synchronized (mp) {
      mp.put(parts[5], arr);
    }
  }

  private void storeMsg(String msg) {
    try (Jedis jedis = pool.getResource()) {
      //[217, 10, 1, 2024, 1, 123]
      //[time, liftID, resortID, seasonID, dayID, skierID]
      String[] parts = msg.split(",");

      //"For skier N, how many days have they skied this season?"
      // set, key: "sk" + skierID + "se" + seasonID + "dSet", value: dayID
      // query: SCARD "sk" + skierID + "se" + seasonID + "dSet"
      String skiedDaysKey = "sk" + parts[5] + "se" + parts[3] + "dSet";
      jedis.sadd(skiedDaysKey, parts[4]);

      //For skier N, what are the vertical totals for each ski day?" (calculate vertical as liftID*10)
      // hash, key: "sk" + skierID + "se" + seasonID + "vMap", field: "d" + dayID, value: vertical
      // query: HGET "sk" + skierID + "se" + seasonID + "vMap", "d" + dayID
      String verticalKey = "sk" + parts[5] + "se" + parts[3] + "vMap";
      String field = "d" + parts[4];
      if (!jedis.hexists(verticalKey, field)) {
        jedis.hset(verticalKey, field, String.valueOf(Integer.parseInt(parts[1]) * 10));
      } else {
        jedis.hincrBy(verticalKey, field, Integer.parseInt(parts[1]) * 10);
      }

      //"For skier N, show me the lifts they rode on each ski day"
      // set, key: "sk" + skierID + "se" + seasonID + "d" + dayID + "lSet", value: liftID
      // query: SMEMBERS "sk" + skierID + "se" + seasonID + "d" + dayID + "lSet"
      String liftsKey = "sk" + parts[5] + "se" + parts[3] + "d" + parts[4] + "lSet";
      jedis.sadd(liftsKey, parts[1]);

      //"How many unique skiers visited resort X on day N?"
      //set, key: "res" + resortID + "d" + dayID + "skSet", value: skierID
      //query: SCARD "res" + resortID + "d" + dayID + "skSet"
      String skiersKey = "res" + parts[2] + "d" + parts[4] + "skSet";
      jedis.sadd(skiersKey, parts[5]);
    }
  }
}
