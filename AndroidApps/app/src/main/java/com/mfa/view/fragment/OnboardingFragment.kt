package com.mfa.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mfa.R


class OnboardingFragment : Fragment() {
    private var pageNumber: Int = 0
    private var title: String? = null
    private var description: String? = null
    private var showSteps: Boolean = false
    private var showGoogleLogin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pageNumber = it.getInt(ARG_PAGE_NUMBER)
            title = it.getString(ARG_TITLE)
            description = it.getString(ARG_DESCRIPTION)
            showSteps = it.getBoolean(ARG_SHOW_STEPS)
            showGoogleLogin = it.getBoolean(ARG_SHOW_GOOGLE_LOGIN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivIllustration: ImageView = view.findViewById(R.id.ivIllustration)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val stepsContainer: LinearLayout = view.findViewById(R.id.stepsContainer)
//        val googleLoginButton: Button = view.findViewById(R.id.btnGoogleLogin)

        // Set title and description
        tvTitle.text = title
        tvDescription.text = description

        // Configure illustration based on page number
        // You will set the drawable resources later
        when (pageNumber) {
            1 -> ivIllustration.setImageResource(R.drawable.human_greeting)
            2 -> ivIllustration.visibility=View.GONE
            3 -> ivIllustration.setImageResource(R.drawable.logo_png)
        }

        // Show/hide steps
        if (showSteps) {
            stepsContainer.visibility = View.VISIBLE
            setupSteps(stepsContainer)
        } else {
            stepsContainer.visibility = View.GONE
        }

        // Show/hide Google login button
//        if (showGoogleLogin) {
//            googleLoginButton.visibility = View.VISIBLE
//        } else {
//            googleLoginButton.visibility = View.GONE
//        }
    }

    private fun setupSteps(container: LinearLayout) {
        val steps = listOf(
            Step(1,R.drawable.face_recognition, "Daftar wajah", "Pastikan anda telah berhasil mendaftarkan wajah di aplikasi"),
            Step(2,R.drawable.door, "Pilih kelas", "Pada halaman utama, pilihlah kelas yang akan anda hadiri"),
            Step(3,R.drawable.pertemuan, "Pilih pertemuan", "Setelah memilih kelas, pilihlah pertemuan yang anda hadiri"),
            Step(4,R.drawable.map_marker, "Cek lokasi", "Setelah anda menekan tombol presensi, pastikan lokasi anda di dalam kelas"),
            Step(5,R.drawable.qrcode, "Scan qr code", "Setelah lokasi anda terdeteksi benar, pastikan anda scan qr code yang dibuat dosen di kelas"),
            Step(6, R.drawable.face_recognition,"Verifikasi wajah", "Setelah hasil scan qr code benar anda dapat melakukan langkah terakhir yaitu verifikasi wajah")
        )

        val inflater = LayoutInflater.from(context)

        for (step in steps) {
            val stepView = inflater.inflate(R.layout.item_step, container, false)
            val tvStepNumber = stepView.findViewById<TextView>(R.id.tvStepNumber)
            val tvStepTitle = stepView.findViewById<TextView>(R.id.tvStepTitle)
            val tvStepDescription = stepView.findViewById<TextView>(R.id.tvStepDescription)
            val ivStepIcon = stepView.findViewById<ImageView>(R.id.ivStepIcon)

            tvStepNumber.text = step.number.toString()
            tvStepTitle.text = step.title
            tvStepDescription.text = step.description
            // Icon will be set by you later
            ivStepIcon.setImageResource(step.icon)

            container.addView(stepView)
        }
    }

    companion object {
        private const val ARG_PAGE_NUMBER = "page_number"
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_SHOW_STEPS = "show_steps"
        private const val ARG_SHOW_GOOGLE_LOGIN = "show_google_login"

        fun newInstance(
            pageNumber: Int,
            title: String,
            description: String,
            showSteps: Boolean = false,
            showGoogleLogin: Boolean = false
        ): OnboardingFragment {
            return OnboardingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PAGE_NUMBER, pageNumber)
                    putString(ARG_TITLE, title)
                    putString(ARG_DESCRIPTION, description)
                    putBoolean(ARG_SHOW_STEPS, showSteps)
                    putBoolean(ARG_SHOW_GOOGLE_LOGIN, showGoogleLogin)
                }
            }
        }
    }

    data class Step(val number: Int, val icon:Int , val title: String, val description: String)
}