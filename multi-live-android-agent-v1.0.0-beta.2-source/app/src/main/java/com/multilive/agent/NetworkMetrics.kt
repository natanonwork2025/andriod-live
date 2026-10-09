package com.multilive.agent
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import org.json.JSONObject
object NetworkMetrics{fun read(c:Context):JSONObject{val cm=c.getSystemService(ConnectivityManager::class.java);val n=cm.activeNetwork;val cap=n?.let{cm.getNetworkCapabilities(it)};val down=(cap?.linkDownstreamBandwidthKbps?:0)/1000.0;val up=(cap?.linkUpstreamBandwidthKbps?:0)/1000.0;val transport=when{cap?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)==true->"wifi";cap?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)==true->"cellular";cap?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)==true->"ethernet";else->"unknown"};val metered=cm.isActiveNetworkMetered;val validated=cap?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)==true;return JSONObject().put("transport",transport).put("linkDownMbps",down).put("linkUpMbps",up).put("metered",metered).put("validated",validated)}}
