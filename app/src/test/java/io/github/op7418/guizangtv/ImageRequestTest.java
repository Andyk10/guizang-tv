package io.github.op7418.guizangtv;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class ImageRequestTest {
    private final List<Runnable> queue=new ArrayList<>();
    private final AtomicInteger ended=new AtomicInteger(), called=new AtomicInteger();
    private final ImageRequest request=new ImageRequest(queue::add,queue::remove,ended::incrementAndGet);

    @Test public void deliversOnceAndDropsFinishedState() {
        request.deliver(called::incrementAndGet);
        assertTrue(request.pending());
        queue.remove(0).run();
        assertEquals(1,called.get());assertEquals(1,ended.get());assertFalse(request.pending());
        request.deliver(called::incrementAndGet);request.cancel();request.cancel();
        assertTrue(queue.isEmpty());assertEquals(1,ended.get());
    }
    @Test public void cancelsQueuedWorkWithoutInvokingUi() {
        FutureTask<Void> work=new FutureTask<>(()->{called.incrementAndGet();return null;});
        request.work(work);request.cancel();work.run();
        assertTrue(work.isCancelled());assertEquals(0,called.get());assertFalse(request.pending());
    }
    @Test public void removesPostedCallbackAndRejectsLateDelivery() {
        request.deliver(called::incrementAndGet);
        Runnable dequeued=queue.get(0);
        request.cancel();
        assertTrue(queue.isEmpty());
        dequeued.run();request.deliver(called::incrementAndGet);
        assertTrue(queue.isEmpty());assertEquals(0,called.get());assertEquals(1,ended.get());
    }
    @Test public void cancellationBeforeFutureRegistrationIsSafe() {
        request.cancel();FutureTask<Void> work=new FutureTask<>(()->null);request.work(work);
        assertTrue(work.isCancelled());assertTrue(request.cancelled());assertEquals(1,ended.get());
    }
    @Test public void completionBeforeFutureRegistrationIsSafe() {
        request.deliver(called::incrementAndGet);queue.remove(0).run();
        FutureTask<Void> work=new FutureTask<>(()->null);request.work(work);
        assertTrue(work.isCancelled());assertEquals(1,called.get());assertEquals(1,ended.get());
    }
}
