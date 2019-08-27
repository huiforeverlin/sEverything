package com.myself.sEverything.core.moniter;


import com.myself.sEverything.core.common.HandlePath;

public interface FileWatch {
    void start();
    void stop();
    void monitor(HandlePath handlePath);
}
