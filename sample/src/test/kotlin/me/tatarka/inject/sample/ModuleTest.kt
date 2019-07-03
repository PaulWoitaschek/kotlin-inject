package me.tatarka.inject.sample

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import me.tatarka.inject.annotations.*
import org.junit.Before
import org.junit.Test

@Inject class Foo : IFoo

@Module abstract class Module1 {
    abstract val foo: Foo
}

@Inject class Bar(val foo: Foo)

@Module abstract class Module2 {
    abstract val bar: Bar
}

@Module abstract class Module3 {
    var providesCalled = false

    abstract val foo: Foo

    @Provides
    fun foo() = Foo().also { providesCalled = true }
}

interface IFoo

@Module abstract class Module4 {
    abstract val foo: IFoo

    @get:Binds
    abstract val Foo.binds: IFoo
}

@Module abstract class Module5 {
    var providesCalledCount = 0

    abstract val foo: Foo

    @Provides
    @Singleton
    fun foo() = Foo().also { providesCalledCount++ }
}

var bazConstructorCount = 0

@Singleton @Inject class Baz {
    init {
        bazConstructorCount++
    }
}

@Singleton @Module abstract class Module6 {
    abstract val baz: Baz
}

class NamedFoo(val name: String)

@Module abstract class Module7 {
    @get:Named("1")
    abstract val foo1: NamedFoo

    @get:Named("2")
    abstract val foo2: NamedFoo

    @Provides @Named("1") fun foo1() = NamedFoo("1")
    @Provides @Named("2") fun foo2() = NamedFoo("2")
}

class ModuleTest {

    @Before
    fun setup() {
        bazConstructorCount = 0
    }

    @Test
    fun generates_a_module_that_provides_a_dep_with_no_arguments() {
        val module = Module1::class.create()

        assertThat(module.foo).isNotNull()
    }

    @Test
    fun generates_a_module_that_provides_a_dep_with_an_argument() {
        val module = Module2::class.create()

        assertThat(module.bar).isNotNull()
        assertThat(module.bar.foo).isNotNull()
    }

    @Test
    fun generates_a_modules_that_provides_a_dep() {
        val module = Module3::class.create()

        assertThat(module.foo).isNotNull()
        assertThat(module.providesCalled).isTrue()
    }

    @Test
    fun generates_a_module_that_binds_an_interface_to_a_dep() {
        val module = Module4::class.create()

        assertThat(module.foo).isInstanceOf(Foo::class)
    }

    @Test
    fun generates_a_module_where_a_singleton_provides_is_only_called_once() {
        val module = Module5::class.create()
        module.foo
        module.foo

        assertThat(module.providesCalledCount).isEqualTo(1)
    }

    @Test
    fun generates_a_module_where_a_singleton_constructor_is_only_called_once() {
        val module = Module6::class.create()
        module.baz
        module.baz

        assertThat(bazConstructorCount).isEqualTo(1)
    }

    @Test
    fun generates_a_module_that_provides_different_values_based_on_the_named_qualifier() {
        val module = Module7::class.create()

        assertAll {
            assertThat(module.foo1.name).isEqualTo("1")
            assertThat(module.foo2.name).isEqualTo("2")
        }
    }
}