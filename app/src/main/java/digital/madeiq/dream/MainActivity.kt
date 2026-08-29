package digital.madeiq.dream

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.WindowManager
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

    private val bg = Color.rgb(3, 11, 22)
    private val navy = Color.rgb(12, 30, 51)
    private val navy2 = Color.rgb(20, 47, 77)
    private val gold = Color.rgb(250, 184, 83)
    private val cream = Color.rgb(255, 248, 231)
    private val muted = Color.rgb(159, 175, 198)
    private val pink = Color.rgb(244, 82, 140)
    private val green = Color.rgb(79, 201, 149)

    private val langs = linkedMapOf("SK" to "sk", "EN" to "en", "DE" to "de", "CZ" to "cs", "PL" to "pl", "HU" to "hu", "ES" to "es", "FR" to "fr", "IT" to "it", "RO" to "ro")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        setContentView(root)
        showHome()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun lang() = prefs.getString("lang", "sk") ?: "sk"
    private fun clear() = root.removeAllViews()

    private fun tr(sk: String, en: String): String = when (lang()) {
        "sk", "cs" -> sk
        else -> en
    }

    private fun money(v: Double): String {
        val locale = when (lang()) {
            "sk" -> Locale("sk", "SK"); "cs" -> Locale("cs", "CZ"); "pl" -> Locale("pl", "PL")
            "hu" -> Locale("hu", "HU"); "de" -> Locale.GERMANY; "fr" -> Locale.FRANCE
            "it" -> Locale.ITALY; "es" -> Locale("es", "ES"); "ro" -> Locale("ro", "RO")
            else -> Locale.UK
        }
        return NumberFormat.getCurrencyInstance(locale).format(v)
    }

    private fun txt(s: String, size: Float = 16f, color: Int = cream, bold: Boolean = false) = TextView(this).apply {
        text = s; textSize = size; setTextColor(color); includeFontPadding = false
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun shape(color: Int, radius: Int = 26, stroke: Int? = null) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat(); stroke?.let { setStroke(dp(1), it) }
    }

    private fun grad(colors: IntArray, radius: Int = 30) = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors).apply { cornerRadius = dp(radius).toFloat() }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(22), dp(20), dp(22), dp(20))
        background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.rgb(25,55,88), Color.rgb(9,26,46))).apply { cornerRadius = dp(30).toFloat() }
        elevation = dp(3).toFloat()
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
    }

    private fun button(label: String, accent: Boolean = false, onClick: () -> Unit) = Button(this).apply {
        text = label; textSize = 15f; isAllCaps = false; typeface = Typeface.DEFAULT_BOLD; stateListAnimator = null
        setTextColor(if (accent) Color.WHITE else bg)
        background = if (accent) grad(intArrayOf(Color.rgb(255,176,82), pink)) else grad(intArrayOf(Color.rgb(255,205,113), Color.rgb(247,174,77)))
        minHeight = 0; minimumHeight = 0
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(-1, dp(62)).apply { setMargins(dp(16), dp(9), dp(16), dp(9)) }
    }

    private fun input(hint: String, numeric: Boolean = false) = EditText(this).apply {
        this.hint = hint; setHintTextColor(muted); setTextColor(cream); textSize = 17f; setSingleLine(true)
        background = shape(navy2, 24, Color.rgb(179,132,72)); setPadding(dp(20), 0, dp(20), 0)
        if (numeric) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layoutParams = LinearLayout.LayoutParams(-1, dp(68)).apply { setMargins(dp(16), dp(7), dp(16), dp(7)) }
    }

    private fun languageStrip(refresh: () -> Unit): HorizontalScrollView {
        val hsv = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false; overScrollMode = View.OVER_SCROLL_NEVER }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(dp(12), dp(6), dp(12), dp(6)) }
        langs.forEach { (label, code) ->
            row.addView(txt(label, 12f, if (lang()==code) bg else cream, true).apply {
                gravity = Gravity.CENTER; setPadding(dp(14), dp(9), dp(14), dp(9))
                background = shape(if (lang()==code) gold else navy, 18, Color.rgb(106,87,61))
                setOnClickListener { prefs.edit().putString("lang", code).apply(); refresh() }
            }, LinearLayout.LayoutParams(-2, -2).apply { setMargins(dp(4),0,dp(4),0) })
        }
        hsv.addView(row)
        return hsv
    }

    private fun top(title: String, back: (() -> Unit)? = null): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(10), dp(16), dp(8))
        if (back != null) addView(txt("‹", 38f, cream, true).apply { gravity = Gravity.CENTER; setOnClickListener { back() } }, LinearLayout.LayoutParams(dp(44), dp(44)))
        else addView(txt("D", 30f, gold, true).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(dp(44), dp(44)))
        addView(txt(title, 23f, cream, true), LinearLayout.LayoutParams(0, -2, 1f))
    }

    private fun showHome() {
        val hasGoal = !(prefs.getString("goal_name", "") ?: "").isBlank()
        if (hasGoal) dashboard() else setup()
    }

    private fun setup() {
        clear()
        val scroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(8), 0, dp(300)) }
        scroll.addView(col)
        col.addView(top("DREAM"))
        col.addView(languageStrip { setup() })

        col.addView(card().apply {
            addView(txt(tr("Vytvor svoj DREAM", "Build your DREAM"), 34f, cream, true))
            addView(txt(tr("Jasný cieľ. Jasný plán. Každý deň bližšie.", "A clear goal. A clear plan. Closer every day."), 15f, muted).apply { setPadding(0, dp(12), 0, 0) })
            addView(txt("FOCUS  •  PLAN  •  ACHIEVE", 13f, gold, true).apply { setPadding(0, dp(18), 0, 0) })
        })

        val goal = input(tr("Názov sna", "Dream name"))
        val target = input(tr("Koľko potrebuješ", "Target amount"), true)
        val saved = input(tr("Koľko už máš", "Already saved"), true)
        val income = input(tr("Mesačný príjem", "Monthly income"), true)
        val expenses = input(tr("Mesačné výdavky", "Monthly expenses"), true)
        listOf(goal,target,saved,income,expenses).forEach { field ->
            col.addView(field)
            field.setOnFocusChangeListener { _, focused -> if (focused) scroll.postDelayed({ scroll.smoothScrollTo(0, field.bottom + dp(220)) }, 220) }
        }

        var picked = 0L
        lateinit var date: Button
        date = button(tr("📅  Vybrať dátum cieľa", "📅  Choose target date")) {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val cc = Calendar.getInstance().apply { set(y,m,d,23,59,59); set(Calendar.MILLISECOND,0) }
                picked = cc.timeInMillis
                date.text = "📅  " + SimpleDateFormat("d. M. yyyy", Locale.getDefault()).format(Date(picked))
                scroll.postDelayed({ scroll.fullScroll(View.FOCUS_DOWN) }, 100)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
        col.addView(date)

        val create = button(tr("VYTVORIŤ MÔJ PLÁN", "CREATE MY PLAN"), true) {
            val n = goal.text.toString().trim()
            val t = target.text.toString().replace(',','.').toDoubleOrNull()
            val s = saved.text.toString().replace(',','.').toDoubleOrNull()
            val i = income.text.toString().replace(',','.').toDoubleOrNull()
            val e = expenses.text.toString().replace(',','.').toDoubleOrNull()
            if (n.isBlank() || t == null || s == null || i == null || e == null || picked == 0L || t <= 0) {
                Toast.makeText(this, tr("Doplň všetky údaje.", "Complete all fields."), Toast.LENGTH_SHORT).show(); return@button
            }
            prefs.edit().putString("goal_name", n).putFloat("target",t.toFloat()).putFloat("saved",s.toFloat()).putFloat("income",i.toFloat()).putFloat("expenses",e.toFloat()).putLong("target_date",picked).apply()
            dashboard()
        }
        col.addView(create)
        create.setOnFocusChangeListener { _, focused -> if (focused) scroll.postDelayed({ scroll.fullScroll(View.FOCUS_DOWN) }, 120) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    private fun dashboard() {
        clear()
        val target = prefs.getFloat("target",0f).toDouble()
        val saved = prefs.getFloat("saved",0f).toDouble()
        val income = prefs.getFloat("income",0f).toDouble()
        val expenses = prefs.getFloat("expenses",0f).toDouble()
        val date = prefs.getLong("target_date",System.currentTimeMillis())
        val remaining = max(0.0, target-saved)
        val days = max(1L, ceil((date-System.currentTimeMillis())/86400000.0).toLong())
        val daily = remaining/days
        val free = max(0.0,(income-expenses)/30.0-daily)
        val pct = if (target>0) (saved/target*100).coerceIn(0.0,100.0) else 0.0
        val name = prefs.getString("goal_name", tr("Môj sen","My dream")) ?: "DREAM"

        val scroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0,dp(6),0,dp(120)) }
        scroll.addView(col)
        col.addView(top(name))
        col.addView(languageStrip { dashboard() })

        col.addView(card().apply {
            gravity = Gravity.CENTER_HORIZONTAL
            addView(txt("${money(saved)}  /  ${money(target)}", 28f, cream, true).apply { gravity = Gravity.CENTER })
            addView(ProgressRing(this@MainActivity,pct.toFloat(),gold,Color.rgb(96,113,135)).apply { layoutParams = LinearLayout.LayoutParams(dp(185),dp(185)).apply { setMargins(0,dp(18),0,dp(12)) } })
            addView(txt(String.format(Locale.getDefault(),"%.1f %%",pct),16f,gold,true).apply { gravity=Gravity.CENTER })
            addView(txt(tr("Do cieľa: $days dní","To goal: $days days"),18f,cream,true).apply { gravity=Gravity.CENTER; setPadding(0,dp(8),0,0) })
            addView(txt(tr("Približne ${money(daily)} denne","About ${money(daily)} per day"),14f,muted).apply { gravity=Gravity.CENTER; setPadding(0,dp(5),0,0) })
        })

        val metrics = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; setPadding(dp(12),dp(2),dp(12),dp(2)) }
        metrics.addView(metric(tr("Dnes môžem minúť","Can spend today"),money(free)),LinearLayout.LayoutParams(0,-2,1f))
        metrics.addView(metric(tr("Progres","Progress"),String.format(Locale.getDefault(),"%.1f %%",pct)),LinearLayout.LayoutParams(0,-2,1f))
        col.addView(metrics)

        col.addView(button(tr("CHCEM NIEČO KÚPIŤ","I WANT TO BUY"),true) { purchase() })
        col.addView(button(tr("PRIDAŤ DNEŠNÝ KROK","ADD TODAY'S STEP")) { addStep() })
        col.addView(card().apply {
            addView(txt(tr("„Malé kroky každý deň tvoria veľké zmeny.“","“Small steps every day create big change.”"),18f,cream,true))
            addView(txt(tr("Každé rozhodnutie ťa môže priblížiť k tomu, na čom ti záleží viac.","Every decision can bring you closer to what matters more."),14f,muted).apply { setPadding(0,dp(8),0,0) })
        })
        root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        root.addView(nav())
    }

    private fun metric(label:String,value:String)=LinearLayout(this).apply {
        orientation=LinearLayout.VERTICAL; setPadding(dp(15),dp(15),dp(15),dp(15)); background=shape(navy,23)
        addView(txt(label,12f,muted)); addView(txt(value,19f,gold,true).apply { setPadding(0,dp(5),0,0) })
        layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply { setMargins(dp(4),dp(4),dp(4),dp(4)) }
    }

    private fun nav()=LinearLayout(this).apply {
        orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER; setPadding(dp(4),dp(7),dp(4),dp(9)); background=shape(Color.rgb(6,18,33),0)
        val items=listOf("⌂\n${tr("Prehľad","Home")}","◷\n${tr("História","History")}","+","▥\n${tr("Štatistiky","Stats")}","⚙\n${tr("Nastavenia","Settings")}")
        items.forEachIndexed { i,label ->
            addView(txt(label, if(i==2) 25f else 10f, if(i==0||i==2) cream else muted, i==0||i==2).apply {
                gravity=Gravity.CENTER
                if(i==2) background=shape(Color.rgb(45,80,128),26)
                setOnClickListener {
                    when(i){0->dashboard();1->simplePage(tr("História","History"));2->addStep();3->simplePage(tr("Štatistiky","Statistics"));4->settings()}
                }
            },LinearLayout.LayoutParams(0,dp(58),1f).apply { setMargins(dp(2),0,dp(2),0) })
        }
    }

    private fun simplePage(title:String){
        clear(); root.addView(top(title){dashboard()}); root.addView(card().apply { addView(txt(tr("Táto sekcia je pripravená na používanie.","This section is ready to use."),16f,muted)) })
    }

    private fun settings(){
        clear(); val col=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(0,dp(8),0,dp(80)) }
        col.addView(top(tr("Nastavenia","Settings")){dashboard()}); col.addView(languageStrip { settings() })
        col.addView(button(tr("UPRAVIŤ CIEĽ","EDIT GOAL")) { prefs.edit().remove("goal_name").apply(); setup() })
        col.addView(button(tr("VYMAZAŤ DÁTA","RESET DATA"),true) { prefs.edit().clear().apply(); setup() })
        root.addView(col)
    }

    private fun purchase(){
        val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(8),dp(8),dp(8),dp(8)) }
        val what=input(tr("Čo chceš kúpiť?","What do you want to buy?")); val price=input(tr("Cena","Price"),true)
        box.addView(what); box.addView(price)
        AlertDialog.Builder(this).setTitle(tr("Rozhodnutie","Decision")).setView(box)
            .setPositiveButton(tr("Uložiť","Save")){_,_->
                val p=price.text.toString().replace(',','.').toDoubleOrNull() ?: 0.0
                if(p>0){ val saved=prefs.getFloat("saved",0f)+p.toFloat(); prefs.edit().putFloat("saved",saved).apply(); dashboard() }
            }.setNegativeButton(tr("Zrušiť","Cancel"),null).show()
    }

    private fun addStep(){
        val e=EditText(this).apply { hint=tr("Koľko si dnes odložil?","How much did you save today?"); inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        AlertDialog.Builder(this).setTitle(tr("Dnešný krok","Today's step")).setView(e)
            .setPositiveButton(tr("Pridať","Add")){_,_->
                val v=e.text.toString().replace(',','.').toFloatOrNull() ?: 0f
                if(v>0){ prefs.edit().putFloat("saved",prefs.getFloat("saved",0f)+v).apply(); dashboard() }
            }.setNegativeButton(tr("Zrušiť","Cancel"),null).show()
    }

    class ProgressRing(ctx:Context, private val pct:Float, private val fg:Int, private val track:Int):View(ctx){
        private val p=Paint(Paint.ANTI_ALIAS_FLAG).apply { style=Paint.Style.STROKE; strokeWidth=22f; strokeCap=Paint.Cap.ROUND }
        private val text=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.WHITE; textSize=54f; textAlign=Paint.Align.CENTER; typeface=Typeface.DEFAULT_BOLD }
        override fun onDraw(c:Canvas){
            super.onDraw(c); val pad=30f; val r=RectF(pad,pad,width-pad,height-pad)
            p.color=track; c.drawArc(r,0f,360f,false,p); p.color=fg; c.drawArc(r,-90f,360f*(pct/100f),false,p)
            c.drawText(String.format(Locale.getDefault(),"%.1f %%",pct),width/2f,height/2f+18f,text)
        }
    }
}
