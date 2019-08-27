package com.myself.sEverything.core.search.impl;


import com.myself.sEverything.core.dao.FileIndexDao;
import com.myself.sEverything.core.model.Condition;
import com.myself.sEverything.core.model.Thing;
import com.myself.sEverything.core.search.FileSearch;

import java.util.List;

public class FileSearchImpl implements FileSearch {
    private final FileIndexDao fileIndexDao;

    public FileSearchImpl(FileIndexDao fileIndexDao) {
        this.fileIndexDao = fileIndexDao;
    }

    @Override
    public List<Thing> search(Condition condition) {
        return this.fileIndexDao.search(condition);
    }
}