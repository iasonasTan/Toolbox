package com.app.toolbox.tools;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;

import com.app.toolbox.R;
import com.app.toolbox.utils.PageFragment;
import com.app.toolbox.utils.Utils;
import com.app.toolbox.view.AnimatedButton;
import com.app.toolbox.view.navigation.NavigationItemView;

import org.mozilla.javascript.Scriptable;

public final class CalculatorFragment extends PageFragment {
    private TextView mMainTv;
    private boolean mHadError =false;

    @Override
    protected String fragmentName() {
        return "toolbox.page.CALCULATOR_PAGE";
    }

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.calculator_icon);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calculator, container, false);
    }

    private void initButtons(View rootView) {
        GridLayout buttonsParent=rootView.findViewById(R.id.buttons_layout);
        String[] buttonsLayout={
                "√", "x²", "!", "L",
                "C", "(",  ")", "÷",
                "7", "8",  "9", "×",
                "4", "5",  "6", "-",
                "1", "2",  "3", "+",
                "R", "0",  ".", "="
        };
        Integer[] actionIndexes = {0,1,2,3,4,23,24,27,20};
        final int ROW_COUNT=6;
        final int COL_COUNT=4;
        buttonsParent.setColumnCount(COL_COUNT);
        buttonsParent.setRowCount(ROW_COUNT);
        View.OnClickListener listener = new CalculatorListener();
        int i=0;
        for(String ch: buttonsLayout) {
            buttonsParent.addView(createButton(listener, ch, i, COL_COUNT, Utils.arrayContains(actionIndexes, i)));
            i++;
        }
    }

    public Button createButton(View.OnClickListener listener, String text, int i, final int COL_COUNT, boolean action) {
        AnimatedButton button=new AnimatedButton(requireContext());
        button.setHighlighted(!action);
        button.setText(text);
        button.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.zalando_sans_bold));
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
        button.setGravity(Gravity.CENTER);
        button.setOnClickListener(listener);
        // noinspection all
        button.setAutoSizeTextTypeWithDefaults(TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

        GridLayout.LayoutParams params=new GridLayout.LayoutParams();
        params.width=0;
        params.height=0;
        params.rowSpec=GridLayout.spec(i/COL_COUNT, 1f);
        params.columnSpec=GridLayout.spec(i%COL_COUNT, 1f);
        button.setLayoutParams(params);
        return button;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initButtons(view);
        mMainTv = view.findViewById(R.id.result);
    }

    private final class CalculatorListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Button b = (Button) view;
            final String buttonText = b.getText().toString();

            new Handler(Looper.getMainLooper()).post(() -> {
                if(!checkForResults(buttonText))
                    return;

                double oldResult = 0;
                try {
                    final String dataToCalculate = mMainTv.getText().toString();
                    oldResult = Double.parseDouble(getResult(dataToCalculate));
                } catch (NumberFormatException nfe) {
                    Log.w("exception_catch", nfe);
                }

                String result = doOperations(buttonText, oldResult);
                if (result!=null) {
                    mMainTv.setText(result);
                }
            });
        }

        public boolean checkForResults(String buttonText) {
            switch (buttonText) {
                case "C":
                    String mainTvText = mMainTv.getText().toString();
                    if (!mainTvText.isEmpty()) {
                        mMainTv.setText(mainTvText.substring(0, mainTvText.length() - 1));
                    }
                    if(mMainTv.getText().equals("")) {
                        mMainTv.setText("0");
                    }
                    return false;
                case "R":
                    mMainTv.setText("");
                    return false;
                case "=":
                    String dataToCalculate = mMainTv.getText().toString();
                    String finalResult = getResult(dataToCalculate);
                    mMainTv.setText(finalResult);
                    if (finalResult.equals("Err")) {
                        mHadError = true;
                    }
                    return false;
            }
            if (mHadError) {
                mMainTv.setText("0");
                mHadError = false;
            }
            return true;
        }

        public String doOperations(String buttonText, double oldResult) {
            return switch (buttonText) {
                case "!" -> oldResult > 5000 ?
                        "Too big!" :
                        Utils.factorial(oldResult).toString();
                case "L" -> Math.log(oldResult) + "";
                case "√" -> Math.sqrt(oldResult) + "";
                case "x²" -> Math.pow(oldResult, 2) + "";
                default -> {
                    if (mMainTv.getText().equals("0")) {
                        mMainTv.setText("");
                        mMainTv.clearComposingText();
                    }
                    mMainTv.append(buttonText);
                    yield null;
                }
            };
        }

        public String getResult (String code) {
            try {
                code = replaceSymbols(code);
                var context = org.mozilla.javascript.Context.enter();
                context.setOptimizationLevel(-1);
                Scriptable scriptable = context.initStandardObjects();
                String finalResult = context.evaluateString(scriptable, code, "Javascript", 1, null).toString();
                finalResult = finalResult.replace(".0","");
                return finalResult;
            } catch (Exception e) {
                return "Err";
            }
        }

        private String replaceSymbols(String code) {
            return code.replace('×', '*')
                    .replace('÷', '/');
        }

    }

}
