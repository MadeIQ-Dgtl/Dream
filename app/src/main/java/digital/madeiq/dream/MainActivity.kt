package digital.madeiq.dream

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
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

    private val bg = Color.rgb(2, 10, 20)
    private val navy = Color.rgb(9, 28, 49)
    private val navy2 = Color.rgb(19, 50, 80)
    private val navy3 = Color.rgb(31, 65, 101)
    private val gold = Color.rgb(255, 184, 79)
    private val cream = Color.rgb(255, 248, 232)
    private val muted = Color.rgb(151, 170, 193)
    private val green = Color.rgb(89, 211, 158)
    private val pink = Color.rgb(244, 76, 137)
    private val purple = Color.rgb(111, 76, 190)

    private val languages = linkedMapOf(
        "sk" to "Slovenčina",
        "en" to "English",
        "de" to "Deutsch",
        "cs" to "Čeština",
        "pl" to "Polski",
        "hu" to "Magyar",
        "fr" to "Français",
        "es" to "Español",
        "it" to "Italiano",
        "pt" to "Português"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        setContentView(root)
        ViewCompat.requestApplyInsets(root)
        showStart()
    }

    private fun showStart() {
        if ((prefs.getString("goal_name", "") ?: "").isBlank()) setupScreen() else dashboardScreen()
    }

    private fun clear() = root.removeAllViews()
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun lang() = prefs.getString("lang", "sk") ?: "sk"

    private fun tr(sk: String, en: String, de: String = en, cs: String = sk, pl: String = en, hu: String = en): String = when (lang()) {
        "de" -> de
        "cs" -> cs
        "pl" -> pl
        "hu" -> hu
        "en", "fr", "es", "it", "pt" -> en
        else -> sk
    }

    private fun locale(): Locale = when (lang()) {
        "de" -> Locale.GERMANY
        "cs" -> Locale("cs", "CZ")
        "pl" -> Locale("pl", "PL")
        "hu" -> Locale("hu", "HU")
        "fr" -> Locale.FRANCE
        "es" -> Locale("es", "ES")
        "it" -> Locale.ITALY
        "pt" -> Locale("pt", "PT")
        "en" -> Locale.UK
        else -> Locale("sk", "SK")
    }

    private fun money(v: Double): String = NumberFormat.getCurrencyInstance(locale()).format(v)

    private fun txt(s: String, size: Float = 16f, color: Int = cream, bold: Boolean = false) = TextView(this).apply {
        text = s
        textSize = size
        setTextColor(color)
        includeFontPadding = false
        letterSpacing = if (size >= 22f) -0.015f else 0f
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun solid(color: Int, radius: Int = 24, stroke: Int? = null, strokeWidth: Int = 1) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (stroke != null) setStroke(dp(strokeWidth), stroke)
    }

    private fun gradient(colors: IntArray, radius: Int = 28, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT) =
        GradientDrawable(orientation, colors).apply { cornerRadius = dp(radius).toFloat() }

    private fun premiumCard(radius: Int = 28) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(19), dp(20), dp(19))
        background = gradient(intArrayOf(Color.rgb(22, 56, 90), Color.rgb(8, 25, 45)), radius, GradientDrawable.Orientation.TL_BR)
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
        background = gradient(intArrayOf(Color.rgb(255, 208, 121), Color.rgb(251, 179, 78)), 30)
        setPadding(dp(18), dp(13), dp(18), dp(13))
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(-1, dp(58)).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
    }

    private fun accentButton(label: String, onClick: () -> Unit) = button(label, onClick).apply {
        setTextColor(Color.WHITE)
        background = gradient(intArrayOf(Color.rgb(255, 173, 72), Color.rgb(255, 115, 98), pink), 30)
    }

    private fun input(hintText: String, numeric: Boolean = false, scroll: ScrollView? = null) = EditText(this).apply {
        hint = hintText
        setHintTextColor(Color.rgb(157, 174, 197))
        setTextColor(cream)
        textSize = 16f
        setSingleLine(true)
        background = solid(Color.rgb(21, 49, 78), 23, Color.rgb(173, 126, 68), 1)
        setPadding(dp(18), 0, dp(18), 0)
        if (numeric) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        imeOptions = EditorInfo.IME_ACTION_NEXT
        layoutParams = LinearLayout.LayoutParams(-1, dp(62)).apply { setMargins(dp(16), dp(6), dp(16), dp(6)) }
        setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && scroll != null) {
                scroll.postDelayed({
                    val rect = android.graphics.Rect(0, 0, v.width, v.height + dp(120))
                    v.requestRectangleOnScreen(rect, true)
                }, 180)
            }
        }
    }

    private fun topBar(title: String, back: Boolean = false, showLanguage: Boolean = false, editAction: (() -> Unit)? = null) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(16), dp(8), dp(16), dp(8))

        val mark = txt(if (back) "‹" else "D", if (back) 38f else 27f, cream, true).apply {
            gravity = Gravity.CENTER
            if (!back) background = gradient(intArrayOf(Color.rgb(255, 186, 77), Color.rgb(239, 95, 119), purple), 22, GradientDrawable.Orientation.TL_BR)
            setOnClickListener { if (back) dashboardScreen() }
        }
        addView(mark, LinearLayout.LayoutParams(dp(44), dp(44)))
        addView(txt(title, 23f, cream, true).apply { setPadding(dp(11), 0, 0, 0) }, LinearLayout.LayoutParams(0, -2, 1f))

        if (editAction != null) {
            addView(txt(tr("Upraviť", "Edit", "Bearbeiten", "Upravit", "Edytuj", "Szerkesztés"), 12f, gold, true).apply {
                gravity = Gravity.CENTER
                setPadding(dp(10), dp(8), dp(10), dp(8))
                setOnClickListener { editAction() }
            })
        }
        if (showLanguage) {
            addView(txt("🌐 ${lang().uppercase(Locale.ROOT)}", 13f, gold, true).apply {
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(8), dp(12), dp(8))
                background = solid(Color.rgb(12, 34, 57), 18, Color.rgb(63, 83, 108))
                setOnClickListener { languageDialog { showStart() } }
            })
        }
    }

    private fun setupScreen() {
        clear()
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(4), 0, dp(24))
        }
        scroll.addView(col)

        col.addView(topBar("DREAM", showLanguage = true))
        col.addView(FrameLayout(this).apply {
            background = solid(navy, 32)
            clipToOutline = true
            layoutParams = LinearLayout.LayoutParams(-1, dp(226)).apply { setMargins(dp(16), dp(7), dp(16), dp(13)) }
            addView(DreamScenicView(this@MainActivity), FrameLayout.LayoutParams(-1, -1))
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM
                setPadding(dp(24), dp(24), dp(24), dp(24))
                addView(txt(tr("Vytvor svoj DREAM", "Build your DREAM"), 34f, cream, true))
                addView(txt(tr("Sen. Plán. Každý deň bližšie.", "Dream. Plan. Closer every day."), 15f, Color.rgb(214, 222, 235)).apply { setPadding(0, dp(8), 0, 0) })
                addView(txt("FOCUS  •  PLAN  •  ACHIEVE", 12f, gold, true).apply { setPadding(0, dp(16), 0, 0) })
            }, FrameLayout.LayoutParams(-1, -1))
        })

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
                val cc = Calendar.getInstance().apply {
                    set(y, m, d, 23, 59, 59)
                    set(Calendar.MILLISECOND, 0)
                }
                picked = cc.timeInMillis
                dateBtn.text = "📅  " + SimpleDateFormat("d. M. yyyy", locale()).format(Date(picked))
                scroll.postDelayed({ scroll.fullScroll(View.FOCUS_DOWN) }, 120)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
        col.addView(dateBtn)

        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val stickyAction = accentButton(tr("VYTVORIŤ MÔJ PLÁN", "CREATE MY PLAN")) {
            val n = goal.text.toString().trim()
            val t = target.text.toString().replace(',', '.').toDoubleOrNull()
            val h = current.text.toString().replace(',', '.').toDoubleOrNull()
            val inc = income.text.toString().replace(',', '.').toDoubleOrNull()
            val exp = expenses.text.toString().replace(',', '.').toDoubleOrNull()
            if (n.isBlank() || t == null || h == null || inc == null || exp == null || picked == 0L || t <= 0 || inc < 0 || exp < 0) {
                toast(tr("Doplň všetky údaje.", "Complete all fields."))
                return@accentButton
            }
            prefs.edit()
                .putString("goal_name", n)
                .putFloat("target", t.toFloat())
                .putFloat("saved", h.toFloat())
                .putFloat("income", inc.toFloat())
                .putFloat("expenses", exp.toFloat())
                .putLong("target_date", picked)
                .apply()
            dashboardScreen()
        }.apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(62)).apply { setMargins(dp(16), dp(8), dp(16), dp(10)) }
        }
        root.addView(stickyAction)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, if (ime.bottom > 0) dp(4) else bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun dashboardScreen() {
        clear()
        val scroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false; overScrollMode = View.OVER_SCROLL_NEVER }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(2), 0, dp(24)) }
        scroll.addView(col)

        val name = prefs.getString("goal_name", "DREAM") ?: "DREAM"
        val target = prefs.getFloat("target", 0f).toDouble()
        val saved = prefs.getFloat("saved", 0f).toDouble()
        val targetDate = prefs.getLong("target_date", System.currentTimeMillis())
        val remaining = max(0.0, target - saved)
        val days = max(1L, ceil((targetDate - System.currentTimeMillis()) / 86400000.0).toLong())
        val dailyGoal = remaining / days
        val pct = if (target > 0) (saved / target * 100).coerceIn(0.0, 100.0) else 0.0

        col.addView(topBar(name, showLanguage = true, editAction = { editGoal() }))

        val hero = FrameLayout(this).apply {
            background = solid(navy, 34)
            clipToOutline = true
            elevation = dp(5).toFloat()
            layoutParams = LinearLayout.LayoutParams(-1, dp(355)).apply { setMargins(dp(16), dp(5), dp(16), dp(10)) }
        }
        hero.addView(DreamScenicView(this), FrameLayout.LayoutParams(-1, -1))
        hero.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
            addView(txt(name.uppercase(locale()), 23f, cream, true).apply { gravity = Gravity.CENTER })
            addView(txt("${money(saved)}  /  ${money(target)}", 27f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
            addView(Space(this@MainActivity), LinearLayout.LayoutParams(1, dp(8)))
            addView(CircularProgressView(this@MainActivity, pct.toFloat(), gold, Color.argb(95, 255, 255, 255)).apply {
                layoutParams = LinearLayout.LayoutParams(dp(178), dp(178))
            })
            addView(txt(tr("Do cieľa: $days dní", "$days days to goal"), 17f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
            addView(txt(tr("Približne ${money(dailyGoal)} denne k snu", "About ${money(dailyGoal)} per day toward your dream"), 13f, Color.rgb(213, 222, 234)).apply { gravity = Gravity.CENTER; setPadding(0, dp(4), 0, 0) })
        }, FrameLayout.LayoutParams(-1, -1))
        col.addView(hero)

        val metrics = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(dp(12), 0, dp(12), 0) }
        metrics.addView(metric(tr("Dnes potrebujem odložiť", "Need to save today"), money(dailyGoal), green), LinearLayout.LayoutParams(0, -2, 1f))
        metrics.addView(metric(tr("Progres", "Progress"), String.format(locale(), "%.1f %%", pct), gold), LinearLayout.LayoutParams(0, -2, 1f))
        col.addView(metrics)

        pendingCard(col)
        col.addView(accentButton(tr("CHCEM NIEČO KÚPIŤ", "I WANT TO BUY")) { purchaseScreen() })
        col.addView(button(tr("PRIDAŤ DNEŠNÝ KROK", "ADD TODAY'S STEP")) { addStepDialog() })
        col.addView(premiumCard(25).apply {
            addView(txt(tr("„Malé kroky každý deň tvoria veľké zmeny.“", "“Small steps every day create big change.”"), 17f, cream, true))
        })

        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav(0))
    }

    private fun metric(label: String, value: String, valueColor: Int = gold) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(15), dp(14), dp(15), dp(14))
        background = gradient(intArrayOf(Color.rgb(22, 55, 88), Color.rgb(8, 25, 45)), 22, GradientDrawable.Orientation.TL_BR)
        elevation = dp(2).toFloat()
        addView(txt(label, 12f, muted))
        addView(txt(value, 19f, valueColor, true).apply { setPadding(0, dp(5), 0, 0) })
        layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(dp(4), dp(4), dp(4), dp(4)) }
    }

    private fun bottomNav(active: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(dp(8), dp(6), dp(8), dp(6))
        background = solid(Color.rgb(6, 20, 36), 24, Color.rgb(34, 58, 84))
        elevation = dp(7).toFloat()
        layoutParams = LinearLayout.LayoutParams(-1, dp(68)).apply { setMargins(dp(10), dp(5), dp(10), dp(6)) }
        val labels = arrayOf(
            tr("⌂\nPrehľad", "⌂\nHome"),
            tr("◷\nHistória", "◷\nHistory"),
            "+",
            tr("▥\nŠtatistiky", "▥\nStats"),
            tr("⚙\nNastavenia", "⚙\nSettings")
        )
        labels.forEachIndexed { i, label ->
            addView(txt(label, if (i == 2) 26f else 10f, if (i == active || i == 2) cream else muted, i == active || i == 2).apply {
                gravity = Gravity.CENTER
                if (i == 2) background = gradient(intArrayOf(gold, Color.rgb(250, 120, 100), pink), 26)
                setOnClickListener {
                    when (i) {
                        0 -> dashboardScreen()
                        1 -> historyScreen()
                        2 -> purchaseScreen()
                        3 -> statsScreen()
                        4 -> settingsScreen()
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(50), 1f).apply { setMargins(dp(3), 0, dp(3), 0) })
        }
    }

    private fun purchaseScreen() {
        clear()
        val scroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(6), 0, dp(120)) }
        scroll.addView(col)
        col.addView(topBar(tr("Chcem niečo kúpiť", "I want to buy"), back = true))
        col.addView(premiumCard(30).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            addView(txt("DREAM CHECK", 13f, gold, true).apply { gravity = Gravity.CENTER })
            addView(txt(tr("Každý nákup má dopad na tvoj sen.", "Every purchase affects your dream."), 18f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
        })
        val item = input(tr("Čo chceš kúpiť?", "What do you want to buy?"), false, scroll)
        val price = input(tr("Cena", "Price"), true, scroll)
        col.addView(item)
        col.addView(price)
        col.addView(accentButton(tr("UKÁZAŤ DOPAD", "SHOW IMPACT")) {
            val p = price.text.toString().replace(',', '.').toDoubleOrNull()
            if (item.text.toString().isBlank() || p == null || p <= 0) {
                toast(tr("Zadaj názov a cenu.", "Enter item and price."))
                return@accentButton
            }
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
        val pct = if (target > 0) (price / target * 100).coerceAtLeast(0.0) else 0.0

        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(6), 0, dp(40)) }
        col.addView(topBar(tr("Dopad na tvoj sen", "Impact on your dream"), back = true))
        col.addView(premiumCard(32).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            background = gradient(intArrayOf(Color.rgb(24, 53, 87), Color.rgb(75, 45, 82), Color.rgb(17, 33, 54)), 32, GradientDrawable.Orientation.TL_BR)
            addView(txt(item, 19f, cream, true).apply { gravity = Gravity.CENTER })
            addView(txt(money(price), 32f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(7), 0, dp(8)) })
            addView(txt(String.format(locale(), "%.1f %%", pct), 38f, pink, true).apply { gravity = Gravity.CENTER })
            addView(txt(tr("tvojho sna", "of your dream"), 13f, muted).apply { gravity = Gravity.CENTER })
            addView(txt(tr("Posun približne o $lostDays dní", "Delay of about $lostDays days"), 22f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(18), 0, 0) })
        })
        col.addView(accentButton(tr("KÚPIM TO", "BUY IT")) {
            saveHistory(item, price, "bought")
            prefs.edit().putFloat("saved", max(0.0, saved - price).toFloat()).apply()
            dashboardScreen()
        })
        col.addView(button(tr("POČKÁM 24 HODÍN", "WAIT 24 HOURS")) {
            prefs.edit().putString("pending_item", item).putFloat("pending_price", price.toFloat()).putLong("pending_until", System.currentTimeMillis() + 86400000L).apply()
            saveHistory(item, price, "wait")
            dashboardScreen()
        })
        col.addView(button(tr("RADŠEJ MÔJ SEN", "CHOOSE MY DREAM")) {
            saveHistory(item, price, "dream")
            prefs.edit().putFloat("saved", (saved + price).toFloat()).apply()
            dashboardScreen()
        })
        root.addView(ScrollView(this).apply { addView(col) }, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    private fun pendingCard(col: LinearLayout) {
        val until = prefs.getLong("pending_until", 0L)
        if (until <= 0L) return
        val item = prefs.getString("pending_item", "") ?: ""
        val price = prefs.getFloat("pending_price", 0f).toDouble()
        val remaining = until - System.currentTimeMillis()
        col.addView(premiumCard(24).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            addView(txt(tr("24-hodinová pauza", "24-hour pause"), 14f, gold, true).apply { gravity = Gravity.CENTER })
            addView(txt("$item  •  ${money(price)}", 18f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(6), 0, 0) })
            addView(txt(if (remaining <= 0) tr("Čas vypršal — rozhodni sa.", "Time is up — decide now.") else tr("Zostáva približne ${max(1, remaining / 3600000)} h", "About ${max(1, remaining / 3600000)} h left"), 13f, muted).apply { gravity = Gravity.CENTER; setPadding(0, dp(6), 0, 0) })
            if (remaining <= 0) setOnClickListener { finalDecision(item, price) }
        })
    }

    private fun finalDecision(item: String, price: Double) {
        AlertDialog.Builder(this)
            .setTitle(tr("Čo chceš viac?", "What do you want more?"))
            .setMessage("$item — ${money(price)}")
            .setPositiveButton(tr("RADŠEJ MÔJ SEN", "CHOOSE MY DREAM")) { _, _ ->
                val s = prefs.getFloat("saved", 0f).toDouble()
                prefs.edit().putFloat("saved", (s + price).toFloat()).remove("pending_item").remove("pending_price").remove("pending_until").apply()
                saveHistory(item, price, "dream_after_24h")
                dashboardScreen()
            }
            .setNegativeButton(tr("KÚPIM TO", "BUY IT")) { _, _ ->
                val s = prefs.getFloat("saved", 0f).toDouble()
                prefs.edit().putFloat("saved", max(0.0, s - price).toFloat()).remove("pending_item").remove("pending_price").remove("pending_until").apply()
                saveHistory(item, price, "bought_after_24h")
                dashboardScreen()
            }
            .show()
    }

    private fun addStepDialog() {
        val e = EditText(this).apply {
            hint = tr("Suma", "Amount")
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        AlertDialog.Builder(this)
            .setTitle(tr("Dnešný krok k snu", "Today's step"))
            .setView(e)
            .setPositiveButton(tr("ULOŽIŤ", "SAVE")) { _, _ ->
                val v = e.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
                if (v > 0) {
                    val s = prefs.getFloat("saved", 0f).toDouble()
                    prefs.edit().putFloat("saved", (s + v).toFloat()).apply()
                    saveHistory(tr("Dnešný krok", "Today's step"), v, "step")
                    dashboardScreen()
                }
            }
            .setNegativeButton(tr("ZRUŠIŤ", "CANCEL"), null)
            .show()
    }

    private fun historyScreen() {
        clear()
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(6), 0, dp(28)) }
        col.addView(topBar(tr("História", "History"), showLanguage = true))
        val entries = prefs.getString("history", "")?.lines()?.filter { it.isNotBlank() } ?: emptyList()
        if (entries.isEmpty()) col.addView(premiumCard().apply { addView(txt(tr("Zatiaľ tu nič nie je.", "Nothing here yet."), 16f, muted)) })
        else entries.takeLast(20).reversed().forEach { raw ->
            val p = raw.split("|")
            if (p.size >= 4) col.addView(premiumCard(22).apply {
                addView(txt(p[1], 16f, cream, true))
                addView(txt("${p[2]}  •  ${p[3]}", 13f, muted).apply { setPadding(0, dp(5), 0, 0) })
            })
        }
        root.addView(ScrollView(this).apply { addView(col) }, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav(1))
    }

    private fun statsScreen() {
        clear()
        val target = prefs.getFloat("target", 0f).toDouble()
        val saved = prefs.getFloat("saved", 0f).toDouble()
        val pct = if (target > 0) (saved / target * 100).coerceIn(0.0, 100.0) else 0.0
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(6), 0, dp(28)) }
        col.addView(topBar(tr("Štatistiky", "Statistics"), showLanguage = true))
        col.addView(premiumCard(32).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            addView(txt(tr("Celkový progres", "Overall progress"), 15f, muted, true).apply { gravity = Gravity.CENTER })
            addView(CircularProgressView(this@MainActivity, pct.toFloat(), gold, Color.rgb(82, 104, 130)).apply {
                layoutParams = LinearLayout.LayoutParams(dp(190), dp(190)).apply { setMargins(0, dp(18), 0, dp(12)) }
            })
            addView(txt("${money(saved)} / ${money(target)}", 15f, gold, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
        })
        root.addView(ScrollView(this).apply { addView(col) }, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav(3))
    }

    private fun settingsScreen() {
        clear()
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(6), 0, dp(28)) }
        col.addView(topBar(tr("Nastavenia", "Settings"), showLanguage = true))
        col.addView(premiumCard().apply {
            addView(txt(tr("Jazyk aplikácie", "App language"), 17f, cream, true))
            addView(txt(languages[lang()] ?: "Slovenčina", 14f, gold, true).apply {
                setPadding(0, dp(8), 0, 0)
                setOnClickListener { languageDialog { settingsScreen() } }
            })
        })
        col.addView(button(tr("UPRAVIŤ MÔJ SEN", "EDIT MY DREAM")) { editGoal() })
        col.addView(button(tr("VYMAZAŤ ÚDAJE", "RESET DATA")) {
            AlertDialog.Builder(this)
                .setTitle(tr("Vymazať všetko?", "Reset everything?"))
                .setMessage(tr("Tento krok sa nedá vrátiť späť.", "This cannot be undone."))
                .setPositiveButton(tr("VYMAZAŤ", "RESET")) { _, _ -> prefs.edit().clear().apply(); setupScreen() }
                .setNegativeButton(tr("ZRUŠIŤ", "CANCEL"), null)
                .show()
        })
        root.addView(ScrollView(this).apply { addView(col) }, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav(4))
    }

    private fun languageDialog(after: () -> Unit) {
        val codes = languages.keys.toList()
        val items = languages.values.toTypedArray()
        val checked = codes.indexOf(lang()).coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle("🌐  ${tr("Jazyk", "Language", "Sprache", "Jazyk", "Język", "Nyelv")}")
            .setSingleChoiceItems(items, checked) { dialog, which ->
                prefs.edit().putString("lang", codes[which]).apply()
                dialog.dismiss()
                after()
            }
            .setNegativeButton(tr("ZRUŠIŤ", "CANCEL", "ABBRECHEN", "ZRUŠIT", "ANULUJ", "MÉGSE"), null)
            .show()
    }

    private fun editGoal() {
        prefs.edit().remove("goal_name").apply()
        setupScreen()
    }

    private fun saveHistory(item: String, price: Double, type: String) {
        val date = SimpleDateFormat("dd.MM.yyyy HH:mm", locale()).format(Date())
        val old = prefs.getString("history", "") ?: ""
        val line = "$date|$item|${money(price)}|$type"
        prefs.edit().putString("history", if (old.isBlank()) line else "$old\n$line").apply()
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    class DreamScenicView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val path = Path()

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()

            paint.shader = LinearGradient(
                0f, 0f, w, h,
                intArrayOf(Color.rgb(28, 65, 103), Color.rgb(34, 47, 83), Color.rgb(110, 58, 99), Color.rgb(16, 31, 50)),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, w, h, paint)
            paint.shader = null

            paint.color = Color.argb(150, 255, 178, 91)
            canvas.drawCircle(w * .76f, h * .26f, w * .14f, paint)
            paint.color = Color.argb(80, 255, 220, 165)
            canvas.drawCircle(w * .76f, h * .26f, w * .20f, paint)

            path.reset()
            path.moveTo(0f, h * .73f)
            path.lineTo(w * .20f, h * .47f)
            path.lineTo(w * .37f, h * .65f)
            path.lineTo(w * .55f, h * .39f)
            path.lineTo(w * .78f, h * .68f)
            path.lineTo(w, h * .51f)
            path.lineTo(w, h)
            path.lineTo(0f, h)
            path.close()
            paint.color = Color.rgb(20, 43, 66)
            canvas.drawPath(path, paint)

            path.reset()
            path.moveTo(0f, h * .82f)
            path.lineTo(w * .28f, h * .68f)
            path.lineTo(w * .52f, h * .80f)
            path.lineTo(w * .74f, h * .65f)
            path.lineTo(w, h * .79f)
            path.lineTo(w, h)
            path.lineTo(0f, h)
            path.close()
            paint.color = Color.rgb(8, 25, 42)
            canvas.drawPath(path, paint)
        }
    }

    class CircularProgressView(context: Context, private val progress: Float, private val active: Int, private val track: Int) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            val stroke = w * 0.075f
            paint.strokeWidth = stroke
            val r = RectF(stroke, stroke, w - stroke, h - stroke)
            paint.color = track
            canvas.drawArc(r, -90f, 360f, false, paint)
            paint.color = active
            canvas.drawArc(r, -90f, 360f * (progress.coerceIn(0f, 100f) / 100f), false, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(255, 248, 231)
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textSize = w * 0.17f
            canvas.drawText(String.format(Locale.getDefault(), "%.1f %%", progress), w / 2, h / 2 + paint.textSize / 3, paint)
            paint.style = Paint.Style.STROKE
        }
    }
}
