package com.intmainreturn00.bookar.domain

import android.content.Context
import android.util.TypedValue
import com.intmainreturn00.bookar.R
import kotlin.random.Random

// Singleton
class ResourceProvider private constructor(
    private val context: Context
) {

    companion object : SingletonHolder<ResourceProvider, Context>(::ResourceProvider)

    fun makeRandomTemplateColors() = when(Random.nextInt(3)) {
        0 -> Pair(context.getColor(R.color.red), context.getColor(R.color.orange))
        1 -> Pair(context.getColor(R.color.orange), context.getColor(R.color.dark))
        else -> Pair(context.getColor(R.color.dark), context.getColor(R.color.orange))
    }

    val templateWidth: Int by lazy {
        dpToPix(context, 75f).toInt()
    }

    val templateHeight: Int by lazy {
        dpToPix(context, 120f).toInt()
    }

    private fun dpToPix(context: Context, dp: Float): Float =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
}