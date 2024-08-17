package com.sproj.arimagerecognizer.authentication;

import android.util.Log;

import junit.framework.TestCase;

import java.sql.SQLOutput;

public class ValidationTest extends TestCase {
    private TestObject[] emails;
    private TestObject[] passwords;
    private TestObject[] repeatPassword;
    public void setUpEmails() {
        emails = new TestObject[]{
                new TestObject("user.gmail", false),
                new TestObject("user1@gmail.com", true),
                new TestObject("user.gmail@.com", false),
                new TestObject("user.@gmail.com", false),
                new TestObject("username@gmail.com", true),
                new TestObject("user.gmail@.com", false),
                new TestObject("username123@gmail.com", true),
                new TestObject("info@university.edu", true),
                new TestObject("user.name@gmail.com", true),
                new TestObject("user_name@gmail.com", true),
                new TestObject("hello@creativeagency.com", true),
                new TestObject("events@citynews.com", true),
        };
    }

    public void setPasswords() {
        passwords = new TestObject[]{
                new TestObject("A3@dfG7!", true),
                new TestObject("ComplexP@ssw0rd12", true),
                new TestObject("abc", false),
                new TestObject("Passw0rd!", true),
                new TestObject("A1b2C3d4$", false),
                new TestObject("Secure@Pass123", true),
                new TestObject("P@ss", false),
                new TestObject("abcdef", false),
                new TestObject("user123", false),
                new TestObject("L0ngC0mpl3xP@ss", true),
                new TestObject("2g0!dToB3@Tru3", true),
                new TestObject("iloveyou", false),
        };
    }

    public void setRepeatPasswords() {
        repeatPassword = new TestObject[]{
                new TestObject("A3@dfG7!", "A3@dfG7!", true),
                new TestObject("ComplexP@ssw0rd12", "ComplexP@ssw0rd12", true),
                new TestObject("abc", "", false),
                new TestObject("Passw0rd!", "ComplexP@ssw0rd12", false),
                new TestObject("A1b2C3d4$", "A1b2C3d4$", true),
                new TestObject("Secure#Pass123", "Secure#Pass123", true),
                new TestObject("P@ss", "ComplexP@ssw0rd12", false),
                new TestObject("", "abcdef", false),
                new TestObject("user123", "user123", true),
                new TestObject("L0ngC0mpl3xP@ss", "ComplexP@ssw0rd12", false),
                new TestObject("2g0!dToB3@Tru3", "L0ngC0mpl3xP@ss", false),
                new TestObject("2g0!dToB3@Tru3", "2g0!dToB3@Tru3", true),
        };
    }

    private void freeRepeatPasswords() {
        if (repeatPassword != null)
            repeatPassword = null;
    }

    private void freePassword() {
        if (passwords != null)
            passwords = null;
    }

    public void freeEmails() {
        if (emails != null)
            emails = null;
    }

    public void testEmailValidator() {
        setUpEmails();
        for (TestObject testObject : emails) {
            assertEquals(Validation.emailValidator(testObject.value).result, testObject.result);
        }
        freeEmails();
    }

    public void testPasswordValidator() {
        setPasswords();
        for (TestObject testObject : passwords) {
            System.out.println(testObject.value +"-->"+ Validation.passwordValidator(testObject.value).message);
            assertEquals(Validation.passwordValidator(testObject.value).result, testObject.result);
        }
        freePassword();
    }

    public void testRepeatPasswordValidator() {
        setRepeatPasswords();
        for (TestObject testObject : repeatPassword) {
            assertEquals(Validation.repeatPasswordValidator(testObject.value, testObject.value2).result, testObject.result);
        }
        freeRepeatPasswords();
    }

    private static class TestObject {
        String value, value2;
        boolean result;

        public TestObject(String value, boolean result) {
            this.value = value;
            this.result = result;
        }

        public TestObject(String value, String value2, boolean result) {
            this.value = value;
            this.value2 = value2;
            this.result = result;
        }
    }
}