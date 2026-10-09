package com.multilive.agent
import android.content.Context
import android.net.Uri
import org.json.JSONArray
class MediaPlaylistStore(c:Context){private val p=c.getSharedPreferences("media_playlist",Context.MODE_PRIVATE)
 fun list():List<Uri>{val a=runCatching{JSONArray(p.getString("items","[]"))}.getOrElse{JSONArray()};return (0 until a.length()).map{Uri.parse(a.getString(it))}}
 fun replace(items:List<Uri>){val a=JSONArray();items.forEach{a.put(it.toString())};p.edit().putString("items",a.toString()).apply()}
 var loop:Boolean;get()=p.getBoolean("loop",true);set(v)=p.edit().putBoolean("loop",v).apply()
}
