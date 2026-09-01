package digital.madeiq.dream

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

class MainActivity : AppCompatActivity() {
    private lateinit var root: LinearLayout
    private val prefs by lazy { getSharedPreferences("dream", Context.MODE_PRIVATE) }

    private val bg = Color.rgb(5, 12, 22)
    private val surface = Color.rgb(12, 23, 37)
    private val surface2 = Color.rgb(18, 31, 48)
    private val line = Color.rgb(47, 59, 75)
    private val gold = Color.rgb(224, 174, 82)
    private val gold2 = Color.rgb(248, 202, 116)
    private val cream = Color.rgb(247, 242, 232)
    private val muted = Color.rgb(156, 164, 176)
    private val green = Color.rgb(113, 201, 145)

    private val languages = linkedMapOf(
        "sk" to "Slovenčina", "en" to "English", "de" to "Deutsch", "cs" to "Čeština",
        "pl" to "Polski", "hu" to "Magyar", "fr" to "Français", "es" to "Español",
        "it" to "Italiano", "pt" to "Português"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Let Android resize the usable app area when the keyboard opens.  Edge-to-edge
        // plus a system-bars-only inset listener caused the last form fields to remain
        // behind the IME on Xiaomi devices.
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        setContentView(root)
        showStart()
    }

    private fun showStart() {
        if ((prefs.getString("goal_name", "") ?: "").isBlank()) setupScreen() else dashboardScreen()
    }

    private fun clear() = root.removeAllViews()
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun lang() = prefs.getString("lang", "sk") ?: "sk"
    private fun tr(sk: String, en: String): String = if (lang() == "sk" || lang() == "cs") sk else en
    private fun locale(): Locale = when (lang()) {
        "de" -> Locale.GERMANY; "cs" -> Locale("cs", "CZ"); "pl" -> Locale("pl", "PL")
        "hu" -> Locale("hu", "HU"); "fr" -> Locale.FRANCE; "es" -> Locale("es", "ES")
        "it" -> Locale.ITALY; "pt" -> Locale("pt", "PT"); "en" -> Locale.UK
        else -> Locale("sk", "SK")
    }
    private fun money(v: Double): String = NumberFormat.getCurrencyInstance(locale()).format(v)

    private fun txt(s: String, size: Float = 15f, color: Int = cream, bold: Boolean = false) = TextView(this).apply {
        text = s
        textSize = size
        setTextColor(color)
        includeFontPadding = false
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        if (size >= 20f) letterSpacing = -0.015f
    }

    private fun solid(color: Int, radius: Int = 18, stroke: Int? = null, strokeWidth: Int = 1) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (stroke != null) setStroke(dp(strokeWidth), stroke)
    }

    private fun gradient(colors: IntArray, radius: Int = 18, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT) =
        GradientDrawable(orientation, colors).apply { cornerRadius = dp(radius).toFloat() }

    private fun card(radius: Int = 18) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(15), dp(16), dp(15))
        background = gradient(intArrayOf(Color.rgb(22, 37, 56), Color.rgb(10, 21, 35)), radius, GradientDrawable.Orientation.TL_BR).apply {
            setStroke(dp(1), Color.rgb(43, 57, 75))
        }
        elevation = dp(4).toFloat()
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(dp(16), dp(6), dp(16), dp(6)) }
    }

    private fun goldButton(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 14f
        setTextColor(Color.rgb(37, 29, 17))
        typeface = Typeface.DEFAULT_BOLD
        stateListAnimator = null
        minHeight = 0
        minimumHeight = 0
        background = gradient(intArrayOf(gold2, gold), 12)
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(-1, dp(52)).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
    }

    private fun outlineButton(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 14f
        setTextColor(cream)
        typeface = Typeface.DEFAULT_BOLD
        stateListAnimator = null
        minHeight = 0
        minimumHeight = 0
        background = solid(Color.TRANSPARENT, 12, Color.rgb(80, 89, 101))
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(-1, dp(52)).apply { setMargins(dp(16), dp(6), dp(16), dp(6)) }
    }

    private fun input(hintText: String, numeric: Boolean = false, scroll: ScrollView? = null) = EditText(this).apply {
        hint = hintText
        setHintTextColor(Color.rgb(133, 143, 156))
        setTextColor(cream)
        textSize = 15f
        setSingleLine(true)
        background = gradient(intArrayOf(Color.rgb(20, 35, 54), Color.rgb(13, 26, 42)), 14).apply {
            setStroke(dp(1), Color.rgb(68, 83, 103))
        }
        setPadding(dp(16), 0, dp(16), 0)
        if (numeric) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        imeOptions = EditorInfo.IME_ACTION_NEXT
        layoutParams = LinearLayout.LayoutParams(-1, dp(56)).apply { setMargins(dp(16), dp(6), dp(16), dp(6)) }
        setOnFocusChangeListener { v, focused ->
            if (focused && scroll != null) {
                scroll.postDelayed({
                    val target = Rect(0, 0, v.width, v.height + dp(180))
                    v.requestRectangleOnScreen(target, true)
                    scroll.smoothScrollBy(0, dp(72))
                }, 220)
            }
        }
    }

    private fun topBar(title: String, back: Boolean = false, showLanguage: Boolean = false, onEdit: (() -> Unit)? = null) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(16), dp(8), dp(16), dp(8))
        val left = txt(if (back) "‹" else "", if (back) 36f else 20f, cream, true).apply {
            gravity = Gravity.CENTER
            if (back) setOnClickListener { dashboardScreen() }
        }
        addView(left, LinearLayout.LayoutParams(dp(34), dp(44)))
        addView(txt(title, 22f, cream, true), LinearLayout.LayoutParams(0, -2, 1f))
        if (onEdit != null) addView(txt("✎", 20f, gold, true).apply {
            gravity = Gravity.CENTER; setPadding(dp(9), dp(8), dp(9), dp(8)); setOnClickListener { onEdit() }
        })
        if (showLanguage) addView(txt("◎", 20f, gold, true).apply {
            gravity = Gravity.CENTER; setPadding(dp(9), dp(8), dp(9), dp(8)); setOnClickListener { languageDialog { showStart() } }
        })
    }

    private fun setupScreen() {
        clear()
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            overScrollMode = View.OVER_SCROLL_NEVER
            // Permanent breathing room means the last input and CTA can always be
            // scrolled above the keyboard, even with a tall numeric keyboard.
            setPadding(0, 0, 0, dp(128))
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(36)) }
        scroll.addView(col)
        col.addView(topBar("DREAM", showLanguage = true))

        val hero = FrameLayout(this).apply {
            background = solid(surface, 22)
            clipToOutline = true
            layoutParams = LinearLayout.LayoutParams(-1, dp(238)).apply { setMargins(dp(16), dp(4), dp(16), dp(16)) }
        }
        hero.addView(DreamScenicView(this), FrameLayout.LayoutParams(-1, -1))
        hero.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            setPadding(dp(24), dp(20), dp(24), dp(30))
            addView(CloudLogoView(this@MainActivity), LinearLayout.LayoutParams(dp(82), dp(52)))
            addView(txt("D R E A M", 26f, gold2, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
            addView(txt(tr("TVOR SI SEN. MY POMÔŽEME S CESTOU.", "BUILD YOUR DREAM. WE HELP WITH THE PATH."), 13f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(12), 0, 0) })
        }, FrameLayout.LayoutParams(-1, -1))
        col.addView(hero)

        col.addView(txt(tr("Vytvor svoj DREAM", "Create your DREAM"), 23f, cream, true).apply { setPadding(dp(16), dp(2), 0, dp(4)) })
        col.addView(txt(tr("Jeden jasný cieľ. Každý deň o krok bližšie.", "One clear goal. One step closer every day."), 14f, muted).apply { setPadding(dp(16), 0, dp(16), dp(12)) })
        val goal = input(tr("Názov cieľa", "Goal name"), false, scroll)
        val target = input(tr("Cieľová suma", "Target amount"), true, scroll)
        val current = input(tr("Počiatočná suma", "Starting amount"), true, scroll)
        val income = input(tr("Mesačný príjem", "Monthly income"), true, scroll)
        val expenses = input(tr("Mesačné výdavky", "Monthly expenses"), true, scroll)
        listOf(goal, target, current, income, expenses).forEach { col.addView(it) }

        var picked = 0L
        lateinit var dateBtn: Button
        dateBtn = outlineButton(tr("Vybrať dátum cieľa", "Choose target date")) {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val cc = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59); set(Calendar.MILLISECOND, 0) }
                picked = cc.timeInMillis
                dateBtn.text = SimpleDateFormat("d. M. yyyy", locale()).format(Date(picked))
                scroll.postDelayed({ scroll.fullScroll(View.FOCUS_DOWN) }, 100)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
        col.addView(dateBtn)
        col.addView(goldButton(tr("VYTVORIŤ DREAM", "CREATE DREAM")) {
            val n = goal.text.toString().trim()
            val t = target.text.toString().replace(',', '.').toDoubleOrNull()
            val h = current.text.toString().replace(',', '.').toDoubleOrNull()
            val inc = income.text.toString().replace(',', '.').toDoubleOrNull()
            val exp = expenses.text.toString().replace(',', '.').toDoubleOrNull()
            if (n.isBlank() || t == null || h == null || inc == null || exp == null || picked == 0L || t <= 0 || h < 0 || inc < 0 || exp < 0) {
                toast(tr("Doplň všetky údaje.", "Complete all fields.")); return@goldButton
            }
            prefs.edit().putString("goal_name", n).putFloat("target", t.toFloat()).putFloat("saved", h.toFloat())
                .putFloat("income", inc.toFloat()).putFloat("expenses", exp.toFloat()).putLong("target_date", picked).apply()
            dashboardScreen()
        })
        root.addView(scroll, LinearLayout.LayoutParams(-1, -1))
    }

    private fun calc(): GoalData {
        val target = prefs.getFloat("target", 0f).toDouble()
        val saved = prefs.getFloat("saved", 0f).toDouble()
        val date = prefs.getLong("target_date", System.currentTimeMillis() + 86400000L)
        val remaining = max(0.0, target - saved)
        val days = max(1L, ceil((date - System.currentTimeMillis()) / 86400000.0).toLong())
        val daily = if (remaining <= 0) 0.0 else ceil((remaining / days) * 100.0) / 100.0
        val pct = if (target > 0) (saved / target * 100.0).coerceIn(0.0, 100.0) else 0.0
        return GoalData(target, saved, date, remaining, days, daily, pct)
    }

    private fun dashboardScreen() {
        clear()
        val d = calc()
        val name = prefs.getString("goal_name", "DREAM") ?: "DREAM"
        val scroll = ScrollView(this).apply { isFillViewport = true; overScrollMode = View.OVER_SCROLL_NEVER }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(24)) }
        scroll.addView(col)
        col.addView(topBar(tr("Prehľad", "Overview"), showLanguage = true, onEdit = { editGoal() }))

        col.addView(card(18).apply {
            addView(txt(tr("CELKOVO ODLOŽENÉ", "TOTAL SAVED"), 11f, muted, true))
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                addView(txt(money(d.saved), 29f, cream, true), LinearLayout.LayoutParams(0, -2, 1f))
                addView(txt("${d.pct.toInt()}%", 13f, gold2, true).apply { background = solid(Color.rgb(62, 52, 35), 14); setPadding(dp(10), dp(6), dp(10), dp(6)) })
            })
            addView(txt(tr("Tvoj pokrok rastie", "Your progress is growing"), 13f, muted).apply { setPadding(0, dp(7), 0, dp(6)) })
            addView(TrendView(this@MainActivity), LinearLayout.LayoutParams(-1, dp(72)))
        })

        col.addView(txt(tr("TVOJ CIEĽ", "YOUR GOAL"), 12f, cream, true).apply { setPadding(dp(16), dp(12), 0, dp(3)) })
        col.addView(goalRow(name, d))
        col.addView(goldButton(tr("+  PRIDAŤ VKLAD", "+  ADD SAVING")) { addStepDialog() })
        col.addView(outlineButton(tr("CHCEM NIEČO KÚPIŤ", "I WANT TO BUY")) { purchaseScreen() })
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav(0))
    }

    private fun goalRow(name: String, d: GoalData) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(13), dp(12), dp(13), dp(12))
        background = solid(surface2, 16, line)
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(dp(16), dp(4), dp(16), dp(8)) }
        addView(GoalThumbView(this@MainActivity), LinearLayout.LayoutParams(dp(60), dp(60)))
        addView(LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, 0, 0)
            addView(txt(name, 16f, cream, true))
            addView(txt("${money(d.saved)} / ${money(d.target)}", 12f, muted).apply { setPadding(0, dp(5), 0, dp(6)) })
            addView(ProgressBar(this@MainActivity, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 1000; progress = (d.pct * 10).toInt(); progressTintList = android.content.res.ColorStateList.valueOf(gold)
                progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(45, 53, 64))
            }, LinearLayout.LayoutParams(-1, dp(5)))
        }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(txt("${d.pct.toInt()}%", 13f, cream, true).apply { setPadding(dp(10), 0, 0, 0) })
        setOnClickListener { dailyScreen() }
    }

    private fun dailyScreen() {
        clear()
        val d = calc()
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(26)) }
        col.addView(topBar(tr("Koľko treba denne odložiť?", "How much to save daily?"), back = true))
        col.addView(card(22).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            addView(CircularProgressView(this@MainActivity, d.pct.toFloat(), gold, Color.rgb(48, 55, 65), money(d.daily)), LinearLayout.LayoutParams(dp(220), dp(220)).apply { gravity = Gravity.CENTER_HORIZONTAL })
        })
        col.addView(card().apply {
            addMetricLine(tr("Cieľová suma", "Target amount"), money(d.target))
            addMetricLine(tr("Aktuálne uložené", "Saved now"), money(d.saved))
            addMetricLine(tr("Zostáva", "Remaining"), money(d.remaining))
            addMetricLine(tr("Dátum cieľa", "Target date"), SimpleDateFormat("d. M. yyyy", locale()).format(Date(d.date)))
            addMetricLine(tr("Zostáva dní", "Days left"), "${d.days}")
        })
        col.addView(card().apply {
            addView(txt(tr("Ak budeš odkladať ${money(d.daily)} denne, svoj cieľ dosiahneš včas.", "Save ${money(d.daily)} daily and you will reach your goal on time."), 14f, cream, true).apply { gravity = Gravity.CENTER; textAlignment = View.TEXT_ALIGNMENT_CENTER })
        })
        root.addView(ScrollView(this).apply { addView(col) }, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav(2))
    }

    private fun LinearLayout.addMetricLine(label: String, value: String) {
        addView(LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(6), 0, dp(6))
            addView(txt(label, 13f, muted), LinearLayout.LayoutParams(0, -2, 1f))
            addView(txt(value, 14f, gold2, true))
        })
    }

    private fun purchaseScreen() {
        clear()
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(80)) }
        scroll.addView(col)
        col.addView(topBar(tr("Nákup vs. sen", "Purchase vs dream"), back = true))
        col.addView(card().apply {
            addView(txt("DREAM CHECK", 11f, gold, true))
            addView(txt(tr("Každý nákup má dopad na tvoj cieľ.", "Every purchase affects your goal."), 18f, cream, true).apply { setPadding(0, dp(8), 0, 0) })
        })
        val item = input(tr("Čo chceš kúpiť?", "What do you want to buy?"), false, scroll)
        val price = input(tr("Cena", "Price"), true, scroll)
        col.addView(item); col.addView(price)
        col.addView(goldButton(tr("UKÁZAŤ DOPAD", "SHOW IMPACT")) {
            val p = price.text.toString().replace(',', '.').toDoubleOrNull()
            if (item.text.toString().isBlank() || p == null || p <= 0) { toast(tr("Zadaj názov a cenu.", "Enter item and price.")); return@goldButton }
            impactScreen(item.text.toString().trim(), p)
        })
        root.addView(scroll, LinearLayout.LayoutParams(-1, -1))
    }

    private fun impactScreen(item: String, price: Double) {
        clear()
        val d = calc()
        val delay = if (d.daily > 0) ceil(price / d.daily).toInt() else 0
        val pct = if (d.target > 0) (price / d.target * 100).coerceAtLeast(0.0) else 0.0
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(28)) }
        col.addView(topBar(tr("Dopad na tvoj sen", "Impact on your dream"), back = true))
        col.addView(card(22).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            addView(txt(item, 18f, cream, true).apply { gravity = Gravity.CENTER })
            addView(txt(money(price), 32f, gold2, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(10), 0, dp(5)) })
            addView(txt(String.format(locale(), "%.1f %%", pct), 28f, cream, true).apply { gravity = Gravity.CENTER })
            addView(txt(tr("približne $delay dní navyše", "about $delay extra days"), 14f, muted).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
        })
        col.addView(goldButton(tr("RADŠEJ MÔJ SEN", "CHOOSE MY DREAM")) {
            saveHistory(item, price, "dream"); prefs.edit().putFloat("saved", (d.saved + price).toFloat()).apply(); dashboardScreen()
        })
        col.addView(outlineButton(tr("POČKÁM 24 HODÍN", "WAIT 24 HOURS")) { saveHistory(item, price, "wait"); dashboardScreen() })
        col.addView(outlineButton(tr("KÚPIM TO", "BUY IT")) { saveHistory(item, price, "bought"); dashboardScreen() })
        root.addView(ScrollView(this).apply { addView(col) }, LinearLayout.LayoutParams(-1, -1))
    }

    private fun addStepDialog() {
        val input = EditText(this).apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; hint = "0.00" }
        AlertDialog.Builder(this).setTitle(tr("Pridať vklad", "Add saving")).setView(input)
            .setPositiveButton(tr("PRIDAŤ", "ADD")) { _, _ ->
                val v = input.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
                if (v > 0) {
                    val old = prefs.getFloat("saved", 0f).toDouble(); prefs.edit().putFloat("saved", (old + v).toFloat()).apply()
                    saveHistory(tr("Vklad", "Saving"), v, "saved"); dashboardScreen()
                }
            }.setNegativeButton(tr("ZRUŠIŤ", "CANCEL"), null).show()
    }

    private fun historyScreen() {
        clear()
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(24)) }
        col.addView(topBar(tr("História", "History"), showLanguage = true))
        val entries = prefs.getString("history", "")?.lines()?.filter { it.isNotBlank() } ?: emptyList()
        if (entries.isEmpty()) col.addView(card().apply { addView(txt(tr("Zatiaľ tu nič nie je.", "Nothing here yet."), 14f, muted)) })
        else entries.takeLast(30).reversed().forEach { raw ->
            val p = raw.split("|")
            if (p.size >= 4) col.addView(card(14).apply {
                addView(txt(p[1], 15f, cream, true)); addView(txt("${p[2]}  •  ${p[3]}", 12f, muted).apply { setPadding(0, dp(5), 0, 0) })
            })
        }
        root.addView(ScrollView(this).apply { addView(col) }, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav(1))
    }

    private fun settingsScreen() {
        clear()
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(24)) }
        col.addView(topBar(tr("Profil a nastavenia", "Profile & settings"), showLanguage = true))
        col.addView(card().apply {
            addView(txt(tr("Jazyk aplikácie", "App language"), 16f, cream, true))
            addView(txt(languages[lang()] ?: "Slovenčina", 13f, gold, true).apply { setPadding(0, dp(7), 0, 0); setOnClickListener { languageDialog { settingsScreen() } } })
        })
        col.addView(outlineButton(tr("UPRAVIŤ CIEĽ", "EDIT GOAL")) { editGoal() })
        col.addView(outlineButton(tr("VYMAZAŤ ÚDAJE", "RESET DATA")) {
            AlertDialog.Builder(this).setTitle(tr("Vymazať všetko?", "Reset everything?"))
                .setMessage(tr("Tento krok sa nedá vrátiť späť.", "This cannot be undone."))
                .setPositiveButton(tr("VYMAZAŤ", "RESET")) { _, _ -> prefs.edit().clear().apply(); setupScreen() }
                .setNegativeButton(tr("ZRUŠIŤ", "CANCEL"), null).show()
        })
        root.addView(ScrollView(this).apply { addView(col) }, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav(3))
    }

    private fun bottomNav(active: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER
        setPadding(dp(6), dp(5), dp(6), dp(5)); background = solid(Color.rgb(8, 18, 30), 0, Color.rgb(34, 45, 59))
        val labels = arrayOf(tr("⌂\nPrehľad", "⌂\nOverview"), tr("◷\nHistória", "◷\nHistory"), tr("▣\nDenná suma", "▣\nDaily"), tr("♙\nProfil", "♙\nProfile"))
        labels.forEachIndexed { i, label ->
            addView(txt(label, 10f, if (i == active) gold2 else muted, i == active).apply {
                gravity = Gravity.CENTER; setOnClickListener { when (i) { 0 -> dashboardScreen(); 1 -> historyScreen(); 2 -> dailyScreen(); 3 -> settingsScreen() } }
            }, LinearLayout.LayoutParams(0, dp(52), 1f))
        }
    }

    private fun languageDialog(after: () -> Unit) {
        val codes = languages.keys.toList(); val values = languages.values.toTypedArray(); val checked = codes.indexOf(lang()).coerceAtLeast(0)
        AlertDialog.Builder(this).setTitle("Language").setSingleChoiceItems(values, checked) { dialog, which ->
            prefs.edit().putString("lang", codes[which]).apply(); dialog.dismiss(); after()
        }.setNegativeButton(tr("ZRUŠIŤ", "CANCEL"), null).show()
    }

    private fun editGoal() { prefs.edit().remove("goal_name").apply(); setupScreen() }
    private fun saveHistory(item: String, price: Double, type: String) {
        val date = SimpleDateFormat("dd.MM.yyyy HH:mm", locale()).format(Date())
        val old = prefs.getString("history", "") ?: ""; val line = "$date|$item|${money(price)}|$type"
        prefs.edit().putString("history", if (old.isBlank()) line else "$old\n$line").apply()
    }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    data class GoalData(val target: Double, val saved: Double, val date: Long, val remaining: Double, val days: Long, val daily: Double, val pct: Double)

    class CloudLogoView(context: Context) : View(context) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 6f; strokeCap = Paint.Cap.ROUND; color = Color.rgb(224,174,82) }
        override fun onDraw(c: Canvas) {
            val w = width.toFloat(); val h = height.toFloat(); val path = Path()
            path.moveTo(w*.18f,h*.72f); path.cubicTo(w*.04f,h*.72f,w*.03f,h*.48f,w*.21f,h*.45f)
            path.cubicTo(w*.25f,h*.20f,w*.55f,h*.12f,w*.68f,h*.35f); path.cubicTo(w*.91f,h*.31f,w*.97f,h*.68f,w*.78f,h*.72f); path.close(); c.drawPath(path,p)
        }
    }

    class DreamScenicView(context: Context) : View(context) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(c: Canvas) {
            val w = width.toFloat(); val h = height.toFloat()
            p.shader = LinearGradient(0f,0f,0f,h,intArrayOf(Color.rgb(8,18,32),Color.rgb(14,31,50),Color.rgb(57,58,62),Color.rgb(9,17,29)),null,Shader.TileMode.CLAMP); c.drawRect(0f,0f,w,h,p); p.shader=null
            p.color = Color.argb(210,245,183,85); c.drawCircle(w*.52f,h*.72f,w*.07f,p)
            p.shader = RadialGradient(w*.52f,h*.72f,w*.32f,Color.argb(150,244,177,70),Color.TRANSPARENT,Shader.TileMode.CLAMP); c.drawCircle(w*.52f,h*.72f,w*.32f,p); p.shader=null
            fun cloud(cx:Float, cy:Float, s:Float, a:Int){ p.color=Color.argb(a,96,103,108); c.drawCircle(cx,cy,s,p); c.drawCircle(cx+s*.8f,cy+s*.12f,s*.75f,p); c.drawCircle(cx-s*.75f,cy+s*.16f,s*.65f,p); c.drawRect(cx-s*1.4f,cy, cx+s*1.55f,cy+s*.8f,p)}
            cloud(w*.24f,h*.66f,w*.12f,185); cloud(w*.78f,h*.62f,w*.14f,175); cloud(w*.52f,h*.80f,w*.16f,210)
        }
    }

    class GoalThumbView(context: Context) : View(context) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(c: Canvas) {
            val w=width.toFloat(); val h=height.toFloat(); p.shader=LinearGradient(0f,0f,w,h,Color.rgb(27,53,73),Color.rgb(66,85,92),Shader.TileMode.CLAMP); c.drawRoundRect(0f,0f,w,h,14f,14f,p); p.shader=null
            p.color=Color.rgb(232,193,110); c.drawCircle(w*.72f,h*.28f,w*.10f,p); p.color=Color.rgb(17,37,54); val path=Path(); path.moveTo(0f,h*.78f); path.lineTo(w*.37f,h*.43f); path.lineTo(w*.58f,h*.64f); path.lineTo(w,h*.35f); path.lineTo(w,h); path.lineTo(0f,h); path.close(); c.drawPath(path,p)
        }
    }

    class TrendView(context: Context) : View(context) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 3.2f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND; color = Color.rgb(224,174,82) }
        override fun onDraw(c: Canvas) {
            val w=width.toFloat(); val h=height.toFloat(); val ys=floatArrayOf(.78f,.68f,.75f,.66f,.73f,.63f,.58f,.46f,.51f,.34f,.39f,.16f)
            val path=Path(); ys.forEachIndexed { i,y -> val x=w*i/(ys.size-1); val yy=h*y; if(i==0) path.moveTo(x,yy) else path.lineTo(x,yy) }; c.drawPath(path,p)
            p.style=Paint.Style.FILL; ys.forEachIndexed { i,y -> if(i%3==0 || i==ys.lastIndex) c.drawCircle(w*i/(ys.size-1),h*y,4.5f,p) }; p.style=Paint.Style.STROKE
        }
    }

    class CircularProgressView(context: Context, private val progress: Float, private val active: Int, private val track: Int, private val centerText: String) : View(context) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
        override fun onDraw(c: Canvas) {
            val w=width.toFloat(); val h=height.toFloat(); val stroke=w*.045f; p.strokeWidth=stroke; val r=RectF(stroke,stroke,w-stroke,h-stroke)
            p.color=track; c.drawArc(r,-215f,250f,false,p); p.color=active; c.drawArc(r,-215f,250f*(progress.coerceIn(0f,100f)/100f),false,p)
            p.style=Paint.Style.FILL; p.textAlign=Paint.Align.CENTER; p.typeface=Typeface.DEFAULT_BOLD; p.color=Color.rgb(247,242,232); p.textSize=w*.16f; c.drawText(centerText,w/2,h*.54f,p)
            p.typeface=Typeface.DEFAULT; p.color=Color.rgb(156,164,176); p.textSize=w*.052f; c.drawText("DENNE TREBA ODLOŽIŤ",w/2,h*.38f,p); p.style=Paint.Style.STROKE
        }
    }
}
