package com.rosan.xposed

import com.rosan.util.log
import com.rosan.xposed.hook.MiShare
import dalvik.system.DexClassLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookInit : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam == null) return
        if (!lpparam.isFirstApplication) return
        if (lpparam.classLoader == null) return
        /*var topClassLoader = this::class.java.classLoader
        while (true) {
            var parentClassLoader = topClassLoader.parent ?: break
            topClassLoader = parentClassLoader
        }
        log(topClassLoader::class.java.getDeclaredField("parent"))*/
        MiShare(lpparam).start()
    }
}