package com.exemplo.assistentefacil;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/** Tela única com 3 abas: Ajuda (IA), Resultado e Ajustes. A interface é montada por código. */
public class MainActivity extends Activity {

    private static final int REQ_VOICE = 1;

    private Prefs prefs;
    private AiService ai;
    private DeviceActions device;

    private LinearLayout root, content, nav;
    private ScrollView scroll;
    private final Button[] navBtns = new Button[3];

    private int tab = 0;
    private boolean loading = false;
    private AiService.Result result;

    private int bg, card, ink, muted, pri, onPri, ok, line;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = new Prefs(this);
        ai = new AiService();
        device = new DeviceActions(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setFitsSystemWindows(true);

        scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(16));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        String[] labels = {"🎤\nAjuda", "✅\nResultado", "⚙️\nAjustes"};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            Button nb = new Button(this);
            nb.setAllCaps(false);
            nb.setText(labels[i]);
            nb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            nb.setBackgroundColor(Color.TRANSPARENT);
            nb.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { tab = idx; render(); }
            });
            navBtns[i] = nb;
            nav.addView(nb, new LinearLayout.LayoutParams(0, -2, 1f));
        }
        root.addView(nav, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
        render();
    }

    @Override
    protected void onDestroy() {
        device.shutdown();
        super.onDestroy();
    }

    // ---------------------------------------------------------------- AÇÕES

    /** Envia o pedido para a IA e mostra a tela de Resultado. */
    private void ask(String text) {
        loading = true; result = null; tab = 1;
        render();
        ai.ask(text, new AiService.Callback() {
            @Override public void onResult(AiService.Result r) {
                loading = false; result = r;
                if (prefs.get("voice", true)) device.speak(r.title + ". Pronto, tudo certo.");
                render();
            }
        });
    }

    private void startVoice() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale o que você precisa");
        try {
            startActivityForResult(i, REQ_VOICE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Reconhecimento de voz indisponível (simulando)", Toast.LENGTH_SHORT).show();
            ask("ligar para minha filha");
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_VOICE && res == RESULT_OK && data != null) {
            ArrayList<String> list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (list != null && !list.isEmpty()) ask(list.get(0));
        }
    }

    // --------------------------------------------------------------- TELAS

    private void render() {
        applyColors();
        content.removeAllViews();
        if (tab == 0) screenHelp();
        else if (tab == 1) screenResult();
        else screenSettings();
        for (int i = 0; i < 3; i++) navBtns[i].setTextColor(i == tab ? pri : muted);
        scroll.scrollTo(0, 0);
    }

    private void screenHelp() {
        content.addView(tv("Olá, Dona Maria 👋", 1.3, true, ink));
        LinearLayout c = cardBox();
        c.addView(tv("Como posso ajudar?", 1, false, ink));
        content.addView(c, lp(12));

        Button mic = btn("🎤\nToque e fale", true, new View.OnClickListener() {
            @Override public void onClick(View v) { startVoice(); }
        });
        mic.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(1.2));
        mic.setPadding(dp(12), dp(28), dp(12), dp(28));
        content.addView(mic, lp(12));

        final EditText et = new EditText(this);
        et.setHint("Ou escreva aqui");
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(1));
        et.setTextColor(ink);
        et.setHintTextColor(muted);
        et.setSingleLine(true);
        et.setImeOptions(EditorInfo.IME_ACTION_SEND);
        et.setBackground(box(card, line, 14));
        et.setPadding(dp(14), dp(14), dp(14), dp(14));
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent e) {
                String s = et.getText().toString().trim();
                if (!s.isEmpty()) ask(s);
                return true;
            }
        });
        content.addView(et, lp(12));

        content.addView(tv("Atalhos:", 0.8, false, muted), lp(12));
        String[][] q = {
                {"📞", "Ligar para minha filha"}, {"📷", "Mandar foto no WhatsApp"},
                {"🔠", "Aumentar a letra"}, {"⛅", "Ver a previsão do tempo"}};
        for (int i = 0; i < q.length; i += 2) {
            LinearLayout row = new LinearLayout(this);
            for (int j = i; j < i + 2; j++) {
                final String label = q[j][1];
                Button b = btn(q[j][0] + "\n" + label, false, new View.OnClickListener() {
                    @Override public void onClick(View v) { ask(label); }
                });
                b.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(0.8));
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1f);
                p.setMargins(dp(4), dp(4), dp(4), dp(4));
                row.addView(b, p);
            }
            content.addView(row, lp(0));
        }
    }

    private void screenResult() {
        if (loading) {
            content.addView(tv("Um momento...", 1.3, true, ink));
            LinearLayout c = cardBox();
            c.addView(tv("⏳ Estou cuidando disso para você.", 1, false, ink));
            content.addView(c, lp(12));
            return;
        }
        if (result == null) {
            content.addView(tv("Resultado", 1.3, true, ink));
            LinearLayout c = cardBox();
            c.addView(tv("Nada por aqui ainda. Peça algo na tela de Ajuda.", 1, false, ink));
            content.addView(c, lp(12));
            content.addView(btn("Ir para Ajuda", true, new View.OnClickListener() {
                @Override public void onClick(View v) { tab = 0; render(); }
            }), lp(8));
            return;
        }

        content.addView(tv("Pronto! Tudo certo ✅", 1.3, true, ink));
        LinearLayout c1 = cardBox();
        c1.addView(tv("Você pediu:", 0.75, false, muted));
        c1.addView(tv(result.title, 1, true, ink));
        content.addView(c1, lp(12));

        LinearLayout c2 = cardBox();
        for (String s : result.steps) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(8), 0, dp(8));
            TextView chk = tv("✓", 0.9, true, Color.WHITE);
            chk.setGravity(Gravity.CENTER);
            GradientDrawable g = new GradientDrawable();
            g.setShape(GradientDrawable.OVAL);
            g.setColor(ok);
            chk.setBackground(g);
            row.addView(chk, new LinearLayout.LayoutParams(dp(30), dp(30)));
            TextView t = tv(s, 0.9, false, ink);
            t.setPadding(dp(10), 0, 0, 0);
            row.addView(t);
            c2.addView(row);
        }
        content.addView(c2, lp(12));

        if (prefs.get("voice", true))
            content.addView(tv("🔊 A resposta foi lida em voz alta.", 0.75, false, muted), lp(8));

        content.addView(btn("▶ Fazer agora", true, new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (device.execute(result, prefs)) render();
            }
        }), lp(12));
        content.addView(btn("Obrigada, voltar", false, new View.OnClickListener() {
            @Override public void onClick(View v) { tab = 0; render(); }
        }), lp(8));
        content.addView(btn("Desfazer", false, new View.OnClickListener() {
            @Override public void onClick(View v) { result = null; tab = 0; render(); }
        }), lp(8));
        if (prefs.get("family", true)) {
            content.addView(btn("📨 Avisar minha filha", false, new View.OnClickListener() {
                @Override public void onClick(View v) { ((Button) v).setText("✓ Ana foi avisada"); }
            }), lp(8));
        }
    }

    private void screenSettings() {
        content.addView(tv("Ajustes", 1.3, true, ink));

        LinearLayout c1 = cardBox();
        c1.addView(tv("Tamanho da letra", 1, true, ink));
        LinearLayout row = new LinearLayout(this);
        final int[] sizes = {18, 20, 24};
        String[] names = {"A", "A+", "A++"};
        for (int i = 0; i < 3; i++) {
            final int size = sizes[i];
            Button b = btn(names[i], prefs.fontSize() == size, new View.OnClickListener() {
                @Override public void onClick(View v) { prefs.setFontSize(size); render(); }
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1f);
            p.setMargins(dp(3), dp(8), dp(3), 0);
            row.addView(b, p);
        }
        c1.addView(row);
        content.addView(c1, lp(12));

        LinearLayout c2 = cardBox();
        addSwitch(c2, "Alto contraste (modo escuro)", "contrast", false);
        addSwitch(c2, "Ler respostas em voz alta", "voice", true);
        addSwitch(c2, "Tela inicial simplificada", "simple", true);
        addSwitch(c2, "Familiar de confiança (Ana)", "family", true);
        content.addView(c2, lp(12));
    }

    private void addSwitch(LinearLayout parent, String label, final String key, boolean def) {
        Switch sw = new Switch(this);
        sw.setText(label);
        sw.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(0.9));
        sw.setTextColor(ink);
        sw.setChecked(prefs.get(key, def));
        sw.setPadding(0, dp(12), 0, dp(12));
        sw.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(android.widget.CompoundButton b, boolean on) {
                prefs.set(key, on);
                render();
            }
        });
        parent.addView(sw, lp(0));
    }

    // ------------------------------------------------------------- VISUAL

    private void applyColors() {
        if (prefs.get("contrast", false)) {
            bg = 0xFF15171A; card = 0xFF22262B; ink = 0xFFF2F2F2; muted = 0xFFB4B4B4;
            pri = 0xFF6DB3FF; onPri = 0xFF0A1A2B; ok = 0xFF6FD48C; line = 0xFF3A3F46;
        } else {
            bg = 0xFFF4F1EA; card = 0xFFFFFFFF; ink = 0xFF1B1B1B; muted = 0xFF5C5C5C;
            pri = 0xFF0B5CAD; onPri = 0xFFFFFFFF; ok = 0xFF1E7A3C; line = 0xFFD8D2C4;
        }
        root.setBackgroundColor(bg);
        nav.setBackgroundColor(card);
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
    private float sp(double scale) { return (float) (prefs.fontSize() * scale); }

    private GradientDrawable box(int fill, int stroke, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        g.setStroke(dp(2), stroke);
        return g;
    }

    private TextView tv(String s, double scale, boolean bold, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(scale));
        t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    private LinearLayout.LayoutParams lp(int topDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(topDp);
        return p;
    }

    private LinearLayout cardBox() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(14), dp(14), dp(14), dp(14));
        l.setBackground(box(card, line, 18));
        return l;
    }

    private Button btn(String label, boolean primary, View.OnClickListener l) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(label);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(1));
        b.setTypeface(null, Typeface.BOLD);
        b.setTextColor(primary ? onPri : ink);
        b.setBackground(box(primary ? pri : card, primary ? pri : line, 16));
        b.setPadding(dp(12), dp(16), dp(12), dp(16));
        b.setOnClickListener(l);
        return b;
    }
}
