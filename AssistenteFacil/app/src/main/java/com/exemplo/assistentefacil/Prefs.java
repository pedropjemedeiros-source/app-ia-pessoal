package com.exemplo.assistentefacil;

import android.content.Context;
import android.content.SharedPreferences;

/** Guarda os ajustes do usuário (tamanho da letra, contraste, voz...). */
public class Prefs {
    private final SharedPreferences sp;

    public Prefs(Context c) {
        sp = c.getSharedPreferences("assistente", Context.MODE_PRIVATE);
    }

    public int fontSize() { return sp.getInt("fs", 20); }
    public void setFontSize(int v) { sp.edit().putInt("fs", v).apply(); }

    public boolean get(String key, boolean def) { return sp.getBoolean(key, def); }
    public void set(String key, boolean v) { sp.edit().putBoolean(key, v).apply(); }
}
