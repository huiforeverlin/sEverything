package com.myself.sEverything.core.search;

import com.myself.sEverything.core.model.Condition;
import com.myself.sEverything.core.model.Thing;

import java.util.List;

public interface FileSearch {
    List<Thing> search(Condition condition);
}
