package me.tatarka.inject.internal

import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.isSameAs
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze
import co.touchlab.stately.isolate.IsolateState
import kotlin.test.Test

class LazyMapTestNative {

    @Test
    fun caches_value_from_multiple_threads() {
        // only works with the new experimental runtime that's in kotlin 1.6+
        if (!KotlinVersion.CURRENT.isAtLeast(1, 6)) return

        val lazyMap = LazyMap()
        val count = 100
        val results = List(count) {
            Worker.start().execute(TransferMode.SAFE, { lazyMap }) { lazyMap ->
                lazyMap.get("test") { Any() }
            }
        }.map { it.result }

        assertThat(results).each {
            it.isSameAs(results.first())
        }
    }

    @Test
    fun caches_value_from_multiple_threads_statly() {
        val lazyMap = IsolateState { LazyMap() }
        val count = 100
        val results = List(count) {
            Worker.start().execute(TransferMode.SAFE, { lazyMap.freeze() }) { lazyMap ->
                lazyMap.access { it.get("test") { Any() } }
            }
        }.map { it.result }

        assertThat(results).each {
            it.isSameAs(results.first())
        }

        lazyMap.dispose()
    }
}