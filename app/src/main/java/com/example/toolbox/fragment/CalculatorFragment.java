package com.example.toolbox.fragment;

import static android.content.Context.VIBRATOR_SERVICE;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.toolbox.MainActivity;
import com.example.toolbox.view.navigation.NavigationItemView;
import com.game.toolbox.R;

import org.mozilla.javascript.Scriptable;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CalculatorFragment extends ToolFragment implements View.OnClickListener {
    private TextView mainTv;

    public CalculatorFragment(Context context) {
        super(new NavigationItemView(context, R.drawable.calculator_icon));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mainTv = view.findViewById(R.id.result);
        defineButton(view, R.id.button_c, R.id.button_bracket_open, R.id.button_bracket_close,
                R.id.button_ac, R.id.button_dot, R.id.button_equals, R.id.button_plus,
                R.id.button_minus, R.id.button_multiply, R.id.button_divide, R.id.button_0,
                R.id.button_1, R.id.button_2, R.id.button_3, R.id.button_4, R.id.button_5,
                R.id.button_6, R.id.button_7, R.id.button_8, R.id.button_9, R.id.button_sqrt,
                R.id.button_power, R.id.button_factorial, R.id.button_log);

    }

    void defineButton (View rootView, int... ids) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        final int BUTTON_GAP=20;
        final int BUTTONS_PER_ROW=4;
        final int SCREEN_WIDTH = metrics.widthPixels;
        final int BUTTON_SIZE = SCREEN_WIDTH/BUTTONS_PER_ROW-BUTTON_GAP;
        ViewGroup.LayoutParams buttonLayout = rootView.findViewById(R.id.button_0).getLayoutParams();
        buttonLayout.width = BUTTON_SIZE;
        buttonLayout.height = (int)((double)BUTTON_SIZE/5*3.8);

        for (int id: ids) {
            Button b = rootView.findViewById(id);
            b.setOnClickListener(this);
            b.setLayoutParams(buttonLayout);
        }
    }
    public long factorial (long val) {
        long result = 1;
        for (int i = 2; i <= val; i++) {
            result *= i;
        }
        return result;
    }

    public static String doubleToString(double d) {
        return String.format(Locale.ENGLISH, "%.9f", d);
    }

    @FunctionalInterface
    private interface Getter {
        double get();
    }

    @Override
    public void onClick(View view) {
        try {
            vibrate();
            Button b = (Button) view;
            String viewText = b.getText().toString();

            final Getter g = () -> {
                String dataToCalculate = mainTv.getText().toString();
                return Double.parseDouble(getResult(dataToCalculate));
            };

            switch (viewText) {
                case "x²":
                    mainTv.setText(doubleToString(Math.pow(g.get(), 2)));
                    break;
                case "√":
                    mainTv.setText(doubleToString(Math.sqrt(g.get())));
                    break;
                case "L":
                    mainTv.setText(doubleToString(Math.log(g.get())));
                    break;
                case "!":
                    long inputVal = Long.parseLong(mainTv.getText().toString());
                    mainTv.setText(doubleToString(factorial(inputVal)));
                    break;
                case "R":
                    mainTv.setText("");
                    mainTv.setText("");
                    break;
                case "C":
                    String mainTvText = mainTv.getText().toString();
                    int n = mainTvText.length();
                    mainTv.setText(mainTvText.substring(0, n - 1));
                    break;
                case "=":
                    String dataToCalculate = mainTv.getText().toString();
                    String finalResult = getResult(dataToCalculate);
                    if (!finalResult.equals("Err")) {
                        mainTv.setText(finalResult);
                    }
                    break;
                default:
                    mainTv.setText(mainTv.getText().toString() + viewText);
                    break;
            }

        } catch (Exception e) {
            // ignore
        }

    }

    void vibrate () {
        Vibrator v = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
//        v.vibrate(45);
    }

    public String getResult (String code) {
        try {
            org.mozilla.javascript.Context context = org.mozilla.javascript.Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scriptable = context.initStandardObjects();
            String finalResult = context.evaluateString(scriptable,
                    code, "Javascript", 1, null).toString();
            if (finalResult.endsWith(".0"))
                finalResult = finalResult.replace(".0","");
            return finalResult;
        } catch (Exception e) {
            return "Err";
        }
    }

}
