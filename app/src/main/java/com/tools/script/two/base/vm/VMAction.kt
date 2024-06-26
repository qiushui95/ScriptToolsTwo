package com.tools.script.two.base.vm

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import pro.respawn.flowmvi.api.MVIAction

interface VMAction : MVIAction {
    fun throwOuterInvoke() {
        throw RuntimeException("需要在外部处理该Action")
    }

    fun dismissDialogFragment(owner: LifecycleOwner?, tag: String) {
        owner ?: return

        val fragmentManager = when (owner) {
            is FragmentActivity -> owner.supportFragmentManager
            is Fragment -> owner.childFragmentManager
            else -> return
        }

        val fragment = fragmentManager.findFragmentByTag(tag)

        if (fragment is DialogFragment) {
            fragment.dismiss()
        }
    }

    operator fun invoke(owner: LifecycleOwner?)
}
