package com.ysj.demo.aplugin.demo1

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ysj.demo.aplugin.demo1.databinding.Demo1FragmentTestBinding

/**
 *
 *
 * @author Ysj
 * Create time: 2025/4/8
 */
class Demo1Fragment : Fragment(R.layout.demo1_fragment_test) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vb = Demo1FragmentTestBinding.bind(requireView())
        vb.btn.setOnClickListener {
            Toast.makeText(requireContext(), vb.btn.text, Toast.LENGTH_SHORT).show()
        }
    }

}