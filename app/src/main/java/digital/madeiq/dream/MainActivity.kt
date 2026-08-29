package digital.madeiq.dream

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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

    private val bg = Color.rgb(5, 15, 29)
    private val card = Color.rgb(14, 31, 51)
    private val card2 = Color.rgb(19, 42, 68)
    private val gold = Color.rgb(249, 185, 94)
    private val cream = Color.rgb(255, 247, 226)
    private val muted = Color.rgb(165, 177, 197)
    private val green = Color.rgb(82, 205, 151)
    private val purple = Color.rgb(133, 92, 238)
    private val pink = Color.rgb(239, 96, 145)
    private val orange = Color.rgb(247, 129, 101)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        setContentView(root)
        if ((prefs.getString("goal_name", "") ?: "").isBlank()) setupScreen() else dashboardScreen()
    }

    private fun clear() { root.removeAllViews() }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun money(v: Double): String = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(v)

    private fun txt(s: String, size: Float = 16f, color: Int = cream, bold: Boolean = false): TextView = TextView(this).apply {
        text = s
        textSize = size
        setTextColor(color)
        includeFontPadding = false
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        letterSpacing = if (bold) 0.01f else 0f
    }

    private fun solid(color: Int, radius: Int = 24, stroke: Int? = null): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (stroke != null) setStroke(dp(1), stroke)
    }

    private fun gradient(colors: IntArray, radius: Int = 28, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT): GradientDrawable =
        GradientDrawable(orientation, colors).apply { cornerRadius = dp(radius).toFloat() }

    private fun cardBox(radius: Int = 24): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(18), dp(20), dp(18))
        background = gradient(intArrayOf(Color.rgb(18, 39, 64), Color.rgb(10, 27, 47)), radius)
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(dp(18), dp(7), dp(18), dp(7)) }
        elevation = dp(2).toFloat()
    }

    private fun button(label: String, onClick: () -> Unit): Button = Button(this).apply {
        text = label
        textSize = 15f
        setTextColor(bg)
        typeface = Typeface.DEFAULT_BOLD
        isAllCaps = false
        stateListAnimator = null
        minHeight = 0
        minimumHeight = 0
        background = gradient(intArrayOf(Color.rgb(255, 198, 101), Color.rgb(245, 174, 84)), 28)
        setPadding(dp(18), dp(15), dp(18), dp(15))
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(-1, dp(58)).apply { setMargins(dp(18), dp(9), dp(18), dp(9)) }
    }

    private fun accentButton(label: String, colors: IntArray, textColor: Int = Color.WHITE, onClick: () -> Unit): Button = button(label, onClick).apply {
        setTextColor(textColor)
        background = gradient(colors, 28)
    }

    private fun input(hintText: String, numeric: Boolean = false): EditText = EditText(this).apply {
        hint = hintText
        setHintTextColor(muted)
        setTextColor(cream)
        textSize = 16f
        singleLine = true
        background = solid(card2, 20, Color.rgb(194, 144, 77))
        setPadding(dp(18), 0, dp(18), 0)
        if (numeric) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layoutParams = LinearLayout.LayoutParams(-1, dp(62)).apply { setMargins(dp(18), dp(6), dp(18), dp(6)) }
    }

    private fun sectionHeader(title: String, subtitle: String? = null): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(18), dp(8), dp(18), dp(8))
        addView(txt(title, 26f, cream, true))
        subtitle?.let {
            val s = txt(it, 13f, muted)
            s.setPadding(0, dp(4), 0, 0)
            addView(s)
        }
    }

    private fun setupScreen() {
        clear()
        val scroll = ScrollView(this)
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(14), 0, dp(28)) }
        scroll.addView(col)

        val head = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(6), dp(18), dp(8))
        }
        head.addView(txt("D", 32f, gold, true), LinearLayout.LayoutParams(dp(54), dp(48)))
        head.addView(txt("DREAM", 22f, cream, true), LinearLayout.LayoutParams(0, -2, 1f))
        val lang = TextView(this).apply {
            text = "🌐  ${languageLabel()}"
            setTextColor(gold)
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = solid(card, 20, gold)
            setOnClickListener { languageDialog() }
        }
        head.addView(lang)
        col.addView(head)

        val hero = cardBox(26).apply {
            background = gradient(intArrayOf(Color.rgb(21, 47, 75), Color.rgb(12, 30, 52)), 26, GradientDrawable.Orientation.TL_BR)
            addView(txt("Vytvor svoj\nDREAM", 34f, cream, true))
            val sub = txt("Premeníme sen na jasný plán.", 16f, muted)
            sub.setPadding(0, dp(12), 0, 0)
            addView(sub)
            val quote = txt("FOCUS  •  PLAN  •  ACHIEVE", 13f, gold, true)
            quote.setPadding(0, dp(22), 0, 0)
            addView(quote)
        }
        col.addView(hero)

        val goal = input("Názov sna")
        val target = input("Koľko potrebuješ", true)
        val current = input("Koľko už máš", true)
        val income = input("Mesačný príjem", true)
        val expenses = input("Mesačné výdavky", true)
        col.addView(goal); col.addView(target); col.addView(current); col.addView(income); col.addView(expenses)

        var picked = 0L
        lateinit var dateBtn: Button
        dateBtn = button("📅  Vybrať dátum cieľa") {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val cc = Calendar.getInstance().apply {
                    set(y, m, d, 23, 59, 59)
                    set(Calendar.MILLISECOND, 0)
                }
                picked = cc.timeInMillis
                dateBtn.text = "📅  " + SimpleDateFormat("d. M. yyyy", Locale.getDefault()).format(Date(picked))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
        col.addView(dateBtn)
        col.addView(button("VYTVORIŤ MÔJ PLÁN") {
            val n = goal.text.toString().trim()
            val t = target.text.toString().replace(',', '.').toDoubleOrNull()
            val h = current.text.toString().replace(',', '.').toDoubleOrNull()
            val inc = income.text.toString().replace(',', '.').toDoubleOrNull()
            val exp = expenses.text.toString().replace(',', '.').toDoubleOrNull()
            if (n.isBlank() || t == null || h == null || inc == null || exp == null || picked == 0L || t <= 0) {
                toast("Doplň všetky údaje."); return@button
            }
            prefs.edit().putString("goal_name", n).putFloat("target", t.toFloat()).putFloat("saved", h.toFloat())
                .putFloat("income", inc.toFloat()).putFloat("expenses", exp.toFloat()).putLong("target_date", picked).apply()
            dashboardScreen()
        })
        root.addView(scroll)
    }

    private fun dashboardScreen() {
        clear()
        val scroll = ScrollView(this)
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(14), 0, dp(88)) }
        scroll.addView(col)

        val name = prefs.getString("goal_name", "Môj sen") ?: "Môj sen"
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

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(6), dp(18), dp(8))
        }
        header.addView(txt("D", 30f, gold, true), LinearLayout.LayoutParams(dp(50), dp(46)))
        val title = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(txt(name, 23f, cream, true))
            addView(txt("Môj sen", 13f, muted))
        }
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(txt("Upraviť", 14f, gold, true).apply { setOnClickListener { editGoal() } })
        col.addView(header)

        val hero = cardBox(28).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            background = gradient(intArrayOf(Color.rgb(20, 46, 75), Color.rgb(11, 29, 50)), 28, GradientDrawable.Orientation.TL_BR)
            addView(txt("${money(saved)}  /  ${money(target)}", 27f, cream, true).apply { gravity = Gravity.CENTER })
            addView(txt(String.format(Locale.getDefault(), "%.1f %%", pct), 16f, gold, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(5), 0, 0) })
            val ring = CircularProgressView(this@MainActivity, pct.toFloat(), gold, Color.argb(95, 255, 255, 255)).apply {
                layoutParams = LinearLayout.LayoutParams(dp(154), dp(154)).apply { setMargins(0, dp(14), 0, dp(12)) }
            }
            addView(ring)
            addView(txt("Do cieľa:  $days dní", 16f, cream, true).apply { gravity = Gravity.CENTER })
            addView(txt("Potrebujem približne ${money(daily)} denne", 14f, muted).apply { gravity = Gravity.CENTER; setPadding(0, dp(5), 0, 0) })
        }
        col.addView(hero)

        val overview = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(dp(14), dp(5), dp(14), dp(5)) }
        overview.addView(metric("Dnes môžem minúť", money(freeDaily)), LinearLayout.LayoutParams(0, -2, 1f))
        overview.addView(metric("Progres", String.format(Locale.getDefault(), "%.1f %%", pct)), LinearLayout.LayoutParams(0, -2, 1f))
        col.addView(overview)
        pendingCard(col)

        col.addView(accentButton("CHCEM NIEČO KÚPIŤ", intArrayOf(Color.rgb(255, 181, 89), Color.rgb(239, 91, 137)), Color.WHITE) { purchaseScreen() })
        col.addView(button("PRIDAŤ DNEŠNÝ KROK") { addStepDialog() })

        val quote = cardBox(24)
        quote.addView(txt("„Malé kroky každý deň tvoria veľké zmeny.“", 16f, cream, true))
        val q2 = txt("Každé rozhodnutie ťa môže priblížiť k tomu, na čom ti záleží viac.", 14f, muted)
        q2.setPadding(0, dp(8), 0, 0)
        quote.addView(q2)
        col.addView(quote)

        col.addView(bottomNav())
        root.addView(scroll)
    }

    private fun metric(label: String, value: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(14), dp(14), dp(14))
        background = gradient(intArrayOf(Color.rgb(18, 39, 64), Color.rgb(11, 29, 49)), 20)
        addView(txt(label, 12f, muted))
        addView(txt(value, 18f, gold, true).apply { setPadding(0, dp(5), 0, 0) })
        val lp = LinearLayout.LayoutParams(0, -2, 1f)
        lp.setMargins(dp(4), dp(4), dp(4), dp(4))
        layoutParams = lp
    }

    private fun bottomNav(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(dp(12), dp(18), dp(12), dp(8))
        val items = arrayOf("⌂\nPrehľad", "◷\nHistória", "+", "▥\nŠtatistiky", "⚙\nNastavenia")
        items.forEachIndexed { i, label ->
            val v = txt(label, if (i == 2) 26f else 11f, if (i == 0 || i == 2) cream else muted, i == 0 || i == 2).apply {
                gravity = Gravity.CENTER
                if (i == 2) background = solid(Color.rgb(42, 76, 124), 30)
            }
            addView(v, LinearLayout.LayoutParams(0, if (i == 2) dp(52) else dp(48), 1f).apply { setMargins(dp(3), 0, dp(3), 0) })
        }
    }

    private fun purchaseScreen() {
        clear()
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(16), 0, dp(24)) }
        col.addView(backTitle("Chcem niečo kúpiť"))
        val product = cardBox(28).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(28), dp(24), dp(28))
            addView(txt("⌚", 54f, cream).apply { gravity = Gravity.CENTER })
            addView(txt("Každý nákup má svoju cenu", 14f, muted, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(10), 0, 0) })
        }
        col.addView(product)
        val item = input("Čo chceš kúpiť?")
        val price = input("Cena", true)
        col.addView(item); col.addView(price)
        col.addView(accentButton("UKÁZAŤ DOPAD", intArrayOf(Color.rgb(255, 181, 89), Color.rgb(239, 91, 137)), Color.WHITE) {
            val p = price.text.toString().replace(',', '.').toDoubleOrNull()
            if (item.text.toString().isBlank() || p == null || p <= 0) { toast("Zadaj názov a cenu."); return@accentButton }
            impactScreen(item.text.toString().trim(), p)
        })
        root.addView(col)
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
        val name = prefs.getString("goal_name", "tvoj sen") ?: "tvoj sen"

        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(16), 0, dp(24)) }
        col.addView(backTitle("Čo to znamená pre tvoj sen?"))
        val c = cardBox(28).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            background = gradient(intArrayOf(Color.rgb(24, 49, 78), Color.rgb(57, 43, 72), Color.rgb(19, 38, 63)), 28, GradientDrawable.Orientation.TL_BR)
            setPadding(dp(22), dp(25), dp(22), dp(25))
            addView(txt(item, 19f, cream, true).apply { gravity = Gravity.CENTER })
            addView(txt(money(price), 31f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(6), 0, dp(12)) })
            addView(txt("To je", 13f, muted).apply { gravity = Gravity.CENTER })
            addView(txt(String.format(Locale.getDefault(), "%.1f %%", pct), 33f, pink, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(4), 0, 0) })
            addView(txt("tvojho sna", 13f, muted).apply { gravity = Gravity.CENTER })
            addView(txt("Ak to kúpiš, tvoj sen sa posunie približne o", 13f, cream).apply { gravity = Gravity.CENTER; setPadding(0, dp(16), 0, 0) })
            addView(txt("$lostDays dni", 34f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(5), 0, 0) })
            addView(txt("$name", 13f, muted).apply { gravity = Gravity.CENTER; setPadding(0, dp(10), 0, 0) })
        }
        col.addView(c)

        col.addView(accentButton("KÚPIM TO", intArrayOf(Color.rgb(83, 205, 151), Color.rgb(72, 184, 143))) {
            saveHistory(item, price, "bought")
            prefs.edit().putFloat("saved", max(0.0, saved - price).toFloat()).apply()
            dashboardScreen()
        })
        col.addView(accentButton("POČKÁM 24 HODÍN", intArrayOf(Color.rgb(255, 199, 102), Color.rgb(249, 173, 83)), bg) {
            prefs.edit().putString("pending_item", item).putFloat("pending_price", price.toFloat())
                .putLong("pending_until", System.currentTimeMillis() + 24 * 60 * 60 * 1000L).apply()
            saveHistory(item, price, "wait")
            dashboardScreen()
        })
        col.addView(accentButton("RADŠEJ MÔJ SEN", intArrayOf(Color.rgb(145, 94, 244), Color.rgb(88, 89, 203))) {
            saveHistory(item, price, "dream")
            prefs.edit().putFloat("saved", (saved + price).toFloat()).apply()
            dashboardScreen()
        })
        root.addView(col)
    }

    private fun pendingCard(col: LinearLayout) {
        val until = prefs.getLong("pending_until", 0L)
        if (until <= 0L) return
        val item = prefs.getString("pending_item", "") ?: ""
        val price = prefs.getFloat("pending_price", 0f).toDouble()
        val c = cardBox(24).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            addView(txt("24-hodinová pauza", 14f, gold, true).apply { gravity = Gravity.CENTER })
            addView(txt("$item  •  ${money(price)}", 18f, cream, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(5), 0, 0) })
        }
        val ring = CircularCountdownView(this, until, gold, card2).apply {
            layoutParams = LinearLayout.LayoutParams(dp(128), dp(128)).apply { setMargins(0, dp(12), 0, dp(8)) }
            setOnClickListener { if (System.currentTimeMillis() >= until) finalDecision(item, price) }
        }
        c.addView(ring)
        val remain = txt("", 14f, muted, true).apply { gravity = Gravity.CENTER }
        c.addView(remain)
        c.setOnClickListener { if (System.currentTimeMillis() >= until) finalDecision(item, price) }
        col.addView(c)

        fun update() {
            val ms = until - System.currentTimeMillis()
            if (ms <= 0) {
                remain.text = "Čas vypršal — ťukni a rozhodni sa."
                remain.setTextColor(green)
            } else {
                val h = ms / 3600000
                val m = (ms % 3600000) / 60000
                remain.text = String.format(Locale.getDefault(), "Zostáva %02d:%02d", h, m)
                ring.invalidate()
                handler.postDelayed({ update() }, 30000)
            }
        }
        update()
    }

    private fun finalDecision(item: String, price: Double) {
        AlertDialog.Builder(this).setTitle("Čo chceš viac?")
            .setMessage("$item — ${money(price)}\n\nAlebo tvoj sen: ${prefs.getString("goal_name", "")}")
            .setPositiveButton("RADŠEJ MÔJ SEN") { _, _ ->
                val s = prefs.getFloat("saved", 0f).toDouble()
                prefs.edit().putFloat("saved", (s + price).toFloat()).remove("pending_item").remove("pending_price").remove("pending_until").apply()
                saveHistory(item, price, "dream_after_24h"); dashboardScreen()
            }
            .setNegativeButton("KÚPIM TO") { _, _ ->
                val s = prefs.getFloat("saved", 0f).toDouble()
                prefs.edit().putFloat("saved", max(0.0, s - price).toFloat()).remove("pending_item").remove("pending_price").remove("pending_until").apply()
                saveHistory(item, price, "bought_after_24h"); dashboardScreen()
            }.show()
    }

    private fun addStepDialog() {
        val e = EditText(this).apply { hint = "Suma"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        AlertDialog.Builder(this).setTitle("Dnešný krok k snu").setView(e)
            .setPositiveButton("ULOŽIŤ") { _, _ ->
                val v = e.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
                val s = prefs.getFloat("saved", 0f).toDouble()
                prefs.edit().putFloat("saved", (s + v).toFloat()).apply()
                saveHistory("Denný krok", v, "step"); dashboardScreen()
            }.setNegativeButton("Zrušiť", null).show()
    }

    private fun saveHistory(item: String, price: Double, action: String) {
        val old = prefs.getString("history", "") ?: ""
        prefs.edit().putString("history", "${System.currentTimeMillis()}|$action|$item|$price\n$old").apply()
    }

    private fun editGoal() { prefs.edit().remove("goal_name").apply(); setupScreen() }

    private fun backTitle(title: String) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(18), dp(8), dp(18), dp(14))
        val back = txt("‹", 36f, cream, true).apply { gravity = Gravity.CENTER; setOnClickListener { dashboardScreen() } }
        addView(back, LinearLayout.LayoutParams(dp(48), dp(48)))
        addView(txt(title, 19f, cream, true).apply { gravity = Gravity.CENTER_VERTICAL })
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    private fun languageLabel(): String = when (prefs.getString("lang", "auto")) {
        "sk" -> "Slovenčina"; "en" -> "English"; "de" -> "Deutsch"; "cs" -> "Čeština"; "pl" -> "Polski"
        "fr" -> "Français"; "es" -> "Español"; "it" -> "Italiano"; else -> "Auto"
    }

    private fun languageDialog() {
        val labels = arrayOf("Automaticky", "Slovenčina", "English", "Deutsch", "Čeština", "Polski", "Français", "Español", "Italiano")
        val codes = arrayOf("auto", "sk", "en", "de", "cs", "pl", "fr", "es", "it")
        AlertDialog.Builder(this).setTitle("Jazyk").setItems(labels) { _, i ->
            prefs.edit().putString("lang", codes[i]).apply()
            if ((prefs.getString("goal_name", "") ?: "").isBlank()) setupScreen() else dashboardScreen()
        }.show()
    }

    private inner class CircularProgressView(context: Context, private val percent: Float, private val color: Int, private val trackColor: Int) : View(context) {
        private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeWidth = dp(10).toFloat(); this.color = trackColor }
        private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeWidth = dp(10).toFloat(); this.color = color }
        private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; textSize = dp(20).toFloat(); typeface = Typeface.DEFAULT_BOLD; this.color = cream }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val inset = dp(12).toFloat()
            val r = RectF(inset, inset, width - inset, height - inset)
            canvas.drawArc(r, -90f, 360f, false, track)
            canvas.drawArc(r, -90f, 360f * (percent.coerceIn(0f, 100f) / 100f), false, progressPaint)
            canvas.drawText(String.format(Locale.getDefault(), "%.1f %%", percent), width / 2f, height / 2f + dp(7), label)
        }
    }

    private inner class CircularCountdownView(context: Context, private val until: Long, private val color: Int, private val trackColor: Int) : View(context) {
        private val startedAt = prefs.getLong("pending_until", until) - 24 * 60 * 60 * 1000L
        private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeWidth = dp(9).toFloat(); this.color = trackColor }
        private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeWidth = dp(9).toFloat(); this.color = color }
        private val big = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; textSize = dp(18).toFloat(); typeface = Typeface.DEFAULT_BOLD; this.color = cream }
        private val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; textSize = dp(10).toFloat(); this.color = muted }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val now = System.currentTimeMillis()
            val total = max(1L, until - startedAt)
            val remain = max(0L, until - now)
            val ratio = remain.toFloat() / total.toFloat()
            val inset = dp(11).toFloat()
            val r = RectF(inset, inset, width - inset, height - inset)
            canvas.drawArc(r, -90f, 360f, false, track)
            canvas.drawArc(r, -90f, 360f * ratio, false, progressPaint)
            val h = remain / 3600000
            val m = (remain % 3600000) / 60000
            canvas.drawText(String.format(Locale.getDefault(), "%02d:%02d", h, m), width / 2f, height / 2f + dp(2), big)
            canvas.drawText("24H PAUZA", width / 2f, height / 2f + dp(21), small)
        }
    }
}
