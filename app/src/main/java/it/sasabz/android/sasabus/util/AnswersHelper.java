package it.sasabz.android.sasabus.util;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.SignUpEvent;

import it.sasabz.android.sasabus.BuildConfig;

public final class AnswersHelper {

    private AnswersHelper() {
    }

    public static void logLogin() {
        if (!BuildConfig.DEBUG) {
            LoginEvent event = new LoginEvent()
                    .putSuccess(true);

            Answers.getInstance().logLogin(event);
        }
    }

    public static void logLogin(String error) {
        if (!BuildConfig.DEBUG) {
            LoginEvent event = new LoginEvent()
                    .putSuccess(false)
                    .putCustomAttribute("Error", error);

            Answers.getInstance().logLogin(event);
        }
    }

    public static void logSignUp() {
        if (!BuildConfig.DEBUG) {
            SignUpEvent event = new SignUpEvent()
                    .putSuccess(true);

            Answers.getInstance().logSignUp(event);
        }
    }

    public static void logSignUp(String error) {
        if (!BuildConfig.DEBUG) {
            SignUpEvent event = new SignUpEvent()
                    .putSuccess(false)
                    .putCustomAttribute("Error", error);

            Answers.getInstance().logSignUp(event);
        }
    }
}
