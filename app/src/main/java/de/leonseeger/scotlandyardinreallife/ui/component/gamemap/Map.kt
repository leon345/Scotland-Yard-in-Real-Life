package de.leonseeger.scotlandyardinreallife.ui.component.gamemap

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import de.leonseeger.scotlandyardinreallife.BuildConfig
import de.leonseeger.scotlandyardinreallife.R
import de.leonseeger.scotlandyardinreallife.entity.Player
import de.leonseeger.scotlandyardinreallife.entity.PlayerRole
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.iconColor
import org.maplibre.android.style.layers.PropertyFactory.iconIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.iconOpacity
import org.maplibre.android.style.layers.PropertyFactory.iconSize
import org.maplibre.android.style.layers.PropertyValue
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

/**
 * Kapselt den MapLibre-Kartenzustand und stellt Composable- sowie Hilfsmethoden zur
 * Verwaltung von Spielfeld-Polygon, Spieler-Markern und Echtzeit-Positionen bereit.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author Jannes Schophuis
 */
class PlayMapData {
    private val polygonMapSrcName = "playarea-src"
    private val polygonFillName = "playarea-fill"
    private val markerSrcName = "marker-source"

    private lateinit var mapLibreMap: MapLibreMap
    private var polygonPoints = mutableListOf<Point>()
    private var markers = mutableListOf<Feature>()

    fun updatePlayers(players: List<Player>) {
        val style = mapLibreMap.style ?: return
        val source = style.getSourceAs<GeoJsonSource>("players-source") ?: return

        val features = players
            .filter { it.currentLocation != null }
            .map { player ->
                Log.d("Player", "" + player.currentLocation!!.longitude + " " + player.currentLocation.latitude)
                Feature.fromGeometry(
                    Point.fromLngLat(player.currentLocation!!.longitude, player.currentLocation!!.latitude)
                ).apply {
                    addStringProperty("icon", if (player.role == PlayerRole.BANDIT) "bandit-icon" else "detective-icon")
                    addStringProperty("color", if (player.role == PlayerRole.BANDIT) "#FF4539" else "#226AF0")
                    addStringProperty("haloColor", if (player.role == PlayerRole.BANDIT) "#D55B52" else "#3C6CC5")
                }
            }

        source.setGeoJson(FeatureCollection.fromFeatures(features))
    }

    fun addPolyPoint(coord: LatLng, inverted: Boolean = false): Boolean {
        var polyFull = false
        if(polygonPoints.isNotEmpty()) {
            polygonPoints.removeAt(polygonPoints.lastIndexOf(polygonPoints.first()))
        }
        polygonPoints.add(Point.fromLngLat(coord.longitude, coord.latitude))
        mapLibreMap.getStyle{ style -> polyFull = updatePolygon(style, inverted) }
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

    fun updatePolygon(style: Style, invert: Boolean = false): Boolean {
        polygonPoints.add(polygonPoints.first())

        if (polygonPoints.size < 4) {
            // polygon to small
            Log.w("Polygon Making", "updatePolygon size: " + polygonPoints.size)
            return false;
        }

        val source = style.getSourceAs<GeoJsonSource>(polygonMapSrcName) ?: return false
        val poly: Polygon
        if(!invert){
            poly = Polygon.fromLngLats(listOf(polygonPoints))
        }
        else{
            val inverter = listOf(
                Point.fromLngLat(-179.9, -85.0),
                Point.fromLngLat(179.9, -85.0),
                Point.fromLngLat(179.9, 85.0),
                Point.fromLngLat(-179.9, 85.0),
                Point.fromLngLat(-179.9, -85.0)
            )
            poly = Polygon.fromLngLats(listOf(inverter, polygonPoints))
        }
        source.setGeoJson(Feature.fromGeometry(poly))
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
        if(polygonPoints.size > 2){
            polygonPoints.removeAt(polygonPoints.lastIndex-1)
            val newLast = polygonPoints.removeAt(polygonPoints.lastIndex-1)

            markers.removeAt(markers.lastIndex)
            val markerSrc = mapLibreMap.style?.getSourceAs<GeoJsonSource>(markerSrcName)
            markerSrc?.setGeoJson(FeatureCollection.fromFeatures(markers))

            addPolyPoint(LatLng(newLast.latitude(), newLast.longitude()))
        }
        else if(polygonPoints.isNotEmpty()){
            polygonPoints.removeAt(polygonPoints.lastIndex)

            if(markers.isNotEmpty())
                markers.removeAt(markers.lastIndex)
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
        modifier: Modifier,
        lat: Double,
        lon: Double,
        appContext: Context,
        invert: Boolean = false,
        onMapReady: (PlayMapData) -> Unit
    ) {
        val mapOptions = MapLibreMapOptions.createFromAttributes(appContext)
        mapOptions.apply {
            apiBaseUri(BuildConfig.MAPTILER_API_PATH)
            camera(
                CameraPosition.Builder()
                    .bearing(0.0)
                    .target(LatLng(lat, lon))
                    .zoom(16.0)
                    .build()
            )
            maxZoomPreference(20.0)
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
                    val fillColor: PropertyValue<String>
                    if(!invert)
                        fillColor = fillColor("#22ff00")
                    else
                        fillColor = fillColor("#B3402E")
                    val polySrc = GeoJsonSource(polygonMapSrcName, poly)
                    style.addSource(polySrc)
                    val fill = FillLayer(polygonFillName, polygonMapSrcName)
                        .withProperties(
                            fillColor,
                            fillOpacity(0.5f)
                        )
                    style.addLayer(fill)

                    style.addImage(
                        "marker-icon",
                        BitmapFactory.decodeResource(appContext.resources, R.drawable.maplibre_marker_icon_default)
                    )
                    /*---Marker Layer---*/
                    val markerSrc = GeoJsonSource(markerSrcName)
                    style.addSource(markerSrc)
                    val symbolLayer = SymbolLayer("marker-layer", "marker-source")
                        .withProperties(
                            iconImage("marker-icon"),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true)
                        )
                    style.addLayer(symbolLayer)

                    /*---Player Layer---*/
                    val playerSource = GeoJsonSource("players-source")
                    style.addSource(playerSource)

                    val banditBitmap = BitmapFactory.decodeResource(appContext.resources, R.drawable.mask)
                    val detectiveBitmap = BitmapFactory.decodeResource(appContext.resources, R.drawable.siren)
                    style.addImage("bandit-icon", banditBitmap, true)
                    style.addImage("detective-icon", detectiveBitmap, true)

                    val playerLayer = SymbolLayer("players-layer", "players-source")
                        .withProperties(
                            iconImage(Expression.get("icon")),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true),
                            iconColor(Expression.get("color"))
                        )

                    val haloBitmap = BitmapFactory.decodeResource(appContext.resources, R.drawable.halo)
                    style.addImage("player-bg", haloBitmap, true)
                    val haloLayer = SymbolLayer("players-halo", "players-source")
                        .withProperties(
                            iconImage("player-bg"),
                            iconColor(Expression.get("haloColor")),
                            iconSize(0.8f),
                            iconOpacity(0.3f),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true)
                        )
                    style.addLayer(haloLayer)
                    style.addLayer(playerLayer)

                    onMapReady(this)
                }
            }
        }
    }
}



