package com.willowtreeapps.apptest

import com.willowtreeapps.apptest.proto.Rpc

class MockClient private constructor(val root: Element) {

    val requests = mutableListOf<Rpc.FindRequest>()

    class Builder {
        private var elements = mutableListOf<MockElement>()

        fun element(element: MockElement): Builder {
            elements.add(element)
            return this
        }

        fun build(): MockClient {
            return MockClient(MockElement("_root", children = elements))
        }
    }
}

class MockAppController : AppController {
    override fun setup() {
    }

    override fun startApp() {
    }

    override fun stopApp() {
    }

    override fun shutdown() {
    }
}

class MockElement @JvmOverloads constructor(private val id: String,
                                            override var text: String = "",
                                            val children: List<MockElement> = emptyList()): Element {

    override val exists: Boolean
        get() = true

    override val count: Int
        get() = children.size

    override fun find(path: String): Element
        = children.find { it.id == path } ?: MissingElement

    override fun get(child: Int): Element
            = children[child]

    override fun click() {
    }

    override fun typeText(text: String) {
        this.text = this.text + text
    }

    override fun replaceText(text: String) {
        this.text = text
    }
}

private object MissingElement : Element {

    override val exists: Boolean
        get() = false

    override val text: String
        get() = ""

    override val count: Int
        get() = 0

    override fun find(path: String): Element = MissingElement

    override fun get(child: Int): Element = MissingElement

    override fun click() {
    }

    override fun typeText(text: String) {
    }

    override fun replaceText(text: String) {
    }
}

