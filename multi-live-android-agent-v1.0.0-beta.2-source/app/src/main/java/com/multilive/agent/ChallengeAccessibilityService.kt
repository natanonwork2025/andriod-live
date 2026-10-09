package com.multilive.agent
import android.accessibilityservice.AccessibilityService
import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Base64
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
class ChallengeAccessibilityService:AccessibilityService(){private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO);private var last=0L
 private val keys=listOf("captcha","verify","verification","ยืนยัน","ตรวจสอบ","ลากจิ๊กซอว์","puzzle")
 override fun onAccessibilityEvent(e:AccessibilityEvent?){val pkg=e?.packageName?.toString().orEmpty();if(!(pkg.contains("tiktok",true)||pkg.contains("musically",true)||pkg.contains("ugc.trill",true)))return;val text=(e.text.joinToString(" ")+" "+collect(rootInActiveWindow,0)).lowercase();if(keys.none{text.contains(it)})return;val now=System.currentTimeMillis();if(now-last<60_000)return;last=now;val prefs=AgentPrefs(this);if(prefs.captureEvidence&&Build.VERSION.SDK_INT>=30)captureAndSend(text,prefs) else send(text.take(500),prefs)}
 private fun captureAndSend(text:String,prefs:AgentPrefs){takeScreenshot(Display.DEFAULT_DISPLAY,mainExecutor,object:TakeScreenshotCallback{override fun onSuccess(r:ScreenshotResult){val hw=r.hardwareBuffer;val bmp=Bitmap.wrapHardwareBuffer(hw,r.colorSpace)?.copy(Bitmap.Config.ARGB_8888,false);hw.close();if(bmp==null){send(text.take(500),prefs);return};val w=540;val h=(bmp.height*(w.toFloat()/bmp.width)).toInt().coerceAtLeast(1);val scaled=Bitmap.createScaledBitmap(bmp,w,h,true);val out=ByteArrayOutputStream();scaled.compress(Bitmap.CompressFormat.JPEG,65,out);val uri="data:image/jpeg;base64,"+Base64.encodeToString(out.toByteArray(),Base64.NO_WRAP);send(uri,prefs)};override fun onFailure(errorCode:Int){send(text.take(500),prefs)}})}
 private fun send(evidence:String,prefs:AgentPrefs){notifyHuman();scope.launch{runCatching{if(prefs.token!=null)ApiClient(prefs).challenge("captcha",evidence)}}}
 private fun notifyHuman(){val nm=getSystemService(NotificationManager::class.java);nm.createNotificationChannel(NotificationChannel("urgent","Challenge / Remote",NotificationManager.IMPORTANCE_HIGH));val pi=PendingIntent.getActivity(this,51,Intent(this,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE);nm.notify(51,Notification.Builder(this,"urgent").setContentTitle("Verification challenge detected").setContentText("Human attention is required now").setSmallIcon(android.R.drawable.ic_dialog_alert).setContentIntent(pi).setAutoCancel(true).setVibrate(longArrayOf(0,400,250,400)).build())}
 private fun collect(n:AccessibilityNodeInfo?,depth:Int):String{if(n==null||depth>5)return "";val b=StringBuilder();n.text?.let{b.append(it).append(' ')};for(i in 0 until n.childCount)b.append(collect(n.getChild(i),depth+1));return b.toString()}
 override fun onInterrupt(){};override fun onDestroy(){scope.cancel();super.onDestroy()}}
