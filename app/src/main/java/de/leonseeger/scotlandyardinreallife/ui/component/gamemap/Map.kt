package de.leonseeger.scotlandyardinreallife.ui.component.gamemap

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import  android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import de.leonseeger.scotlandyardinreallife.BuildConfig
import de.leonseeger.scotlandyardinreallife.R
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.iconIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

class PlayMapData {
    private val polygonMapSrcName = "playarea-src"
    private val polygonFillName = "playarea-fill"
    private val markerSrcName = "marker-source"

    private lateinit var mapLibreMap: MapLibreMap
    private var polygonPoints = mutableListOf<Point>()
    private var markers = mutableListOf<Feature>()

    fun addPlayerPointToMap(loc: LatLng){

    }

    fun addPolyPoint(coord: LatLng): Boolean {
        var polyFull = false
        if(polygonPoints.isNotEmpty()) {
            polygonPoints.removeAt(polygonPoints.lastIndexOf(polygonPoints.first()))
        }
        polygonPoints.add(Point.fromLngLat(coord.longitude, coord.latitude))
        mapLibreMap.getStyle{ style -> polyFull = updatePolygon(style) }
        return polyFull
    }

    fun addMarker(coord: LatLng){
        mapLibreMap.getStyle { style ->
            val source = style.getSourceAs<GeoJsonSource>(markerSrcName)
            val marker = Feature.fromGeometry(
                Point.fromLngLat(coord.longitude, coord.latitude))
            markers.add(marker)
            source?.setGeoJson(
                FeatureCollection.fromFeatures(markers)
            )
        }
    }

    fun updatePolygon(style: Style): Boolean {
        polygonPoints.add(polygonPoints.first())

        if (polygonPoints.size < 4) {
            // polygon to small
            Log.w("Polygon Making", "updatePolygon size: " + polygonPoints.size)
            return false;
        }

        val source = style.getSourceAs<GeoJsonSource>(polygonMapSrcName) ?: return false;
        val polygon = Polygon.fromLngLats(listOf(polygonPoints))
        source.setGeoJson(Feature.fromGeometry(polygon))
        return true
    }

    fun getMapLibreMap(): MapLibreMap {
        return this.mapLibreMap;
    }

    fun latLngToPointList(positions: Array<LatLng>): List<List<Point>> {
        val points = positions.map {
            Point.fromLngLat(it.longitude, it.latitude)
        }.toMutableList()

        if (points.first() != points.last()) {
            points.add(points.first())
        }
        return listOf(points)
    }

    fun getPolygonPoints(): List<Point>{
        return polygonPoints;
    }

    fun removeLastPolyPoint(){
        if(polygonPoints.size > 3){
            polygonPoints.removeAt(polygonPoints.size-2)
            val newLast = polygonPoints.removeAt(polygonPoints.size-2)

            markers.removeLast()
            val markerSrc = mapLibreMap.style?.getSourceAs<GeoJsonSource>(markerSrcName)
            markerSrc?.setGeoJson(FeatureCollection.fromFeatures(markers))

            addPolyPoint(LatLng(newLast.latitude(), newLast.longitude()))
        }
        else if(polygonPoints.isNotEmpty()){
            polygonPoints.removeAt(polygonPoints.lastIndex)

            markers.removeLast()
            val markerSrc = mapLibreMap.style?.getSourceAs<GeoJsonSource>(markerSrcName)
            markerSrc?.setGeoJson(FeatureCollection.fromFeatures(markers))
        }
        //remove poly if not full poly
        if(polygonPoints.size < 4){
            mapLibreMap.getStyle{ style ->
                    val polySrc = style.getSourceAs<GeoJsonSource>(polygonMapSrcName) ?: return@getStyle;
                    polySrc.setGeoJson(FeatureCollection.fromFeatures(arrayOf()))
            }
        }
    }

    @Composable
    fun CustomMap(
        modifier: Modifier, lat: Double, lon: Double, appContext: Context,
        onMapReady: (PlayMapData) -> Unit
    ) {
        val mapOptions = MapLibreMapOptions.createFromAttributes(appContext)
        mapOptions.apply {
            apiBaseUri(BuildConfig.MAPTILER_API_PATH)
            camera(
                CameraPosition.Builder()
                    .bearing(0.0)
                    .target(LatLng(lat, lon))
                    .zoom(12.0)
                    .build()
            )
            maxZoomPreference(26.0)
            minZoomPreference(2.0)
            zoomGesturesEnabled(true)
            scrollGesturesEnabled(true)
            rotateGesturesEnabled(true)
            compassEnabled(true)
            tiltGesturesEnabled(false)
        }
        val mapView = MapView(appContext, mapOptions)
        AndroidView(
            modifier = modifier, factory = { mapView }) { view ->
            view.getMapAsync { map ->
                mapLibreMap = map
                mapLibreMap.setStyle(BuildConfig.MAPTILER_API_PATH){ style ->
                    //sets style of polygon fill and links list of points
                    val poly = Polygon.fromLngLats(listOf(polygonPoints))
                    val polySrc = GeoJsonSource(polygonMapSrcName, poly)
                    style.addSource(polySrc)
                    val fill = FillLayer(polygonFillName, polygonMapSrcName)
                        .withProperties(
                            fillColor("#22ff00"),
                            fillOpacity(0.5f)
                        )
                    style.addLayer(fill)


                    style.addImage(
                        "marker-icon",
                        BitmapFactory.decodeResource(appContext.resources, R.drawable.maplibre_marker_icon_default)
                    )

                    val markerSrc = GeoJsonSource(markerSrcName)
                    style.addSource(markerSrc)
                    val layer = SymbolLayer("marker-layer", "marker-source")
                        .withProperties(
                            iconImage("marker-icon"),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true)
                        )

                    style.addLayer(layer)
                }
                onMapReady(this)
            }
        }
    }
}



