package com.example.opengraphsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opengraphsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        binding.apply {
            btnAddLink.setOnClickListener {

            }

            rvLinkList.layoutManager = LinearLayoutManager(this@MainActivity)
            rvLinkList.adapter = null // todo: make adapter.
        }
    }
}