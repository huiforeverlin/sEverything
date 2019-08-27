package com.myself.sEverything.core.dao;


import com.myself.sEverything.core.model.Condition;
import com.myself.sEverything.core.model.Thing;

import java.util.List;

public interface FileIndexDao {
    void insert(Thing thing);

    List<Thing> search(Condition condition);

    void delete(Thing thing);
}
