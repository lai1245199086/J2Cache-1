package net.oschina.j2cache.hibernate4;

import net.oschina.j2cache.CacheChannel;
import net.oschina.j2cache.CacheObject;
import org.hibernate.HibernateException;

import java.io.IOException;
import java.io.Serializable;

public class J2CacheCacheRegion implements CacheRegion {

    private CacheChannel cacheChannel;
    private String region;

    public J2CacheCacheRegion(CacheChannel channel, String region) {
        this.cacheChannel = channel;
        this.region = region;
    }

    @Override
    public String getName() {
        return this.region;
    }

    @Override
    public void clear() {
        try {
            this.cacheChannel.clear(this.region);
        } catch (IOException e){
            throw new HibernateException(e);
        }
    }

    @Override
    public CacheObject get(Object key) {
        try {
            return this.cacheChannel.get(this.region, (Serializable) key);
        } catch (IOException e){
            throw new HibernateException(e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        try {
            this.cacheChannel.set(this.region, (Serializable) key, (Serializable) value);
        } catch (IOException e){
            throw new HibernateException(e);
        }
    }

    @Override
    public void evict(Object key) {
        try {
            this.cacheChannel.evict(this.region, (Serializable)key);
        } catch (IOException e){
            throw new HibernateException(e);
        }
    }

    public Iterable<? extends Object> keys() {
        try {
            return this.cacheChannel.keys(this.region);
        } catch (IOException e){
            throw new HibernateException(e);
        }
    }
}