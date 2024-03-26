package edu.northeastern;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mysql.cj.util.DnsSrv.SrvRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Main {

  public static void main(String[] args) {
    System.out.println("Hello redis!");
    JedisPool jedisPool = new JedisPool("localhost", 6379);
    Jedis jedis = jedisPool.getResource();
    String skierID = "123";

    int[] msg = {217, 10, 1, 2024, 1, 123};
    int[] msg1 = {217, 10, 1, 2024, 10, 123};
    int[] msg2 = {217, 10, 1, 2024, 100, 123};
    //[217, 10, 1, 2024, 1, 123];
    //[time, liftID, resortID, seasonID, dayID, skierID]
    String setKey = "sk" + "123";
    String hashKey = "sk" + "123" + "vMap";
    String field = "d" + 100;
    //jedis.sadd(setKey, Arrays.toString(msg));

    if (!jedis.hexists(hashKey, field)) {
      jedis.hset(hashKey, field, String.valueOf(msg[1]));
    } else {
      jedis.hincrBy(hashKey, field, msg[1]);
    }
    //jedis.hset()
    //Map<String, String> mp = jedis.hgetAll(hashKey);
    //Map<String, String> mp = new HashMap<>();
    //mp.put("dayID", String.valueOf(Integer.parseInt(mp.get("dayID")) + 10));
    //jedis.hset(hashKey, mp);
    System.out.println(jedis.smembers(setKey));
    System.out.println(jedis.hgetAll(hashKey));
    System.out.println("query " + jedis.hget(hashKey, field));


//    SkierMsg skierMsg = new SkierMsg(msg[0], msg[1], msg[2], msg[3], msg[4], msg[5]);
//    SkierMsg skierMsg1 = new SkierMsg(msg1[0], msg1[1], msg1[2], msg1[3], msg1[4], msg1[5]);
//    SkierMsg skierMsg2 = new SkierMsg(msg2[0], msg2[1], msg2[2], msg2[3], msg2[4], msg2[5]);
//    Gson gson = new Gson();
//
//
//    JsonObject valueOfSkierID = gson.toJsonTree(skierMsg).getAsJsonObject();
//    JsonObject valueOfSkierID1 = gson.toJsonTree(skierMsg1).getAsJsonObject();
//    JsonObject valueOfSkierID2 = gson.toJsonTree(skierMsg2).getAsJsonObject();
//
//    JsonObject skierMsgJson = (JsonObject) gson.toJsonTree(skierMsg);
//    valueOfSkierID.add("liftID", skierMsgJson.get("liftID"));
//    valueOfSkierID.add("time", skierMsgJson.get("time"));
//    valueOfSkierID.add("resortID", skierMsgJson.get("resortID"));
//    valueOfSkierID.add("seasonID", skierMsgJson.get("seasonID"));
//    valueOfSkierID.add("dayID", skierMsgJson.get("dayID"));

//    jedis.sadd(String.valueOf(skierMsg.getSkierID()), gson.toJson(valueOfSkierID));
//    jedis.sadd(String.valueOf(skierMsg.getSkierID()), gson.toJson(valueOfSkierID1));
//    jedis.sadd(String.valueOf(skierMsg.getSkierID()), gson.toJson(valueOfSkierID2));
//    //jedis.sadd(String.valueOf(skierMsg.getSkierID()), valueOfSkierID);
//    long res = jedis.scard(String.valueOf(skierMsg.getSkierID()));
//    System.out.println(res);
//    System.out.println(jedis.smembers(String.valueOf(skierMsg.getSkierID())));
//
//    Set<Integer> uniqueDays = new HashSet<>();
//    for (String s : jedis.smembers(String.valueOf(skierMsg.getSkierID()))) {
//      SkierMsg skierMsgFromRedis = gson.fromJson(s, SkierMsg.class);
//      uniqueDays.add(skierMsgFromRedis.getDayID());
//    }
//    System.out.println(uniqueDays.size());

  }
}