package com.multilive.agent
import android.app.*
import android.content.*
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Base64
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
class ScreenCaptureService:Service(){private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO);private var projection:MediaProjection?=null;private var reader:ImageReader?=null;private lateinit var prefs:AgentPrefs;private lateinit var api:ApiClient
 override fun onCreate(){super.onCreate();prefs=AgentPrefs(this);api=ApiClient(prefs);val nm=getSystemService(NotificationManager::class.java);nm.createNotificationChannel(NotificationChannel("capture","Screen Preview",NotificationManager.IMPORTANCE_LOW));startForeground(41,Notification.Builder(this,"capture").setContentTitle("Multi Live screen preview").setContentText("Sharing is active only while you allow it").setSmallIcon(android.R.drawable.ic_menu_camera).build())}
 override fun onStartCommand(i:Intent?,flags:Int,startId:Int):Int{val code=i?.getIntExtra("resultCode",Activity.RESULT_CANCELED)?:Activity.RESULT_CANCELED;val data=i?.getParcelableExtra<Intent>("resultData");if(code!=Activity.RESULT_OK||data==null){stopSelf();return START_NOT_STICKY};projection=getSystemService(MediaProjectionManager::class.java).getMediaProjection(code,data);startCapture();return START_STICKY}
 private fun startCapture(){val dm=resources.displayMetrics;val w=minOf(dm.widthPixels,720);val h=(dm.heightPixels*(w.toFloat()/dm.widthPixels)).toInt();reader=ImageReader.newInstance(w,h,PixelFormat.RGBA_8888,2);projection?.createVirtualDisplay("mlm-preview",w,h,dm.densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader?.surface,null,null);scope.launch{while(isActive){delay(3000);captureOnce(w,h)}}}
 private suspend fun captureOnce(w:Int,h:Int){val image=reader?.acquireLatestImage()?:return;try{val plane=image.planes[0];val buf=plane.buffer;val pixelStride=plane.pixelStride;val rowStride=plane.rowStride;val rowPadding=rowStride-pixelStride*w;val bmp=Bitmap.createBitmap(w+rowPadding/pixelStride,h,Bitmap.Config.ARGB_8888);bmp.copyPixelsFromBuffer(buf);val cropped=Bitmap.createBitmap(bmp,0,0,w,h);val out=ByteArrayOutputStream();cropped.compress(Bitmap.CompressFormat.JPEG,45,out);val encoded=Base64.encodeToString(out.toByteArray(),Base64.NO_WRAP);api.preview("data:image/jpeg;base64,$encoded",JSONObject().put("w",w).put("h",h).put("source","android-mediaprojection"))}catch(_:Exception){}finally{image.close()}}
 override fun onDestroy(){scope.cancel();reader?.close();projection?.stop();super.onDestroy()};override fun onBind(i:Intent?):IBinder?=null
 companion object{fun start(c:Context,resultCode:Int,data:Intent){c.startForegroundService(Intent(c,ScreenCaptureService::class.java).putExtra("resultCode",resultCode).putExtra("resultData",data))};fun stop(c:Context){c.stopService(Intent(c,ScreenCaptureService::class.java))}}
}
