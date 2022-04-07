import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import domain.LiftRide;
import domain.SkierPost;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleConsumerThread implements Runnable{
    private JedisPool jedisPool;
    private final Channel channel;
    private final Gson gson = new Gson();
    private final String queueName;
    private String type;

    public SingleConsumerThread(JedisPool jedisPool, Channel channel, String queueName, String type) {
        this.jedisPool = jedisPool;
        this.channel = channel;
        this.queueName = queueName;
        this.type = type;
    }

    @Override
    public void run() {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            LiftRide ride = gson.fromJson(message, LiftRide.class);
            Vector<LiftRide> vector = new Vector<>();
            Integer userId = ride.getSkierId();
            Integer seasonId = ride.getSeasonId();
            Integer daysId = ride.getDaysId();
            Integer liftId = ride.getLiftID();
            Integer resortId = ride.getResortId();
            Integer time = ride.getTime();
            try (Jedis jedis = jedisPool.getResource()) {
                if (type.equals("skier_service")) {
                    jedis.sadd("01" + "u" + userId + "s" + seasonId + "days", "" + daysId);
                    jedis.sadd("01" + "u" + userId + "s" + seasonId + "d" + daysId + "lifts", "" + liftId);
                    String verticalKey = "01" + "u" + userId + "s" + seasonId + "d" + daysId + "vertical";
                    Integer vertical = 10 * liftId;
                    jedis.incrBy(verticalKey, (long)vertical);
                } else if (type.equals("resort_service")) {
                    jedis.sadd("02" + "re" + resortId + "s" + seasonId + "d" + daysId + "users", "" + userId);
                    jedis.incr("02" + "li" + liftId + "s" + seasonId + "d" + daysId + "freq");
                    Integer hour = (time - 1) / 60;
                    jedis.incr("02" + "s" + seasonId + "d" + daysId + "h" + hour + "freq");
                }
            }
            //System.out.println(key + ": " + post.getLiftID() + " " + post.getTime() + " " + post.getWaitTime());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        try {
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
            Logger.getLogger(SingleConsumerThread.class.getName()).log(Level.SEVERE, null, e);
        }

    }


}
