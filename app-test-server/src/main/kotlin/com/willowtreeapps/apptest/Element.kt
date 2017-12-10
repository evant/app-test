package com.willowtreeapps.apptest

import com.willowtreeapps.apptest.proto.Rpc
import com.willowtreeapps.apptest.proto.ServiceGrpc

interface Element {
    val exists: Boolean

    val text: String?

    val count: Int

    fun find(path: String): Element

    fun get(child: Int): Element

    fun click()

    fun clearText() {
        replaceText("")
    }

    fun typeText(text: String)

    fun replaceText(text: String)
}

internal class RemoteElement(private val stub: ServiceGrpc.ServiceBlockingStub, private val path: String): Element {
    private val backingElement
        get() = stub.find(Rpc.FindRequest.newBuilder()
                .setPath(path)
                .build())

    override val exists: Boolean
        get() = backingElement.error.isEmpty()

    override val text: String?
        get() = backingElement.check().attrs.text

    override val count: Int
        get() = backingElement.check().attrs.count

    override fun find(path: String): Element
            = RemoteElement(stub, "${this.path}/$path")

    override fun get(child: Int): Element
            = RemoteElement(stub, "${this.path}/$child")

    override fun click() {
        checkError(stub.click(Rpc.ClickRequest.newBuilder()
                .setPath(path)
                .build()).error)
    }

    override fun typeText(text: String) {
        checkError(stub.text(Rpc.TextRequest.newBuilder()
                .setPath(path)
                .setText(text)
                .setMode(Rpc.TextRequest.Mode.APPEND)
                .build()).error)
    }

    override fun replaceText(text: String) {
        checkError(stub.text(Rpc.TextRequest.newBuilder()
                .setPath(path)
                .setText(text)
                .setMode(Rpc.TextRequest.Mode.REPLACE)
                .build()).error)
    }

    private fun Rpc.Element.check(): Rpc.Element
            = apply { checkError(error) }

    private fun checkError(error: String) {
        if (error.isNotEmpty()) {
            throw AssertionError(error)
        }
    }
}
