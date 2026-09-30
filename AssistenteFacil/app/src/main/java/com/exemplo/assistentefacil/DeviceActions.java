package com.exemplo.assistentefacil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

/** Ações do telefone: ligar, abrir apps, falar em voz alta. */
public class DeviceActions {
    private final Activity act;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    public DeviceActions(Activity a) {
        act = a;
        tts = new TextToSpeech(a, new TextToSpeech.OnInitListener() {
            @Override public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale("pt", "BR"));
                    ttsReady = true;
                }
            }
        });
    }

    public void speak(String text) {
        if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "assistente");
    }

    /** Executa a ação que a IA decidiu. Retorna true se mudou algum ajuste do app. */
    public boolean execute(AiService.Result r, Prefs prefs) {
        try {
            switch (r.action) {
                case "ligar":
                    // Abre o discador (não precisa de permissão). FUTURO: buscar o contato de verdade.
                    act.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:")));
                    break;
                case "abrir":
                    Intent i = act.getPackageManager().getLaunchIntentForPackage(r.target);
                    if (i != null) act.startActivity(i);
                    else Toast.makeText(act, "Aplicativo não instalado", Toast.LENGTH_LONG).show();
                    break;
                case "ajuste":
                    prefs.setFontSize(24);
                    Toast.makeText(act, "Letra aumentada", Toast.LENGTH_SHORT).show();
                    return true;
                case "info":
                    act.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/search?q=previs%C3%A3o+do+tempo")));
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(act, "Não consegui fazer isso agora", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public void shutdown() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
    }
}
