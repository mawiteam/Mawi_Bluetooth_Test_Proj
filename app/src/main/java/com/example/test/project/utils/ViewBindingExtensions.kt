@file:Suppress("UNCHECKED_CAST")

package com.example.test.project.utils

import android.app.Activity
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.test.project.fragments.BaseFragment
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

typealias Finder<T> = T.(Int) -> View?

object ViewBinder {
    fun reset(target: Any?) {
        target?.let(LazyRegistry::reset)
    }
}

private object LazyRegistry {
    private val lazyMap = WeakHashMap<Any, MutableCollection<LazyBinder<*, *>>>()

    fun register(target: Any, binder: LazyBinder<*, *>) {
        lazyMap.getOrPut(target, { Collections.newSetFromMap(WeakHashMap()) }).add(binder)
    }

    fun reset(target: Any) {
        lazyMap[target]?.forEach { it.reset() }
        lazyMap[target]?.clear()
    }
}

private class LazyBinder<T, V>(
    private val initializer: (T, KProperty<*>) -> V
) : ReadOnlyProperty<T, V> {

    private var value: Any? = null

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        LazyRegistry.register(thisRef!!, this)
        if (value == null)
            value = initializer(thisRef, property)

        return value as V
    }

    fun reset() {
        value = null
    }
}

private fun viewNotFound(id: Int, desc: KProperty<*>): Nothing =
    throw IllegalStateException("View ID $id for '${desc.name}' not found.")

private fun <T, V : View> bind(
    id: Int,
    finder: T.(Int) -> View?
) = LazyBinder { t: T, desc ->
    t.finder(id) as V? ?: viewNotFound(id, desc)
}


/** Finders */

private val DialogFragment.viewFinder: Finder<DialogFragment>
    get() = { dialog?.findViewById(id) ?: view?.findViewById(it) }


private val Activity.viewFinder: Finder<Activity>
    get() = { findViewById(it) }

private val BaseFragment.viewFinder: Finder<BaseFragment>
    get() = { containerView?.findViewById(it) }

private val View.viewFinder: Finder<View>
    get() = { findViewById(it) }

private val RecyclerView.ViewHolder.viewFinder: Finder<RecyclerView.ViewHolder>
    get() = { itemView.findViewById(it) }

/** Public Binders */

fun <V : View> DialogFragment.id(viewId: Int): ReadOnlyProperty<DialogFragment, V> {
    return bind(viewId, viewFinder)
}


fun <V : View> BaseFragment.id(id: Int): ReadOnlyProperty<BaseFragment, V> {
    return bind(id, viewFinder)
}

fun <V : View> Activity.id(id: Int): ReadOnlyProperty<Activity, V> {
    return bind(id, viewFinder)
}

fun <V : View> View.id(id: Int): ReadOnlyProperty<View, V> {
    return bind(id, viewFinder)
}

fun <V : View> RecyclerView.ViewHolder.id(
    id: Int
): ReadOnlyProperty<RecyclerView.ViewHolder, V> {
    return bind(id, viewFinder)
}

