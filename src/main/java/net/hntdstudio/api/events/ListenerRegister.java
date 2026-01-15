package net.hntdstudio.api.events;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.IBaseEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ListenerRegister {

    private final EventRegistry eventRegistry;

    public ListenerRegister(EventRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
    }

    public void registerListener(Object listener) {
        Class<?> clazz = listener.getClass();

        for (Method method : clazz.getDeclaredMethods()) {

            if (!method.isAnnotationPresent(EventHandler.class)) continue;
            if (method.getParameterCount() != 1) continue;

            Class<?> param = method.getParameterTypes()[0];

            if (!IBaseEvent.class.isAssignableFrom(param)) continue;

            method.setAccessible(true);

            registerTyped(param, method, listener);
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends IBaseEvent<Void>> void registerTyped(
            Class<?> rawEventClass,
            Method method,
            Object listener
    ) {
        Class<E> eventClass = (Class<E>) rawEventClass;

        eventRegistry.register(eventClass, (E event) -> {
            try {
                if (Modifier.isStatic(method.getModifiers())) {
                    method.invoke(null, event);
                } else {
                    method.invoke(listener, event);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(
                        "Failed to invoke event handler: " + method, e
                );
            }
        });
    }

}