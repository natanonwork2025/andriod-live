package com.multilive.agent
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class MainActivity:ComponentActivity(){
 private lateinit var prefs:AgentPrefs
 private val capture=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){r->if(r.resultCode==Activity.RESULT_OK&&r.data!=null){ScreenCaptureService.start(this,r.resultCode,r.data!!);prefs.previewEnabled=true}}
 private val mediaPicker=registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){uris->if(uris.isNotEmpty()){uris.forEach{runCatching{contentResolver.takePersistableUriPermission(it,Intent.FLAG_GRANT_READ_URI_PERMISSION)}};MediaPlaylistStore(this).replace(uris)}}
 override fun onCreate(b:Bundle?){super.onCreate(b);prefs=AgentPrefs(this);setContent{App(this)}}
 fun requestPreview(){capture.launch(getSystemService(MediaProjectionManager::class.java).createScreenCaptureIntent())}
 fun chooseMedia(){mediaPicker.launch(arrayOf("video/*"))}
 fun openMediaPreview(){startActivity(Intent(this,MediaPreviewActivity::class.java))}
}
@Composable fun App(a:MainActivity){
 val p=remember{AgentPrefs(a)};val media=remember{MediaPlaylistStore(a)};var server by remember{mutableStateOf(p.server)};var accountId by remember{mutableStateOf(p.accountId)};var operatorLabel by remember{mutableStateOf(p.operatorLabel)};var pushToken by remember{mutableStateOf(p.pushToken)};var pushProvider by remember{mutableStateOf(p.pushProvider)};var code by remember{mutableStateOf("")};var health by remember{mutableStateOf("NOT CHECKED")};var status by remember{mutableStateOf(if(p.token==null)"NOT PAIRED" else "PAIRED")};var evidence by remember{mutableStateOf(p.captureEvidence)};var preview by remember{mutableStateOf(p.previewEnabled)};var remoteSession by remember{mutableStateOf(p.pendingRemoteSession)};var mediaCount by remember{mutableStateOf(media.list().size)};var mediaCue by remember{mutableStateOf(MediaCommandStore(a).pendingAction)};var loop by remember{mutableStateOf(media.loop)};val scope=rememberCoroutineScope()
 MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFF45E7FF),secondary=Color(0xFFFF4BD8),surface=Color(0xFF0B1823))){
  Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF06111B),Color(0xFF130B20),Color(0xFF05080E)))).padding(20.dp)){
   Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(14.dp)){
    Text("MULTI LIVE",color=Color(0xFF45E7FF));Text("Mobile Agent",style=MaterialTheme.typography.headlineLarge,color=Color.White)
    Card(colors=CardDefaults.cardColors(containerColor=Color(0xAA0B1823)),shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("DEVICE LINK",color=Color(0xFF77FFB5));Text("${p.deviceName} • ${p.deviceId.take(10)}",color=Color.White);Text(status,color=if(status=="PAIRED")Color(0xFF77FFB5) else Color(0xFFFFCC66));OutlinedTextField(server,{server=it},label={Text("Control Center URL")});OutlinedTextField(accountId,{accountId=it;p.accountId=it},label={Text("Account ID / Shop label")});OutlinedTextField(operatorLabel,{operatorLabel=it;p.operatorLabel=it},label={Text("Operator / shift label")});OutlinedTextField(pushToken,{pushToken=it;p.pushToken=it},label={Text("Push token (optional)")});OutlinedTextField(pushProvider,{pushProvider=it;p.pushProvider=it},label={Text("Push provider (fcm / ntfy)")});OutlinedTextField(code,{code=it},label={Text("6-digit pairing code")});Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={scope.launch(Dispatchers.IO){health=runCatching{val h=ApiClient(p).health();"SERVER ${h.optString("version","?")} • "+if(h.optBoolean("ok"))"HEALTHY" else "CHECK"}.getOrElse{"UNREACHABLE"}}}){Text("CHECK SERVER")};Text(health,color=Color(0xFFA9BAC4))};Button(onClick={scope.launch(Dispatchers.IO){runCatching{p.server=server;p.accountId=accountId;p.token=ApiClient(p).pair(code);status="PAIRED";AgentService.start(a)}.onFailure{status=it.message?:"PAIR FAILED"}}}){Text("PAIR DEVICE")}}}
    Card(colors=CardDefaults.cardColors(containerColor=Color(0xAA0B1823)),shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("LOCAL MEDIA ENGINE",color=Color(0xFFFFCC66));Text("Playlist / loop / preview / automatic retry. This is a local media preview layer; it does not bypass app camera or platform controls.",color=Color(0xFFA9BAC4));Text("$mediaCount video(s) selected",color=Color.White);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={a.chooseMedia();mediaCount=media.list().size}){Text("SELECT VIDEOS")};OutlinedButton(onClick={a.openMediaPreview()}){Text("PREVIEW")}};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Switch(checked=loop,onCheckedChange={loop=it;media.loop=it});Text("Loop playlist",color=Color.White)}}}
    Card(colors=CardDefaults.cardColors(containerColor=Color(0xAA0B1823)),shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("AI VOICE / APPROVAL",color=Color(0xFF77FFB5));Text("Approved replies and per-device Thai TTS voice profiles are delivered from Control Center. AI replies require operator approval before speech.",color=Color(0xFFA9BAC4));Text("Pending cue: ${mediaCue ?: "none"}",color=Color.White);if(mediaCue!=null)Button(onClick={a.openMediaPreview();MediaCommandStore(a).clear();mediaCue=null}){Text("OPEN MEDIA PREVIEW")}}}
    Card(colors=CardDefaults.cardColors(containerColor=Color(0xAA0B1823)),shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("LIVE SCREEN PREVIEW",color=Color(0xFF45E7FF));Text("Opt-in only. Android asks for screen-capture consent and shows a persistent notification while sharing.",color=Color(0xFFA9BAC4));Button(onClick={a.requestPreview()}){Text(if(preview)"RE-AUTHORIZE PREVIEW" else "START PREVIEW")};OutlinedButton(onClick={ScreenCaptureService.stop(a);p.previewEnabled=false;preview=false}){Text("STOP PREVIEW")}}}
    Card(colors=CardDefaults.cardColors(containerColor=Color(0xAA0B1823)),shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("CHALLENGE GUARDIAN",color=Color(0xFFFF4BD8));Text("Detection + alert + human handoff only. CAPTCHA is never solved automatically.",color=Color(0xFFA9BAC4));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Switch(checked=evidence,onCheckedChange={evidence=it;p.captureEvidence=it});Text("Screenshot evidence",color=Color.White)};Button(onClick={a.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}){Text("OPEN ACCESSIBILITY SETTINGS")};OutlinedButton(onClick={scope.launch(Dispatchers.IO){runCatching{ApiClient(p).challenge("manual-test","manual challenge test")}}}){Text("SEND TEST ALERT")}}}
    if(!remoteSession.isNullOrBlank())Card(colors=CardDefaults.cardColors(containerColor=Color(0xAA271628)),shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("REMOTE HUMAN HANDOFF",color=Color(0xFFFFCC66));Text("An operator requested assistance. Accept only if you recognize the request.",color=Color.White);Button(onClick={scope.launch(Dispatchers.IO){runCatching{ApiClient(p).acceptRemote(remoteSession!!);p.pendingRemoteSession=null;remoteSession=null}}}){Text("ACCEPT HANDOFF")};OutlinedButton(onClick={p.pendingRemoteSession=null;remoteSession=null}){Text("DECLINE")}}}
    Text("Agent v1.0.0-beta.2 • hardened server checks • human handoff",color=Color(0xFF7E98A6));Spacer(Modifier.height(12.dp))
   }
  }
 }
}
