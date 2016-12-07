package com.cml.imageselector.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 作者：陈明亮 on 2016/10/13 10:04
 * 博客：http://blog.csdn.net/zc2_5781
 */

public class ImageLoader {

    private static ImageLoader mImageLoader;

    /**
     * 图片的缓存处理
     */
    private LruCache<String,Bitmap> mLruCache;

    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    private static final int DEFAULT_THREDD_COUNT = 1;

    /**
     * 线程池的线程数量
     */
    private int mThreadCount;
    /**
     * 轮询的线程
     */
    private Thread mPoolThread ;
    private Handler mPoolThreadHander;
    /**
     * UI线程中的Handler
     */
    private Handler mUIHandler;

    /**
     * 队列的调度方式
     */
    private Type mType = Type.LIFO;
    public enum Type{
        FIFO,LIFO;
    }

    /**
     * 从尾部和头部取值
     * 内部链表(不需要连续内存)
     */
    private LinkedList<Runnable> mTaskQueue;


    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    private Semaphore mSemaphorePThreadPool ;


    /**
     * 单例
     */
    public static ImageLoader newInstance(){
        if(mImageLoader == null){
            synchronized (ImageLoader.class){
                if(mImageLoader == null){
                    mImageLoader = new ImageLoader(DEFAULT_THREDD_COUNT, Type.LIFO);
                }
            }
        }
        return mImageLoader;
    }

    private ImageLoader(int mThreadCount,Type type){
        init(mThreadCount,type);
    }

    private void init(int mThreadCount,Type type) {
        //后台轮询线程
       mPoolThread = new Thread(){
           @Override
           public void run() {
               Looper.prepare();

               mPoolThreadHander = new Handler(){
                   @Override
                   public void handleMessage(Message msg) {
                       //线程池去取出一个任务执行
                       mThreadPool.execute(getTask());

                       try {
                           mSemaphorePThreadPool.acquire();
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                   }
               };
                //释放一个信号量
               mSemaphorePoolThreadHandler.release();

               Looper.loop();
           }
       } ;

        mPoolThread.start();

        //获取应用的最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory/8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes()*value.getHeight();//每行占据的内存*高度
            }
        };

        mThreadPool = Executors.newFixedThreadPool(mThreadCount);
        mTaskQueue = new LinkedList<>();
        mType = type;

        mSemaphorePThreadPool = new Semaphore(mThreadCount);//可多个同时执行
    }

    /**
     * 取出一个任务
     * @return
     */
    private Runnable getTask() {
        if(mType == Type.FIFO){
            return mTaskQueue.removeFirst();
        }else {
            return mTaskQueue.removeLast();
        }
    }

    public void loadImage(final String path, final ImageView imageView){
        imageView.setTag(path);
        if(mUIHandler == null){
            mUIHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    //获取得到的图片,为imageview回调设置图片
                    ImageBeanHolder imageBeanHolder  = (ImageBeanHolder) msg.obj;
                    //imageview是复用的,不比较会出现错位
                    if(imageBeanHolder.imageView.getTag().toString().equals(imageBeanHolder.path)){
                        imageBeanHolder.imageView.setImageBitmap(imageBeanHolder.bitmap);
                    }
                }
            };
        }

        Bitmap bm = getBitmapFromLruCache(path);
        if(bm != null){
            refreshBitmap(path,imageView,bm);
        }else {
            addTasks(new Runnable(){
                @Override
                public void run() {
                    //加载图片
                    //图片的压缩
                    //获取图片需要显示的大小
                    ImageSize imageViewSize = getImageViewSize(imageView);
                    //压缩图片
                    Bitmap bm = decodeSampledBitmapFromPath(path,imageViewSize.width,imageViewSize.height);
                    //加入到缓存
                    addBitmapToLruCache(path,bm);

                    refreshBitmap(path,imageView,bm);

                    mSemaphorePThreadPool.release();
                }
            });
        }

    }

    private void refreshBitmap(String path,ImageView imageView,Bitmap bitmap){
        Message message = Message.obtain();
        ImageBeanHolder imageBeanHolder = new ImageBeanHolder();
        imageBeanHolder.bitmap = bitmap;
        imageBeanHolder.path = path;
        imageBeanHolder.imageView = imageView;
        message.obj = imageBeanHolder;
        mUIHandler.sendMessage(message);
    }

    private void addBitmapToLruCache(String path, Bitmap bm) {
        if(getBitmapFromLruCache(path) == null){
            if(bm != null){
                mLruCache.put(path,bm);
            }
        }
    }

    private Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inSampleSize = caculateInSampleSize(options,width,height);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    private int caculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if(width > reqWidth || height > reqHeight){
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);
            inSampleSize = Math.max(widthRadio,heightRadio);//最大程度的压缩图片
//            inSampleSize = Math.min(widthRadio,heightRadio);//保持图片的比例,不失真
        }

        return inSampleSize;
    }

    private ImageSize getImageViewSize(ImageView imageView) {

        ImageSize imageSize = new ImageSize();

        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        int width = imageView.getWidth();//实际宽度

        if(width <= 0){
            width = layoutParams.width;//layout中声明的宽度
        }


        if(width <= 0){
            width = getImageViewFieldValue(imageView,"mMaxWidth");//layout中声明的宽度
        }

        if(width <= 0){
            width = displayMetrics.widthPixels;
        }


        int height = imageView.getHeight();

        if(height <= 0){
            height = layoutParams.height;
        }


        if(height <= 0){
            height = getImageViewFieldValue(imageView,"mMaxHeight");
        }

        if(height <= 0){
            height = displayMetrics.heightPixels;
        }

        imageSize.width = width;
        imageSize.height = height;
        return imageSize;
    }

    private static int getImageViewFieldValue(Object obj,String fieldName){
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(obj);
            if(fieldValue >0 && fieldValue <Integer.MAX_VALUE){
                value = fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;

    }

    private synchronized void addTasks(Runnable runnable) {
        mTaskQueue.add(runnable);
        try {
            if(mPoolThreadHander == null){
                mSemaphorePoolThreadHandler.acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mPoolThreadHander.sendEmptyMessage(0x110);
    }

    private Bitmap getBitmapFromLruCache(String key) {
        return mLruCache.get(key);
    }

    private class ImageBeanHolder{
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }

    private class ImageSize{
        int width;
        int height;
    }

}
