from pathlib import Path

p = Path('app/src/main/java/digital/madeiq/dream/MainActivity.kt')
s = p.read_text(encoding='utf-8')

# Keep the approved daily-saving calculation and wording.
old_daily = '        val dailyGoal = remaining / days'
new_daily = '        val dailyGoal = if (remaining <= 0.0) 0.0 else ceil((remaining / days) * 100.0) / 100.0'
if old_daily in s:
    s = s.replace(old_daily, new_daily, 1)

old_metric = 'metrics.addView(metric(tr("Dnes potrebujem odložiť", "Need to save today"), money(dailyGoal), green), LinearLayout.LayoutParams(0, -2, 1f))'
new_metric = 'metrics.addView(metric(tr("Denne musíš odložiť", "You must save per day"), money(dailyGoal), green), LinearLayout.LayoutParams(0, -2, 1f))'
if old_metric in s:
    s = s.replace(old_metric, new_metric, 1)

# Put the create action inside the same ScrollView as all form fields.
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
        col.addView(Space(this), LinearLayout.LayoutParams(1, dp(32)))
        root.addView(scroll, LinearLayout.LayoutParams(-1, -1))

        // ADJUST_RESIZE already shrinks the Activity to the visible area above IME.
        // Do not add IME height again as padding: that caused the form to jump/over-scroll.
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, bars.bottom + dp(16))
            insets
        }
        ViewCompat.requestApplyInsets(scroll)
    }
'''
if old2 in s:
    s = s.replace(old2, new2, 1)

p.write_text(s, encoding='utf-8')
print('DREAM keyboard scrolling fix applied')
