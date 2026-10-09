package com.multilive.agent
import android.app.ActivityManager
import android.content.*
import android.os.*
import org.json.JSONObject
object DeviceMetrics{fun read(c:Context):JSONObject{val i=c.registerReceiver(null,IntentFilter(Intent.ACTION_BATTERY_CHANGED));val level=i?.getIntExtra(BatteryManager.EXTRA_LEVEL,-1)?:-1;val scale=i?.getIntExtra(BatteryManager.EXTRA_SCALE,100)?:100;val temp=(i?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0)?:0)/10.0;val pct=if(level>=0)level*100.0/scale else -1.0;val am=c.getSystemService(ActivityManager::class.java);val mi=ActivityManager.MemoryInfo();am.getMemoryInfo(mi);val memPressure=if(mi.totalMem>0)(1.0-mi.availMem.toDouble()/mi.totalMem)*100.0 else 0.0;val stat=StatFs(c.filesDir.absolutePath);val freeGb=stat.availableBytes/1024.0/1024.0/1024.0;return JSONObject().put("batteryPct",pct).put("temperatureC",temp).put("memoryPressurePct",memPressure).put("storageFreeGb",freeGb).put("sdk",Build.VERSION.SDK_INT).put("model",Build.MODEL)}}
