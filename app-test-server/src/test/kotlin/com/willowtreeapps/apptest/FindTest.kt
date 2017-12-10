package com.willowtreeapps.apptest

import org.junit.Assert.assertEquals
import org.junit.Test

class FindTest {
    @Test
    fun findsElement() {
        val client = MockClient.Builder()
                .element(MockElement("hello_world", text = "Hello, World!"))
                .build()
        val driver: Driver = Driver.mock(client)
        val element = driver.find("hello_world")

        assertEquals("Hello, World!", element.text)
    }

    @Test
    fun findsNestedElement() {
        val client = MockClient.Builder()
                .element(MockElement("container",
                        children = listOf(
                                MockElement("hello_world", text = "Hello, World!")
                        )))
                .build()
        val driver: Driver = Driver.mock(client)
        val element = driver.find("container").find("hello_world")

        assertEquals("Hello, World!", element.text)
        assertEquals("container/hello_world", client.requests[0].path)
    }
}