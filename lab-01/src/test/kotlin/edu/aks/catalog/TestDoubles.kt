package edu.aks.catalog

import java.lang.reflect.Proxy

internal inline fun <reified T> stubInterface(crossinline invoke: (String, List<Any?>) -> Any?): T =
    T::class.java.cast(Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
        invoke(method.name, args.orEmpty().toList())
    })

internal fun injectTestField(target: Any, name: String, value: Any) {
    target.javaClass.getDeclaredField(name).apply { isAccessible = true }.set(target, value)
}
