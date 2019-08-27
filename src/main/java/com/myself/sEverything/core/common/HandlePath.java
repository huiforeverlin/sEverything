package com.myself.sEverything.core.common;

import lombok.Data;

import java.util.Set;

@Data
public class HandlePath {
    Set<String> includePath;
    Set<String> excludePath;
}
