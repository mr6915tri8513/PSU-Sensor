package com.example.psusensor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.psusensor.databinding.ActivityMainBinding
import com.example.psusensor.drawer.custom_gif.UploadResult
import com.example.psusensor.set_wifi.dialog_fragment.WarningDialogFragment
import com.example.psusensor.theme.Theme
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity(), Communicator {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebase: DatabaseReference
    private lateinit var powerViewModel: PowerViewModel
    private val root = FirebaseDatabase.getInstance().reference.root
    private val uuidSPP = ParcelUuid.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var overloadWarningShowed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.e("main activity", "onCreate")

        //supportActionBar?.hide()

        firebase = root.database.reference
        powerViewModel = ViewModelProvider(this)[PowerViewModel::class.java]

        //getPermission(Manifest.permission.BLUETOOTH_CONNECT, BLUETOOTH_CONNECT_REQUEST_CODE)

        getPowerLimit()

        val offlineDialog = PhoneOfflineDialogFragment()

        Timer().schedule(1000, 1000) {
            if (overloadWarningShowed > 0) {
                overloadWarningShowed--
            }
            getPowerOfTheSecond()
            if (!isOnline()) {
                if (!offlineDialog.isShowed) {
                    offlineDialog.show(supportFragmentManager, "phone_offline")
                }
            } else if (offlineDialog.isShowed) {
                offlineDialog.dismiss()
                getPowerLimit()
            }
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    //Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    //Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    //Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    //???
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun setSupportActionBar() {
        Handler(Looper.getMainLooper()).postDelayed(0) {
            val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
            val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
            val navigationView: NavigationView = findViewById(R.id.navigation_view)
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            setSupportActionBar(toolbar)
            appBarConfiguration = AppBarConfiguration(
                /*
                setOf(
                    R.id.home_page,
                    R.id.instant_power_curve_page,
                    R.id.monthly_electricity_bill_page,
                    R.id.theme_selection_page,
                    R.id.custom_gif_page,
                    R.id.reset_wifi_connection_page,
                    R.id.troubleshooting_page,
                    R.id.languages_page
                ), drawerLayout*/
                navController.graph, drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navigationView.setupWithNavController(navController)
        }
    }

    override fun setDrawerChecked(categoryIndex: Int, itemIndex: Int, isChecked: Boolean) {
        val navigationView: NavigationView? = findViewById(R.id.navigation_view)
        if (navigationView != null) {
            Log.e(
                "navigationView",
                "${navigationView.menu[categoryIndex].subMenu[itemIndex].title} $isChecked"
            )
            navigationView.menu[categoryIndex].subMenu[itemIndex].isChecked = isChecked
        } else {
            Log.e("navigationView", "is null")
        }
    }

    override fun setTheme(theme: Theme) {
        firebase.child("theme").apply {
            child("bg").setValue(String.format("%06X", theme.backgroundColor and 0xFFFFFF))
            child("led").setValue(String.format("%06X", theme.ledColor and 0xFFFFFF))
            child("txt").setValue(String.format("%06X", theme.textColor and 0xFFFFFF))
            child("flag").setValue(1)
        }
    }

    override fun compressAndUploadGif(uri: Uri) {
        Thread {
            val gif = Glide.with(this)
                .asGif()
                //.override(240)
                .load(uri)
                .submit(240, 240)
                .get()

            val byteBuffer = gif.buffer
            val imageBytes = ByteArray(byteBuffer.capacity())
            (byteBuffer.duplicate().clear() as ByteBuffer).get(imageBytes)
            val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            Log.e("base64", "${imageBytes.size} ${base64.length} " + base64.substring(0, 50))
            firebase.child("gif").apply {
                val data = base64.chunked(1024)
                child("file").setValue(data)//TODO listener?
                child("count").setValue(data.count())
                child("length").setValue(base64.length)
                child("flag").setValue(1)
            }
        }.start()

                /*
            .into(object: CustomTarget<GifDrawable>() {
                override fun onResourceReady(
                    resource: GifDrawable,
                    transition: Transition<in GifDrawable>?
                ) {
                    val byteBuffer = resource.buffer
                    val imageBytes = ByteArray(byteBuffer.capacity())
                    (byteBuffer.duplicate().clear() as ByteBuffer).get(imageBytes)
                    val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                    Log.e("base64", "${imageBytes.size} ${base64.length} " + base64.substring(0, 50))
                    firebase.child("gif").apply {
                        val data = base64.chunked(1024)
                        child("file").setValue(data)//TODO listener?
                        child("count").setValue(data.count())
                        child("length").setValue(base64.length)
                        child("flag").setValue(1)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.e("glide", "load cleared")
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    Log.e("glide", "load failed")
                }
            })*/
    }

    override fun uploadGif(uri: Uri, result: (Int) -> Unit) {
        firebase.child("gif/flag").get().addOnSuccessListener {
            val flag = it.getValue(Int::class.java)
            if (flag == 2) {
                result(UploadResult.DOWNLOADING)
            } else {
                contentResolver.openInputStream(uri)?.let { inputStream ->
                    val imageBytes = ByteArray(inputStream.available())
                    //Log.e("gif", inputStream.bufferedReader().use { it.readText() }) //it will close the inputStream!!
                    inputStream.read(imageBytes, 0, imageBytes.size)
                    inputStream.close()
                    val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                    Log.e(
                        "base64",
                        "${imageBytes.size} ${base64.length} " + base64.substring(0, 50)
                    )
                    firebase.child("gif").apply {
                        val data = base64.chunked(1024)
                        child("file").setValue(data)
                        child("count").setValue(data.count())
                        child("length").setValue(base64.length)
                        child("flag").setValue(1)
                            .addOnSuccessListener {
                                result(UploadResult.SUCCESS)
                            }.addOnFailureListener {
                                result(UploadResult.FAILED)
                            }.addOnCanceledListener {
                                result(UploadResult.CANCELED)
                            }
                    }
                }
            }
        }
    }

    private fun getPowerOfTheSecond() {
        //Log.e("power per second", firebase.child("power_per_second/$datetime").toString())
        firebase.child("power_per_second").get().addOnSuccessListener { snapShot ->
            val type = object: GenericTypeIndicator<List<String>>() {}
            val data = snapShot.getValue(type)
            if (data != null) {
                val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                val updateDatetime = LocalDateTime.parse(data[30], formatter)
                if (powerViewModel.isNeedToUpdate(updateDatetime)) {
                    val powerList = data.subList(0, 29).map { power ->
                        val voltage = power.substringBefore(',').toDoubleOrNull()
                        val current = power.substringAfter(',').toDoubleOrNull()
                        if (voltage != null && current != null) {
                            Pair(voltage, current)
                        } else {
                            null
                        }
                    }
                    if (powerViewModel.updateData(powerList, updateDatetime) && overloadWarningShowed == 0) {
                        overloadWarningShowed = 5
                        WarningDialogFragment(R.string.device_overload)
                            .show(supportFragmentManager, "device_overload")
                        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val vibratorManager =  this.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                            vibratorManager.defaultVibrator
                        } else {
                            getSystemService(VIBRATOR_SERVICE) as Vibrator
                        }
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                500,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    }
                } else {
                    powerViewModel.nextSecond()
                }
            } else {
                //Log.d("power per second", "get null")
                powerViewModel.nextSecond()
            }
        }.addOnFailureListener {
            //Log.d("power per second", "get failed")
            powerViewModel.nextSecond()
        }.addOnCanceledListener {
            powerViewModel.nextSecond()
        }
    }

    private fun getPowerLimit() {
        firebase.child("power_limit").get()
            .addOnSuccessListener {
                val data = it.getValue(Int::class.java)
                if (data != null) {
                    powerViewModel.updatePowerLimit(data)
                    Log.e("power limit", data.toString())
                } else {
                    Log.e("power limit", "get null")
                }
            }.addOnFailureListener {
                Log.e("power limit", "get failed")
            }.addOnCanceledListener {
                Log.e("power limit", "get canceled")
            }
    }

    override fun setPowerLimit(limit: Int) {
        firebase.child("power_limit").setValue(limit)
            .addOnSuccessListener {
                powerViewModel.updatePowerLimit(limit)
            }
    }

    override fun getPowerOfTheDay(year: Int, month: Int, day: Int, ret: (Array<Double?>) -> Unit) {
        val array = Array<Double?>(24) { null }
        var count = 0
        fun result(message: String) {
            Log.d("power per day", message)
            count++
            if (count == 24) {
                ret(array)
            }
        }
        firebase.child("power_per_hour").apply {
            for (i in 0..23) {
                //Log.e("date", child(String.format("%04d%02d%02d%02d", year, month, day, i)).toString())
                //child(String.format("%04d%02d%02d%02d", year, month, day, i)).setValue((0..300).random() + 0.05)
                child(String.format("%04d%02d%02d%02d", year, month, day, i)).get()
                    .addOnSuccessListener {
                        val data = it.getValue(Double::class.java)
                        if (data != null) {
                            array[i] = data / 3600.0
                            result(data.toString())
                        } else {
                            Log.e("exist", it.exists().toString())
                            result("get null")
                        }
                    }.addOnFailureListener {
                        result("get failed $it")
                    }.addOnCanceledListener {
                        result("get canceled")
                    }
            }
        }
    }

    override fun getPowerOfTheYear(year: Int, ret: (String) -> Unit) {
        val oddMonth = Array<Double?>(6) { null }
        val evenMonth = Array<Double?>(6) { null }
        var count = 0
        fun result(message: String) {
            Log.d("power per month", message)
            count++
            if (count == 12) {
                ret(oddMonth.joinToString(",") { (it?:0).toString() } + '|' + evenMonth.joinToString(",") { (it?:0).toString() })
            }
        }
        firebase.child("power_per_month").apply {
            for (i in 0..11) {
                //Log.e("month", child(String.format("%04d%02d", year, i + 1)).toString())
                child(String.format("%04d%02d", year, i + 1)).get()
                    .addOnSuccessListener {
                        val data = it.getValue(Double::class.java)
                        if (data != null) {
                            if (i and 1 == 0) {
                                oddMonth[i shr 1] = data * 2.5596 / 3600000.0
                            } else {
                                evenMonth[i shr 1] = data * 2.5596 / 3600000.0
                            }
                            result(data.toString())
                        } else {
                            result("get null")
                        }
                    }.addOnFailureListener {
                        result("get failed")
                    }.addOnCanceledListener {
                        result("get canceled")
                    }
            }
        }
    }

    override fun getUUID(): ParcelUuid {
        return uuidSPP
    }
}