package dev.slne.surf.core.core.common.event

import dev.slne.surf.core.api.common.event.SurfServerStartEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * `fireLocal` runs on redis subscriber threads while `subscribe` may be called from any lifecycle
 * thread. This exercises both at once; against the previous `HashMap` of `ArrayList`s it reliably
 * failed with a [ConcurrentModificationException] (or lost handlers).
 */
class SurfEventBusConcurrencyTest {

    @Test
    fun `concurrent subscribe and dispatch neither fails nor loses handlers`() {
        val subscriberCount = 4
        val dispatcherCount = 4
        val handlersPerSubscriber = 250

        val invocations = AtomicLong()
        val failures = CopyOnWriteArrayList<Throwable>()
        val registered = AtomicInteger()

        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(subscriberCount + dispatcherCount)

        try {
            repeat(subscriberCount) {
                executor.submit {
                    start.await()
                    runCatching {
                        repeat(handlersPerSubscriber) {
                            SurfEventBus.subscribe(SurfServerStartEvent::class) {
                                invocations.incrementAndGet()
                            }
                            registered.incrementAndGet()
                        }
                    }.onFailure(failures::add)
                }
            }

            repeat(dispatcherCount) {
                executor.submit {
                    start.await()
                    runCatching {
                        val event = SurfServerStartEvent("test-server")
                        repeat(1_000) { SurfEventBus.fireLocal(event) }
                    }.onFailure(failures::add)
                }
            }

            start.countDown()
            executor.shutdown()
            assertTrue(
                executor.awaitTermination(60, TimeUnit.SECONDS),
                "event bus stress run did not finish in time"
            )
        } finally {
            executor.shutdownNow()
        }

        assertTrue(failures.isEmpty(), "concurrent access failed: ${failures.map { it.toString() }}")
        assertEquals(subscriberCount * handlersPerSubscriber, registered.get())

        // Every registered handler must still be reachable after the concurrent registrations.
        val before = invocations.get()
        SurfEventBus.fireLocal(SurfServerStartEvent("test-server"))
        assertEquals(
            (subscriberCount * handlersPerSubscriber).toLong(),
            invocations.get() - before
        )
    }
}
