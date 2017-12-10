package com.willowtreeapps.apptest

import com.willowtreeapps.apptest.proto.Rpc
import com.willowtreeapps.apptest.proto.ServiceGrpc
import io.grpc.Metadata
import io.grpc.StatusRuntimeException

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
        get() = check {
            stub.find(Rpc.FindRequest.newBuilder()
                    .setPath(path)
                    .build())
        }

    override val exists: Boolean
        get() = backingElement.error.isEmpty()

    override val text: String?
        get() = backingElement.attrs.text

    override val count: Int
        get() = backingElement.attrs.count

    override fun find(path: String): Element
            = RemoteElement(stub, "${this.path}/$path")

    override fun get(child: Int): Element
            = RemoteElement(stub, "${this.path}/$child")

    override fun click() {
        check {
            stub.click(Rpc.ClickRequest.newBuilder()
                    .setPath(path)
                    .build())
        }
    }

    override fun typeText(text: String) {
        check {
            stub.text(Rpc.TextRequest.newBuilder()
                    .setPath(path)
                    .setText(text)
                    .setMode(Rpc.TextRequest.Mode.APPEND)
                    .build())
        }
    }

    override fun replaceText(text: String) {
        check {
            stub.text(Rpc.TextRequest.newBuilder()
                    .setPath(path)
                    .setText(text)
                    .setMode(Rpc.TextRequest.Mode.REPLACE)
                    .build())
        }
    }

    private inline fun <T> check(f: () -> T): T {
        try {
            return f()
        } catch (e: StatusRuntimeException) {
            val message = e.trailers.get(Metadata.Key.of("message-bin", Metadata.BINARY_BYTE_MARSHALLER))?.let {
                String(it, Charsets.UTF_8)
            }
            throw AssertionError(e.status.code.toString() + if (message != null) " " + message else "", e)
        }
    }
}
