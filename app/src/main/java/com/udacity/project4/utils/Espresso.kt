package com.udacity.project4.utils

import androidx.test.espresso.idling.CountingIdlingResource

object Espresso {
    private const val resource = "GLOBAL"

    @JvmField
    val counting_id_resource = CountingIdlingResource(resource)
    fun decrement() {
        if (counting_id_resource != null) {
            counting_id_resource.decrement()
        }
    }

    fun incrementNotEmpty() {
        counting_id_resource.increment()
    }

}

inline fun <T> wrapEspressoResource(function: () -> T): T {
    Espresso.incrementNotEmpty()
    return try {
        function()
    } finally {
        Espresso.decrement()
    }
}