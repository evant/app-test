package com.willowtreeapps.apptest.android

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.*
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.util.TreeIterables
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import com.willowtreeapps.apptest.client.android.R
import com.willowtreeapps.apptest.proto.Rpc
import com.willowtreeapps.apptest.proto.ServiceGrpc
import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.stub.StreamObserver
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.SelfDescribing
import kotlin.math.roundToInt

class Service : ServiceGrpc.ServiceImplBase() {

    override fun find(find: Rpc.FindRequest, responseObserver: StreamObserver<Rpc.Element>) {
        val attributes = AttributeRecordAssertion()
        catchExpected(responseObserver) {
            Espresso.onView(ViewMatchers.isRoot()).perform(buildAction(find.path, attributes))
            attributes.toElement()
        }
    }

    override fun click(click: Rpc.ClickRequest, responseObserver: StreamObserver<Rpc.ClickResponse>) {
        catchExpected(responseObserver) {
            Espresso.onView(ViewMatchers.isRoot()).perform(buildAction(click.path, ViewActions.click()))
            Rpc.ClickResponse.getDefaultInstance()
        }
    }

    override fun text(text: Rpc.TextRequest, responseObserver: StreamObserver<Rpc.TextResponse>) {
        catchExpected(responseObserver) {
            val action = when (text.mode) {
                Rpc.TextRequest.Mode.REPLACE ->
                    ViewActions.replaceText(text.text)
                Rpc.TextRequest.Mode.APPEND ->
                    ViewActions.typeText(text.text)
                else -> {
                    throw AssertionError("Unknown text mode: ${text.mode}")
                }
            }
            Espresso.onView(ViewMatchers.isRoot()).perform(buildAction(text.path, action))
            Rpc.TextResponse.getDefaultInstance()
        }
    }

    override fun button(request: Rpc.ButtonRequest, responseObserver: StreamObserver<Rpc.ButtonResponse>) {
        catchExpected(responseObserver) {
            Espresso.pressBack()
            Rpc.ButtonResponse.getDefaultInstance()
        }
    }

    override fun screen(request: Rpc.ScreenRequest, responseObserver: StreamObserver<Rpc.ScreenInfo>) {
        val res = InstrumentationRegistry.getTargetContext().resources
        val m = res.displayMetrics
        responseObserver.onNext(Rpc.ScreenInfo.newBuilder()
                .setWidthPixels(m.widthPixels)
                .setHeightPixels(m.heightPixels)
                .setWidthPoints((m.widthPixels.toFloat() / m.density).roundToInt())
                .setHeightPoints((m.heightPixels.toFloat() / m.density).roundToInt())
                .setDeviceClass(if (res.getBoolean(R.bool.apptest_isTablet)) {
                    Rpc.ScreenInfo.DeviceClass.TABLET
                } else {
                    Rpc.ScreenInfo.DeviceClass.HANDSET
                })
                .build())
        responseObserver.onCompleted()
    }

    private inline fun <V> catchExpected(observer: StreamObserver<V>, f: () -> V) {
        try {
            observer.onNext(f())
            observer.onCompleted()
        } catch (e: NoMatchingViewException) {
            observer.onError(noMatchingViewStatus(e))
        }
    }

    private fun noMatchingViewStatus(e: NoMatchingViewException): StatusException
            = StatusException(Status.NOT_FOUND, Metadata().apply {
                put(Metadata.Key.of("message-bin", Metadata.BINARY_BYTE_MARSHALLER), e.message?.toByteArray())
            })

    private fun buildAction(path: String, viewAction: ViewAction): ViewAction {
        return path.split("/")
                .reversed()
                .fold(viewAction) { action, part ->
                    val index = part.toIntOrNull()
                    if (index != null) {
                        EnsureChildIndexAction(path, part.toInt(), action)
                    } else {
                        EnsureChildIdAction(path, part, action)
                    }
                }
    }

    private class EnsureChildIdAction(private val path: String,
                                      private val id: String,
                                      private val viewAction: ViewAction) : ViewAction {
        override fun getDescription(): String = ""

        override fun getConstraints(): Matcher<View> = withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()
            for (child in TreeIterables.breadthFirstViewTraversal(view)) {
                if (child.idName == id) {
                    viewAction.perform(uiController, child)
                    return
                }
            }
            throw NoMatchingViewException.Builder()
                    .withViewMatcher(IdViewMatcher())
                    .withRootView(view)
                    .build()
        }

        private inner class IdViewMatcher : BaseMatcher<View>() {
            override fun matches(item: Any?): Boolean = true

            override fun describeTo(description: Description) {
                description.appendText("id $id in $path")
            }
        }
    }

    private class EnsureChildIndexAction(private val path: String,
                                         private val index: Int,
                                         private val viewAction: ViewAction) : ViewAction {
        override fun getDescription(): String = ""

        override fun getConstraints(): Matcher<View> = withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)

        override fun perform(uiController: UiController, view: View) {
            if (view is RecyclerView) {
                view.scrollToPosition(index)
                uiController.loopMainThreadUntilIdle()
                val child = view.findViewHolderForLayoutPosition(index)?.itemView
                if (child != null) {
                    viewAction.perform(uiController, child)
                } else {
                    throw NoMatchingViewException.Builder()
                            .withViewMatcher(IndexViewMatcher())
                            .withRootView(view)
                            .build()
                }
            } else if (view is ListView) {
                view.setSelection(index)
                uiController.loopMainThreadUntilIdle()
                val child = view.getChildAt(index - view.firstVisiblePosition)
                viewAction.perform(uiController, child)
            } else if (view is ViewGroup && index > 0 && index < view.childCount) {
                val child = view.getChildAt(index)
                viewAction.perform(uiController, child)
            } else {
                throw NoMatchingViewException.Builder()
                        .withViewMatcher(IndexViewMatcher())
                        .withRootView(view)
                        .build()
            }
        }

        private inner class IndexViewMatcher : BaseMatcher<View>() {
            override fun matches(item: Any?): Boolean = true

            override fun describeTo(description: Description) {
                description.appendText("index $index in $path")
            }
        }
    }

    private class AttributeRecordAssertion : ViewAction {
        var exists: Boolean = false
        var text: String? = null
        var count: Int = 0

        override fun getDescription(): String = ""

        override fun getConstraints(): Matcher<View> = Matchers.any(View::class.java)

        override fun perform(uiController: UiController, view: View) {
            exists = true
            if (view is TextView) {
                text = view.text.toString()
            }
            count = when (view) {
                is RecyclerView -> view.adapter?.itemCount ?: 0
                is ListView -> view.count
                is ViewGroup -> view.childCount
                else -> 0
            }
        }
    }

    private fun AttributeRecordAssertion.toElement(): Rpc.Element {
        val builder = Rpc.Element.newBuilder()
        if (text != null) {
            builder.text = text
        }
        builder.count = count
        return builder.build()
    }
}

private val View.idName get() = if (id != View.NO_ID) resources.getResourceEntryName(id) else null
