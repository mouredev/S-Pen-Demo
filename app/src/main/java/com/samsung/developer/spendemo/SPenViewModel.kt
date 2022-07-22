package com.samsung.developer.spendemo

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.samsung.android.sdk.penremote.*
import kotlin.math.abs

/**
 * Created by MoureDev by Brais Moure on 12/7/22.
 * www.mouredev.com
 */

enum class SPenAction(val symbol: String) {

    NONE(""),
    ACTION("⏺️"),
    RIGHT("➡️"),
    LEFT("⬅️"),
    UP("⬆️"),
    DOWN("⬇️");

    companion object {
        fun randomAction(): SPenAction {
            return arrayListOf(RIGHT, LEFT, UP, DOWN).random()
        }
    }

}

class SPenViewModel: ViewModel() {

    private var manager: SpenUnitManager? = null

    var connected by mutableStateOf(false)
    var errorState by mutableStateOf("")

    var action by mutableStateOf(SPenAction.NONE)
    var airMotion by mutableStateOf("")

    var tryAction by mutableStateOf(SPenAction.NONE)

    fun communication(context: Context) {

        if(SpenRemote.getInstance().isConnected) {
            disconnect(context)
        } else {
            connect(context)
        }
    }

    private fun connect(context: Context) {

        try {

            SpenRemote.getInstance().connect(context, object : SpenRemote.ConnectionResultCallback {

                override fun onSuccess(spenUnitManager: SpenUnitManager?) {
                    manager = spenUnitManager
                    connected = true
                    errorState = ""
                    listenEvents()
                }

                override fun onFailure(error: Int) {
                    connected = false
                    errorState = when (error) {
                        SpenRemote.Error.UNSUPPORTED_DEVICE -> context.getString(R.string.spen_unsupported_device)
                        SpenRemote.Error.CONNECTION_FAILED -> context.getString(R.string.spen_connection_failed)
                        else -> context.getString(R.string.spen_error)
                    }
                }

            })

        } catch (e: NoClassDefFoundError) {
            errorState = context.getString(R.string.spen_unsupported_device)
        }
    }

    private fun disconnect(context: Context) {

        manager?.getUnit(SpenUnit.TYPE_BUTTON)?.let { button ->
            manager?.unregisterSpenEventListener(button)
        }
        manager?.getUnit(SpenUnit.TYPE_AIR_MOTION)?.let { airMotion ->
            manager?.unregisterSpenEventListener(airMotion)
        }

        SpenRemote.getInstance().disconnect(context)

        connected = false
        errorState = ""

        action = SPenAction.NONE
        airMotion = ""

        tryAction = SPenAction.NONE
    }

    private fun listenEvents() {

        val sPen = SpenRemote.getInstance()

        // Button
        if (sPen.isFeatureEnabled(SpenRemote.FEATURE_TYPE_BUTTON)) {
            manager?.registerSpenEventListener({ event ->

                when (ButtonEvent(event).action) {
                    ButtonEvent.ACTION_DOWN -> action = SPenAction.ACTION
                    ButtonEvent.ACTION_UP -> action = SPenAction.NONE
                }

            }, manager?.getUnit(SpenUnit.TYPE_BUTTON))
        }

        // Air motion
        if (sPen.isFeatureEnabled(SpenRemote.FEATURE_TYPE_AIR_MOTION)) {
            manager?.registerSpenEventListener({ event ->

                if (action != SPenAction.NONE) {

                    val airMotionEvent = AirMotionEvent(event)
                    val deltaX = airMotionEvent.deltaX
                    val deltaY = airMotionEvent.deltaY

                    if (deltaX > 0 && deltaY > 0 && abs(deltaY) > abs(deltaX)) {
                        action = SPenAction.UP
                    } else if (deltaX < 0 && deltaY < 0 && abs(deltaY) > abs(deltaX)) {
                        action = SPenAction.DOWN
                    } else if (deltaX < 0 && deltaY > 0 && abs(deltaX) > abs(deltaY)) {
                        action = SPenAction.LEFT
                    } else if (deltaX > 0 && deltaY < 0 && abs(deltaX) > abs(deltaY)) {
                        action = SPenAction.RIGHT
                    }

                    airMotion = "$deltaX, $deltaY"

                    if (tryAction == action) {
                        tryAction = SPenAction.randomAction()
                    }

                } else {
                    airMotion = ""
                }

            }, manager?.getUnit(SpenUnit.TYPE_AIR_MOTION))
        }

        tryAction = SPenAction.randomAction()
    }

}