package com.mophsic.checkinview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_first.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        view.findViewById<Button>(R.id.button_first).setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }
        checkInView.firstSevenDay = listOf(
            Bonus(1, 100),
            Bonus(2, 200),
            Bonus(3, 300),
            Bonus(4, 400),
            Bonus(5, 500),
            Bonus(6, 600),
            Bonus(7, 700)
        )

        checkInView.nextSevenDay = listOf(
            Bonus(8, 700),
            Bonus(9, 600),
            Bonus(10, 500),
            Bonus(11, 400),
            Bonus(12, 300),
            Bonus(13, 200),
            Bonus(14, 100)
        )

        checkInView.currentDay = 3

        checkInView2.firstSevenDay = listOf(
            Bonus(1, 100),
            Bonus(2, 200),
            Bonus(3, 300),
            Bonus(4, 400),
            Bonus(5, 500),
            Bonus(6, 600),
            Bonus(7, 700)
        )

        checkInView2.currentDay = 3

        button_first.setOnClickListener {
            checkInView.currentDay++
            checkInView2.currentDay ++
        }
    }
}