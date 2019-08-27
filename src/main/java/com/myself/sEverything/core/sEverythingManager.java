package com.myself.sEverything.core;

import com.myself.sEverything.config.sEverythingConfig;
import com.myself.sEverything.core.common.HandlePath;
import com.myself.sEverything.core.dao.DataSourceFactory;
import com.myself.sEverything.core.dao.FileIndexDao;
import com.myself.sEverything.core.dao.impl.FileIndexDaoImpl;
import com.myself.sEverything.core.index.FileScan;
import com.myself.sEverything.core.index.impl.FileScanImpl;
import com.myself.sEverything.core.interceptor.impl.FileIndexInterceptor;
import com.myself.sEverything.core.interceptor.impl.ThingDeleteInterceptor;
import com.myself.sEverything.core.model.Condition;
import com.myself.sEverything.core.model.Thing;
import com.myself.sEverything.core.moniter.FileWatch;
import com.myself.sEverything.core.moniter.impl.FileWatchImpl;
import com.myself.sEverything.core.search.FileSearch;
import com.myself.sEverything.core.search.impl.FileSearchImpl;

import javax.sql.DataSource;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class sEverythingManager {
    private static volatile sEverythingManager manager;
    private FileSearch fileSearch;
    private FileScanImpl fileScan;
    private ExecutorService executorService;

    private ThingDeleteInterceptor thingInterceptor;
    private Thread thingClearThread;
    private AtomicBoolean backgroundThreadStatus = new AtomicBoolean(false);

    private FileWatch fileWatch;

    private sEverythingManager() {
        this.initComponent();
    }

    public static sEverythingManager getInstance() {
        if (manager == null) {
            synchronized (sEverythingManager.class) {
                if (manager == null) {
                    manager = new sEverythingManager();
                }
            }
        }
        return manager;
    }

    private void initComponent() {
        DataSource dataSource = DataSourceFactory.getInstance();
        initOrResetDatabase();
        FileIndexDao fileIndexDao = new FileIndexDaoImpl(dataSource);
        this.fileSearch = new FileSearchImpl(fileIndexDao);

        this.fileScan = new FileScanImpl(fileIndexDao);
        this.fileScan.interceptors(new FileIndexInterceptor(fileIndexDao));

        this.thingInterceptor = new ThingDeleteInterceptor(fileIndexDao);
        this.thingClearThread = new Thread(this.thingInterceptor);
        this.thingClearThread.setDaemon(true);
        this.thingClearThread.setName("Thing-Clear-Thread");

        this.fileWatch = new FileWatchImpl(fileIndexDao);

        new Thread(new Runnable() {
            @Override
            public void run() {
                buildIndex();
            }
        }).start();
    }

    public List<Thing> search(Condition condition) {
        return this.fileSearch.search(condition)
                .stream().filter(new Predicate<Thing>() {
                    @Override
                    public boolean test(Thing thing) {
                        String path = thing.getPath();
                        File file = new File(path);
                        boolean flag = file.exists();
                        if (!flag) {
                            thingInterceptor.apply(thing);
                        }
                        return flag;
                    }
                }).collect(Collectors.toList());
    }

    public void buildIndex() {
        initOrResetDatabase();
        Set<String> directories = sEverythingConfig.getInstance().getIncludePath();
        if (this.executorService == null) {
            this.executorService = Executors.newFixedThreadPool(directories.size(), new ThreadFactory() {
                private final AtomicInteger threadID = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("Thread-Clear-" + threadID.getAndIncrement());
                    return thread;
                }
            });
        }
        final CountDownLatch countDownLatch = new CountDownLatch(directories.size());
        System.out.println("Start scanning...");
        long start = System.currentTimeMillis();
        for (String path : directories) {
            this.executorService.submit(new Runnable() {
                @Override
                public void run() {
                    sEverythingManager.this.fileScan.index(path);
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        Integer fileTotalCount = fileScan.getFileCount();
        System.out.println("Finish scanning...");
        System.out.println("Cost time of scanning is " + (end - start) + " milli");
        System.out.println("total file count is " + fileTotalCount + " 个");
    }

    private void initOrResetDatabase() {
        DataSourceFactory.initDatabase();
    }

    public void startThingClearThread() {
        if (this.backgroundThreadStatus.compareAndSet(false, true)) {
            this.thingClearThread.start();
        } else {
            System.out.println("can not start Thread repeatedly");
        }
    }

    public void startFileSystemMonitor() {
        sEverythingConfig config = sEverythingConfig.getInstance();
        HandlePath handlePath = new HandlePath();
        handlePath.setIncludePath(config.getIncludePath());
        handlePath.setExcludePath(config.getExcludePath());
        this.fileWatch.monitor(handlePath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                fileWatch.start();
            }
        }).start();
    }
}
