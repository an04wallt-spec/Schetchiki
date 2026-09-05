from pathlib import Path
import sys

root = Path(sys.argv[1])
main = root / 'smali/ru/schetchiki/app/MainActivity.smali'
text = main.read_text(encoding='utf-8')

needle = '.field private touchDownY:F\n'
repl = needle + '\n.field private historyLastTap:J\n\n.field private historyTapMonth:Ljava/time/YearMonth;\n\n.field private historyUnlockedMonth:Ljava/time/YearMonth;\n'
assert needle in text and 'historyUnlockedMonth' not in text
text = text.replace(needle, repl, 1)

old = '''    .line 246\n    iget-object v0, p0, Lru/schetchiki/app/MainActivity;->calendarMonth:Ljava/time/YearMonth;\n\n    invoke-virtual {p1, v0}, Ljava/time/YearMonth;->equals(Ljava/lang/Object;)Z\n\n    move-result v0\n\n    if-nez v0, :cond_0\n\n    return-void\n\n    .line 247\n    :cond_0\n'''
new = '''    .line 246\n    iget-object v0, p0, Lru/schetchiki/app/MainActivity;->calendarMonth:Ljava/time/YearMonth;\n\n    invoke-virtual {p1, v0}, Ljava/time/YearMonth;->equals(Ljava/lang/Object;)Z\n\n    move-result v0\n\n    if-nez v0, :cond_0\n\n    if-nez p3, :history_edit_reject\n\n    iget-object v0, p0, Lru/schetchiki/app/MainActivity;->historyUnlockedMonth:Ljava/time/YearMonth;\n\n    if-eqz v0, :history_edit_reject\n\n    invoke-virtual {p1, v0}, Ljava/time/YearMonth;->equals(Ljava/lang/Object;)Z\n\n    move-result v0\n\n    if-nez v0, :cond_0\n\n    :history_edit_reject\n    return-void\n\n    .line 247\n    :cond_0\n'''
assert old in text
text = text.replace(old, new, 1)

old = '''.method private sectionHeader(Ljava/time/YearMonth;)Landroid/view/View;\n    .locals 7\n\n    .line 176\n    invoke-direct {p0, p1}, Lru/schetchiki/app/MainActivity;->stateOf(Ljava/time/YearMonth;)Lru/schetchiki/app/MainActivity$MonthState;\n'''
new = '''.method private sectionHeader(Ljava/time/YearMonth;)Landroid/view/View;\n    .locals 8\n\n    move-object v7, p1\n\n    .line 176\n    invoke-direct {p0, p1}, Lru/schetchiki/app/MainActivity;->stateOf(Ljava/time/YearMonth;)Lru/schetchiki/app/MainActivity$MonthState;\n'''
assert old in text
text = text.replace(old, new, 1)

old = '''    invoke-virtual {p1, v0}, Landroid/widget/TextView;->setBackground(Landroid/graphics/drawable/Drawable;)V\n\n    .line 192\n    new-instance v0, Landroid/widget/LinearLayout$LayoutParams;\n'''
new = '''    invoke-virtual {p1, v0}, Landroid/widget/TextView;->setBackground(Landroid/graphics/drawable/Drawable;)V\n\n    invoke-direct {p0, v7}, Lru/schetchiki/app/MainActivity;->stateOf(Ljava/time/YearMonth;)Lru/schetchiki/app/MainActivity$MonthState;\n\n    move-result-object v3\n\n    sget-object v4, Lru/schetchiki/app/MainActivity$MonthState;->ARCHIVE:Lru/schetchiki/app/MainActivity$MonthState;\n\n    if-ne v3, v4, :history_badge_listener_done\n\n    new-instance v3, Lru/schetchiki/app/MainActivity$HistoryClickListener;\n\n    invoke-direct {v3, p0, v7}, Lru/schetchiki/app/MainActivity$HistoryClickListener;-><init>(Lru/schetchiki/app/MainActivity;Ljava/time/YearMonth;)V\n\n    invoke-virtual {p1, v3}, Landroid/widget/TextView;->setOnClickListener(Landroid/view/View$OnClickListener;)V\n\n    :history_badge_listener_done\n    .line 192\n    new-instance v0, Landroid/widget/LinearLayout$LayoutParams;\n'''
assert old in text
text = text.replace(old, new, 1)

old = '''    invoke-virtual {v10, v14, v1}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V\n\n    .line 227\n    sget-object v1, Lru/schetchiki/app/MainActivity$MonthState;->CURRENT:Lru/schetchiki/app/MainActivity$MonthState;\n'''
new = '''    invoke-virtual {v10, v14, v1}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V\n\n    sget-object v1, Lru/schetchiki/app/MainActivity$MonthState;->FUTURE:Lru/schetchiki/app/MainActivity$MonthState;\n\n    if-eq v9, v1, :history_meter_listener_done\n\n    new-instance v1, Lru/schetchiki/app/MainActivity$$ExternalSyntheticLambda7;\n\n    invoke-direct {v1, p0, v6, v7}, Lru/schetchiki/app/MainActivity$$ExternalSyntheticLambda7;-><init>(Lru/schetchiki/app/MainActivity;Ljava/time/YearMonth;I)V\n\n    invoke-virtual {v14, v1}, Lru/schetchiki/app/MainActivity$DrumView;->setOnClickListener(Landroid/view/View$OnClickListener;)V\n\n    :history_meter_listener_done\n    .line 227\n    sget-object v1, Lru/schetchiki/app/MainActivity$MonthState;->CURRENT:Lru/schetchiki/app/MainActivity$MonthState;\n'''
assert old in text
text = text.replace(old, new, 1)

marker = '.method private sectionHeader(Ljava/time/YearMonth;)Landroid/view/View;\n'
helper = '''.method final historyHeaderTap(Ljava/time/YearMonth;)V\n    .locals 6\n\n    invoke-static {}, Landroid/os/SystemClock;->elapsedRealtime()J\n\n    move-result-wide v0\n\n    iget-object v2, p0, Lru/schetchiki/app/MainActivity;->historyTapMonth:Ljava/time/YearMonth;\n\n    if-eqz v2, :history_first_tap\n\n    invoke-virtual {p1, v2}, Ljava/time/YearMonth;->equals(Ljava/lang/Object;)Z\n\n    move-result v2\n\n    if-eqz v2, :history_first_tap\n\n    iget-wide v2, p0, Lru/schetchiki/app/MainActivity;->historyLastTap:J\n\n    sub-long v2, v0, v2\n\n    const-wide/16 v4, 0x1f4\n\n    cmp-long v4, v2, v4\n\n    if-lez v4, :history_unlock\n\n    :history_first_tap\n    iput-object p1, p0, Lru/schetchiki/app/MainActivity;->historyTapMonth:Ljava/time/YearMonth;\n\n    iput-wide v0, p0, Lru/schetchiki/app/MainActivity;->historyLastTap:J\n\n    return-void\n\n    :history_unlock\n    iput-object p1, p0, Lru/schetchiki/app/MainActivity;->historyUnlockedMonth:Ljava/time/YearMonth;\n\n    const/4 v2, 0x0\n\n    iput-object v2, p0, Lru/schetchiki/app/MainActivity;->historyTapMonth:Ljava/time/YearMonth;\n\n    const-wide/16 v2, 0x0\n\n    iput-wide v2, p0, Lru/schetchiki/app/MainActivity;->historyLastTap:J\n\n    const/4 v2, 0x0\n\n    invoke-direct {p0, v2}, Lru/schetchiki/app/MainActivity;->showPage(I)V\n\n    return-void\n.end method\n\n'''
assert marker in text and 'historyHeaderTap' not in text
text = text.replace(marker, helper + marker, 1)
main.write_text(text, encoding='utf-8')

listener = root / 'smali/ru/schetchiki/app/MainActivity$HistoryClickListener.smali'
listener.write_text('''.class final Lru/schetchiki/app/MainActivity$HistoryClickListener;\n.super Ljava/lang/Object;\n.implements Landroid/view/View$OnClickListener;\n\n.field private final activity:Lru/schetchiki/app/MainActivity;\n.field private final month:Ljava/time/YearMonth;\n\n.method constructor <init>(Lru/schetchiki/app/MainActivity;Ljava/time/YearMonth;)V\n    .locals 0\n    invoke-direct {p0}, Ljava/lang/Object;-><init>()V\n    iput-object p1, p0, Lru/schetchiki/app/MainActivity$HistoryClickListener;->activity:Lru/schetchiki/app/MainActivity;\n    iput-object p2, p0, Lru/schetchiki/app/MainActivity$HistoryClickListener;->month:Ljava/time/YearMonth;\n    return-void\n.end method\n\n.method public onClick(Landroid/view/View;)V\n    .locals 2\n    iget-object v0, p0, Lru/schetchiki/app/MainActivity$HistoryClickListener;->activity:Lru/schetchiki/app/MainActivity;\n    iget-object v1, p0, Lru/schetchiki/app/MainActivity$HistoryClickListener;->month:Ljava/time/YearMonth;\n    invoke-virtual {v0, v1}, Lru/schetchiki/app/MainActivity;->historyHeaderTap(Ljava/time/YearMonth;)V\n    return-void\n.end method\n''', encoding='utf-8')
print('PATCH_OK')
