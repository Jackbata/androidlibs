package com.learn.battertoolslib

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings

import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout



/**
 * FileName: EyeViewUtils
 * Author: barry
 * Date: 2020/9/9 11:46 AM
 * Description: 护眼模式
 */
class EyeViewUtils {
    var eyeView: FrameLayout? = null
    var windowManager: WindowManager? = null

    companion object {
        @Volatile
        private var single: EyeViewUtils? = null
        val instance: EyeViewUtils?
            get() {
                if (single == null) {
                    synchronized(EyeViewUtils::class.java) {
                        if (single == null) {
                            single = EyeViewUtils()
                        }
                    }
                }
                return single
            }
    }

    /**
     * 获取护眼色值
     */
    private fun getFilterColor(): Int {
        return Color.parseColor("#338C5D00")
    }

    /**
     * 切换护眼模式覆盖应用view
     */
    fun openEye(isOpen: Boolean, mContext: Context, viewGroup: ViewGroup) {
        if (isOpen) openEye(mContext, viewGroup) else closeEye()
    }

    /**
     * 切换护眼模式覆盖系统最顶层window
     */
    fun openEyeOnWindow(isOpen: Boolean, mContext: Context) {
        if (isOpen) openEyeOnWindow(mContext) else closeEye()
    }

    private fun initEyeView(mContext: Context, viewGroup: ViewGroup) {
        eyeView = FrameLayout(mContext)
        eyeView?.setBackgroundColor(Color.TRANSPARENT)
        val params = WindowManager.LayoutParams()
        params.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        viewGroup.addView(eyeView, params)
    }

    private  fun openEye(mContext: Context, viewGroup: ViewGroup) {
        eyeView ?: initEyeView(mContext, viewGroup)
        eyeView?.setBackgroundColor(getFilterColor())
    }

    private fun closeEye() {
        eyeView?.setBackgroundColor(Color.TRANSPARENT)
    }

    open fun release() {
        windowManager?.removeViewImmediate(eyeView)
        windowManager=null
        eyeView = null
        single = null
    }


    private fun openEyeOnWindow(mContext: Context) {
        eyeView ?: initEyeView(mContext)
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(mContext)) { //有悬浮窗权限开启服务绑定 绑定权限
                eyeView?.setBackgroundColor(getFilterColor())

            } else { //没有悬浮窗权限,去开启悬浮窗权限
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    mContext.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else { //默认有悬浮窗权限 但是 华为, 小米,oppo等手机会有自己的一套Android6.0以下 会有自己的一套悬浮窗权限管理 也需要做适配
            eyeView?.setBackgroundColor(getFilterColor())

        }
    }

    private fun initEyeView(mContext: Context) {
        windowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val params = WindowManager.LayoutParams().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY or
                        WindowManager.LayoutParams.TYPE_STATUS_BAR
            } else {
                this.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            }
            this.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            this.format = PixelFormat.TRANSLUCENT
        }
        params.gravity = Gravity.START or Gravity.TOP
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT

        eyeView = FrameLayout(mContext)
        eyeView?.setBackgroundColor(Color.TRANSPARENT)
        windowManager?.addView(eyeView, params)
    }


}