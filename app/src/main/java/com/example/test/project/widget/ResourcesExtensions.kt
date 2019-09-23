package com.example.test.project.widget

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTabHost
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import java.util.*

fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun Context.dip(value: Float): Int = (value * resources.displayMetrics.density).toInt()

fun Context.sp(value: Int): Int = (value * resources.displayMetrics.scaledDensity).toInt()
fun Context.sp(value: Float): Int = (value * resources.displayMetrics.scaledDensity).toInt()

fun Context.px2dip(px: Int): Float = (px.toFloat() / resources.displayMetrics.density).toFloat()
fun Context.px2sp(px: Int): Float = (px.toFloat() / resources.displayMetrics.scaledDensity).toFloat()

fun Context.dimen(resource: Int): Int = resources.getDimensionPixelSize(resource)

typealias StringArray = Array<out String>

fun Context.getConfiguredResources(): android.content.res.Resources {
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(Locale.getDefault())
    return createConfigurationContext(configuration).resources
}

fun Context.configuredStringArray(@ArrayRes resId: Int): StringArray {
    return getConfiguredResources().getStringArray(resId)
}

fun Context.string(@StringRes resId: Int): String {
    return Resources.string(
        applicationContext,
        resId
    )
}

fun Context.formattedString(
    @StringRes resId: Int,
    vararg formatArgs: String
): String {
    return Resources.formattedString(
        applicationContext,
        resId,
        *formatArgs
    )
}

fun Context.stringArray(@ArrayRes resId: Int): StringArray {
    return Resources.stringArray(
        applicationContext,
        resId
    )
}

@ColorInt
fun Context.color(@ColorRes resId: Int): Int {
    return Resources.color(
        applicationContext,
        resId
    )
}

fun Context.drawable(@DrawableRes resId: Int): Drawable {
    return Resources.drawable(
        applicationContext,
        resId
    )
}

fun Activity.string(@StringRes resId: Int): String {
    return application.string(resId)
}

fun Fragment.string(@StringRes resId: Int): String {
    return requireContext().string(resId)
}

fun View.string(@StringRes resId: Int): String {
    return context.string(resId)
}

@ColorInt
fun Activity.color(@ColorRes resId: Int): Int {
    return application.color(resId)
}

@ColorInt
fun Fragment.color(@ColorRes resId: Int): Int {
    return requireContext().color(resId)
}

@ColorInt
fun View.color(@ColorRes resId: Int): Int {
    return context.color(resId)
}

fun Activity.drawable(@DrawableRes resId: Int): Drawable {
    return application.drawable(resId)
}

fun Fragment.drawable(@DrawableRes resId: Int): Drawable {
    return requireContext().drawable(resId)
}

fun View.drawable(@DrawableRes resId: Int): Drawable {
    return context.drawable(resId)
}

fun Activity.stringArray(@ArrayRes resId: Int): StringArray {
    return application.stringArray(resId)
}

fun Fragment.stringArray(@ArrayRes resId: Int): StringArray {
    return requireContext().stringArray(resId)
}


fun View.stringArray(@ArrayRes resId: Int): StringArray {
    return context.stringArray(resId)
}

fun Activity.formattedString(
    @StringRes resId: Int,
    vararg formatArgs: String
): String {
    return application.formattedString(resId, *formatArgs)
}

fun Fragment.formattedString(
    @StringRes resId: Int,
    vararg formatArgs: String
): String {
    return requireContext().formattedString(resId, *formatArgs)
}

fun View.formattedString(
    @StringRes resId: Int,
    vararg formatArgs: String
): String {
    return context.formattedString(resId, *formatArgs)
}

internal object Resources {

    fun formattedString(
        context: Context,
        @StringRes resId: Int,
        vararg formatArgs: String
    ): String {
        return context.resources.getString(resId, *formatArgs)
    }

    fun string(
        context: Context,
        @StringRes resId: Int
    ): String {
        return context.resources.getString(resId)
    }

    fun stringArray(
        context: Context,
        @ArrayRes resId: Int
    ): Array<out String> {
        return context.resources.getStringArray(resId)
    }

    @ColorInt
    fun color(
        context: Context,
        @ColorRes resId: Int
    ): Int {
        return ContextCompat.getColor(context, resId)
    }

    fun drawable(
        context: Context,
        @DrawableRes resId: Int
    ): Drawable {
        return context.resources.getDrawable(resId, null)
    }
}