package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private val REQUEST_LOCATION_PERMISSION = 1
    var marker: Marker? = null
    private val zoomLevel = 15f
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private var poi: PointOfInterest? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun onLocationSelected() {
        _viewModel.latitude.value = poi?.latLng?.latitude
        _viewModel.longitude.value = poi?.latLng?.longitude
        _viewModel.selectedPOI.value = poi
        _viewModel.reminderSelectedLocationStr.value = poi?.name
        findNavController().navigateUp()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    //I change map here, idk why it's not work for reviewer, it works for me
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                // Permission denied.
                Snackbar.make(
                    requireView(),
                    R.string.permission_denied_explanation, Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.settings) {
                        // Displays App settings screen.
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", requireActivity().packageName, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })

                    }.show()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val currentlocation = LatLng(it.latitude, it.longitude)
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                currentlocation,
                                zoomLevel
                            )
                        )
                    }
                }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        enableMyLocation()
        setMapStyle(map)
        setPoiClick(map)
        setMapClick(map)
    }

    private fun setMapClick(map: GoogleMap) {
        map.setOnMapClickListener {
            marker?.remove()

            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                it.latitude,
                it.longitude
            )
            marker = map.addMarker(
                MarkerOptions().position(it)
                    .snippet(snippet)
                    .title(getString(R.string.dropped_pin))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            binding.save.visibility = View.VISIBLE
            binding.save.setOnClickListener { view ->
                _viewModel.latitude.value = it.latitude
                _viewModel.longitude.value = it.longitude
                _viewModel.reminderSelectedLocationStr.value = getString(R.string.dropped_pin)
                findNavController().navigateUp()
            }
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            this.poi = poi
            marker?.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            marker?.showInfoWindow()
            binding.save.visibility = View.VISIBLE
            binding.save.setOnClickListener {
                onLocationSelected()
            }
        }
    }
    private val TAG = SelectLocationFragment::class.java.simpleName
}
