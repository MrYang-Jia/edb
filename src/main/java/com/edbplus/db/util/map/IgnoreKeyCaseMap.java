package com.edbplus.db.util.map;
import java.util.*;

/**
 * 忽略大小写KEY，但保留原始KEY大小写的Map
 * 支持：IgnoreKeyCaseMap<String, Object> 写法
 */
public class IgnoreKeyCaseMap<K, V> implements Map<K, V> {

    private final Map<K, V> originalMap = new LinkedHashMap<>();
    private final Map<String, K> lowerKeyMap = new HashMap<>();

    public IgnoreKeyCaseMap() {
    }

    public IgnoreKeyCaseMap(Map<K, V> m) {
        putAll(m);
    }

    @Override
    public V put(K key, V value) {
        if (key instanceof String) {
            String lowerKey = ((String) key).toLowerCase();
            lowerKeyMap.put(lowerKey, key);
        }
        return originalMap.put(key, value);
    }

    @Override
    public V get(Object key) {
        if (key instanceof String) {
            String lowerKey = ((String) key).toLowerCase();
            K realKey = lowerKeyMap.get(lowerKey);
            return originalMap.get(realKey);
        }
        return originalMap.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            String lowerKey = ((String) key).toLowerCase();
            return lowerKeyMap.containsKey(lowerKey);
        }
        return originalMap.containsKey(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    // ------------------------------
    // 以下都是标准实现，不用管
    // ------------------------------

    @Override
    public Set<K> keySet() {
        return originalMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return originalMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return originalMap.entrySet();
    }

    @Override
    public int size() {
        return originalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return originalMap.isEmpty();
    }

    @Override
    public V remove(Object key) {
        if (key instanceof String) {
            String lowerKey = ((String) key).toLowerCase();
            K realKey = lowerKeyMap.remove(lowerKey);
            return originalMap.remove(realKey);
        }
        return originalMap.remove(key);
    }

    @Override
    public void clear() {
        originalMap.clear();
        lowerKeyMap.clear();
    }

    @Override
    public boolean containsValue(Object value) {
        return originalMap.containsValue(value);
    }
}