package com.izhan.clientssh;

import android.content.Context;
import android.widget.*;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import com.google.common.base.Strings;
import java.util.ArrayList;

public class Terminal {

    private LinearLayout layout;
    private TextView outputText;
    private EditText inputText;
    private InputListener inputListener;
    private boolean maskInput = false;
    private ArrayList<String> history = new ArrayList<>();
    private int historyIndex = -1;

    public interface InputListener { void onInput(String input); }

    public Terminal(Context context) {

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        outputText = new TextView(context);
        outputText.setMovementMethod(new ScrollingMovementMethod());
        LinearLayout.LayoutParams outputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1
        );
        outputText.setLayoutParams(outputParams);
        layout.addView(outputText);

        inputText = new EditText(context);
        inputText.setSingleLine(true);
        layout.addView(inputText);

        inputText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (history.size() > 0) {
                        historyIndex = Math.max(0, historyIndex - 1);
                        inputText.setText(history.get(historyIndex));
                        inputText.setSelection(inputText.getText().length());
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (history.size() > 0) {
                        historyIndex = Math.min(history.size() - 1, historyIndex + 1);
                        inputText.setText(history.get(historyIndex));
                        inputText.setSelection(inputText.getText().length());
                    }
                    return true;
                }
            }
            return false;
        });

        inputText.setOnEditorActionListener((v, actionId, event) -> {

            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                String input = Strings.nullToEmpty(inputText.getText().toString());
                if(maskInput) appendOutput("\n"); else appendOutput(input + "\n");
                if(!input.isEmpty()) {
                    history.add(input);
                    historyIndex = history.size();
                }
                if (inputListener != null) inputListener.onInput(input);
                inputText.setText("");
                handled = true;
            }
            return handled;
        });
    }

    public void setInputListener(InputListener listener) { this.inputListener = listener; }

    public void appendOutput(String text) {
        outputText.append(text);
        final int scrollAmount = outputText.getLayout().getLineTop(outputText.getLineCount()) - outputText.getHeight();
        if (scrollAmount > 0) outputText.scrollTo(0, scrollAmount); else outputText.scrollTo(0, 0);
    }

    public void setMaskInput(boolean mask) {
        maskInput = mask;
        if (mask) inputText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        else inputText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
    }

    public LinearLayout getLayout() { return layout; }
}
