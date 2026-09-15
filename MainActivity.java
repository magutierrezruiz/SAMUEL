package com.mundomatematico.samuel;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final String PLAYER_NAME = "Samuel";
    private static final int ADD = 0;
    private static final int SUB = 1;
    private static final int MUL = 2;
    private static final int DIV = 3;
    private static final int QUESTIONS_PER_ROUND = 10;
    private static final int STARTING_LIVES = 3;

    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayList<Question>[] banks = new ArrayList[4];
    private final ArrayList<Integer>[] decks = new ArrayList[4];

    private TextToSpeech tts;
    private boolean ttsReady = false;
    private String pendingSpeech = null;

    private int currentOperation = ADD;
    private ArrayList<Question> roundQuestions = new ArrayList<Question>();
    private int roundIndex = 0;
    private int lives = STARTING_LIVES;
    private int score = 0;
    private boolean answered = false;

    private LinearLayout questionCard;
    private TextView feedbackText;
    private TextView burstText;
    private TextView statusText;
    private Button nextButton;
    private final ArrayList<Button> answerButtons = new ArrayList<Button>();

    private final String[] operationNames = {
            "Sumas", "Restas", "Multiplicaciones", "Divisiones"
    };

    private final String[] operationSymbols = {
            "+", "−", "×", "÷"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TextToSpeech(this, this);
        buildQuestionBanks();
        showHome();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("es", "ES"));
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            if (ttsReady && pendingSpeech != null) {
                speak(pendingSpeech);
                pendingSpeech = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void speak(String text) {
        if (ttsReady && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "math_voice");
        } else {
            pendingSpeech = text;
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private LinearLayout makeVerticalContainer() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(24), dp(20), dp(28));
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        return layout;
    }

    private ScrollView wrap(LinearLayout content) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(8), dp(8), dp(8), dp(8));
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private Button button(String label, int color) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(19);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(color);
        button.setAllCaps(false);
        button.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(7), 0, dp(7));
        button.setLayoutParams(lp);
        return button;
    }

    private void showHome() {
        LinearLayout root = makeVerticalContainer();
        root.setBackgroundColor(Color.rgb(244, 249, 255));

        TextView title = text("🚀 Mundo Matemático de " + PLAYER_NAME, 29, Color.rgb(25, 75, 140), true);
        root.addView(title);

        TextView hello = text("¡Hola, " + PLAYER_NAME + "! Hoy vas a aprender jugando. Tendrás 3 vidas por aventura y siempre te explicaré cada respuesta.", 18, Color.rgb(55, 65, 81), false);
        root.addView(hello);

        TextView guide = text("🧭 " + PLAYER_NAME + ", elige un mundo. Primero veremos una explicación sencilla y luego jugarás 10 retos escogidos al azar de un banco de 100.", 17, Color.rgb(76, 81, 191), true);
        root.addView(guide);

        int[] colors = {
                Color.rgb(33, 150, 243),
                Color.rgb(239, 108, 0),
                Color.rgb(67, 160, 71),
                Color.rgb(123, 31, 162)
        };
        String[] icons = {"➕", "➖", "✖️", "➗"};

        for (int i = 0; i < 4; i++) {
            final int op = i;
            Button b = button(icons[i] + "  " + PLAYER_NAME + ", practicar " + operationNames[i], colors[i]);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLesson(op);
                }
            });
            root.addView(b);
        }

        TextView footer = text("🎯 " + PLAYER_NAME + ", hay 400 retos diferentes: 100 sumas, 100 restas, 100 multiplicaciones y 100 divisiones.", 16, Color.rgb(31, 41, 55), true);
        root.addView(footer);

        setContentView(wrap(root));
        speak("Hola " + PLAYER_NAME + ". Bienvenido a tu Mundo Matemático. Elige sumas, restas, multiplicaciones o divisiones.");
    }

    private void showLesson(final int op) {
        currentOperation = op;
        LinearLayout root = makeVerticalContainer();
        root.setBackgroundColor(Color.rgb(255, 252, 240));

        String symbol = operationSymbols[op];
        root.addView(text("📘 " + PLAYER_NAME + ", aprendamos " + operationNames[op], 27, Color.rgb(80, 55, 120), true));

        String explanation;
        String example;
        String voice;
        if (op == ADD) {
            explanation = PLAYER_NAME + ", sumar significa juntar cantidades. Puedes contar desde el número mayor y avanzar tantas veces como indique el otro número.";
            example = "Ejemplo para " + PLAYER_NAME + ": 6 + 3 = 9.\nPiensa: tengo 6, avanzo 3 pasos: 7, 8, 9.";
            voice = PLAYER_NAME + ", sumar es juntar. Por ejemplo, seis más tres es nueve. Puedes empezar en seis y avanzar tres pasos.";
        } else if (op == SUB) {
            explanation = PLAYER_NAME + ", restar significa quitar o encontrar la diferencia entre dos cantidades. Empieza con la cantidad mayor y quita la menor.";
            example = "Ejemplo para " + PLAYER_NAME + ": 9 − 4 = 5.\nSi tienes 9 fichas y quitas 4, quedan 5.";
            voice = PLAYER_NAME + ", restar es quitar. Nueve menos cuatro es cinco.";
        } else if (op == MUL) {
            explanation = PLAYER_NAME + ", multiplicar es sumar varias veces la misma cantidad. También puedes pensar en grupos iguales.";
            example = "Ejemplo para " + PLAYER_NAME + ": 4 × 3 = 12.\nSon 4 grupos de 3: 3 + 3 + 3 + 3 = 12.";
            voice = PLAYER_NAME + ", multiplicar es formar grupos iguales. Cuatro grupos de tres son doce.";
        } else {
            explanation = PLAYER_NAME + ", dividir significa repartir una cantidad en grupos iguales. Puedes comprobar tu respuesta multiplicando.";
            example = "Ejemplo para " + PLAYER_NAME + ": 12 ÷ 3 = 4.\nRepartir 12 objetos entre 3 grupos deja 4 en cada grupo. Comprobación: 3 × 4 = 12.";
            voice = PLAYER_NAME + ", dividir es repartir en partes iguales. Doce dividido entre tres es cuatro, porque tres por cuatro es doce.";
        }

        root.addView(text(explanation, 19, Color.rgb(55, 65, 81), false));
        TextView exampleView = text(example, 20, Color.rgb(22, 101, 52), true);
        exampleView.setBackgroundColor(Color.rgb(236, 253, 245));
        root.addView(exampleView);

        root.addView(text("💡 " + PLAYER_NAME + ", en el juego encontrarás ejercicios de cálculo y problemas de la vida diaria. Si te equivocas, te explicaré cómo pensarlo.", 17, Color.rgb(146, 64, 14), true));

        Button start = button("🎮 Estoy listo, " + PLAYER_NAME, Color.rgb(0, 137, 123));
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRound(op);
            }
        });
        root.addView(start);

        Button home = button("🏠 " + PLAYER_NAME + ", volver al inicio", Color.rgb(96, 125, 139));
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHome();
            }
        });
        root.addView(home);

        setContentView(wrap(root));
        speak(voice + " Cuando estés listo, comienza la aventura, " + PLAYER_NAME + ".");
    }

    private void startRound(int op) {
        currentOperation = op;
        roundQuestions = drawQuestions(op, QUESTIONS_PER_ROUND);
        roundIndex = 0;
        lives = STARTING_LIVES;
        score = 0;
        showQuestion();
    }

    private void showQuestion() {
        answered = false;
        answerButtons.clear();

        final Question q = roundQuestions.get(roundIndex);
        LinearLayout root = makeVerticalContainer();
        root.setBackgroundColor(Color.rgb(248, 250, 252));

        statusText = text(statusLine(), 17, Color.rgb(30, 64, 175), true);
        root.addView(statusText);

        questionCard = new LinearLayout(this);
        questionCard.setOrientation(LinearLayout.VERTICAL);
        questionCard.setPadding(dp(14), dp(16), dp(14), dp(16));
        questionCard.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.setMargins(0, dp(10), 0, dp(10));
        questionCard.setLayoutParams(cardLp);

        TextView modeLabel = text(q.problem ? "🌍 Problema de aplicación" : "🧠 Ejercicio de práctica", 16, q.problem ? Color.rgb(180, 83, 9) : Color.rgb(37, 99, 235), true);
        questionCard.addView(modeLabel);

        TextView prompt = text(q.prompt, 24, Color.rgb(17, 24, 39), true);
        questionCard.addView(prompt);
        root.addView(questionCard);

        ArrayList<Integer> options = buildOptions(q.answer);
        for (int i = 0; i < options.size(); i++) {
            final int option = options.get(i);
            Button answer = button(String.valueOf(option), Color.rgb(59, 130, 246));
            answer.setTextSize(24);
            answer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkAnswer(option, q);
                }
            });
            answerButtons.add(answer);
            root.addView(answer);
        }

        feedbackText = text("🎧 " + PLAYER_NAME + ", toca una respuesta. Yo te diré cómo lo hiciste.", 17, Color.rgb(75, 85, 99), false);
        feedbackText.setBackgroundColor(Color.rgb(241, 245, 249));
        root.addView(feedbackText);

        burstText = text("⭐ ✨ 🌟 ✨ ⭐", 28, Color.rgb(245, 158, 11), true);
        burstText.setVisibility(View.INVISIBLE);
        root.addView(burstText);

        nextButton = button("➡️ Siguiente reto, " + PLAYER_NAME, Color.rgb(16, 185, 129));
        nextButton.setVisibility(View.GONE);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lives <= 0) {
                    showRoundEnd(false);
                    return;
                }
                roundIndex++;
                if (roundIndex >= roundQuestions.size()) {
                    showRoundEnd(true);
                } else {
                    showQuestion();
                }
            }
        });
        root.addView(nextButton);

        Button quit = button("🏠 " + PLAYER_NAME + ", salir de esta aventura", Color.rgb(100, 116, 139));
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHome();
            }
        });
        root.addView(quit);

        setContentView(wrap(root));
        speak(q.prompt);
    }

    private String statusLine() {
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < STARTING_LIVES; i++) {
            hearts.append(i < lives ? "❤️" : "🖤");
        }
        return PLAYER_NAME + "  •  " + operationNames[currentOperation] + "  •  Reto " + (roundIndex + 1) + "/" + QUESTIONS_PER_ROUND + "  •  " + hearts + "  •  ⭐ " + score;
    }

    private void checkAnswer(int selected, Question q) {
        if (answered) {
            return;
        }
        answered = true;
        for (Button b : answerButtons) {
            b.setEnabled(false);
        }

        if (selected == q.answer) {
            score += 10;
            statusText.setText(statusLine());
            String message = "🌟 ¡Correcto, " + PLAYER_NAME + "! " + q.explanation;
            feedbackText.setText(message);
            feedbackText.setTextColor(Color.rgb(21, 128, 61));
            feedbackText.setBackgroundColor(Color.rgb(220, 252, 231));
            animateCorrect();
            speak("Muy bien, " + PLAYER_NAME + ". " + q.explanation);
        } else {
            lives--;
            statusText.setText(statusLine());
            String message = "💪 Casi, " + PLAYER_NAME + ". Elegiste " + selected + ", pero la respuesta correcta es " + q.answer + ". " + q.explanation;
            feedbackText.setText(message);
            feedbackText.setTextColor(Color.rgb(185, 28, 28));
            feedbackText.setBackgroundColor(Color.rgb(254, 226, 226));
            animateWrong();
            speak("Casi, " + PLAYER_NAME + ". La respuesta correcta es " + q.answer + ". " + q.explanation);
        }

        if (lives <= 0) {
            nextButton.setText("📊 Ver resultado, " + PLAYER_NAME);
        } else if (roundIndex == roundQuestions.size() - 1) {
            nextButton.setText("🏆 Terminar aventura, " + PLAYER_NAME);
        }
        nextButton.setVisibility(View.VISIBLE);
    }

    private void animateCorrect() {
        questionCard.setBackgroundColor(Color.rgb(220, 252, 231));
        questionCard.animate()
                .scaleX(1.06f)
                .scaleY(1.06f)
                .setDuration(170)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        questionCard.animate().scaleX(1f).scaleY(1f).setDuration(170).start();
                    }
                }).start();

        burstText.setVisibility(View.VISIBLE);
        burstText.setAlpha(1f);
        burstText.setTranslationY(0f);
        burstText.animate().translationY(-dp(45)).alpha(0f).setDuration(850).withEndAction(new Runnable() {
            @Override
            public void run() {
                burstText.setVisibility(View.INVISIBLE);
                burstText.setAlpha(1f);
                burstText.setTranslationY(0f);
            }
        }).start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                questionCard.setBackgroundColor(Color.WHITE);
            }
        }, 900);
    }

    private void animateWrong() {
        questionCard.setBackgroundColor(Color.rgb(254, 226, 226));
        ObjectAnimator shake = ObjectAnimator.ofFloat(questionCard, "translationX",
                0f, -dp(18), dp(18), -dp(14), dp(14), -dp(8), dp(8), 0f);
        shake.setDuration(520);
        shake.start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                questionCard.setBackgroundColor(Color.WHITE);
            }
        }, 800);
    }

    private void showRoundEnd(boolean completedAll) {
        LinearLayout root = makeVerticalContainer();
        root.setBackgroundColor(Color.rgb(245, 250, 255));

        String title;
        String body;
        if (completedAll) {
            title = "🏆 ¡Aventura completada, " + PLAYER_NAME + "!";
            body = PLAYER_NAME + ", terminaste los 10 retos de " + operationNames[currentOperation] + ". Lograste " + score + " puntos y te quedaron " + lives + " vidas.";
        } else {
            title = "🌈 " + PLAYER_NAME + ", seguimos aprendiendo";
            body = PLAYER_NAME + ", tus vidas se terminaron en este intento, pero cada error enseña algo. Conseguías " + score + " puntos. Puedes intentarlo de nuevo con preguntas diferentes.";
        }

        root.addView(text(title, 29, Color.rgb(30, 64, 175), true));
        root.addView(text(body, 20, Color.rgb(55, 65, 81), false));

        String encouragement;
        if (score >= 90) {
            encouragement = "🌟 " + PLAYER_NAME + ", tu dominio fue excelente. ¡Sigue así!";
        } else if (score >= 60) {
            encouragement = "👏 " + PLAYER_NAME + ", vas muy bien. Repite la aventura para fortalecer lo que falta.";
        } else {
            encouragement = "💡 " + PLAYER_NAME + ", practicar poco a poco hace que tu mente matemática crezca. ¡Vamos otra vez!";
        }
        root.addView(text(encouragement, 18, Color.rgb(22, 101, 52), true));

        Button repeat = button("🔁 Jugar otra vez, " + PLAYER_NAME, Color.rgb(16, 185, 129));
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLesson(currentOperation);
            }
        });
        root.addView(repeat);

        Button home = button("🏠 Elegir otro mundo, " + PLAYER_NAME, Color.rgb(79, 70, 229));
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHome();
            }
        });
        root.addView(home);

        setContentView(wrap(root));
        speak(body + " " + encouragement);
    }

    private ArrayList<Integer> buildOptions(int answer) {
        Set<Integer> set = new LinkedHashSet<Integer>();
        set.add(answer);

        int[] preferred = {answer + 1, answer - 1, answer + 2, answer - 2, answer + 5, answer - 5};
        for (int value : preferred) {
            if (value >= 0 && value != answer) {
                set.add(value);
            }
            if (set.size() >= 4) {
                break;
            }
        }

        while (set.size() < 4) {
            int candidate = Math.max(0, answer + random.nextInt(13) - 6);
            if (candidate != answer) {
                set.add(candidate);
            }
        }

        ArrayList<Integer> list = new ArrayList<Integer>(set);
        Collections.shuffle(list, random);
        return list;
    }

    private ArrayList<Question> drawQuestions(int op, int count) {
        ArrayList<Question> result = new ArrayList<Question>();
        while (result.size() < count) {
            if (decks[op] == null || decks[op].isEmpty()) {
                decks[op] = new ArrayList<Integer>();
                for (int i = 0; i < banks[op].size(); i++) {
                    decks[op].add(i);
                }
                Collections.shuffle(decks[op], random);
            }
            int index = decks[op].remove(decks[op].size() - 1);
            result.add(banks[op].get(index));
        }
        return result;
    }

    private void buildQuestionBanks() {
        for (int i = 0; i < 4; i++) {
            banks[i] = new ArrayList<Question>();
        }
        buildAdditionBank();
        buildSubtractionBank();
        buildMultiplicationBank();
        buildDivisionBank();
    }

    private void buildAdditionBank() {
        int n = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int a = 2 + i * 3;
                int b = 1 + j;
                int answer = a + b;
                boolean problem = n % 3 == 0;
                String prompt = problem ? additionProblem(a, b, n) : PLAYER_NAME + ", resuelve con calma: " + a + " + " + b + " = ?";
                String explanation = PLAYER_NAME + ", " + a + " + " + b + " = " + answer + ". Al juntar las dos cantidades obtenemos " + answer + ".";
                banks[ADD].add(new Question(prompt, explanation, answer, problem));
                n++;
            }
        }
    }

    private String additionProblem(int a, int b, int n) {
        switch (n % 6) {
            case 0:
                return PLAYER_NAME + ", tienes " + a + " pegatinas y te regalan " + b + " más. ¿Cuántas pegatinas tienes ahora?";
            case 1:
                return PLAYER_NAME + ", ves " + a + " pájaros en un árbol y llegan " + b + " más. ¿Cuántos pájaros hay en total?";
            case 2:
                return PLAYER_NAME + ", guardaste " + a + " bloques y después guardaste " + b + " más. ¿Cuántos bloques guardaste?";
            case 3:
                return PLAYER_NAME + ", en una caja hay " + a + " lápices y en otra hay " + b + ". ¿Cuántos lápices hay entre las dos cajas?";
            case 4:
                return PLAYER_NAME + ", marcaste " + a + " puntos en un juego y luego ganaste " + b + " puntos. ¿Cuántos puntos tienes?";
            default:
                return PLAYER_NAME + ", recogiste " + a + " semillas y luego " + b + " más. ¿Cuántas semillas recogiste en total?";
        }
    }

    private void buildSubtractionBank() {
        int n = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int a = 20 + i * 4 + j;
                int b = 1 + j;
                int answer = a - b;
                boolean problem = n % 3 == 0;
                String prompt = problem ? subtractionProblem(a, b, n) : PLAYER_NAME + ", resuelve con calma: " + a + " − " + b + " = ?";
                String explanation = PLAYER_NAME + ", " + a + " − " + b + " = " + answer + ". Si quitamos " + b + " de " + a + ", quedan " + answer + ".";
                banks[SUB].add(new Question(prompt, explanation, answer, problem));
                n++;
            }
        }
    }

    private String subtractionProblem(int a, int b, int n) {
        switch (n % 6) {
            case 0:
                return PLAYER_NAME + ", tenías " + a + " fichas y usaste " + b + ". ¿Cuántas fichas te quedan?";
            case 1:
                return PLAYER_NAME + ", había " + a + " frutas y se comieron " + b + ". ¿Cuántas frutas quedan?";
            case 2:
                return PLAYER_NAME + ", tenías " + a + " puntos y gastaste " + b + " en una pista. ¿Cuántos puntos conservas?";
            case 3:
                return PLAYER_NAME + ", una caja tenía " + a + " colores y prestaste " + b + ". ¿Cuántos colores quedaron?";
            case 4:
                return PLAYER_NAME + ", había " + a + " globos y se fueron " + b + ". ¿Cuántos globos quedan?";
            default:
                return PLAYER_NAME + ", coleccionaste " + a + " cartas y regalaste " + b + ". ¿Cuántas cartas conservas?";
        }
    }

    private void buildMultiplicationBank() {
        int n = 0;
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                int answer = i * j;
                boolean problem = n % 3 == 0;
                String prompt = problem ? multiplicationProblem(i, j, n) : PLAYER_NAME + ", calcula: " + i + " × " + j + " = ?";
                String explanation = PLAYER_NAME + ", " + i + " × " + j + " = " + answer + ". Son " + i + " grupos de " + j + ".";
                banks[MUL].add(new Question(prompt, explanation, answer, problem));
                n++;
            }
        }
    }

    private String multiplicationProblem(int groups, int each, int n) {
        switch (n % 6) {
            case 0:
                return PLAYER_NAME + ", tienes " + groups + " bolsas con " + each + " canicas en cada una. ¿Cuántas canicas hay en total?";
            case 1:
                return PLAYER_NAME + ", hay " + groups + " filas con " + each + " sillas en cada fila. ¿Cuántas sillas hay?";
            case 2:
                return PLAYER_NAME + ", armaste " + groups + " torres con " + each + " bloques cada una. ¿Cuántos bloques usaste?";
            case 3:
                return PLAYER_NAME + ", tienes " + groups + " cajas y cada caja guarda " + each + " lápices. ¿Cuántos lápices hay?";
            case 4:
                return PLAYER_NAME + ", completas " + groups + " vueltas y ganas " + each + " estrellas por vuelta. ¿Cuántas estrellas ganas?";
            default:
                return PLAYER_NAME + ", dibujaste " + groups + " grupos de " + each + " círculos. ¿Cuántos círculos dibujaste?";
        }
    }

    private void buildDivisionBank() {
        int n = 0;
        for (int divisor = 1; divisor <= 10; divisor++) {
            for (int quotient = 1; quotient <= 10; quotient++) {
                int dividend = divisor * quotient;
                boolean problem = n % 3 == 0;
                String prompt = problem ? divisionProblem(dividend, divisor, n) : PLAYER_NAME + ", calcula: " + dividend + " ÷ " + divisor + " = ?";
                String explanation = PLAYER_NAME + ", " + dividend + " ÷ " + divisor + " = " + quotient + ", porque " + divisor + " × " + quotient + " = " + dividend + ".";
                banks[DIV].add(new Question(prompt, explanation, quotient, problem));
                n++;
            }
        }
    }

    private String divisionProblem(int total, int groups, int n) {
        switch (n % 6) {
            case 0:
                return PLAYER_NAME + ", reparte " + total + " caramelos por igual entre " + groups + " niños. ¿Cuántos recibe cada uno?";
            case 1:
                return PLAYER_NAME + ", tienes " + total + " bloques para formar " + groups + " grupos iguales. ¿Cuántos bloques tendrá cada grupo?";
            case 2:
                return PLAYER_NAME + ", coloca " + total + " pegatinas en " + groups + " páginas por igual. ¿Cuántas van en cada página?";
            case 3:
                return PLAYER_NAME + ", repartes " + total + " fichas en " + groups + " cajas iguales. ¿Cuántas fichas van en cada caja?";
            case 4:
                return PLAYER_NAME + ", tienes " + total + " frutas y quieres hacer " + groups + " bolsas iguales. ¿Cuántas frutas lleva cada bolsa?";
            default:
                return PLAYER_NAME + ", distribuyes " + total + " estrellas en " + groups + " equipos por igual. ¿Cuántas estrellas recibe cada equipo?";
        }
    }

    private static class Question {
        final String prompt;
        final String explanation;
        final int answer;
        final boolean problem;

        Question(String prompt, String explanation, int answer, boolean problem) {
            this.prompt = prompt;
            this.explanation = explanation;
            this.answer = answer;
            this.problem = problem;
        }
    }
}
