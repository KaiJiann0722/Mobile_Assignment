package com.example.demo

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.demo.data.AuthVM
import com.example.demo.data.User
import com.example.demo.data.UserVM
import com.example.demo.databinding.ActivityMainBinding
import com.example.demo.databinding.HeaderLoginBinding
import com.example.demo.util.setImageBlob
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val nav by lazy { supportFragmentManager.findFragmentById(R.id.host)!!.findNavController() }
    private val userVM: UserVM by viewModels()

    private lateinit var abc: AppBarConfiguration
    private val auth: AuthVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        abc = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.forumFragment,
                R.id.friendsFragment,
                R.id.chatFragment,
                R.id.profileFragment
            ),
            binding.root
        )

        setupActionBarWithNavController(nav, abc)
        binding.bv.setupWithNavController(nav)
        binding.nv.setupWithNavController(nav)

        // TODO(5): Observe login status -> userLiveData
        auth.getUserLD().observe(this) { user ->
            // TODO(5A): Clear menu + remove header
            binding.nv.menu.clear()
            val h = binding.nv.getHeaderView(0)
            binding.nv.removeHeaderView(h)

            // TODO(5B): Inflate menu + header (based on login status)
            if (user == null) {
                binding.nv.inflateMenu(R.menu.drawer)
                binding.nv.inflateHeaderView(R.layout.header)
                // Optional
                nav.popBackStack(R.id.homeFragment, false)
                nav.navigateUp()
            }
            else {
                binding.nv.inflateMenu(R.menu.drawer_login)
                binding.nv.inflateHeaderView(R.layout.header_login)
                setHeader(user)
            }

            // TODO(5C): Handle logout menu item
            binding.nv.menu.findItem(R.id.logout)?.setOnMenuItemClickListener { logout() }

            binding.nv.menu.findItem(R.id.exit)?.setOnMenuItemClickListener {
                finishAndRemoveTask()
                true
            }
        }

        // TODO(8): Auto login -> auth.loginFromPreferences(...)
        lifecycleScope.launch { auth.loginFromPreferences() }


    }

    private fun setHeader(user: User) {
        val h = binding.nv.getHeaderView(0)
        val b = HeaderLoginBinding.bind(h)
        b.imgPhoto.setImageBlob(user.photo)
        b.txtName.text  = user.name
        b.txtEmail.text = user.id
    }

    private fun logout(): Boolean {
        // TODO(4): Logout -> auth.logout(...)
        //          Clear navigation backstack
        val sharedPref = getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)?:return false
        userVM.updateStatus(userId, "Offline")
        auth.logout()


        nav.popBackStack(R.id.homeFragment, false)
        nav.navigateUp()

        binding.root.close()
        return true
    }

    override fun onStop() {
        super.onStop()

        val sharedPref = getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)
        userId?.let {
            userVM.updateStatus(it, "Offline")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // TODO: AppBarConfiguration
        return nav.navigateUp(abc)
    }

}