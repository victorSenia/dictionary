package org.leo.dictionary.controller;

import java.util.function.Consumer;

final class BackgroundTaskRunner {

    private BackgroundTaskRunner() {
    }

    static void runInBackground(String threadName, Runnable work, Consumer<RuntimeException> onError) {
        Thread thread = new Thread(() -> {
            try {
                work.run();
            } catch (RuntimeException ex) {
                onError.accept(ex);
            }
        }, threadName);
        thread.setDaemon(true);
        thread.start();
    }
}
