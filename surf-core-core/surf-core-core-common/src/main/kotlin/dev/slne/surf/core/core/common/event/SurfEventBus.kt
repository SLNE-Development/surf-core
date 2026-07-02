package dev.slne.surf.core.core.common.event

import dev.slne.surf.core.api.common.event.SurfEvent
import dev.slne.surf.core.api.common.event.SurfEventHandler
import dev.slne.surf.core.api.common.event.redis.SurfEventFireRedisEvent
import dev.slne.surf.core.core.CoreInstance
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

object SurfEventBus {
    private val listeners = mutableMapOf<KClass<out SurfEvent>, MutableList<(SurfEvent) -> Unit>>()

    fun registerListener(listener: Any) {
        val clazz = listener::class

        clazz.declaredFunctions.forEach { function ->
            if (function.findAnnotation<SurfEventHandler>() != null) {
                val params = function.parameters

                if (params.size != 2) {
                    return@forEach
                }

                val eventType = params[1].type.classifier as? KClass<*>
                    ?: return@forEach

                if (!SurfEvent::class.java.isAssignableFrom(eventType.java)) {
                    return@forEach
                }

                @Suppress("UNCHECKED_CAST")
                val eventClass = eventType as KClass<out SurfEvent>

                function.isAccessible = true

                val executor: (SurfEvent) -> Unit = { event ->
                    if (eventClass.isInstance(event)) {
                        function.call(listener, event)
                    }
                }

                listeners.computeIfAbsent(eventClass) { mutableListOf() }.add(executor)
            }
        }
    }

    fun subscribe(eventClass: KClass<out SurfEvent>, handler: (SurfEvent) -> Unit) {
        listeners.computeIfAbsent(eventClass) { mutableListOf() }.add(handler)
    }


    fun fireLocal(event: SurfEvent) {
        listeners[event::class]?.forEach { handler ->
            handler(event)
        }
    }

    fun fire(event: SurfEvent) {
        CoreInstance.redisApi.publishEvent(SurfEventFireRedisEvent(event))
    }
}