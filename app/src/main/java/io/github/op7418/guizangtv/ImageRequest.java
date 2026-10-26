package io.github.op7418.guizangtv;

import java.util.concurrent.Future;
import java.util.function.Consumer;

/** Cancellation drops queued callbacks as well as work, without recycling shared bitmaps. */
final class ImageRequest {
    private final Consumer<Runnable> post, remove;
    private final Runnable ended, dispatch = this::dispatch;
    private Future<?> work;
    private Runnable delivery;
    private boolean cancelled, finished;

    ImageRequest(Consumer<Runnable> post, Consumer<Runnable> remove) { this(post, remove, () -> {}); }
    ImageRequest(Consumer<Runnable> post, Consumer<Runnable> remove, Runnable ended) {
        this.post = post; this.remove = remove; this.ended = ended;
    }
    synchronized void work(Future<?> task) {
        if (cancelled || finished) task.cancel(false); else work = task;
    }
    synchronized void deliver(Runnable ready) {
        if (cancelled || finished) return;
        delivery = ready;
        post.accept(dispatch);
    }
    private void dispatch() {
        Runnable ready;
        synchronized (this) {
            if (cancelled || finished) return;
            ready = delivery; delivery = null; work = null; finish();
        }
        if (ready != null) ready.run();
    }
    synchronized void cancel() {
        cancelled = true;
        if (work != null) work.cancel(false);
        work = null; delivery = null; remove.accept(dispatch); finish();
    }
    private void finish() { if (!finished) { finished = true; ended.run(); } }
    synchronized boolean cancelled() { return cancelled; }
    synchronized boolean pending() { return !finished; }
}
