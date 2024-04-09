package com.example.forecast

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.forecast.Utils.RetrofitInstances
import com.example.forecast.adapter.RvAdapter
import com.example.forecast.data.forecastModels.ForecastData
import com.example.forecast.databinding.ActivityMainBinding
import com.example.forecast.databinding.BottomSheetLayoutBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var sheetLayoutBinding:BottomSheetLayoutBinding
    lateinit var binding : ActivityMainBinding
    private lateinit var dialog:BottomSheetDialog
    private var city ="new york"
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sheetLayoutBinding = BottomSheetLayoutBinding.inflate(layoutInflater)
        dialog = BottomSheetDialog(this,R.style.BottomSheetTheme)
        dialog.setContentView(sheetLayoutBinding.root)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query!=null)
                {
                 city =query //searchinng location
                }
                getCurrentWeather(city)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        getCurrentWeather(city)

        binding.tvForecast.setOnClickListener{
            openDialog()

        }
        // getting location
        binding.tvLocation.setOnClickListener {
            fetchLocation()
        }


        }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun fetchLocation() {
        val task: Task<Location> = fusedLocationProviderClient.lastLocation


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),101
            )
            return
        }

        task.addOnSuccessListener {
            val geocoder=Geocoder(this,Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                geocoder.getFromLocation(it.latitude,it.longitude,1, object: Geocoder.GeocodeListener{
                    override fun onGeocode(addresses: MutableList<Address>) {
                        city = addresses[0].locality
                    }

                })
            }else{
                val address = geocoder.getFromLocation(it.latitude,it.longitude,1) as List<Address>

                city = address[0].locality
            }

            getCurrentWeather(city)
        }
    }



    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun openDialog(){
        getForecast()

        sheetLayoutBinding.rvForecast.apply{
            setHasFixedSize(true)
            layoutManager=GridLayoutManager(this@MainActivity,1,RecyclerView.HORIZONTAL,false)
        }
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.show()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun getForecast() {
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstances.api.getForecast(
                    "new york",
                    "metric",
                    applicationContext.getString(R.string.api_key)
                )


            } catch (e: IOException) {
                Toast.makeText(applicationContext, "app error ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            } catch (e: HttpException) {
                Toast.makeText(applicationContext, "app error ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    val data = response.body()!!
                    var forecastArray = arrayListOf<ForecastData>()
                    forecastArray = data.list as ArrayList<ForecastData>
                   val adapter = RvAdapter(forecastArray)
                    sheetLayoutBinding.rvForecast.adapter = adapter
                    sheetLayoutBinding.tvSheet.text = "five days forecast in ${data.city.name}"

                }

            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun getCurrentWeather(city:String){
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstances.api.getCurrentWeather(
                    city,
                "metric",
                applicationContext.getString(R.string.api_key))


            }catch (e:IOException){
                Toast.makeText(applicationContext,"app error ${e.message}",Toast.LENGTH_SHORT).show()
                return@launch
            }catch (e:HttpException){
                Toast.makeText(applicationContext,"app error ${e.message}",Toast.LENGTH_SHORT).show()
                return@launch
            }
            if(response.isSuccessful&&response.body()!=null){
                withContext(Dispatchers.Main){
                  val data = response.body()!!
                    val iconId = data.weather[0].icon

                    val imgUrl = "https://openweathermap.org/img/w/$iconId.png"
                    Picasso.get().load(imgUrl).into(binding.imgWeather)
                    binding.tvSunrise.text=
                        SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
                            Date(data.sys.sunrise.toLong()*1000))//sunrise huda ko icon

                    binding.tvSunrise.text=
                        SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(data.sys.sunset*1000)//sunset huda ko icon
                    binding.apply{
                        tvStatus.text = data.weather[0].description
                        tvWind.text ="${data.wind.speed.toString()} km/hr"
                        tvLocation.text = "${data.name}\n${data.sys.country}"
                        tvTemp.text = "${data.main.temp.toInt()}°C"
                        tvFeelsLike.text= "Feels like:${data.main.feels_like.toInt()}°C "
                        tvMinTemp.text = "${data.main.temp_min.toInt()}°C"
                        tvMaxTemp.text = "${data.main.temp_max.toInt()}°C"
                        tvHumidity.text= "${data.main.humidity}%"
                        tvPressure.text= "${data.main.pressure} hPa"
                        tvUpdateTime.text= "Last update :${
                            SimpleDateFormat("hh:mm a", 
                                Locale.ENGLISH)
                                .format(data.dt*1000)
                        }"


                    }

                }
            }

        }
    }
    }
//@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//private fun fetchLocation() {
//    if (ActivityCompat.checkSelfPermission(
//            this,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//            this,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        ) != PackageManager.PERMISSION_GRANTED
//    ) {
//        // Request permissions if not granted.
//        // You should handle this properly in your app.
//        return
//    }
//
//    fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//        if (location != null) {
//            try {
//                val geocoder = Geocoder(this, Locale.getDefault())
//                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
//                if (addresses.isNotEmpty()) {
//                    city = addresses[0].locality
//                    getCurrentWeather(city)
//                } else {
//                    Toast.makeText(this, "Unable to get city name", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: IOException) {
//                Toast.makeText(this, "Geocoder IO Exception: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            Toast.makeText(this, "Unable to retrieve location", Toast.LENGTH_SHORT).show()
//        }
//    }.addOnFailureListener { e ->
//        Toast.makeText(this, "Failed to retrieve location: ${e.message}", Toast.LENGTH_SHORT).show()
//    }
//}
