package com.myself.sEverything.core.interceptor.impl;


import com.myself.sEverything.core.dao.FileIndexDao;
import com.myself.sEverything.core.interceptor.ThingInterceptor;
import com.myself.sEverything.core.model.Thing;

import java.util.PriorityQueue;

public class ThingDeleteInterceptor implements ThingInterceptor,Runnable {
    private FileIndexDao fileIndexDao;
    private PriorityQueue<Thing> queue = new PriorityQueue<>(10);

    public ThingDeleteInterceptor(FileIndexDao fileIndexDao) {
        this.fileIndexDao = fileIndexDao;
    }

    @Override
    public void apply(Thing thing) {
        this.queue.add(thing);
    }

    @Override
    public void run() {
        Thing thing = this.queue.poll();
        if (thing != null) {
            this.fileIndexDao.delete(thing);
        }
    }
}
