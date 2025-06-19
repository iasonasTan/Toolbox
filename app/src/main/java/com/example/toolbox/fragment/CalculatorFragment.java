package com.example.toolbox.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.toolbox.Utils;
import com.example.toolbox.view.AnimatedButton;
import com.example.toolbox.view.navigation.NavigationItemView;
import com.game.toolbox.R;

import org.mozilla.javascript.Scriptable;

import java.util.Locale;

public class CalculatorFragment extends Utils.ToolFragment implements View.OnClickListener {
    private TextView mainTv;

    @Override
    protected String getName() {
        return "CALCULATOR_FRAGMENT";
    }

    @Override
    protected NavigationItemView getNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.calculator_icon);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_calculator, container, false);
        createButtons(rootView);

        return rootView;
    }

    private void createButtons(View rootView) {
        GridLayout buttonsParent=rootView.findViewById(R.id.buttons_layout);
        String[] chars={
                "C", "(", ")", "/",
                "√", "x²", "!", "L",
                "7", "8", "9", "*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                "R", "0", ".", "="
        };
        final int ROW_COUNT=6;
        final int COL_COUNT=4;
        buttonsParent.setColumnCount(COL_COUNT);
        buttonsParent.setRowCount(ROW_COUNT);
        int i=0;
        for(String ch: chars) {
            Button button=new AnimatedButton(requireContext());
            button.setText(ch);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
            button.setGravity(Gravity.CENTER);
            button.setOnClickListener(this);
            button.setBackgroundResource(R.drawable.round_shape);
            button.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);

            GridLayout.LayoutParams params=new GridLayout.LayoutParams();
            params.width=0;
            params.height=0;
            params.rowSpec=GridLayout.spec(i/COL_COUNT, 1f);
            params.columnSpec=GridLayout.spec(i%COL_COUNT, 1f);
            button.setLayoutParams(params);

            buttonsParent.addView(button);
            i++;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainTv = view.findViewById(R.id.result);
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
        Vibrator v = ContextCompat.getSystemService(requireContext(), Vibrator.class);
        if(v!=null)
            v.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
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
