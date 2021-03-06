package com.example.harisont.librery

import android.content.Intent
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.harisont.librery.db.AppDB

import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private var db: AppDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        db = AppDB.getInstance(this)

        fab.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            if (CheckNetworkStatus.isNetworkAvailable(this)) {
                val url = "http://anonimalettori.altervista.org/db/select_all.php"
                val client = OkHttpClient()
                val req = Request.Builder().url(url).build()
                thread {
                    client.newCall(req).enqueue(object : Callback {  // cannot use .execute() in the UI thread
                        override fun onResponse(call: Call?, response: Response?) {
                            val json = response?.body()?.string()
                            println("Works like a charm!")
                            startActivity(Intent(this@MainActivity, RecommendationsActivity::class.java)
                                    .putExtra("res", json))     // search results are sent to the new activity as JSON
                        }

                        override fun onFailure(call: Call?, e: IOException?) {
                            println("Epic fail!")
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, getString(R.string.query_failure), Toast.LENGTH_LONG).show()
                            }
                        }
                    })
                }
            } else Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_LONG).show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> {
                    val haveReadFragment = MainActivityFragment() as Fragment
                    val args = Bundle()
                    args.putBoolean("read", true)
                    haveReadFragment.arguments = args
                    return haveReadFragment
                }
                1 -> {
                    val haveReadFragment = MainActivityFragment() as Fragment
                    val args = Bundle()
                    args.putBoolean("read", false)
                    haveReadFragment.arguments = args
                    return haveReadFragment
                }
                else -> return null
            }
        }

        override fun getCount(): Int {
            // Show 2 total pages.
            return 2
        }
    }
}
