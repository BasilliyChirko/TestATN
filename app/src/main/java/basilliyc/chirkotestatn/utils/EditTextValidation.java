package basilliyc.chirkotestatn.utils;

import android.content.res.Resources;
import android.widget.EditText;

import java.util.regex.Pattern;

import basilliyc.chirkotestatn.R;

public class EditTextValidation {

    public enum ErrorType {
        EMPTY, INCORRECT_HOST_ADDRESS
    }

    public static Pattern regexHostAddress = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");

    public static String validate(EditText editText, ErrorType... errorTypes) {
        String s = editText.getText().toString();
        ErrorType errorType = getErrorType(s, errorTypes);
        if (errorType != null) {
            showError(editText, errorType);
            return null;
        } else {
            return s;
        }
    }

    public static ErrorType getErrorType(String text, ErrorType... errorTypes) {

        for (ErrorType errorType : errorTypes) {
            switch (errorType) {
                case EMPTY:
                    if (text == null || text.isEmpty()) {
                        return errorType;
                    }
                    break;
                case INCORRECT_HOST_ADDRESS:
                    if (!regexHostAddress.matcher(text).matches()) {
                        return errorType;
                    }
                    break;
            }
        }

        return null;
    }


    public static void showError(EditText editText, ErrorType errorType) {
        Resources resources = editText.getResources();
        switch (errorType) {
            case EMPTY:
                editText.setError(resources.getString(R.string.error_validation_empty));
                break;
            case INCORRECT_HOST_ADDRESS:
                editText.setError(resources.getString(R.string.error_validation_incorrect_host_address));
                break;
        }
    }


}
