package com.multilive.agent
import android.content.Context
class MediaCommandStore(c:Context){private val p=c.getSharedPreferences("media_commands",Context.MODE_PRIVATE)
 var pendingAction:String?;get()=p.getString("pendingAction",null);set(v)=p.edit().putString("pendingAction",v).apply()
 var updatedAt:Long;get()=p.getLong("updatedAt",0);set(v)=p.edit().putLong("updatedAt",v).apply()
 fun set(action:String){pendingAction=action;updatedAt=System.currentTimeMillis()}
 fun clear(){pendingAction=null}
}
