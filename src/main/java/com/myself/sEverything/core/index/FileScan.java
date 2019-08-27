package com.myself.sEverything.core.index;


import com.myself.sEverything.core.interceptor.FileInterceptor;

public interface FileScan {
    void index(String path);
    void interceptors(FileInterceptor interceptor);
}
