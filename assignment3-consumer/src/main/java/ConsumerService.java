import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import domain.LiftRide;
import domain.SkierPost;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.xml.bind.*;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.*;

public class ConsumerService {
    private static int numThreads;
    private static String TYPE;
    private static String QUEUE_NAME;
    private final static String IP_MQ = "172.31.27.239";
    private static String IP_REDIS = "172.31.24.177";
    //private static ConcurrentMap<Integer, Vector<LiftRide>> skierRecordMap = new ConcurrentHashMap<>();


    /**
     * Entry point for the program. Can read arguments either from commandline or from "src/main/java/parameters.xml" file
     *
     * @param args user specified arguments.
     *              - arg0: number of threads to run (numThreads >= 1)
     *              - arg1: choose one type "skier_service" or "resort_service"
     *              - arg2: the redis IP address
     *              For example: java -jar assignment2-consumer.jar 128 skier_service 172.31.26.25
     */
    public static void main(String[] args) throws JAXBException, IOException, TimeoutException {
        Integer[] argsRecNum = new Integer[1];
        String[] argsRecStr = new String[2];
        if (args.length == 0) {
            JAXBContext jc = JAXBContext.newInstance(Parameters.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setEventHandler(new ValidationEventHandler() {
                @Override
                public boolean handleEvent(ValidationEvent event) {
                    System.out.println(event.getMessage());
                    return true;
                }}

            );
            File xml = new File("src/main/java/parameters.xml");
            Parameters root = (Parameters) unmarshaller.unmarshal(xml);
            argsRecNum[0] = root.numThreads;
            argsRecStr[0] = root.service;
            argsRecStr[1] = root.RedisIP;
        } else if (args.length == 3){
            argsRecNum[0] = Utils.parseSingleNumber(args[0]);
            argsRecStr[0] = args[1];
            argsRecStr[1] = args[2];
        } else {
            System.err.println("wrong number of arguments");
        }
        if (argsRecNum[0] == null || argsRecNum[0] <= 0) {
            System.err.println("wrong arguments");
            return;
        }
        numThreads = argsRecNum[0];
        TYPE = argsRecStr[0];
        IP_REDIS= argsRecStr[1];
        QUEUE_NAME = TYPE + "_queue";
        JedisPool pool = new JedisPool(IP_REDIS, 6379);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(IP_MQ);
        Connection conn = factory.newConnection();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            Channel channel = conn.createChannel();
            channel.basicQos(1);
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            executor.execute(new SingleConsumerThread(pool, channel, QUEUE_NAME, TYPE));
        }
        executor.shutdown();
    }



}
