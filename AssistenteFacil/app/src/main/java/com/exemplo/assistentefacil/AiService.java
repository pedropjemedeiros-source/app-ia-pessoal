package com.exemplo.assistentefacil;

import android.os.Handler;
import android.os.Looper;

/**
 * ÚNICO ponto que conversa com a IA.
 * Hoje é SIMULADO. No futuro, troque o conteúdo de ask() por uma chamada à sua API
 * (por exemplo com HttpURLConnection ou OkHttp, em uma thread separada).
 */
public class AiService {

    public static class Result {
        public String title;
        public String[] steps;
        public String action;   // ligar | abrir | ajuste | info
        public String target;

        Result(String title, String[] steps, String action, String target) {
            this.title = title; this.steps = steps; this.action = action; this.target = target;
        }
    }

    public interface Callback { void onResult(Result r); }

    private final Handler main = new Handler(Looper.getMainLooper());

    public void ask(String text, final Callback cb) {
        // FUTURO: enviar "text" para a sua IA e converter a resposta em um Result.
        final Result r = simulate(text);
        main.postDelayed(new Runnable() {
            @Override public void run() { cb.onResult(r); }
        }, 900);
    }

    private Result simulate(String text) {
        String t = text.toLowerCase();
        if (t.contains("lig") || t.contains("filha")) {
            return new Result("Ligar para sua filha",
                    new String[]{"Abri seus contatos", "Encontrei \"Ana (filha)\"", "Deixei a chamada pronta"},
                    "ligar", "Ana");
        } else if (t.contains("foto") || t.contains("whats")) {
            return new Result("Enviar foto no WhatsApp",
                    new String[]{"Abri a galeria", "Escolhi a última foto", "Abri o WhatsApp para você enviar"},
                    "abrir", "com.whatsapp");
        } else if (t.contains("letra")) {
            return new Result("Aumentar a letra",
                    new String[]{"Abri os ajustes de tela", "Aumentei o tamanho da letra", "Salvei a mudança"},
                    "ajuste", "fontSize");
        }
        return new Result("Previsão do tempo",
                new String[]{"Busquei sua cidade", "Encontrei a previsão de hoje", "Mostrei na tela"},
                "info", "tempo");
    }
}
