package com.myself.sEverything.core.index.impl;


import com.myself.sEverything.config.sEverythingConfig;
import com.myself.sEverything.core.dao.FileIndexDao;
import com.myself.sEverything.core.index.FileScan;
import com.myself.sEverything.core.interceptor.FileInterceptor;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class FileScanImpl implements FileScan {
    private final FileIndexDao fileIndexDao;
    private final sEverythingConfig config = sEverythingConfig.getInstance();
    private List<FileInterceptor> interceptors = new ArrayList<>();
    private Integer fileCount = 0;

    public FileScanImpl(FileIndexDao fileIndexDao) {
        this.fileIndexDao = fileIndexDao;
    }

    @Override
    public void index(String path) {

        File file = new File(path);
        if (file.isFile()) {
            if (config.getExcludePath().contains(file.getParent())) {
                return;
            }
        } else {
            if (config.getExcludePath().contains(path)) {
                return;
            }
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    index(f.getAbsolutePath());
                }
            }
        }
        for (FileInterceptor interceptor : interceptors) {
            fileCount++;
            interceptor.apply(file);
        }
    }

    @Override
    public void interceptors(FileInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }
}
