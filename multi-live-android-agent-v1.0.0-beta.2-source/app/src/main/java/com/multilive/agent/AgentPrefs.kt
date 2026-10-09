package com.multilive.agent

import android.content.Context

class AgentPrefs(private val context: Context) {
    private val p = context.getSharedPreferences("agent", Context.MODE_PRIVATE)

    var server: String
        get() = p.getString("server", "http://192.168.1.2:8787")!!
        set(v) = p.edit().putString("server", v.trimEnd('/')).apply()

    var token: String?
        get() = p.getString("token", null)
        set(v) = p.edit().putString("token", v).apply()

    var deviceId: String
        get() = p.getString(
            "deviceId",
            android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
        )!!
        set(v) = p.edit().putString("deviceId", v).apply()

    var deviceName: String
        get() = p.getString("deviceName", android.os.Build.MODEL)!!
        set(v) = p.edit().putString("deviceName", v).apply()

    var accountId: String
        get() = p.getString("accountId", "")!!
        set(v) = p.edit().putString("accountId", v.trim()).apply()

    var captureEvidence: Boolean
        get() = p.getBoolean("captureEvidence", false)
        set(v) = p.edit().putBoolean("captureEvidence", v).apply()

    var pendingRemoteSession: String?
        get() = p.getString("pendingRemoteSession", null)
        set(v) = p.edit().putString("pendingRemoteSession", v).apply()

    var previewEnabled: Boolean
        get() = p.getBoolean("previewEnabled", false)
        set(v) = p.edit().putBoolean("previewEnabled", v).apply()

    var pushToken: String
        get() = p.getString("pushToken", "")!!
        set(v) = p.edit().putString("pushToken", v.trim()).apply()

    var pushProvider: String
        get() = p.getString("pushProvider", "fcm")!!
        set(v) = p.edit().putString("pushProvider", v.trim().lowercase()).apply()

    var operatorLabel: String
        get() = p.getString("operatorLabel", "")!!
        set(v) = p.edit().putString("operatorLabel", v.trim()).apply()
}
