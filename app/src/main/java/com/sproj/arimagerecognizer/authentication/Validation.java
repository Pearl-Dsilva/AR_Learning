package com.sproj.arimagerecognizer.authentication;

public class Validation {

    public static AuthenticationResult emailValidator(String email) {
        if (email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            return new AuthenticationResult("", true);
        }
        return new AuthenticationResult("Invalid email address", false);
    }

    public static AuthenticationResult passwordValidator(String password) {
        if (password.length() < 8) {
            return new AuthenticationResult("Password cannot be less than 8 characters", false);
        } else if (!password.matches(".*[a-z].*")) {
            return new AuthenticationResult("Password must contain lower case alphabets", false);
        } else if (!password.matches(".*[A-Z].*")) {
            return new AuthenticationResult("Password must contain upper case alphabets", false);
        } else if (!password.matches(".*[0-9].*")) {
            return new AuthenticationResult("Password must contain at least 1 number", false);
        } else if (!password.matches(".*[?=.*@!].*")) { // check this regex
            return new AuthenticationResult("Password must contain at least 1 special character", false);
        } else
            return new AuthenticationResult("", true);
    }

    public static AuthenticationResult repeatPasswordValidator(String firstPassword, String secondPassword) {
        if (firstPassword.equals(secondPassword)) {
            return new AuthenticationResult("", true);
        }
        return new AuthenticationResult("Password does not match", false);
    }
}
