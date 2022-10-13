package com.example.notificationutils;

import android.graphics.Bitmap;
import android.util.LruCache;

public class ImageCacheManager {
    public static ImageCacheManager instance;
    private LruCache<Object,Object> lruCache;

    public ImageCacheManager(){
        lruCache=new LruCache<Object,Object>(1024);
    }

    public static ImageCacheManager getInstance(){
        if(instance==null){
            instance= new ImageCacheManager();
        }
        return instance;
    }
    public LruCache<Object, Object> getLru() {
        return lruCache;
    }

    public void saveBitmapToCahche(String key, Bitmap bitmap){
        try {
            ImageCacheManager.getInstance().getLru().put(key, bitmap);
        }catch (Exception ignored){}
    }

    public Bitmap getBitmapFromCache(String key){
        try {
            return (Bitmap) ImageCacheManager.getInstance().getLru().get(key);
        }catch (Exception ignored){}
        return null;
    }
}
