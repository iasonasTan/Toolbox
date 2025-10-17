package com.app.toolbox.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
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

import com.app.toolbox.R;
import com.app.toolbox.utils.ToolFragment;
import com.app.toolbox.view.AnimatedButton;
import com.app.toolbox.view.navigation.NavigationItemView;

import org.mozilla.javascript.Scriptable;

import java.math.BigInteger;

public class CalculatorFragment extends ToolFragment implements View.OnClickListener {
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
                "C", "(", ")", "/",
                "√", "x²", "!", "L",
                "7", "8", "9", "*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                "R", "0", ".", "="
        };
        final boolean NIGHT_MODE = (requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        final int ROW_COUNT=6;
        final int COL_COUNT=4;
        buttonsParent.setColumnCount(COL_COUNT);
        buttonsParent.setRowCount(ROW_COUNT);
        int i=0;
        for(String ch: buttonsLayout) {
            buttonsParent.addView(createButton(NIGHT_MODE, ch, i, COL_COUNT));
            i++;
        }
    }

    public Button createButton(final boolean NIGHT_MODE, String text, int i, final int COL_COUNT) {
        Button button=new AnimatedButton(requireContext());
        button.setText(text);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
        button.setGravity(Gravity.CENTER);
        button.setOnClickListener(this);
        button.setBackgroundResource(R.drawable.background_with_border);
        if(NIGHT_MODE) button.setTextColor(Color.WHITE);
        button.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);

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

    public BigInteger factorial (long val) {
        BigInteger bigInteger=BigInteger.ONE;
        for (long i = 2; i <= val; i++) {
            bigInteger = bigInteger.multiply(BigInteger.valueOf(i));
        }
        return bigInteger;
    }

    public boolean checkForResults(String buttonText) {
        switch (buttonText) {
            case "C":
                mMainTv.post(() -> {
                    String mainTvText = mMainTv.getText().toString();
                    if (!mainTvText.isEmpty()) {
                        mMainTv.setText(mainTvText.substring(0, mainTvText.length() - 1));
                    }
                    if(mMainTv.getText().equals("")) {
                        mMainTv.setText("0");
                    }
                });
                return false;
            case "R":
                mMainTv.post(() -> mMainTv.setText("0"));
                return false;
            case "=":
                String dataToCalculate = mMainTv.getText().toString();
                String finalResult = getResult(dataToCalculate);
                mMainTv.post(() -> mMainTv.setText(finalResult));
                if (finalResult.equals("Err")) {
                    mHadError = true;
                }
                return false;
        }
        if (mHadError) {
            mMainTv.post(() -> mMainTv.setText("0"));
            mHadError = false;
        }
        return true;
    }

    @SuppressWarnings("SetTextI18n")
    @Override
    public void onClick(View view) {
        Button b = (Button) view;
        final String buttonText = b.getText().toString();

        new Thread(() -> {
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
                mMainTv.post(() -> mMainTv.setText(result));
            }
        }).start();
    }

    public String doOperations(String buttonText, double oldResult) {
        String newResult = null;
        switch (buttonText) {
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
                if(mMainTv.getText().equals("0")) {
                    mMainTv.setText("");
                }
                mMainTv.post(() -> mMainTv.append(buttonText));
        }
        return newResult;
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
