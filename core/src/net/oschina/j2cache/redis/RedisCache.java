package net.oschina.j2cache.redis;

import net.oschina.j2cache.Cache;
import net.oschina.j2cache.CacheException;
import net.oschina.j2cache.util.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Redis 缓存基于Hashs实现
 *
 * @author wendal
 */
class RedisCache implements Cache {

    // 记录region
    protected String namespace;
    protected String region;
    protected byte[] regionBytes;
    protected RedisClient client;

    public RedisCache(String namespace, String region, RedisClient client) {
        if (region == null || region.isEmpty())
            region = "_"; // 缺省region

        this.client = client;
        this.namespace = namespace;
        this.region = getRegionName(region);
        this.regionBytes = region.getBytes();
    }

    /**
     * 在region里增加一个可选的层级,作为命名空间,使结构更加清晰
     * 同时满足小型应用,多个J2Cache共享一个redis database的场景
     *
     * @param region
     * @return
     */
    private String getRegionName(String region) {
        if (namespace != null && !namespace.isEmpty()) {
            region = namespace + ":" + region;
        }
        return region;
    }

    private byte[] getKeyName(Object key) {
        if (key instanceof Number)
            return ("I:" + key).getBytes();
        else if (key instanceof String || key instanceof StringBuilder || key instanceof StringBuffer)
            return ("S:" + key).getBytes();
        return ("O:" + key).getBytes();
    }

    @Override
    public Serializable get(Serializable key) throws IOException, CacheException {
        if (null == key)
            return null;
        byte[] bytes = client.get().hget(regionBytes, getKeyName(key));
        return (Serializable)SerializationUtils.deserialize(bytes);
    }

    @Override
    public void put(Serializable key, Serializable value) throws IOException, CacheException {
        if (key == null)
            return;
        if (value == null)
            evict(key);
        else
            client.get().hset(regionBytes, getKeyName(key), SerializationUtils.serialize(value));
    }

    @Override
    public void update(Serializable key, Serializable value) throws IOException, CacheException {
        this.put(key, value);
    }

    @Override
    public void evict(Serializable key) {
        if (key == null)
            return;
        client.get().hdel(regionBytes, getKeyName(key));
    }

    @Override
    public void evicts(List<Serializable> keys) {
        if (keys == null || keys.size() == 0)
            return;
        int size = keys.size();
        byte[][] o_keys = new byte[size][];
        for (int i = 0; i < size; i++) {
            o_keys[i] = getKeyName(keys.get(i));
        }
        client.get().hdel(regionBytes, o_keys);
    }

    @Override
    public Set<Serializable> keys() {
        Set<Serializable> keys = new HashSet<>();
        client.get().hkeys(regionBytes).forEach(keyBytes -> {
            try {
                keys.add((Serializable)SerializationUtils.deserialize(keyBytes));
            }catch(IOException e){}
        });
        return keys;
    }

    @Override
    public void clear() throws CacheException {
        client.get().del(regionBytes);
    }

}