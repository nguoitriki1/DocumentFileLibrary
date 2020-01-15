package com.example.mymanager.storage.usbmass.usb.util;

import java.util.LinkedHashMap;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private int cacheSize;

    public LRUCache(int cacheSize) {
        super(16, 0.75f, true);
        this.cacheSize = cacheSize;
    }

    protected boolean removeEldestEntry(Entry<K, V> eldest) {
        return size() >= cacheSize;
    }
}
