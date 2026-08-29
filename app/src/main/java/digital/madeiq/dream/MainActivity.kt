package digital.madeiq.dream

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("dream_prefs", MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private var lang = "en"

    private val languageNames = linkedMapOf(
        "auto" to "Auto",
        "en" to "English",
        "sk" to "Slovenčina",
        "cs" to "Čeština",
        "de" to "Deutsch",
        "pl" to "Polski",
        "fr" to "Français",
        "es" to "Español",
        "it" to "Italiano"
    )

    private val tr = mapOf(
        "en" to mapOf("tag" to "One day closer.", "dream" to "My dream", "goal" to "Goal", "saved" to "Saved", "left" to "Left", "add" to "ADD TODAY'S STEP", "locked" to "Today's step is complete", "edit" to "Edit dream", "language" to "Language", "name" to "What are you dreaming of?", "target" to "Target amount", "daily" to "Daily step", "save" to "Save", "cancel" to "Cancel", "ready" to "Ready now", "hours" to "Available in", "days" to "days", "day" to "day", "completed" to "Dream reached ✦", "progress" to "Progress"),
        "sk" to mapOf("tag" to "Každý deň o krok bližšie.", "dream" to "Môj sen", "goal" to "Cieľ", "saved" to "Uložené", "left" to "Zostáva", "add" to "PRIDAŤ DNEŠNÝ KROK", "locked" to "Dnešný krok je splnený", "edit" to "Upraviť sen", "language" to "Jazyk", "name" to "O čom snívaš?", "target" to "Cieľová suma", "daily" to "Denný krok", "save" to "Uložiť", "cancel" to "Zrušiť", "ready" to "Pripravené teraz", "hours" to "Dostupné o", "days" to "dní", "day" to "deň", "completed" to "Sen dosiahnutý ✦", "progress" to "Pokrok"),
        "cs" to mapOf("tag" to "Každý den o krok blíž.", "dream" to "Můj sen", "goal" to "Cíl", "saved" to "Uloženo", "left" to "Zbývá", "add" to "PŘIDAT DNEŠNÍ KROK", "locked" to "Dnešní krok je splněn", "edit" to "Upravit sen", "language" to "Jazyk", "name" to "O čem sníš?", "target" to "Cílová částka", "daily" to "Denní krok", "save" to "Uložit", "cancel" to "Zrušit", "ready" to "Připraveno nyní", "hours" to "Dostupné za", "days" to "dní", "day" to "den", "completed" to "Sen dosažen ✦", "progress" to "Pokrok"),
        "de" to mapOf("tag" to "Jeden Tag einen Schritt näher.", "dream" to "Mein Traum", "goal" to "Ziel", "saved" to "Gespart", "left" to "Verbleibend", "add" to "HEUTIGEN SCHRITT HINZUFÜGEN", "locked" to "Heutiger Schritt ist erledigt", "edit" to "Traum bearbeiten", "language" to "Sprache", "name" to "Wovon träumst du?", "target" to "Zielbetrag", "daily" to "Täglicher Schritt", "save" to "Speichern", "cancel" to "Abbrechen", "ready" to "Jetzt bereit", "hours" to "Verfügbar in", "days" to "Tagen", "day" to "Tag", "completed" to "Traum erreicht ✦", "progress" to "Fortschritt"),
        "pl" to mapOf("tag" to "Każdego dnia krok bliżej.", "dream" to "Moje marzenie", "goal" to "Cel", "saved" to "Odłożono", "left" to "Pozostało", "add" to "DODAJ DZISIEJSZY KROK", "locked" to "Dzisiejszy krok wykonany", "edit" to "Edytuj marzenie", "language" to "Język", "name" to "O czym marzysz?", "target" to "Kwota docelowa", "daily" to "Dzienny krok", "save" to "Zapisz", "cancel" to "Anuluj", "ready" to "Gotowe teraz", "hours" to "Dostępne za", "days" to "dni", "day" to "dzień", "completed" to "Marzenie osiągnięte ✦", "progress" to "Postęp"),
        "fr" to mapOf("tag" to "Un pas de plus chaque jour.", "dream" to "Mon rêve", "goal" to "Objectif", "saved" to "Épargné", "left" to "Reste", "add" to "AJOUTER L'ÉTAPE DU JOUR", "locked" to "Étape du jour terminée", "edit" to "Modifier le rêve", "language" to "Langue", "name" to "De quoi rêves-tu ?", "target" to "Montant cible", "daily" to "Étape quotidienne", "save" to "Enregistrer", "cancel" to "Annuler", "ready" to "Prêt maintenant", "hours" to "Disponible dans", "days" to "jours", "day" to "jour", "completed" to "Rêve atteint ✦", "progress" to "Progression"),
        "es" to mapOf("tag" to "Cada día, un paso más cerca.", "dream" to "Mi sueño", "goal" to "Objetivo", "saved" to "Ahorrado", "left" to "Falta", "add" to "AÑADIR EL PASO DE HOY", "locked" to "Paso de hoy completado", "edit" to "Editar sueño", "language" to "Idioma", "name" to "¿Con qué sueñas?", "target" to "Cantidad objetivo", "daily" to "Paso diario", "save" to "Guardar", "cancel" to "Cancelar", "ready" to "Listo ahora", "hours" to "Disponible en", "days" to "días", "day" to "día", "completed" to "Sueño alcanzado ✦", "progress" to "Progreso"),
        "it" to mapOf("tag" to "Ogni giorno un passo più vicino.", "dream" to "Il mio sogno", "goal" to "Obiettivo", "saved" to "Risparmiato", "left" to "Manca", "add" to "AGGIUNGI IL PASSO DI OGGI", "locked" to "Passo di oggi completato", "edit" to "Modifica sogno", "language" to "Lingua", "name" to "Cosa sogni?", "target" to "Importo obiettivo", "daily" to "Passo giornaliero", "save" to "Salva", "cancel" to "Annulla", "ready" to "Pronto ora", "hours" to "Disponibile tra", "days" to "giorni", "day" to "giorno", "completed" to "Sogno raggiunto ✦", "progress" to "Progresso")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolveLanguage()
        render()
        if (!prefs.contains("target")) root.post { showEditDialog() }
    }

    private fun resolveLanguage() {
        val chosen = prefs.getString("language", "auto") ?: "auto"
        val system = Locale.getDefault().language
        lang = if (chosen == "auto") {
            if (tr.containsKey(system)) system else "en"
        } else chosen
        if (!tr.containsKey(lang)) lang = "en"
    }

    private fun s(key: String) = tr[lang]?.get(key) ?: tr["en"]?.get(key) ?: key

    private fun render() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(24))
            setBackgroundColor(Color.rgb(247, 244, 238))
        }
        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "DREAM"
            textSize = 28f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.rgb(32, 51, 42))
            letterSpacing = 0.16f
        }
        top.addView(brand, LinearLayout.LayoutParams(0, dp(52), 1f))
        val langBtn = TextView(this).apply {
            text = "◉  ${languageNames[prefs.getString("language", "auto")] ?: "Auto"}"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(32, 51, 42))
            background = rounded(Color.rgb(232, 228, 217), 24f)
            setPadding(dp(13), 0, dp(13), 0)
            setOnClickListener { showLanguageDialog() }
        }
        top.addView(langBtn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(40)))
        root.addView(top)

        root.addView(TextView(this).apply {
            text = s("tag")
            textSize = 15f
            setTextColor(Color.rgb(102, 111, 104))
            setPadding(0, 0, 0, dp(24))
        })

        val name = prefs.getString("name", s("dream")) ?: s("dream")
        val target = prefs.getFloat("target", 1000f).toDouble()
        val daily = prefs.getFloat("daily", 10f).toDouble()
        val saved = prefs.getFloat("saved", 0f).toDouble()
        val remaining = (target - saved).coerceAtLeast(0.0)
        val progress = if (target > 0) (saved / target).coerceIn(0.0, 1.0) else 0.0

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(22))
            background = rounded(Color.rgb(32, 51, 42), 28f)
        }
        hero.addView(TextView(this).apply {
            text = "✦  ${s("dream")}"
            textSize = 13f
            setTextColor(Color.rgb(231, 212, 165))
        })
        hero.addView(TextView(this).apply {
            text = name
            textSize = 30f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, dp(10), 0, dp(18))
        })
        hero.addView(TextView(this).apply {
            text = "${(progress * 100).roundToInt()}%  •  ${money(saved)} / ${money(target)}"
            textSize = 15f
            setTextColor(Color.rgb(225, 230, 225))
        })
        val barBg = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = rounded(Color.rgb(72, 88, 78), 12f)
            setPadding(0, 0, 0, 0)
        }
        val fill = View(this).apply { background = rounded(Color.rgb(231, 212, 165), 12f) }
        barBg.addView(fill, LinearLayout.LayoutParams(0, dp(10), progress.toFloat().coerceAtLeast(0.01f)))
        if (progress < 1) barBg.addView(View(this), LinearLayout.LayoutParams(0, dp(10), (1f - progress.toFloat()).coerceAtLeast(0.01f)))
        hero.addView(barBg, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(10)).apply { topMargin = dp(14) })
        root.addView(hero, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(16) })

        val stats = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        stats.addView(statCard(s("left"), money(remaining)), LinearLayout.LayoutParams(0, dp(105), 1f).apply { marginEnd = dp(8) })
        stats.addView(statCard(s("daily"), money(daily)), LinearLayout.LayoutParams(0, dp(105), 1f).apply { marginStart = dp(8) })
        root.addView(stats)

        val now = System.currentTimeMillis()
        val last = prefs.getLong("last_step", 0L)
        val wait = 24L * 60L * 60L * 1000L
        val canAdd = last == 0L || now - last >= wait
        val completed = saved >= target && target > 0

        val action = TextView(this).apply {
            text = when {
                completed -> s("completed")
                canAdd -> s("add")
                else -> s("locked")
            }
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(if (canAdd && !completed) Color.WHITE else Color.rgb(95, 104, 97))
            background = rounded(if (canAdd && !completed) Color.rgb(78, 111, 88) else Color.rgb(229, 226, 217), 22f)
            setOnClickListener {
                if (canAdd && !completed) {
                    prefs.edit().putFloat("saved", (saved + daily).coerceAtMost(target).toFloat()).putLong("last_step", System.currentTimeMillis()).apply()
                    render()
                }
            }
        }
        root.addView(action, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(58)).apply { topMargin = dp(22) })

        val status = TextView(this).apply {
            text = if (canAdd) s("ready") else countdown(last + wait - now)
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(104, 111, 105))
            setPadding(0, dp(10), 0, dp(18))
        }
        root.addView(status)

        val edit = TextView(this).apply {
            text = "✎  ${s("edit")}"
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(Color.rgb(32, 51, 42))
            background = rounded(Color.TRANSPARENT, 20f, Color.rgb(205, 199, 188))
            setOnClickListener { showEditDialog() }
        }
        root.addView(edit, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)))

        root.addView(TextView(this).apply {
            text = "MADE IQ DIGITAL  •  DREAM 1.0"
            textSize = 10f
            gravity = Gravity.CENTER
            letterSpacing = 0.18f
            setTextColor(Color.rgb(151, 148, 140))
            setPadding(0, dp(30), 0, dp(8))
        })
    }

    private fun statCard(label: String, value: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(14))
            background = rounded(Color.WHITE, 22f)
            addView(TextView(context).apply { text = label.uppercase(); textSize = 10f; letterSpacing = .12f; setTextColor(Color.rgb(125, 130, 125)) })
            addView(TextView(context).apply { text = value; textSize = 21f; setTypeface(typeface, Typeface.BOLD); setTextColor(Color.rgb(32, 51, 42)); setPadding(0, dp(7), 0, 0) })
        }
    }

    private fun showEditDialog() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(22), dp(6), dp(22), 0) }
        val name = EditText(this).apply { hint = s("name"); setText(prefs.getString("name", "")); isSingleLine = true }
        val target = EditText(this).apply { hint = s("target"); inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setText(if (prefs.contains("target")) prefs.getFloat("target", 0f).toString() else "") }
        val daily = EditText(this).apply { hint = s("daily"); inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setText(if (prefs.contains("daily")) prefs.getFloat("daily", 0f).toString() else "") }
        box.addView(name); box.addView(target); box.addView(daily)
        val d = AlertDialog.Builder(this).setTitle(s("edit")).setView(box).setNegativeButton(s("cancel"), null).setPositiveButton(s("save"), null).create()
        d.setOnShowListener {
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val t = target.text.toString().replace(',', '.').toFloatOrNull()
                val step = daily.text.toString().replace(',', '.').toFloatOrNull()
                if (name.text.isNotBlank() && t != null && t > 0 && step != null && step > 0) {
                    prefs.edit().putString("name", name.text.toString().trim()).putFloat("target", t).putFloat("daily", step).apply()
                    d.dismiss(); render()
                }
            }
        }
        d.show()
    }

    private fun showLanguageDialog() {
        val keys = languageNames.keys.toTypedArray()
        val labels = languageNames.values.toTypedArray()
        val current = keys.indexOf(prefs.getString("language", "auto"))
        AlertDialog.Builder(this).setTitle(s("language")).setSingleChoiceItems(labels, current) { dialog, which ->
            prefs.edit().putString("language", keys[which]).apply()
            dialog.dismiss(); resolveLanguage(); render()
        }.show()
    }

    private fun money(value: Double): String {
        return try {
            val locale = Locale.getDefault()
            val f = NumberFormat.getCurrencyInstance(locale)
            if (f.currency == null) f.currency = Currency.getInstance("EUR")
            f.format(value)
        } catch (_: Exception) { String.format(Locale.US, "%.2f", value) }
    }

    private fun countdown(ms: Long): String {
        val hours = ((ms.coerceAtLeast(0L) + 3_599_999L) / 3_600_000L).coerceAtLeast(1)
        return "${s("hours")} ${hours}h"
    }

    private fun rounded(fill: Int, radius: Float, stroke: Int? = null) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(fill)
        cornerRadius = dp(radius.toInt()).toFloat()
        if (stroke != null) setStroke(dp(1), stroke)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).roundToInt()
}
