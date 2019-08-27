package com.myself.sEverything.core.interceptor.impl;


import com.myself.sEverything.core.common.FileConvertThing;
import com.myself.sEverything.core.dao.FileIndexDao;
import com.myself.sEverything.core.interceptor.FileInterceptor;
import com.myself.sEverything.core.model.Thing;

import java.io.File;

public class FileIndexInterceptor implements FileInterceptor {
    private final FileIndexDao fileIndexDao;

    public FileIndexInterceptor(FileIndexDao fileIndexDao) {
        this.fileIndexDao = fileIndexDao;
    }

    @Override
    public void apply(File file) {
        Thing thing = FileConvertThing.convert(file);
        this.fileIndexDao.insert(thing);
    }
}