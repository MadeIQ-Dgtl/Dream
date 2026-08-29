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

    private val bg = Color.rgb(4, 12, 24)
    private val panel = Color.rgb(12, 29, 49)
    private val panel2 = Color.rgb(18, 43, 70)
    private val gold = Color.rgb(250, 183, 83)
    private val cream = Color.rgb(255, 248, 231)
    private val muted = Color.rgb(157, 172, 195)
    private val green = Color.rgb(79, 201, 149)
    private val pink = Color.rgb(242, 91, 140)
    private val purple = Color.rgb(139, 96, 245)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        setContentView(root)
        if ((prefs.getString("goal_name", "") ?: "").isBlank()) setupScreen() else dashboardScreen()
    }

    private fun clear() = root.removeAllViews()
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun lang() = prefs.getString("lang", "sk") ?: "sk"
    private fun tr(sk: String, en: String) = if (lang() == "en") en else sk
    private fun money(v: Double): String = NumberFormat.getCurrencyInstance(if (lang() == "en") Locale.UK else Locale("sk", "SK")).format(v)

    private fun txt(s: String, size: Float = 16f, color: Int = cream, bold: Boolean = false) = TextView(this).apply {
        text = s
        textSize = size
        setTextColor(color)
        includeFontPadding = false
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun solid(color: Int, radius: Int = 24, stroke: Int? = null) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (stroke != null) setStroke(dp(1), stroke)
    }

    private fun gradient(colors: IntArray, radius: Int = 28, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT) =
        GradientDrawable(orientation, colors).apply { cornerRadius = dp(radius).toFloat() }

    private fun cardBox(radius: Int = 26) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(19), dp(20), dp(19))
        background = gradient(intArrayOf(Color.rgb(20, 46, 75), Color.rgb(9, 26, 46)), radius, GradientDrawable.Orientation.TL_BR)
        elevation = dp(3).toFloat()
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(dp(16), dp(7), dp(16), dp(7)) }
    }

    private fun button(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        textSize = 15f
        setTextColor(bg)
        typeface = Typeface.DEFAULT_BOLD
        isAllCaps = false
        stateListAnimator = null
        minHeight = 0
        minimumHeight = 0
        background = gradient(intArrayOf(Color.rgb(255, 204, 112), Color.rgb(247, 174, 77)), 30)
        setPadding(dp(18), dp(14), dp(18), dp(14))
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(-1, dp(58)).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
    }

    private fun accentButton(label: String, onClick: () -> Unit) = button(label, onClick).apply {
        setTextColor(Color.WHITE)
        background = gradient(intArrayOf(Color.rgb(255, 176, 82), Color.rgb(244, 83, 139)), 30)
    }

    private fun input(hintText: String, numeric: Boolean = false, scroll: ScrollView? = null) = EditText(this).apply {
        hint = hintText
        setHintTextColor(muted)
        setTextColor(cream)
        textSize = 16f
        setSingleLine(true)
        background = solid(panel2, 22, Color.rgb(185, 135, 70))
        setPadding(dp(18), 0, dp(18), 0)
        if (numeric) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layoutParams = LinearLayout.LayoutParams(-1, dp(64)).apply { setMargins(dp(16), dp(6), dp(16), dp(6)) }
        setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && scroll != null) scroll.postDelayed({ scroll.smoothScrollTo(0, v.bottom + dp(180)) }, 220)
        }
    }

    private fun topBar(title: String, back: Boolean = false, action: String? = null, actionClick: (() -> Unit)? = null) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(16), dp(10), dp(16), dp(10))
        if (back) addView(txt("‹", 38f, cream, true).apply { gravity = Gravity.CENTER; setOnClickListener { dashboardScreen() } }, LinearLayout.LayoutParams(dp(44), dp(44)))
        else addView(txt("D", 31f, gold, true), LinearLayout.LayoutParams(dp(48), dp(44)))
        addView(txt(title, 23f, cream, true), LinearLayout.LayoutParams(0, -2, 1f))
        if (action != null) addView(txt(action, 13f, gold, true).apply { gravity = Gravity.CENTER; setPadding(dp(10), dp(8), dp(10), dp(8)); setOnClickListener { actionClick?.invoke() } })
    }

    private fun setupScreen() {
        clear()
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(8), 0, dp(220)) }
        scroll.addView(col)
        col.addView(topBar("DREAM", false, "🌐 ${if (lang() == "en") "EN" else "SK"}") { languageDialog { setupScreen() } })

        val hero = cardBox(30).apply {
            setPadding(dp(24), dp(26), dp(24), dp(26))
            background = gradient(intArrayOf(Color.rgb(26, 58, 92), Color.rgb(10, 26, 47)), 30, GradientDrawable.Orientation.TL_BR)
            addView(txt(tr("Vytvor svoj\nDREAM", "Build your\nDREAM"), 36f, cream, true))
            addView(txt(tr("Premeníme sen na jasný plán.", "Turn your dream into a clear plan."), 16f, muted).apply { setPadding(0, dp(14), 0, 0) })
            addView(txt("FOCUS  •  PLAN  •  ACHIEVE", 13f, gold, true).apply { setPadding(0, dp(22), 0, 0) })
        }
        col.addView(hero)

        val goal = input(tr("Názov sna", "Dream name"), false, scroll)
        val target = input(tr("Koľko potrebuješ", "Target amount"), true, scroll)
        val current = input(tr("Koľko už máš", "Already saved"), true, scroll)
        val income = input(tr("Mesačný príjem", "Monthly income"), true, scroll)
        val expenses = input(tr("Mesačné výdavky", "Monthly expenses"), true, scroll)
        listOf(goal, target, current, income, expenses).forEach { col.addView(it) }

        var picked = 0L
        lateinit var dateBtn: Button
        dateBtn = button(tr("📅  Vybrať dátum cieľa", "📅  Choose target date")) {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val cc = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59); set(Calendar.MILLISECOND, 0) }
                picked = cc.timeInMillis
                dateBtn.text = "📅  " + SimpleDateFormat("d. M. yyyy", Locale.getDefault()).format(Date(picked))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
        col.addView(dateBtn)
        col.addView(accentButton(tr("VYTVORIŤ MÔJ PLÁN", "CREATE MY PLAN")) {
            val n = goal.text.toString().trim()
            val t = target.text.toString().replace(',', '.').toDoubleOrNull()
            val h = current.text.toString().replace(',', '.').toDoubleOrNull()
            val inc = income.text.toString().replace(',', '.').toDoubleOrNull()
            val exp = expenses.text.toString().replace(',', '.').toDoubleOrNull()
            if (n.isBlank() || t == null || h == null || inc == null || exp == null || picked == 0L || t <= 0) {
                toast(tr("Doplň všetky údaje.", "Complete all fields.")); return@accentButton
            }
            prefs.edit().putString("goal_name", n).putFloat("target", t.toFloat()).putFloat("saved", h.toFloat())
                .putFloat("income", inc.toFloat()).putFloat("expenses", exp.toFloat()).putLong("target_date", picked).apply()
            dashboardScreen()
        })
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    private fun dashboardScreen() {
        clear()
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(6), 0, dp(24)) }
        scroll.addView(col)

        val name = prefs.getString("goal_name", tr("Môj sen", "My dream")) ?: tr("Môj sen", "My dream")
        val target = prefs.getFloat("target", 0f).toDouble()
        val saved = prefs.getFloat("saved", 0f).toDouble()
        val income = prefs.getFloat("income", 0f).toDouble()
        val expenses = prefs.getFloat("expenses", 0f).toDouble()
        val targetDate = prefs.getLong("target_date", System.currentTimeMillis())
        val remaining = max(0.0, target - saved)
        val days = max(1L, ceil((targetDate - System.currentTimeMillis()) / 86400000.0).toLong())
        val daily = remaining / days
        val freeDaily = max(0.0, (income - expenses) / 30.0 - daily)
        val pct = if (target > 0) (saved / target * 100).coerceIn(0.0, 100.0) else 0.0

        col.addView(topBar(name, false, tr("Upraviť", "Edit")) { editGoal() })

        val hero = cardBox(30).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(24), dp(22), dp(24))
            background = gradient(intArrayOf(Color.rgb(24, 55, 87), Color.rgb(10, 28, 49)), 30, GradientDrawable.Orientation.TL_BR)
            addView(txt("${money(saved)}  /  ${money(target)}", 27f, cream, true).apply { gravity = Gravity.CENTER })
            val ring = CircularProgressView(this@MainActivity, pct.toFloat(), gold, Color.rgb(94, 112, 134)).apply {
                layoutParams = LinearLayout.LayoutParams(dp(176), dp(176)).apply { setMargins(0, dp(18), 0, dp(14)) }
            }
            addView(ring)
            addView(txt(String.format(Locale.getDefault(), "%.1f %%", pct), 16f, gold, true).apply { gravity = Gravity.CENTER })
            addView(txt(tr("Do cieľa: $days dní", "To goal: $days days"), 17f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
            addView(txt(tr("Približne ${money(daily)} denne", "About ${money(daily)} per day"), 14f, muted).apply { gravity = Gravity.CENTER; setPadding(0, dp(5), 0, 0) })
        }
        col.addView(hero)

        val metrics = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(dp(12), dp(3), dp(12), dp(3)) }
        metrics.addView(metric(tr("Dnes môžem minúť", "Can spend today"), money(freeDaily)), LinearLayout.LayoutParams(0, -2, 1f))
        metrics.addView(metric(tr("Progres", "Progress"), String.format(Locale.getDefault(), "%.1f %%", pct)), LinearLayout.LayoutParams(0, -2, 1f))
        col.addView(metrics)
        pendingCard(col)
        col.addView(accentButton(tr("CHCEM NIEČO KÚPIŤ", "I WANT TO BUY")) { purchaseScreen() })
        col.addView(button(tr("PRIDAŤ DNEŠNÝ KROK", "ADD TODAY'S STEP")) { addStepDialog() })

        val quote = cardBox(25).apply {
            addView(txt(tr("„Malé kroky každý deň tvoria veľké zmeny.“", "“Small steps every day create big change.”"), 17f, cream, true))
            addView(txt(tr("Každé rozhodnutie ťa môže priblížiť k tomu, na čom ti záleží viac.", "Every decision can bring you closer to what matters more."), 14f, muted).apply { setPadding(0, dp(8), 0, 0) })
        }
        col.addView(quote)

        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav(0))
    }

    private fun metric(label: String, value: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(14), dp(14), dp(14))
        background = gradient(intArrayOf(Color.rgb(18, 42, 68), Color.rgb(10, 28, 48)), 22)
        addView(txt(label, 12f, muted))
        addView(txt(value, 19f, gold, true).apply { setPadding(0, dp(5), 0, 0) })
        layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(dp(4), dp(4), dp(4), dp(4)) }
    }

    private fun bottomNav(active: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(dp(8), dp(8), dp(8), dp(10))
        background = solid(Color.rgb(6, 18, 33), 0)
        val labels = arrayOf(tr("⌂\nPrehľad", "⌂\nHome"), tr("◷\nHistória", "◷\nHistory"), "+", tr("▥\nŠtatistiky", "▥\nStats"), tr("⚙\nNastavenia", "⚙\nSettings"))
        labels.forEachIndexed { i, label ->
            val v = txt(label, if (i == 2) 26f else 11f, if (i == active || i == 2) cream else muted, i == active || i == 2).apply {
                gravity = Gravity.CENTER
                if (i == 2) background = solid(Color.rgb(43, 79, 127), 28)
                setOnClickListener {
                    when (i) {
                        0 -> dashboardScreen()
                        1 -> historyScreen()
                        2 -> purchaseScreen()
                        3 -> statsScreen()
                        4 -> settingsScreen()
                    }
                }
            }
            addView(v, LinearLayout.LayoutParams(0, dp(56), 1f).apply { setMargins(dp(3), 0, dp(3), 0) })
        }
    }

    private fun purchaseScreen() {
        clear()
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(8), 0, dp(180)) }
        scroll.addView(col)
        col.addView(topBar(tr("Chcem niečo kúpiť", "I want to buy"), true))
        val product = cardBox(30).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(28), dp(24), dp(28))
            addView(txt("⌚", 54f, cream).apply { gravity = Gravity.CENTER })
            addView(txt(tr("Každý nákup má svoju cenu", "Every purchase has a cost"), 15f, muted, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(10), 0, 0) })
        }
        col.addView(product)
        val item = input(tr("Čo chceš kúpiť?", "What do you want to buy?"), false, scroll)
        val price = input(tr("Cena", "Price"), true, scroll)
        col.addView(item); col.addView(price)
        col.addView(accentButton(tr("UKÁZAŤ DOPAD", "SHOW IMPACT")) {
            val p = price.text.toString().replace(',', '.').toDoubleOrNull()
            if (item.text.toString().isBlank() || p == null || p <= 0) { toast(tr("Zadaj názov a cenu.", "Enter item and price.")); return@accentButton }
            impactScreen(item.text.toString().trim(), p)
        })
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    private fun impactScreen(item: String, price: Double) {
        clear()
        val target = prefs.getFloat("target", 1f).toDouble()
        val saved = prefs.getFloat("saved", 0f).toDouble()
        val targetDate = prefs.getLong("target_date", System.currentTimeMillis() + 86400000)
        val days = max(1L, ceil((targetDate - System.currentTimeMillis()) / 86400000.0).toLong())
        val daily = max(0.01, (target - saved) / days)
        val lostDays = ceil(price / daily).toInt()
        val pct = (price / target * 100).coerceAtLeast(0.0)
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(8), 0, dp(28)) }
        col.addView(topBar(tr("Dopad na tvoj sen", "Impact on your dream"), true))
        val c = cardBox(30).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            background = gradient(intArrayOf(Color.rgb(27, 54, 85), Color.rgb(57, 38, 73), Color.rgb(12, 31, 52)), 30, GradientDrawable.Orientation.TL_BR)
            addView(txt(item, 19f, cream, true).apply { gravity = Gravity.CENTER })
            addView(txt(money(price), 32f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(7), 0, dp(10)) })
            addView(txt(String.format(Locale.getDefault(), "%.1f %%", pct), 35f, pink, true).apply { gravity = Gravity.CENTER })
            addView(txt(tr("tvojho sna", "of your dream"), 13f, muted).apply { gravity = Gravity.CENTER })
            addView(txt(tr("Posun približne o $lostDays dní", "Delay of about $lostDays days"), 18f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(18), 0, 0) })
        }
        col.addView(c)
        col.addView(accentButton(tr("KÚPIM TO", "BUY IT")) { saveHistory(item, price, "bought"); prefs.edit().putFloat("saved", max(0.0, saved - price).toFloat()).apply(); dashboardScreen() })
        col.addView(button(tr("POČKÁM 24 HODÍN", "WAIT 24 HOURS")) { prefs.edit().putString("pending_item", item).putFloat("pending_price", price.toFloat()).putLong("pending_until", System.currentTimeMillis() + 86400000L).apply(); saveHistory(item, price, "wait"); dashboardScreen() })
        col.addView(button(tr("RADŠEJ MÔJ SEN", "CHOOSE MY DREAM")) { saveHistory(item, price, "dream"); prefs.edit().putFloat("saved", (saved + price).toFloat()).apply(); dashboardScreen() })
        root.addView(ScrollView(this).apply { addView(col) }, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    private fun pendingCard(col: LinearLayout) {
        val until = prefs.getLong("pending_until", 0L)
        if (until <= 0L) return
        val item = prefs.getString("pending_item", "") ?: ""
        val price = prefs.getFloat("pending_price", 0f).toDouble()
        val remaining = until - System.currentTimeMillis()
        val c = cardBox(24).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            addView(txt(tr("24-hodinová pauza", "24-hour pause"), 14f, gold, true).apply { gravity = Gravity.CENTER })
            addView(txt("$item  •  ${money(price)}", 18f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(6), 0, 0) })
            addView(txt(if (remaining <= 0) tr("Čas vypršal — rozhodni sa.", "Time is up — decide now.") else tr("Zostáva približne ${max(1, remaining / 3600000)} h", "About ${max(1, remaining / 3600000)} h left"), 13f, muted).apply { gravity = Gravity.CENTER; setPadding(0, dp(6), 0, 0) })
            if (remaining <= 0) setOnClickListener { finalDecision(item, price) }
        }
        col.addView(c)
    }

    private fun finalDecision(item: String, price: Double) {
        AlertDialog.Builder(this).setTitle(tr("Čo chceš viac?", "What do you want more?"))
            .setMessage("$item — ${money(price)}")
            .setPositiveButton(tr("RADŠEJ MÔJ SEN", "CHOOSE MY DREAM")) { _, _ ->
                val s = prefs.getFloat("saved", 0f).toDouble(); prefs.edit().putFloat("saved", (s + price).toFloat()).remove("pending_item").remove("pending_price").remove("pending_until").apply(); saveHistory(item, price, "dream_after_24h"); dashboardScreen()
            }
            .setNegativeButton(tr("KÚPIM TO", "BUY IT")) { _, _ ->
                val s = prefs.getFloat("saved", 0f).toDouble(); prefs.edit().putFloat("saved", max(0.0, s - price).toFloat()).remove("pending_item").remove("pending_price").remove("pending_until").apply(); saveHistory(item, price, "bought_after_24h"); dashboardScreen()
            }.show()
    }

    private fun addStepDialog() {
        val e = EditText(this).apply { hint = tr("Suma", "Amount"); inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        AlertDialog.Builder(this).setTitle(tr("Dnešný krok k snu", "Today's step")) .setView(e)
            .setPositiveButton(tr("ULOŽIŤ", "SAVE")) { _, _ ->
                val v = e.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
                if (v > 0) { val s = prefs.getFloat("saved", 0f).toDouble(); prefs.edit().putFloat("saved", (s + v).toFloat()).apply(); saveHistory(tr("Dnešný krok", "Today's step"), v, "step"); dashboardScreen() }
            }.setNegativeButton(tr("ZRUŠIŤ", "CANCEL"), null).show()
    }

    private fun historyScreen() {
        clear()
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(8), 0, dp(24)) }
        col.addView(topBar(tr("História", "History"), false))
        val entries = prefs.getString("history", "")?.lines()?.filter { it.isNotBlank() } ?: emptyList()
        if (entries.isEmpty()) col.addView(cardBox().apply { addView(txt(tr("Zatiaľ tu nič nie je.", "Nothing here yet."), 16f, muted)) })
        else entries.takeLast(20).reversed().forEach { raw ->
            val p = raw.split("|")
            if (p.size >= 4) col.addView(cardBox(22).apply {
                addView(txt(p[1], 16f, cream, true))
                addView(txt("${p[2]}  •  ${p[3]}", 13f, muted).apply { setPadding(0, dp(5), 0, 0) })
            })
        }
        val scroll = ScrollView(this).apply { addView(col) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f)); root.addView(bottomNav(1))
    }

    private fun statsScreen() {
        clear()
        val target = prefs.getFloat("target", 0f).toDouble()
        val saved = prefs.getFloat("saved", 0f).toDouble()
        val pct = if (target > 0) (saved / target * 100).coerceIn(0.0, 100.0) else 0.0
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(8), 0, dp(24)) }
        col.addView(topBar(tr("Štatistiky", "Statistics"), false))
        val c = cardBox(30).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            addView(txt(tr("Celkový progres", "Overall progress"), 15f, muted, true).apply { gravity = Gravity.CENTER })
            addView(CircularProgressView(this@MainActivity, pct.toFloat(), gold, Color.rgb(90, 108, 130)).apply { layoutParams = LinearLayout.LayoutParams(dp(190), dp(190)).apply { setMargins(0, dp(18), 0, dp(12)) } })
            addView(txt(String.format(Locale.getDefault(), "%.1f %%", pct), 27f, cream, true).apply { gravity = Gravity.CENTER })
            addView(txt("${money(saved)} / ${money(target)}", 15f, gold, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
        }
        col.addView(c)
        root.addView(ScrollView(this).apply { addView(col) }, LinearLayout.LayoutParams(-1, 0, 1f)); root.addView(bottomNav(3))
    }

    private fun settingsScreen() {
        clear()
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(8), 0, dp(24)) }
        col.addView(topBar(tr("Nastavenia", "Settings"), false))
        val langCard = cardBox().apply {
            addView(txt(tr("Jazyk aplikácie", "App language"), 17f, cream, true))
            addView(txt(if (lang() == "en") "English" else "Slovenčina", 14f, gold).apply { setPadding(0, dp(8), 0, 0); setOnClickListener { languageDialog { settingsScreen() } } })
        }
        col.addView(langCard)
        col.addView(button(tr("UPRAVIŤ MÔJ SEN", "EDIT MY DREAM")) { editGoal() })
        col.addView(button(tr("VYMAZAŤ ÚDAJE", "RESET DATA")) {
            AlertDialog.Builder(this).setTitle(tr("Vymazať všetko?", "Reset everything?"))
                .setMessage(tr("Tento krok sa nedá vrátiť späť.", "This cannot be undone."))
                .setPositiveButton(tr("VYMAZAŤ", "RESET")) { _, _ -> prefs.edit().clear().apply(); setupScreen() }
                .setNegativeButton(tr("ZRUŠIŤ", "CANCEL"), null).show()
        })
        root.addView(ScrollView(this).apply { addView(col) }, LinearLayout.LayoutParams(-1, 0, 1f)); root.addView(bottomNav(4))
    }

    private fun languageDialog(after: () -> Unit) {
        val items = arrayOf("Slovenčina", "English")
        val checked = if (lang() == "en") 1 else 0
        AlertDialog.Builder(this).setTitle(tr("Jazyk", "Language")).setSingleChoiceItems(items, checked) { d, which ->
            prefs.edit().putString("lang", if (which == 1) "en" else "sk").apply(); d.dismiss(); after()
        }.show()
    }

    private fun editGoal() {
        prefs.edit().remove("goal_name").apply(); setupScreen()
    }

    private fun saveHistory(item: String, price: Double, type: String) {
        val date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
        val old = prefs.getString("history", "") ?: ""
        val line = "$date|$item|${money(price)}|$type"
        prefs.edit().putString("history", if (old.isBlank()) line else "$old\n$line").apply()
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    class CircularProgressView(context: Context, private val progress: Float, private val active: Int, private val track: Int) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat(); val h = height.toFloat(); val stroke = w * 0.085f
            paint.strokeWidth = stroke
            val r = RectF(stroke, stroke, w - stroke, h - stroke)
            paint.color = track; canvas.drawArc(r, -90f, 360f, false, paint)
            paint.color = active; canvas.drawArc(r, -90f, 360f * (progress.coerceIn(0f, 100f) / 100f), false, paint)
            paint.style = Paint.Style.FILL; paint.color = Color.rgb(255,248,231); paint.textAlign = Paint.Align.CENTER; paint.typeface = Typeface.DEFAULT_BOLD; paint.textSize = w * 0.17f
            canvas.drawText(String.format(Locale.getDefault(), "%.1f %%", progress), w / 2, h / 2 + paint.textSize / 3, paint)
            paint.style = Paint.Style.STROKE
        }
    }
}
