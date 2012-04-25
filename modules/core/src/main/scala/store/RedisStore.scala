package org.codefirst.katze.core.store

import dispatch.json.JsValue
import java.net.URI
import redis.clients.jedis._

class RedisStore(uri : String) extends Store {
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

  def read(name : String) : Option[JsValue] = {
    val s = redis { redis => redis.get(name) }
    Option(s) map {
      JsValue.fromString(_)
    }
  }

  def write(name : String, value : JsValue) {
    redis { _.set(name, value.toString) }
  }

  def remove(name : String) {
    redis { _.del(name) }
  }
}
