package com.multilive.agent
import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
class TtsEngine(c:Context):TextToSpeech.OnInitListener{
 private var ready=false;private var pending:String?=null;private val tts=TextToSpeech(c.applicationContext,this)
 override fun onInit(status:Int){ready=status==TextToSpeech.SUCCESS;if(ready){tts.language=Locale.forLanguageTag("th-TH");pending?.let{speak(it);pending=null}}}
 fun speak(text:String,lang:String="th-TH",rate:Float=1f,pitch:Float=1f){if(!ready){pending=text;return};tts.language=Locale.forLanguageTag(lang);tts.setSpeechRate(rate.coerceIn(.5f,2f));tts.setPitch(pitch.coerceIn(.5f,2f));tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"mlm-tts")}
 fun shutdown(){tts.stop();tts.shutdown()}
}
