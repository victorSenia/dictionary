package org.leo.dictionary.audio;

import java.util.Collections;
import java.util.List;

public interface AudioService {
    void play(String language, String text);

    void abort();

    default void shutdown() {
    }

    default List<String> getVoicesNames(String language) {
        return Collections.emptyList();
    }

}
