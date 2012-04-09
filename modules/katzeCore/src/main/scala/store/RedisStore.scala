package org.codefirst.katze.core.store

import sjson.json.{Reads,Writes, JsonSerialization}
import dispatch.json.JsValue
import java.net.URI
import redis.clients.jedis._

class RedisStore(uri : String) extends Store {
  import org.codefirst.katze.core._
  import JsonSerialization._

  val redisURI =
    new URI(uri)

  val password =
    if(redisURI.getUserInfo == null)
      null
    else
      redisURI.getUserInfo().split(":",2)(1)

  val pool =
    new JedisPool(new JedisPoolConfig(),
                  redisURI.getHost(),
                  redisURI.getPort(),
                  Protocol.DEFAULT_TIMEOUT,
                  password)

  def redis[T](f : Jedis => T) : T = {
    val redis =
      pool.getResource

    try {
      f(redis)
    } finally {
      pool.returnResource(redis)
    }
  }

  def read[T](name : String)(implicit fjs : Reads[T]) : Option[T] = {
    val s =
      redis { redis => redis.get(name) }
    Option(s) map {
      JsValue.fromString _
    } map {
      fromjson[T](_)
    }
  }


  def write[T](name : String, obj : T)(implicit fjs : Writes[T]) {
    redis { _.set(name, tojson(obj).toString) }
  }
}
