package com.app.toolbox.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
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

import com.app.toolbox.R;
import com.app.toolbox.utils.ToolFragment;
import com.app.toolbox.view.AnimatedButton;
import com.app.toolbox.view.navigation.NavigationItemView;

import org.mozilla.javascript.Scriptable;

import java.math.BigInteger;

public class CalculatorFragment extends ToolFragment implements View.OnClickListener {
    private TextView mainTv;
    private boolean hadError=false;

    @Override
    protected String fragmentName() {
        return "CALCULATOR_FRAGMENT";
    }

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
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
        String[] buttonsLayout={
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
        for(String ch: buttonsLayout) {
            Button button=new AnimatedButton(requireContext());
            button.setText(ch);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
            button.setGravity(Gravity.CENTER);
            button.setOnClickListener(this);
            button.setBackgroundResource(R.drawable.background_with_border);
            boolean isNightMode = (requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            if(isNightMode) button.setTextColor(Color.WHITE);
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

    public BigInteger factorial (long val) {
        BigInteger bigInteger=BigInteger.ONE;
        for (long i = 2; i <= val; i++) {
            bigInteger = bigInteger.multiply(BigInteger.valueOf(i));
        }
        return bigInteger;
    }

    @SuppressWarnings("SetTextI18n")
    @Override
    public void onClick(View view) {
        Button b = (Button) view;
        final String viewText = b.getText().toString();

        new Thread(() -> {
            try {
                switch (viewText) {
                    case "C":
                        mainTv.post(() -> {
                            String mainTvText = mainTv.getText().toString();
                            if (!mainTvText.isEmpty()) {
                                mainTv.setText(mainTvText.substring(0, mainTvText.length() - 1));
                            }
                        });
                        return;
                    case "R":
                        mainTv.post(() -> mainTv.setText("0"));
                        return;
                    case "=":
                        String dataToCalculate = mainTv.getText().toString();
                        String finalResult = getResult(dataToCalculate);
                        mainTv.post(() -> mainTv.setText(finalResult));
                        if (finalResult.equals("Err")) {
                            hadError = true;
                        }
                        return;
                }

                if (hadError) {
                    mainTv.post(() -> mainTv.setText("0"));
                    hadError = false;
                }

                boolean calculated = true;
                String newResult = "";
                double oldResult = 0;
                try {
                    final String dataToCalculate = mainTv.getText().toString();
                    oldResult = Double.parseDouble(getResult(dataToCalculate));
                } catch (NumberFormatException ignored) {}

                switch (viewText) {
                    case "!":
                        if (oldResult > 5000) {
                            newResult = "Too big!";
                        } else {
                            newResult = factorial((long) oldResult).toString();
                        }
                        break;
                    case "L":
                        newResult = Math.log(oldResult) + "";
                        break;
                    case "√":
                        newResult = Math.sqrt(oldResult) + "";
                        break;
                    case "x²":
                        newResult = Math.pow(oldResult, 2) + "";
                        break;
                    default:
                        calculated = false;
                        mainTv.post(() -> mainTv.append(viewText));
                }

                if (calculated) {
                    String finalNewResult = newResult;
                    mainTv.post(() -> mainTv.setText(finalNewResult));
                }
            } catch (Exception e) {
                // noinspection all; ignore
                e.printStackTrace();
            }
        }).start();
    }


    public String getResult (String code) {
        try {
            org.mozilla.javascript.Context context = org.mozilla.javascript.Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scriptable = context.initStandardObjects();
            String finalResult = context.evaluateString(scriptable,
                    code, "Javascript", 1, null).toString();
            finalResult = finalResult.replace(".0","");
            return finalResult;
        } catch (Exception e) {
            return "Err";
        }
    }

}
