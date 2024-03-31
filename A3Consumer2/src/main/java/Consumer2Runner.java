import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Consumer2Runner {

  public static final int CORE_THREADS = 200;
  public static final int MAX_THREADS = 200;
  public static final String REMOTE_RMQ_URL = "44.230.124.84";
  public static final long KEEP_ALIVE_TIME = 0L;
  public static final int NUM_THREADS = 200;
  public static final int NUM_TASKS = 200000;
  public static final String QUEUE_NAME = "test";
  public static final String EXCHANGE_NAME = "exchange_fanout";

  public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
    BlockingDeque<int[]> buffer = new LinkedBlockingDeque<>();
    Runnable liftRideDao = new LiftRideDao(buffer);
    new Thread(liftRideDao).start();
    ExecutorService executor = new ThreadPoolExecutor(
        CORE_THREADS,
        MAX_THREADS,
        KEEP_ALIVE_TIME,
        TimeUnit.MILLISECONDS,
        new SynchronousQueue<>(),
        new CallerRunsPolicy()
    );

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(REMOTE_RMQ_URL);
    factory.setUsername("guest");
    factory.setPassword("guest");
    Connection connection = factory.newConnection(executor);

    for (int i = 0; i < NUM_THREADS; i++) {
      executor.execute(new Consumer2HandlerRunnable(connection, EXCHANGE_NAME, QUEUE_NAME, buffer));
    }

    //executor.shutdown();
    executor.awaitTermination(600, TimeUnit.SECONDS);
    System.out.println("All threads have finished");


  }

}
