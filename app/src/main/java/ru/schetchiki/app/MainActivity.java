package ru.schetchiki.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Clean source reconstruction of the known-good v0.5 behavior.
 *
 * IMPORTANT:
 * The immutable reference APK is stored under /reference.
 * Do not delete or overwrite it. Any future change should be made here,
 * built by CI, and tested before replacing an installed build.
 */
public class MainActivity extends Activity {

    private static final String PREFS = "schetchiki";
    private static final YearMonth FIRST_HISTORY_MONTH = YearMonth.of(2026, 7);

    private static final String[] JULY_2026_READINGS = {
            "234.59", "477.75", "84.27", "110.40"
    };

    private static final Meter[] METERS = {
            new Meter("ВАННАЯ", "ГОРЯЧАЯ", "09-0049452", "0.700"),
            new Meter("ВАННАЯ", "ХОЛОДНАЯ", "09-0069655", "1.300"),
            new Meter("КУХНЯ", "ГОРЯЧАЯ", "09-0046087", "0.160"),
            new Meter("КУХНЯ", "ХОЛОДНАЯ", "09-0068731", "0.600")
    };

    private static final int PAPER = Color.rgb(244, 240, 231);
    private static final int CARD = Color.WHITE;
    private static final int NAVY = Color.rgb(23, 50, 77);
    private static final int MUTED = Color.rgb(105, 111, 118);
    private static final int COLD = Color.rgb(50, 105, 160);
    private static final int HOT = Color.rgb(177, 63, 52);

    private SharedPreferences prefs;
    private YearMonth calendarMonth;
    private LinearLayout content;
    private TextView monthTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        installJulyHistory();
        migrateVersionOneData();
        calendarMonth = YearMonth.now();
        buildScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (content != null) showPage(calendarMonth);
    }

    private void installJulyHistory() {
        SharedPreferences.Editor e = prefs.edit();
        boolean changed = false;
        for (int i = 0; i < JULY_2026_READINGS.length; i++) {
            String key = readingKey(FIRST_HISTORY_MONTH, i);
            if (!prefs.contains(key)) {
                e.putString(key, JULY_2026_READINGS[i]);
                changed = true;
            }
        }
        if (changed) e.apply();
    }

    /** Compatibility hook retained for the old app data model. */
    private void migrateVersionOneData() {
        if (prefs.contains("last_calendar_month")) return;
        prefs.edit().putString("last_calendar_month", YearMonth.now().toString()).apply();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAPER);
        root.setPadding(dp(14), dp(16), dp(14), dp(10));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);

        Button prev = smallButton("‹");
        Button next = smallButton("›");
        monthTitle = makeText("", 18, true, NAVY);
        monthTitle.setGravity(Gravity.CENTER);

        nav.addView(prev, new LinearLayout.LayoutParams(dp(48), dp(42)));
        nav.addView(monthTitle, new LinearLayout.LayoutParams(0, dp(42), 1f));
        nav.addView(next, new LinearLayout.LayoutParams(dp(48), dp(42)));
        root.addView(nav);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(10), 0, dp(10));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER_VERTICAL);

        Button today = smallButton("ТЕКУЩИЙ МЕСЯЦ");
        Button close = smallButton("ЗАКРЫТЬ");
        bottom.addView(today, new LinearLayout.LayoutParams(0, dp(44), 1f));
        View gap = new View(this);
        bottom.addView(gap, new LinearLayout.LayoutParams(dp(8), 1));
        bottom.addView(close, new LinearLayout.LayoutParams(0, dp(44), 0.55f));
        root.addView(bottom);

        prev.setOnClickListener(v -> {
            YearMonth m = calendarMonth.minusMonths(1);
            if (!m.isBefore(FIRST_HISTORY_MONTH)) {
                calendarMonth = m;
                showPage(calendarMonth);
            }
        });
        next.setOnClickListener(v -> {
            calendarMonth = calendarMonth.plusMonths(1);
            showPage(calendarMonth);
        });
        today.setOnClickListener(v -> {
            calendarMonth = YearMonth.now();
            showPage(calendarMonth);
        });
        close.setOnClickListener(v -> finishAndRemoveTask());

        setContentView(root);
        showPage(calendarMonth);
    }

    private void showPage(YearMonth month) {
        if (monthTitle == null || content == null) return;
        monthTitle.setText(month.format(DateTimeFormatter.ofPattern("LLLL yyyy", new Locale("ru"))).toUpperCase(new Locale("ru")));
        content.removeAllViews();

        MonthState state = stateOf(month);
        String stateText = state == MonthState.ARCHIVE ? "ИСТОРИЯ" : state == MonthState.CURRENT ? "ТЕКУЩИЙ" : "ПРОГНОЗ";
        TextView stateView = makeText(stateText, 12, true, state == MonthState.ARCHIVE ? MUTED : NAVY);
        stateView.setGravity(Gravity.CENTER);
        stateView.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(30));
        slp.bottomMargin = dp(8);
        content.addView(stateView, slp);

        for (int i = 0; i < METERS.length; i++) {
            content.addView(meterCard(month, i, state));
        }
    }

    private View meterCard(YearMonth month, int index, MonthState state) {
        Meter meter = METERS[index];
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(11), dp(14), dp(11));
        card.setBackground(rounded(CARD, 14, Color.rgb(215, 211, 203), 1));

        TextView title = makeText(meter.room + " · " + meter.kind, 15, true,
                "ХОЛОДНАЯ".equals(meter.kind) ? COLD : HOT);
        card.addView(title);

        TextView serial = makeText("№ " + meter.serial, 11, false, MUTED);
        card.addView(serial);

        BigDecimal value = valueForMonth(month, index);
        TextView reading = makeText(formatCompact(value) + " м³", 26, true, NAVY);
        reading.setGravity(Gravity.END);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(44));
        rlp.topMargin = dp(6);
        card.addView(reading, rlp);

        BigDecimal avg = average(index);
        TextView avgText = makeText("среднее +" + formatCompact(avg) + " м³/мес", 12, false, MUTED);
        card.addView(avgText);

        if (state == MonthState.CURRENT) {
            card.setClickable(true);
            card.setOnClickListener(v -> editValue(month, index));
            avgText.setClickable(true);
            avgText.setOnClickListener(v -> editAverage(index));
        } else if (state == MonthState.FUTURE) {
            TextView forecast = makeText("расчётное значение", 11, false, MUTED);
            card.addView(forecast);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        card.setLayoutParams(lp);
        return card;
    }

    private void editValue(YearMonth month, int index) {
        EditText input = numericInput(formatInput(valueForMonth(month, index)));
        new AlertDialog.Builder(this)
                .setTitle(METERS[index].room + " · " + METERS[index].kind)
                .setMessage("Показание за " + month.format(DateTimeFormatter.ofPattern("LLLL yyyy", new Locale("ru"))))
                .setView(input)
                .setNegativeButton("ОТМЕНА", null)
                .setPositiveButton("СОХРАНИТЬ", (d, w) -> {
                    BigDecimal v = parseDecimal(input.getText().toString());
                    if (v == null || v.signum() < 0 || v.compareTo(new BigDecimal("99999.999")) > 0) {
                        Toast.makeText(this, "Проверьте значение", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    prefs.edit().putString(readingKey(month, index), v.setScale(3, RoundingMode.HALF_UP).toPlainString()).apply();
                    showPage(calendarMonth);
                }).show();
    }

    private void editAverage(int index) {
        EditText input = numericInput(formatInput(average(index)));
        new AlertDialog.Builder(this)
                .setTitle("Средний расход")
                .setMessage(METERS[index].room + " · " + METERS[index].kind)
                .setView(input)
                .setNegativeButton("ОТМЕНА", null)
                .setPositiveButton("СОХРАНИТЬ", (d, w) -> {
                    BigDecimal v = parseDecimal(input.getText().toString());
                    if (v == null || v.signum() < 0) {
                        Toast.makeText(this, "Проверьте значение", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    prefs.edit().putString("average_" + index, v.setScale(3, RoundingMode.HALF_UP).toPlainString()).apply();
                    showPage(calendarMonth);
                }).show();
    }

    private EditText numericInput(String value) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setSelectAllOnFocus(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(value.replace('.', ','));
        input.setPadding(dp(14), dp(8), dp(14), dp(8));
        return input;
    }

    private MonthState stateOf(YearMonth month) {
        YearMonth now = YearMonth.now();
        if (month.isBefore(now)) return MonthState.ARCHIVE;
        if (month.equals(now)) return MonthState.CURRENT;
        return MonthState.FUTURE;
    }

    /**
     * v0.5 rollover rule: explicit monthly reading wins. Otherwise the month
     * resolves recursively from previous resolved month + stored average.
     */
    private BigDecimal valueForMonth(YearMonth month, int index) {
        String key = readingKey(month, index);
        if (prefs.contains(key)) {
            return readDecimal(prefs.getString(key, "0"), BigDecimal.ZERO);
        }
        if (month.isBefore(FIRST_HISTORY_MONTH)) return BigDecimal.ZERO;
        return valueForMonth(month.minusMonths(1), index).add(average(index));
    }

    private BigDecimal average(int index) {
        return readDecimal(prefs.getString("average_" + index, METERS[index].defaultAverage),
                new BigDecimal(METERS[index].defaultAverage));
    }

    private String readingKey(YearMonth month, int index) {
        return "reading_" + month + "_" + index;
    }

    private BigDecimal parseDecimal(String text) {
        if (text == null) return null;
        String s = text.trim().replace(',', '.');
        if (s.isEmpty()) return null;
        try { return new BigDecimal(s); } catch (Exception e) { return null; }
    }

    private BigDecimal readDecimal(String text, BigDecimal fallback) {
        try { return new BigDecimal(text); } catch (Exception e) { return fallback; }
    }

    private String formatCompact(BigDecimal value) {
        return String.format(Locale.US, "%09.3f", value.setScale(3, RoundingMode.HALF_UP).doubleValue())
                .replace('.', ',');
    }

    private String formatInput(BigDecimal value) {
        return value.setScale(3, RoundingMode.HALF_UP).toPlainString();
    }

    private TextView makeText(String text, float size, boolean bold, int color) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setIncludeFontPadding(false);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    private Button smallButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(12);
        b.setTextColor(NAVY);
        b.setBackground(rounded(Color.WHITE, 10, Color.rgb(210, 210, 210), 1));
        b.setPadding(dp(6), 0, dp(6), 0);
        return b;
    }

    private GradientDrawable rounded(int fill, int radiusDp, int strokeColor, int strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) g.setStroke(dp(strokeDp), strokeColor);
        return g;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class Meter {
        final String room;
        final String kind;
        final String serial;
        final String defaultAverage;
        Meter(String room, String kind, String serial, String defaultAverage) {
            this.room = room;
            this.kind = kind;
            this.serial = serial;
            this.defaultAverage = defaultAverage;
        }
    }

    private enum MonthState { ARCHIVE, CURRENT, FUTURE }
}
