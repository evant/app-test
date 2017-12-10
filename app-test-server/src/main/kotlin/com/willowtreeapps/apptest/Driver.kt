package com.willowtreeapps.apptest

import com.willowtreeapps.apptest.android.AndroidAppController
import com.willowtreeapps.apptest.proto.Rpc
import com.willowtreeapps.apptest.proto.ServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.Server
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.netty.NettyChannelBuilder
import io.grpc.stub.StreamObserver

class Driver private constructor(private val channel: ManagedChannel, val appController: AppController) {
    private val stub = ServiceGrpc.newBlockingStub(channel)

    val platform: Platform
        get() = Platform.ANDROID //TODO

    val formFactor: FormFactor
        get() {
            val screen = stub.screen(Rpc.ScreenRequest.getDefaultInstance())
            return when (screen.deviceClass) {
                Rpc.ScreenInfo.DeviceClass.HANDSET -> FormFactor.HANDSET
                Rpc.ScreenInfo.DeviceClass.TABLET -> FormFactor.TABLET
                else -> FormFactor.NONE
            }
        }

    fun shutdown() {
        channel.shutdown()
    }

    fun find(id: String): Element {
        return RemoteElement(stub, id)
    }

    fun pressBack() {
        stub.button(Rpc.ButtonRequest.newBuilder()
                .setButton(Rpc.ButtonRequest.Button.BACK)
                .build())
    }

    companion object {

        private var mockServer: Server? = null
        private var mockDriver: Driver? = null
        private var currentClient: MockClient? = null

        @JvmStatic
        @JvmOverloads
        fun start(host: String, config: Config, port: Int = 2734): Driver {
            return Driver(NettyChannelBuilder.forAddress(host, port)
                    .usePlaintext(true)
                    .build(), AndroidAppController(config))
        }

        @JvmStatic
        fun mock(client: MockClient): Driver {
            currentClient = client
            if (mockDriver == null) {
                mockDriver = Driver(InProcessChannelBuilder.forName("mock-driver")
                        .build(), MockAppController())
            }
            connectMockClient()
            return mockDriver!!
        }

        private fun connectMockClient() {
            if (mockServer == null) {
                mockServer = InProcessServerBuilder.forName("mock-driver")
                        .addService(MockService())
                        .build()
                        .start()
            }
        }

        private class MockService : ServiceGrpc.ServiceImplBase() {
            override fun find(find: Rpc.FindRequest, responseObserver: StreamObserver<Rpc.Element>) {
                currentClient?.let { client ->
                    client.requests.add(find)
                    val found = find.path.splitToSequence("/").fold(client.root) { element, id ->
                        element.find(id)
                    }
                    responseObserver.onNext(found.toBackingElement())
                    responseObserver.onCompleted()
                }
            }
        }
    }
}

private fun Element.toBackingElement(): Rpc.Element {
    val builder = Rpc.Element.newBuilder()
    val attrs = Rpc.Attributes.newBuilder()
    if (text != null) {
        attrs.text = text
    }
    builder.setAttrs(attrs)
    return builder.build()
}

