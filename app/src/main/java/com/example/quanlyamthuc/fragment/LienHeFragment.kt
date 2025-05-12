package com.example.quanlyamthuc.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quanlyamthuc.R

class LienHeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lien_he, container, false)

        val tvFacebook = view.findViewById<TextView>(R.id.tvFacebook)
        val tvInstagram = view.findViewById<TextView>(R.id.tvInstagram)
        val tvTiktok = view.findViewById<TextView>(R.id.tvTiktok)

        tvFacebook.setOnClickListener {
            openLink("https://www.facebook.com/share/196NPpXPdD/?mibextid=wwXIfr")
        }

        tvInstagram.setOnClickListener {
            openLink("https://www.instagram.com/ten_instagram/")
        }

        tvTiktok.setOnClickListener {
            openLink("https://www.tiktok.com/@ten_tiktok")
        }

        return view
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

}
