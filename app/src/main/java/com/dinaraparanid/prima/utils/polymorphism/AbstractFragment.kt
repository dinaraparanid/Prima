package com.dinaraparanid.prima.utils.polymorphism

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.dinaraparanid.prima.MainActivity
import com.dinaraparanid.prima.MainApplication
import kotlin.reflect.KClass

/**
 * Ancestor [Fragment] for all my fragments.
 * [MainActivity] fragments manipulates with app's main label
 */

abstract class AbstractFragment<B : ViewDataBinding, A : AbstractActivity> : Fragment() {
    protected abstract var binding: B?

    internal inline val fragmentActivity
        get() = requireActivity() as A

    protected inline val application
        get() = requireActivity().application as MainApplication

    internal companion object {
        internal const val MAIN_LABEL_OLD_TEXT_KEY = "main_label_old_text"
        internal const val MAIN_LABEL_CUR_TEXT_KEY = "main_label_cur_text"

        /**
         * Creates instances of fragments
         * with only main label params.
         *
         * @param mainLabelOldText current main label text
         * @param mainLabelCurText text to show when fragment' ll be active.
         * Can be null if fragment has constant title (like FAQ)
         * @param clazz [KClass] of fragment (::class)
         */

        @JvmStatic
        internal fun <B : ViewDataBinding, A : AbstractActivity, T : AbstractFragment<B, A>> defaultInstance(
            mainLabelOldText: String,
            mainLabelCurText: String?,
            clazz: KClass<out T>
        ): T = clazz.constructors.first().call().apply {
            arguments = Bundle().apply {
                putString(MAIN_LABEL_OLD_TEXT_KEY, mainLabelOldText)
                putString(MAIN_LABEL_CUR_TEXT_KEY, mainLabelCurText)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}