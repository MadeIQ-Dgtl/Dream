package digital.madeiq.dream

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

class MainActivity : AppCompatActivity() {
    private lateinit var root: LinearLayout
    private val prefs by lazy { getSharedPreferences("dream", Context.MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())

    private val bg = Color.rgb(7,17,31)
    private val card = Color.rgb(15,31,51)
    private val card2 = Color.rgb(21,41,66)
    private val gold = Color.rgb(246,184,94)
    private val cream = Color.rgb(255,244,210)
    private val muted = Color.rgb(164,177,197)
    private val green = Color.rgb(83,205,151)
    private val purple = Color.rgb(141,92,246)
    private val pink = Color.rgb(239,96,145)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        setContentView(root)
        if ((prefs.getString("goal_name", "") ?: "").isBlank()) setupScreen() else dashboardScreen()
    }

    private fun clear() { root.removeAllViews() }
    private fun dp(v:Int) = (v * resources.displayMetrics.density).toInt()
    private fun money(v:Double):String = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(v)
    private fun txt(s:String, size:Float=16f, color:Int=cream, bold:Boolean=false):TextView = TextView(this).apply {
        text=s; textSize=size; setTextColor(color); typeface=Typeface.create("sans", if(bold) Typeface.BOLD else Typeface.NORMAL)
    }
    private fun bgDrawable(color:Int, radius:Int=24, stroke:Int?=null):GradientDrawable = GradientDrawable().apply {
        setColor(color); cornerRadius=dp(radius).toFloat(); if(stroke!=null) setStroke(dp(1), stroke)
    }
    private fun cardBox():LinearLayout = LinearLayout(this).apply {
        orientation=LinearLayout.VERTICAL; setPadding(dp(18),dp(16),dp(18),dp(16)); background=bgDrawable(card,22)
        layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(dp(18),dp(7),dp(18),dp(7))}
    }
    private fun button(label:String, onClick:()->Unit):Button = Button(this).apply {
        text=label; textSize=15f; setTextColor(bg); typeface=Typeface.DEFAULT_BOLD; isAllCaps=false
        background=bgDrawable(gold,26); setPadding(dp(16),dp(14),dp(16),dp(14)); setOnClickListener{onClick()}
        layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(dp(18),dp(9),dp(18),dp(9))}
    }
    private fun input(hintText:String, numeric:Boolean=false):EditText = EditText(this).apply {
        hint=hintText; setHintTextColor(muted); setTextColor(cream); textSize=16f; background=bgDrawable(card2,18,gold)
        setPadding(dp(16),dp(14),dp(16),dp(14)); if(numeric) inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(dp(18),dp(6),dp(18),dp(6))}
    }

    private fun setupScreen(){
        clear()
        val scroll=ScrollView(this); val col=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(0,dp(18),0,dp(28))}; scroll.addView(col)
        val head=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(18),0,dp(18),dp(6))}
        head.addView(txt("D",32f,gold,true),LinearLayout.LayoutParams(dp(52),dp(52)))
        head.addView(txt("DREAM",22f,cream,true),LinearLayout.LayoutParams(0,-2,1f))
        val lang=TextView(this).apply{text="🌐  ${languageLabel()}"; setTextColor(gold); textSize=13f; setPadding(dp(12),dp(8),dp(12),dp(8)); background=bgDrawable(card,18,gold); setOnClickListener{languageDialog()}}
        head.addView(lang)
        col.addView(head)
        val hero=cardBox().apply{
            addView(txt("Vytvor svoj DREAM",34f,cream,true)); addView(txt("Premeníme sen na jasný plán.",16f,muted));
            val quote=txt("FOCUS  •  PLAN  •  ACHIEVE",13f,gold,true); quote.setPadding(0,dp(18),0,0); addView(quote)
        }; col.addView(hero)
        val goal=input("Názov sna")
        val target=input("Koľko potrebuješ",true)
        val current=input("Koľko už máš",true)
        val income=input("Mesačný príjem",true)
        val expenses=input("Mesačné výdavky",true)
        col.addView(goal); col.addView(target); col.addView(current); col.addView(income); col.addView(expenses)
        var picked=0L
        val dateBtn=button("📅  Vybrať dátum cieľa"){
            val c=Calendar.getInstance(); DatePickerDialog(this,{_,y,m,d-> val cc=Calendar.getInstance().apply{set(y,m,d,23,59,59);set(Calendar.MILLISECOND,0)}; picked=cc.timeInMillis; (itButton(dateBtn)).text="📅  "+SimpleDateFormat("d. M. yyyy",Locale.getDefault()).format(Date(picked))},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()
        }
        col.addView(dateBtn)
        col.addView(button("VYTVORIŤ MÔJ PLÁN"){
            val n=goal.text.toString().trim(); val t=target.text.toString().replace(',','.').toDoubleOrNull(); val h=current.text.toString().replace(',','.').toDoubleOrNull(); val inc=income.text.toString().replace(',','.').toDoubleOrNull(); val exp=expenses.text.toString().replace(',','.').toDoubleOrNull()
            if(n.isBlank()||t==null||h==null||inc==null||exp==null||picked==0L||t<=0){toast("Doplň všetky údaje.");return@button}
            prefs.edit().putString("goal_name",n).putFloat("target",t.toFloat()).putFloat("saved",h.toFloat()).putFloat("income",inc.toFloat()).putFloat("expenses",exp.toFloat()).putLong("target_date",picked).apply()
            dashboardScreen()
        })
        root.addView(scroll)
    }
    private fun itButton(b:Button)=b

    private fun dashboardScreen(){
        clear(); val scroll=ScrollView(this); val col=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(0,dp(16),0,dp(26))}; scroll.addView(col)
        val name=prefs.getString("goal_name","Môj sen") ?: "Môj sen"; val target=prefs.getFloat("target",0f).toDouble(); val saved=prefs.getFloat("saved",0f).toDouble(); val income=prefs.getFloat("income",0f).toDouble(); val expenses=prefs.getFloat("expenses",0f).toDouble(); val targetDate=prefs.getLong("target_date",System.currentTimeMillis())
        val remaining=max(0.0,target-saved); val days=max(1L,ceil((targetDate-System.currentTimeMillis())/86400000.0).toLong()); val daily=remaining/days; val freeDaily=max(0.0,(income-expenses)/30.0-daily); val pct=if(target>0) (saved/target*100).coerceIn(0.0,100.0) else 0.0
        val header=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(18),0,dp(18),dp(4))}; header.addView(txt("D",30f,gold,true),LinearLayout.LayoutParams(dp(48),dp(48))); val title=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; addView(txt(name,24f,cream,true)); addView(txt("Môj sen",13f,muted))}; header.addView(title,LinearLayout.LayoutParams(0,-2,1f)); val edit=txt("Upraviť",14f,gold,true).apply{setOnClickListener{editGoal()}}; header.addView(edit); col.addView(header)
        val hero=cardBox(); hero.addView(txt("${money(saved)}  /  ${money(target)}",28f,cream,true)); hero.addView(txt(String.format(Locale.getDefault(),"%.1f %%",pct),18f,gold,true)); val pb=ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply{max=1000;progress=(pct*10).toInt();progressTintList=android.content.res.ColorStateList.valueOf(gold);progressBackgroundTintList=android.content.res.ColorStateList.valueOf(card2);layoutParams=LinearLayout.LayoutParams(-1,dp(12)).apply{setMargins(0,dp(12),0,dp(12))}}; hero.addView(pb); hero.addView(txt("Do cieľa:  $days dní",16f,cream,true)); hero.addView(txt("Potrebujem približne ${money(daily)} denne",15f,muted)); col.addView(hero)
        val overview=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL; setPadding(dp(18),dp(5),dp(18),dp(5))}; overview.addView(metric("Dnes môžem minúť",money(freeDaily)),LinearLayout.LayoutParams(0,-2,1f)); overview.addView(metric("Progres",String.format(Locale.getDefault(),"%.1f %%",pct)),LinearLayout.LayoutParams(0,-2,1f)); col.addView(overview)
        pendingCard(col)
        col.addView(button("CHCEM NIEČO KÚPIŤ"){purchaseScreen()})
        col.addView(button("PRIDAŤ DNEŠNÝ KROK"){addStepDialog()})
        val quote=cardBox(); quote.addView(txt("„Malé kroky každý deň tvoria veľké zmeny.“",16f,cream,true)); quote.addView(txt("Každé rozhodnutie ťa môže priblížiť k tomu, na čom ti záleží viac.",14f,muted)); col.addView(quote)
        root.addView(scroll)
    }

    private fun metric(label:String,value:String)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(dp(14),dp(14),dp(14),dp(14)); background=bgDrawable(card,18); addView(txt(label,12f,muted)); addView(txt(value,18f,gold,true)); val lp=LinearLayout.LayoutParams(0,-2,1f); lp.setMargins(dp(4),dp(4),dp(4),dp(4)); layoutParams=lp}

    private fun purchaseScreen(){
        clear(); val col=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(0,dp(20),0,dp(24))}; col.addView(backTitle("Chcem niečo kúpiť")); val item=input("Čo chceš kúpiť?"); val price=input("Cena",true); col.addView(item); col.addView(price); col.addView(button("UKÁZAŤ DOPAD"){
            val p=price.text.toString().replace(',','.').toDoubleOrNull(); if(item.text.toString().isBlank()||p==null||p<=0){toast("Zadaj názov a cenu.");return@button}; impactScreen(item.text.toString().trim(),p)
        }); root.addView(col)
    }

    private fun impactScreen(item:String,price:Double){
        clear(); val target=prefs.getFloat("target",1f).toDouble(); val saved=prefs.getFloat("saved",0f).toDouble(); val targetDate=prefs.getLong("target_date",System.currentTimeMillis()+86400000); val days=max(1L,ceil((targetDate-System.currentTimeMillis())/86400000.0).toLong()); val daily=max(0.01,(target-saved)/days); val lostDays=ceil(price/daily).toInt(); val pct=(price/target*100).coerceAtLeast(0.0); val name=prefs.getString("goal_name","tvoj sen") ?: "tvoj sen"
        val col=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(0,dp(18),0,dp(24))}; col.addView(backTitle("Čo to znamená pre tvoj sen?")); val c=cardBox(); c.gravity=Gravity.CENTER_HORIZONTAL; c.addView(txt(item,20f,cream,true)); c.addView(txt(money(price),30f,gold,true)); c.addView(txt("POSUNIE ŤA O",13f,muted,true)); c.addView(txt("$lostDays dní",42f,cream,true)); c.addView(txt("ALEBO",12f,muted,true)); c.addView(txt(String.format(Locale.getDefault(),"%.1f %%",pct),30f,pink,true)); c.addView(txt("tvojho sna „$name“",14f,muted)); col.addView(c)
        val buy=button("KÚPIM TO"){saveHistory(item,price,"bought"); prefs.edit().putFloat("saved",max(0.0,saved-price).toFloat()).apply(); dashboardScreen()}; buy.setTextColor(Color.WHITE); buy.background=bgDrawable(green,26); col.addView(buy)
        val wait=button("POČKÁM 24 HODÍN"){prefs.edit().putString("pending_item",item).putFloat("pending_price",price.toFloat()).putLong("pending_until",System.currentTimeMillis()+24*60*60*1000L).apply(); saveHistory(item,price,"wait"); dashboardScreen()}; wait.setTextColor(bg); wait.background=bgDrawable(gold,26); col.addView(wait)
        val dream=button("RADŠEJ MÔJ SEN"){saveHistory(item,price,"dream"); prefs.edit().putFloat("saved",(saved+price).toFloat()).apply(); dashboardScreen()}; dream.setTextColor(Color.WHITE); dream.background=bgDrawable(purple,26); col.addView(dream)
        root.addView(col)
    }

    private fun pendingCard(col:LinearLayout){
        val until=prefs.getLong("pending_until",0L); if(until<=0L)return; val item=prefs.getString("pending_item","") ?: ""; val price=prefs.getFloat("pending_price",0f).toDouble(); val c=cardBox(); c.addView(txt("24-hodinová pauza",14f,gold,true)); c.addView(txt("$item  •  ${money(price)}",18f,cream,true)); val remain=txt("",15f,muted,true); c.addView(remain); c.setOnClickListener{if(System.currentTimeMillis()>=until) finalDecision(item,price)}; col.addView(c)
        fun update(){val ms=until-System.currentTimeMillis(); if(ms<=0){remain.text="Čas vypršal — ťukni a rozhodni sa.";remain.setTextColor(green)}else{val h=ms/3600000;val m=(ms%3600000)/60000;remain.text=String.format(Locale.getDefault(),"Zostáva %02d:%02d",h,m);handler.postDelayed({update()},30000)}}; update()
    }

    private fun finalDecision(item:String,price:Double){
        AlertDialog.Builder(this).setTitle("Čo chceš viac?").setMessage("$item — ${money(price)}\n\nAlebo tvoj sen: ${prefs.getString("goal_name","")}")
            .setPositiveButton("RADŠEJ MÔJ SEN"){_,_->val s=prefs.getFloat("saved",0f).toDouble();prefs.edit().putFloat("saved",(s+price).toFloat()).remove("pending_item").remove("pending_price").remove("pending_until").apply();saveHistory(item,price,"dream_after_24h");dashboardScreen()}
            .setNegativeButton("KÚPIM TO"){_,_->val s=prefs.getFloat("saved",0f).toDouble();prefs.edit().putFloat("saved",max(0.0,s-price).toFloat()).remove("pending_item").remove("pending_price").remove("pending_until").apply();saveHistory(item,price,"bought_after_24h");dashboardScreen()}.show()
    }

    private fun addStepDialog(){val e=EditText(this).apply{hint="Suma";inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL};AlertDialog.Builder(this).setTitle("Dnešný krok k snu").setView(e).setPositiveButton("ULOŽIŤ"){_,_->val v=e.text.toString().replace(',','.').toDoubleOrNull()?:0.0;val s=prefs.getFloat("saved",0f).toDouble();prefs.edit().putFloat("saved",(s+v).toFloat()).apply();saveHistory("Denný krok",v,"step");dashboardScreen()}.setNegativeButton("Zrušiť",null).show()}
    private fun saveHistory(item:String,price:Double,action:String){val old=prefs.getString("history","") ?: "";prefs.edit().putString("history","${System.currentTimeMillis()}|$action|$item|$price\n$old").apply()}

    private fun editGoal(){ prefs.edit().remove("goal_name").apply(); setupScreen() }
    private fun backTitle(title:String)=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(18),dp(6),dp(18),dp(12));val back=txt("‹",36f,gold,true).apply{setOnClickListener{dashboardScreen()}};addView(back,LinearLayout.LayoutParams(dp(50),-2));addView(txt(title,24f,cream,true))}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()

    private fun languageLabel():String = when(prefs.getString("lang","auto")){"sk"->"Slovenčina";"en"->"English";"de"->"Deutsch";"cs"->"Čeština";"pl"->"Polski";"fr"->"Français";"es"->"Español";"it"->"Italiano";else->"Auto"}
    private fun languageDialog(){val labels=arrayOf("Automaticky","Slovenčina","English","Deutsch","Čeština","Polski","Français","Español","Italiano");val codes=arrayOf("auto","sk","en","de","cs","pl","fr","es","it");AlertDialog.Builder(this).setTitle("Jazyk").setItems(labels){_,i->prefs.edit().putString("lang",codes[i]).apply(); if((prefs.getString("goal_name","")?:"").isBlank()) setupScreen() else dashboardScreen()}.show()}
}
