from pathlib import Path

p = Path('app/src/main/java/digital/madeiq/dream/MainActivity.kt')
s = p.read_text(encoding='utf-8')

# 1) Keep the create-plan action inside the scrollable form so the Android
# keyboard never covers the working area.
old = '''        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val stickyAction = accentButton'''
new = '''        val stickyAction = accentButton'''
if old in s:
    s = s.replace(old, new, 1)

old2 = '''        root.addView(stickyAction)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, if (ime.bottom > 0) dp(4) else bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }
'''
new2 = '''        col.addView(stickyAction)
        col.addView(Space(this), LinearLayout.LayoutParams(1, dp(24)))
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(0, 0, 0, max(bars.bottom, ime.bottom) + dp(24))
            insets
        }
        ViewCompat.requestApplyInsets(scroll)
    }
'''
if old2 in s:
    s = s.replace(old2, new2, 1)

# 2) Required daily saving = remaining amount / remaining days,
# rounded upward to cents so the target is reached by the selected date.
old3 = '        val dailyGoal = remaining / days'
new3 = '        val dailyGoal = if (remaining <= 0.0) 0.0 else ceil((remaining / days) * 100.0) / 100.0'
if old3 not in s:
    raise SystemExit('Expected dailyGoal calculation not found')
s = s.replace(old3, new3, 1)

old4 = 'metrics.addView(metric(tr("Dnes potrebujem odložiť", "Need to save today"), money(dailyGoal), green), LinearLayout.LayoutParams(0, -2, 1f))'
new4 = 'metrics.addView(metric(tr("Denne musíš odložiť", "You must save per day"), money(dailyGoal), green), LinearLayout.LayoutParams(0, -2, 1f))'
if old4 not in s:
    raise SystemExit('Expected daily savings metric not found')
s = s.replace(old4, new4, 1)

p.write_text(s, encoding='utf-8')
print('DREAM fixes applied')
