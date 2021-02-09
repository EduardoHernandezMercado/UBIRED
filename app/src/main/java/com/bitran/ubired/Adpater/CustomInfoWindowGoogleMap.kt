package com.bitran.ubired

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.fragment_adapter_marcador_map.view.*

class CustomInfoWindowGoogleMap(val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(p0: Marker?): View {

        var mInfoView = (context as Activity).layoutInflater.inflate(R.layout.fragment_adapter_marcador_map, null)
        var mInfoWindow: InfoWindowData? = p0?.tag as InfoWindowData?

        mInfoView.info_window_nombre.text = mInfoWindow?.mLocatioName
        mInfoView.info_window_placas.text = mInfoWindow?.mLocationAddres
        mInfoView.info_window_estado.text = mInfoWindow?.mLocationEmail
        mInfoView.info_window_imagen.setImageBitmap(mInfoWindow?.bitmap)


        return mInfoView
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}