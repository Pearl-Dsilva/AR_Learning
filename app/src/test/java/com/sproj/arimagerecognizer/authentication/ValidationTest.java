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