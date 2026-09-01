from pathlib import Path

p = Path("app/src/main/java/digital/madeiq/dream/MainActivity.kt")
s = p.read_text(encoding="utf-8")

# Premium visual pass: tighter hierarchy, calmer navy surfaces and restrained gold.
replacements = {
    "private val bg = Color.rgb(5, 12, 22)": "private val bg = Color.rgb(4, 11, 20)",
    "private val surface = Color.rgb(12, 23, 37)": "private val surface = Color.rgb(10, 21, 35)",
    "private val surface2 = Color.rgb(18, 31, 48)": "private val surface2 = Color.rgb(15, 28, 44)",
    "private val line = Color.rgb(47, 59, 75)": "private val line = Color.rgb(52, 65, 82)",
    "private val gold = Color.rgb(224, 174, 82)": "private val gold = Color.rgb(218, 166, 73)",
    "private val gold2 = Color.rgb(248, 202, 116)": "private val gold2 = Color.rgb(244, 197, 108)",
    "textSize = 15f\n        setSingleLine(true)": "textSize = 14f\n        setSingleLine(true)",
    "background = solid(Color.rgb(16, 27, 42), 12, Color.rgb(59, 70, 84))": "background = gradient(intArrayOf(Color.rgb(18, 31, 48), Color.rgb(12, 24, 39)), 12, GradientDrawable.Orientation.LEFT_RIGHT).apply { setStroke(dp(1), Color.rgb(61, 74, 91)) }",
    "layoutParams = LinearLayout.LayoutParams(-1, dp(56)).apply { setMargins(dp(16), dp(5), dp(16), dp(5)) }": "layoutParams = LinearLayout.LayoutParams(-1, dp(50)).apply { setMargins(dp(16), dp(4), dp(16), dp(4)) }",
    "layoutParams = LinearLayout.LayoutParams(-1, dp(270)).apply { setMargins(dp(16), dp(4), dp(16), dp(14)) }": "layoutParams = LinearLayout.LayoutParams(-1, dp(230)).apply { setMargins(dp(16), dp(2), dp(16), dp(12)) }",
    "setPadding(dp(24), dp(20), dp(24), dp(30))": "setPadding(dp(24), dp(16), dp(24), dp(22))",
    "addView(txt(tr(\"Nový cieľ\", \"New goal\"), 20f, cream, true).apply { setPadding(dp(16), dp(4), 0, dp(8)) })": "addView(txt(tr(\"Vytvor svoj DREAM\", \"Create your DREAM\"), 20f, cream, true).apply { setPadding(dp(16), dp(4), 0, dp(4)) })\n        col.addView(txt(tr(\"Jeden jasný cieľ. Každý deň o krok bližšie.\", \"One clear goal. One step closer every day.\"), 13f, muted).apply { setPadding(dp(16), 0, dp(16), dp(8)) })",
    "layoutParams = LinearLayout.LayoutParams(-1, dp(52)).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }": "layoutParams = LinearLayout.LayoutParams(-1, dp(50)).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }",
}
for old, new in replacements.items():
    if old not in s:
        raise SystemExit(f"Expected source fragment missing: {old[:70]}")
    s = s.replace(old, new, 1)

# Ensure the selected field and create action remain visible above the Android keyboard.
old_focus = "scroll.postDelayed({ v.requestRectangleOnScreen(Rect(0, 0, v.width, v.height + dp(150)), true) }, 120)"
new_focus = "scroll.postDelayed({ v.requestRectangleOnScreen(Rect(0, -dp(12), v.width, v.height + dp(96)), true) }, 120)"
if old_focus not in s:
    raise SystemExit("Keyboard focus fragment missing")
s = s.replace(old_focus, new_focus, 1)

p.write_text(s, encoding="utf-8")
print("DREAM premium visual and keyboard pass applied")
