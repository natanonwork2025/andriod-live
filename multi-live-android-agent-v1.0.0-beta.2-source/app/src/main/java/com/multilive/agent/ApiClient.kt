package com.multilive.agent
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
class ApiClient(private val prefs:AgentPrefs){
 private fun request(path:String,method:String="GET",body:JSONObject?=null,auth:Boolean=true):JSONObject{val c=URL(prefs.server+path).openConnection() as HttpURLConnection;c.requestMethod=method;c.connectTimeout=10000;c.readTimeout=10000;c.setRequestProperty("Content-Type","application/json");if(auth)prefs.token?.let{c.setRequestProperty("Authorization","Bearer $it")};if(body!=null){c.doOutput=true;c.outputStream.use{it.write(body.toString().toByteArray())}};val code=c.responseCode;val text=(if(code<400)c.inputStream else c.errorStream)?.bufferedReader()?.readText().orEmpty();if(code>=400)throw IllegalStateException("HTTP $code $text");return if(text.isBlank())JSONObject() else JSONObject(text)}
 fun health()=request("/api/health",auth=false)
 fun pair(code:String):String{val r=request("/api/agent/pair","POST",JSONObject().put("code",code).put("deviceId",prefs.deviceId).put("name",prefs.deviceName),false);return r.getString("token")}
 fun heartbeat(metrics:JSONObject,live:JSONObject=JSONObject().put("state","ready"))=request("/api/agent/${prefs.deviceId}/heartbeat","POST",JSONObject().put("accountId",prefs.accountId.ifBlank{JSONObject.NULL}).put("pushToken",prefs.pushToken.ifBlank{JSONObject.NULL}).put("pushProvider",prefs.pushProvider).put("operatorLabel",prefs.operatorLabel.ifBlank{JSONObject.NULL}).put("metrics",metrics).put("live",live))
 fun challenge(type:String,evidence:String?=null)=request("/api/agent/${prefs.deviceId}/challenge","POST",JSONObject().put("type",type).put("source","android-accessibility").put("evidence",evidence))
 fun preview(image:String,meta:JSONObject)=request("/api/agent/${prefs.deviceId}/preview","POST",JSONObject().put("image",image).put("meta",meta))
 fun acceptRemote(sessionId:String)=request("/api/agent/${prefs.deviceId}/remote-session","POST",JSONObject().put("sessionId",sessionId))
 fun commands()=request("/api/agent/${prefs.deviceId}/commands").optJSONArray("commands")
 fun ack(id:String)=request("/api/agent/${prefs.deviceId}/commands/$id/ack","POST",JSONObject())
}
