package digital.madeiq.dream

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

class MainActivity : Activity() {

    private val prefs by lazy { getSharedPreferences("dream_prefs_v2", MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private val history = mutableListOf<Pair<Long, Double>>()

    private val cBg = Color.rgb(3, 17, 31)
    private val cPanel = Color.rgb(10, 31, 53)
    private val cPanel2 = Color.rgb(14, 42, 70)
    private val cText = Color.rgb(247, 246, 244)
    private val cMuted = Color.rgb(176, 189, 205)
    private val cGold = Color.rgb(255, 185, 91)
    private val cPink = Color.rgb(226, 86, 146)
    private val cPurple = Color.rgb(113, 83, 214)
    private val cGreen = Color.rgb(77, 197, 145)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadHistory()
        if (!prefs.contains("goal")) showSetup() else showDashboard()
    }

    private fun screen(): LinearLayout {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(24))
            setBackgroundColor(cBg)
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(root)
            setBackgroundColor(cBg)
        }
        setContentView(scroll)
        return root
    }

    private fun showSetup() {
        screen()
        addBrandHeader("Vytvor svoj DREAM", "Premeníme sen na jasný plán.")

        val name = field("Názov sna", prefs.getString("name", "") ?: "", false)
        val goal = field("Koľko potrebuješ (€)", valueText(prefs.getFloat("goal", 0f).toDouble()), true)
        val saved = field("Koľko už máš (€)", valueText(prefs.getFloat("saved", 0f).toDouble()), true)
        val income = field("Mesačný príjem (€)", valueText(prefs.getFloat("income", 0f).toDouble()), true)
        val expenses = field("Mesačné výdavky (€)", valueText(prefs.getFloat("expenses", 0f).toDouble()), true)

        var targetDate = prefs.getLong("targetDate", 0L)
        val dateBtn = button(if (targetDate > 0) "Cieľ: ${dateText(targetDate)}" else "Vybrať dátum cieľa", outline = true) {
            val cal = Calendar.getInstance()
            if (targetDate > 0) cal.timeInMillis = targetDate
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d, 12, 0, 0)
                targetDate = cal.timeInMillis
                showSetupWithDate(name.text.toString(), goal.text.toString(), saved.text.toString(), income.text.toString(), expenses.text.toString(), targetDate)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        root.addView(dateBtn, lpMatch(dp(56), top = 8))

        root.addView(button("VYTVORIŤ MÔJ PLÁN") {
            val n = name.text.toString().trim()
            val g = num(goal.text.toString())
            val s = num(saved.text.toString())
            val i = num(income.text.toString())
            val e = num(expenses.text.toString())
            if (n.isBlank() || g <= 0 || targetDate <= System.currentTimeMillis()) {
                toast("Doplň názov sna, cieľovú sumu a budúci dátum.")
                return@button
            }
            prefs.edit()
                .putString("name", n)
                .putFloat("goal", g.toFloat())
                .putFloat("saved", s.coerceAtMost(g).toFloat())
                .putFloat("income", i.toFloat())
                .putFloat("expenses", e.toFloat())
                .putLong("targetDate", targetDate)
                .apply()
            showDashboard()
        }, lpMatch(dp(58), top = 18))

        addQuote("Každé rozhodnutie ťa môže priblížiť k tomu, na čom ti záleží viac.")
    }

    private fun showSetupWithDate(name: String, goal: String, saved: String, income: String, expenses: String, targetDate: Long) {
        prefs.edit().putString("draftName", name).putString("draftGoal", goal).putString("draftSaved", saved)
            .putString("draftIncome", income).putString("draftExpenses", expenses).putLong("targetDate", targetDate).apply()
        showSetupDraft()
    }

    private fun showSetupDraft() {
        screen()
        addBrandHeader("Vytvor svoj DREAM", "Premeníme sen na jasný plán.")
        val name = field("Názov sna", prefs.getString("draftName", prefs.getString("name", "")) ?: "", false)
        val goal = field("Koľko potrebuješ (€)", prefs.getString("draftGoal", valueText(prefs.getFloat("goal", 0f).toDouble())) ?: "", true)
        val saved = field("Koľko už máš (€)", prefs.getString("draftSaved", valueText(prefs.getFloat("saved", 0f).toDouble())) ?: "", true)
        val income = field("Mesačný príjem (€)", prefs.getString("draftIncome", valueText(prefs.getFloat("income", 0f).toDouble())) ?: "", true)
        val expenses = field("Mesačné výdavky (€)", prefs.getString("draftExpenses", valueText(prefs.getFloat("expenses", 0f).toDouble())) ?: "", true)
        var targetDate = prefs.getLong("targetDate", 0L)

        root.addView(button("Cieľ: ${dateText(targetDate)}", outline = true) {
            val cal = Calendar.getInstance().apply { timeInMillis = targetDate }
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d, 12, 0, 0)
                targetDate = cal.timeInMillis
                showSetupWithDate(name.text.toString(), goal.text.toString(), saved.text.toString(), income.text.toString(), expenses.text.toString(), targetDate)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }, lpMatch(dp(56), top = 8))

        root.addView(button("VYTVORIŤ MÔJ PLÁN") {
            val n = name.text.toString().trim()
            val g = num(goal.text.toString())
            val s = num(saved.text.toString())
            val i = num(income.text.toString())
            val e = num(expenses.text.toString())
            if (n.isBlank() || g <= 0 || targetDate <= System.currentTimeMillis()) {
                toast("Doplň názov sna, cieľovú sumu a budúci dátum.")
                return@button
            }
            prefs.edit().putString("name", n).putFloat("goal", g.toFloat()).putFloat("saved", s.coerceAtMost(g).toFloat())
                .putFloat("income", i.toFloat()).putFloat("expenses", e.toFloat()).putLong("targetDate", targetDate)
                .remove("draftName").remove("draftGoal").remove("draftSaved").remove("draftIncome").remove("draftExpenses").apply()
            showDashboard()
        }, lpMatch(dp(58), top = 18))
        addQuote("Každé rozhodnutie ťa môže priblížiť k tomu, na čom ti záleží viac.")
    }

    private fun showDashboard() {
        screen()
        val name = prefs.getString("name", "Môj sen") ?: "Môj sen"
        val goal = prefs.getFloat("goal", 1f).toDouble()
        val saved = prefs.getFloat("saved", 0f).toDouble()
        val targetDate = prefs.getLong("targetDate", System.currentTimeMillis() + 86400000L)
        val remaining = max(0.0, goal - saved)
        val days = max(1L, ceil((targetDate - System.currentTimeMillis()) / 86400000.0).toLong())
        val dailyNeed = remaining / days
        val progress = if (goal > 0) (saved / goal).coerceIn(0.0, 1.0) else 0.0
        val freeDaily = calculatedFreeDaily()

        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        top.addView(TextView(this).apply { text = "☰"; textSize = 24f; setTextColor(cText); gravity = Gravity.CENTER }, LinearLayout.LayoutParams(dp(44), dp(44)))
        top.addView(TextView(this).apply { text = "DREAM"; textSize = 19f; setTypeface(typeface, Typeface.BOLD); letterSpacing = .14f; setTextColor(cText); gravity = Gravity.CENTER }, LinearLayout.LayoutParams(0, dp(44), 1f))
        top.addView(TextView(this).apply { text = "⚙"; textSize = 21f; setTextColor(cMuted); gravity = Gravity.CENTER; setOnClickListener { showSettings() } }, LinearLayout.LayoutParams(dp(44), dp(44)))
        root.addView(top)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(24), dp(20), dp(22))
            background = gradient(intArrayOf(Color.rgb(20, 48, 80), Color.rgb(45, 40, 75), Color.rgb(80, 48, 73)), 28f)
        }
        hero.addView(TextView(this).apply { text = "✦  ${name.uppercase()}"; textSize = 25f; setTypeface(typeface, Typeface.BOLD); setTextColor(cText); gravity = Gravity.CENTER })
        hero.addView(TextView(this).apply { text = "Môj sen"; textSize = 13f; setTextColor(cMuted); gravity = Gravity.CENTER; setPadding(0, dp(3), 0, dp(16)) })
        hero.addView(TextView(this).apply { text = "${money(saved)} / ${money(goal)}"; textSize = 28f; setTypeface(typeface, Typeface.BOLD); setTextColor(cText); gravity = Gravity.CENTER })
        hero.addView(TextView(this).apply { text = "${(progress * 1000).roundToInt() / 10.0} %"; textSize = 15f; setTextColor(Color.rgb(231, 216, 241)); gravity = Gravity.CENTER; setPadding(0, dp(4), 0, dp(16)) })
        hero.addView(progressRing(progress), LinearLayout.LayoutParams(dp(140), dp(140)))
        hero.addView(TextView(this).apply { text = if (remaining <= 0) "Sen je dosiahnutý ✦" else "Do cieľa: $days dní\nPotrebujem približne ${money(dailyNeed)} denne"; textSize = 14f; setTextColor(cText); gravity = Gravity.CENTER; setLineSpacing(3f, 1f); setPadding(0, dp(14), 0, 0) })
        root.addView(hero, lpMatch(-2, top = 14))

        val cards = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        cards.addView(statCard("DNES MÔŽEM MINÚŤ", money(freeDaily), "bez narušenia plánu"), LinearLayout.LayoutParams(0, dp(112), 1f).apply { marginEnd = dp(7) })
        cards.addView(statCard("ZOSTÁVA", money(remaining), "do tvojho DREAM"), LinearLayout.LayoutParams(0, dp(112), 1f).apply { marginStart = dp(7) })
        root.addView(cards, lpMatch(dp(112), top = 14))

        root.addView(button("CHCEM NIEČO KÚPIŤ") { showPurchase() }, lpMatch(dp(60), top = 18))
        root.addView(button("＋  DNES SOM UŠETRIL") { showAddSaving() }, lpMatch(dp(54), top = 10, outline = true))

        addBottomNav("Prehľad")
    }

    private fun showPurchase() {
        screen()
        backHeader("Chcem niečo kúpiť") { showDashboard() }
        addQuote("Nezakazujeme ti nakupovať. Len ti ukážeme, čo nákup znamená pre tvoj sen.")
        val item = field("Čo chceš kúpiť?", "", false)
        val price = field("Cena (€)", "", true)
        root.addView(button("ĎALEJ") {
            val p = num(price.text.toString())
            if (p <= 0) { toast("Zadaj cenu nákupu."); return@button }
            showImpact(item.text.toString().trim().ifBlank { "Tento nákup" }, p)
        }, lpMatch(dp(58), top = 18))
        addBottomNav("Prehľad")
    }

    private fun showImpact(item: String, price: Double) {
        screen()
        backHeader("Čo to znamená pre tvoj sen?") { showPurchase() }
        val goal = prefs.getFloat("goal", 1f).toDouble()
        val targetDate = prefs.getLong("targetDate", System.currentTimeMillis() + 86400000)
        val saved = prefs.getFloat("saved", 0f).toDouble()
        val days = max(1L, ceil((targetDate - System.currentTimeMillis()) / 86400000.0).toLong())
        val dailyNeed = max(0.01, (goal - saved) / days)
        val delayDays = ceil(price / dailyNeed).toInt().coerceAtLeast(1)
        val percent = if (goal > 0) price / goal * 100 else 0.0

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(24), dp(22), dp(24))
            background = gradient(intArrayOf(cPanel2, Color.rgb(44, 43, 76), Color.rgb(96, 52, 69)), 26f)
        }
        card.addView(TextView(this).apply { text = item; textSize = 18f; setTextColor(cMuted); gravity = Gravity.CENTER })
        card.addView(TextView(this).apply { text = money(price); textSize = 34f; setTypeface(typeface, Typeface.BOLD); setTextColor(cText); gravity = Gravity.CENTER; setPadding(0, dp(6), 0, dp(20)) })
        card.addView(TextView(this).apply { text = "To je"; textSize = 14f; setTextColor(cMuted); gravity = Gravity.CENTER })
        card.addView(TextView(this).apply { text = String.format(Locale.US, "%.1f %%", percent); textSize = 40f; setTypeface(typeface, Typeface.BOLD); setTextColor(cGold); gravity = Gravity.CENTER })
        card.addView(TextView(this).apply { text = "tvojho sna"; textSize = 14f; setTextColor(cMuted); gravity = Gravity.CENTER })
        card.addView(TextView(this).apply { text = "Ak ho kúpiš, tvoj sen sa posunie približne o"; textSize = 14f; setTextColor(cText); gravity = Gravity.CENTER; setPadding(0, dp(22), 0, dp(4)) })
        card.addView(TextView(this).apply { text = "$delayDays ${dayWord(delayDays)}"; textSize = 36f; setTypeface(typeface, Typeface.BOLD); setTextColor(cText); gravity = Gravity.CENTER })
        root.addView(card, lpMatch(-2, top = 20))

        root.addView(button("ČO CHCEŠ VIAC?") { showDecision(item, price, delayDays) }, lpMatch(dp(58), top = 18))
    }

    private fun showDecision(item: String, price: Double, delayDays: Int) {
        screen()
        backHeader("Čo chceš viac?") { showImpact(item, price) }
        val dream = prefs.getString("name", "Môj sen") ?: "Môj sen"
        root.addView(choiceCard("⌚", item, money(price)))
        root.addView(TextView(this).apply { text = "alebo"; setTextColor(cMuted); textSize = 14f; gravity = Gravity.CENTER; setPadding(0, dp(12), 0, dp(12)) })
        root.addView(choiceCard("✦", dream.uppercase(), "Tvoj sen • $delayDays ${dayWord(delayDays)}"))
        root.addView(button("KÚPIM TO") {
            saveDecision("Kúpené", item, price)
            toast("Rozhodnutie je na tebe. DREAM ti ukázal jeho cenu.")
            showDashboard()
        }, lpMatch(dp(56), top = 18, color = cGreen))
        root.addView(button("POČKÁM 24H") {
            saveDecision("Počkám 24h", item, price)
            toast("Dobrý krok. Zajtra sa rozhodneš s čistou hlavou.")
            showDashboard()
        }, lpMatch(dp(56), top = 10, color = cGold))
        root.addView(button("RADŠEJ MÔJ SEN") {
            saveDecision("Radšej sen", item, price)
            showAddSaving(prefill = price)
        }, lpMatch(dp(56), top = 10, color = cPurple))
    }

    private fun showAddSaving(prefill: Double = 0.0) {
        screen()
        backHeader("Pridať denný krok") { showDashboard() }
        root.addView(TextView(this).apply { text = "Dnes si o krok bližšie."; textSize = 16f; setTextColor(cMuted); gravity = Gravity.CENTER; setPadding(0, dp(16), 0, dp(8)) })
        val amount = field("Koľko si dnes ušetril (€)", if (prefill > 0) valueText(prefill) else "", true)
        root.addView(TextView(this).apply { text = "✓"; textSize = 64f; setTextColor(cGold); gravity = Gravity.CENTER; setPadding(0, dp(26), 0, dp(12)) })
        root.addView(button("ULOŽIŤ") {
            val a = num(amount.text.toString())
            if (a <= 0) { toast("Zadaj sumu."); return@button }
            val goal = prefs.getFloat("goal", 0f).toDouble()
            val old = prefs.getFloat("saved", 0f).toDouble()
            val updated = (old + a).coerceAtMost(goal)
            prefs.edit().putFloat("saved", updated.toFloat()).apply()
            history.add(System.currentTimeMillis() to a)
            persistHistory()
            toast("Skvelé. Si o krok bližšie.")
            showDashboard()
        }, lpMatch(dp(58), top = 18))
    }

    private fun showHistory() {
        screen()
        backHeader("História") { showDashboard() }
        if (history.isEmpty()) {
            addQuote("Zatiaľ tu nič nie je. Prvý krok vytvorí tvoju históriu.")
        } else {
            history.sortedByDescending { it.first }.forEach { (time, amount) ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(16), dp(14), dp(16), dp(14)); background = rounded(cPanel, 18f)
                }
                row.addView(TextView(this).apply { text = dateText(time); textSize = 14f; setTextColor(cMuted) }, LinearLayout.LayoutParams(0, -2, 1f))
                row.addView(TextView(this).apply { text = "+ ${money(amount)}"; textSize = 17f; setTypeface(typeface, Typeface.BOLD); setTextColor(cGreen) })
                root.addView(row, lpMatch(-2, top = 8))
            }
        }
        addBottomNav("História")
    }

    private fun showStats() {
        screen()
        backHeader("Štatistiky") { showDashboard() }
        val total = history.sumOf { it.second }
        val avg = if (history.isEmpty()) 0.0 else total / history.size
        val cards = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        cards.addView(statCard("ULOŽENÉ KROKMI", money(total), "spolu"), LinearLayout.LayoutParams(0, dp(112), 1f).apply { marginEnd = dp(7) })
        cards.addView(statCard("PRIEMER KROKU", money(avg), "na záznam"), LinearLayout.LayoutParams(0, dp(112), 1f).apply { marginStart = dp(7) })
        root.addView(cards, lpMatch(dp(112), top = 16))
        addQuote("DREAM nie je o dokonalosti. Je o tom, že každý malý krok má smer.")
        addBottomNav("Štatistiky")
    }

    private fun showSettings() {
        screen()
        backHeader("Môj sen") { showDashboard() }
        val name = field("Názov sna", prefs.getString("name", "") ?: "", false)
        val goal = field("Cieľová suma (€)", valueText(prefs.getFloat("goal", 0f).toDouble()), true)
        val saved = field("Aktuálne mám (€)", valueText(prefs.getFloat("saved", 0f).toDouble()), true)
        val income = field("Môj príjem mesačne (€)", valueText(prefs.getFloat("income", 0f).toDouble()), true)
        val expenses = field("Moje výdavky mesačne (€)", valueText(prefs.getFloat("expenses", 0f).toDouble()), true)
        val targetDate = prefs.getLong("targetDate", 0L)

        val daily = plannedDaily()
        root.addView(statCard("DENNÝ PLÁN", money(daily), "odkladaj denne"), lpMatch(dp(112), top = 12))
        root.addView(TextView(this).apply { text = "Dátum cieľa: ${dateText(targetDate)}"; textSize = 14f; setTextColor(cMuted); gravity = Gravity.CENTER; setPadding(0, dp(14), 0, dp(4)) })
        root.addView(button("ULOŽIŤ ZMENY") {
            val g = num(goal.text.toString())
            if (name.text.toString().isBlank() || g <= 0) { toast("Skontroluj názov a cieľovú sumu."); return@button }
            prefs.edit().putString("name", name.text.toString().trim()).putFloat("goal", g.toFloat())
                .putFloat("saved", num(saved.text.toString()).coerceAtMost(g).toFloat())
                .putFloat("income", num(income.text.toString()).toFloat())
                .putFloat("expenses", num(expenses.text.toString()).toFloat()).apply()
            showDashboard()
        }, lpMatch(dp(58), top = 16))
        root.addView(button("UPRAVIŤ DÁTUM CIEĽA", outline = true) {
            val cal = Calendar.getInstance().apply { if (targetDate > 0) timeInMillis = targetDate }
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d, 12, 0, 0)
                prefs.edit().putLong("targetDate", cal.timeInMillis).apply()
                showSettings()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }, lpMatch(dp(54), top = 10))
    }

    private fun addBrandHeader(title: String, subtitle: String) {
        root.addView(TextView(this).apply {
            text = "DREAM"; textSize = 34f; setTypeface(typeface, Typeface.BOLD); letterSpacing = .18f; setTextColor(cText)
        })
        root.addView(TextView(this).apply { text = "What do you want more?"; textSize = 15f; setTextColor(cGold); setPadding(0, dp(4), 0, dp(28)) })
        root.addView(TextView(this).apply { text = title; textSize = 25f; setTypeface(typeface, Typeface.BOLD); setTextColor(cText) })
        root.addView(TextView(this).apply { text = subtitle; textSize = 14f; setTextColor(cMuted); setPadding(0, dp(5), 0, dp(14)) })
    }

    private fun backHeader(title: String, back: () -> Unit) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        row.addView(TextView(this).apply { text = "‹"; textSize = 38f; setTextColor(cText); gravity = Gravity.CENTER; setOnClickListener { back() } }, LinearLayout.LayoutParams(dp(50), dp(52)))
        row.addView(TextView(this).apply { text = title; textSize = 19f; setTypeface(typeface, Typeface.BOLD); setTextColor(cText); gravity = Gravity.CENTER_VERTICAL }, LinearLayout.LayoutParams(0, dp(52), 1f))
        root.addView(row)
    }

    private fun field(label: String, value: String, numeric: Boolean): EditText {
        val e = EditText(this).apply {
            hint = label; setHintTextColor(Color.rgb(132, 151, 172)); setTextColor(cText); textSize = 16f
            setText(value); setPadding(dp(16), 0, dp(16), 0)
            background = rounded(cPanel, 18f, Color.rgb(31, 63, 93))
            inputType = if (numeric) InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
        root.addView(e, lpMatch(dp(58), top = 10))
        return e
    }

    private fun button(text: String, outline: Boolean = false, click: () -> Unit): TextView = button(text, outline, null, click)

    private fun button(text: String, outline: Boolean = false, color: Int? = null, click: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text; textSize = 15f; setTypeface(typeface, Typeface.BOLD); gravity = Gravity.CENTER
            setTextColor(if (outline) cText else Color.WHITE)
            background = if (outline) rounded(Color.TRANSPARENT, 22f, Color.rgb(62, 87, 112))
            else if (color != null) rounded(color, 22f)
            else gradient(intArrayOf(cGold, Color.rgb(244, 121, 91), cPink), 22f)
            setOnClickListener { click() }
        }
    }

    private fun statCard(kicker: String, value: String, note: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12)); background = rounded(cPanel, 20f, Color.rgb(29, 60, 89))
            addView(TextView(context).apply { text = kicker; textSize = 10f; letterSpacing = .08f; setTextColor(cMuted); gravity = Gravity.CENTER })
            addView(TextView(context).apply { text = value; textSize = 23f; setTypeface(typeface, Typeface.BOLD); setTextColor(cText); gravity = Gravity.CENTER; setPadding(0, dp(6), 0, dp(4)) })
            addView(TextView(context).apply { text = note; textSize = 11f; setTextColor(cMuted); gravity = Gravity.CENTER })
        }
    }

    private fun choiceCard(icon: String, title: String, subtitle: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16)); background = rounded(cPanel, 20f, Color.rgb(30, 64, 96))
            addView(TextView(context).apply { text = icon; textSize = 30f; gravity = Gravity.CENTER }, LinearLayout.LayoutParams(dp(58), dp(58)))
            val texts = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            texts.addView(TextView(context).apply { text = title; textSize = 18f; setTypeface(typeface, Typeface.BOLD); setTextColor(cText) })
            texts.addView(TextView(context).apply { text = subtitle; textSize = 14f; setTextColor(cMuted); setPadding(0, dp(3), 0, 0) })
            addView(texts, LinearLayout.LayoutParams(0, -2, 1f))
        }.also { root.addView(it, lpMatch(-2, top = 14)) }
    }

    private fun addQuote(text: String) {
        root.addView(TextView(this).apply {
            this.text = "“$text”"; textSize = 14f; setTextColor(cMuted); gravity = Gravity.CENTER; setLineSpacing(4f, 1f)
            setPadding(dp(18), dp(18), dp(18), dp(18)); background = rounded(Color.rgb(7, 25, 44), 20f)
        }, lpMatch(-2, top = 18))
    }

    private fun addBottomNav(active: String) {
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, dp(20), 0, 0) }
        val items = listOf("⌂" to "Prehľad", "◷" to "História", "＋" to "Pridať", "▥" to "Štatistiky", "⚙" to "Nastavenia")
        items.forEach { (ico, label) ->
            val t = TextView(this).apply {
                text = "$ico\n$label"; textSize = if (label == "Pridať") 12f else 10f; gravity = Gravity.CENTER
                setTextColor(if (active == label) cGold else cMuted)
                setOnClickListener {
                    when (label) {
                        "Prehľad" -> showDashboard(); "História" -> showHistory(); "Pridať" -> showAddSaving(); "Štatistiky" -> showStats(); "Nastavenia" -> showSettings()
                    }
                }
            }
            nav.addView(t, LinearLayout.LayoutParams(0, dp(62), 1f))
        }
        root.addView(nav)
    }

    private fun progressRing(progress: Double): View {
        val frame = FrameLayout(this)
        val outer = TextView(this).apply {
            text = "◯"; textSize = 112f; gravity = Gravity.CENTER; setTextColor(Color.rgb(136, 96, 201))
        }
        frame.addView(outer, FrameLayout.LayoutParams(-1, -1))
        frame.addView(TextView(this).apply {
            text = "${(progress * 100).roundToInt()}%"; textSize = 20f; setTypeface(typeface, Typeface.BOLD); gravity = Gravity.CENTER; setTextColor(cGold)
        }, FrameLayout.LayoutParams(-1, -1))
        return frame
    }

    private fun calculatedFreeDaily(): Double {
        val income = prefs.getFloat("income", 0f).toDouble()
        val expenses = prefs.getFloat("expenses", 0f).toDouble()
        val freeAfterPlan = ((income - expenses) / 30.0 - plannedDaily()).coerceAtLeast(0.0)
        return freeAfterPlan
    }

    private fun plannedDaily(): Double {
        val goal = prefs.getFloat("goal", 0f).toDouble()
        val saved = prefs.getFloat("saved", 0f).toDouble()
        val targetDate = prefs.getLong("targetDate", System.currentTimeMillis() + 86400000)
        val days = max(1L, ceil((targetDate - System.currentTimeMillis()) / 86400000.0).toLong())
        return max(0.0, goal - saved) / days
    }

    private fun saveDecision(type: String, item: String, price: Double) {
        val key = "decision_${System.currentTimeMillis()}"
        prefs.edit().putString(key, "$type|$item|$price").apply()
    }

    private fun loadHistory() {
        val raw = prefs.getString("savingHistory", "") ?: ""
        if (raw.isBlank()) return
        raw.split(";").forEach { part ->
            val p = part.split(":")
            if (p.size == 2) {
                val t = p[0].toLongOrNull(); val a = p[1].toDoubleOrNull()
                if (t != null && a != null) history.add(t to a)
            }
        }
    }

    private fun persistHistory() {
        prefs.edit().putString("savingHistory", history.joinToString(";") { "${it.first}:${it.second}" }).apply()
    }

    private fun money(v: Double): String {
        val nf = NumberFormat.getCurrencyInstance(Locale("sk", "SK"))
        nf.currency = Currency.getInstance("EUR")
        nf.maximumFractionDigits = 2
        return nf.format(v)
    }

    private fun valueText(v: Double): String = if (v <= 0) "" else if (v % 1.0 == 0.0) v.toInt().toString() else String.format(Locale.US, "%.2f", v)
    private fun num(s: String): Double = s.replace(" ", "").replace(",", ".").replace("€", "").toDoubleOrNull() ?: 0.0
    private fun dateText(time: Long): String = if (time <= 0) "—" else SimpleDateFormat("d. M. yyyy", Locale("sk", "SK")).format(Date(time))
    private fun dayWord(n: Int): String = when { n == 1 -> "deň"; n in 2..4 -> "dni"; else -> "dní" }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    private fun rounded(fill: Int, radius: Float, stroke: Int? = null): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE; cornerRadius = dp(radius.toInt()).toFloat(); setColor(fill)
        if (stroke != null) setStroke(dp(1), stroke)
    }

    private fun gradient(colors: IntArray, radius: Float): GradientDrawable = GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply {
        cornerRadius = dp(radius.toInt()).toFloat()
    }

    private fun lpMatch(height: Int, top: Int = 0, outline: Boolean = false, color: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height).apply { topMargin = dp(top) }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).roundToInt()
}
