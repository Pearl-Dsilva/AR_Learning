package com.sproj.arimagerecognizer.authentication;

import junit.framework.TestCase;

public class ValidationTest extends TestCase {

    private TestObject[] emails;
    private TestObject[] passwords;
    private TestObject[] repeatPassword;

    public void setUpEmails() {
        // TODO: Implement as many cases as possible
        emails = new TestObject[]{
                new TestObject("user.gmail", false),
                new TestObject("user1@gmail.com", true),
                new TestObject("USER@gmail", false),
                new TestObject("user.com", false),
                new TestObject("gmail.user", false),
                new TestObject("@user+gmail", false),
                new TestObject("user123", false),
                new TestObject("user.name@domain.co", true),
                new TestObject("username@com", false),
                new TestObject("1234567890@domain.com", true),
                new TestObject("username@domain@domain.com", false),
        };
    }

    public void setPasswords() {
        // TODO: Implement as many cases as possible
        passwords = new TestObject[]{
                new TestObject("A3@dfG7!", true),
                new TestObject("ComplexP@ssw0rd12", true),
                new TestObject("abc", false),
//                new TestObject("user.gmail", false),
//                new TestObject("user.gmail", false),
//                new TestObject("user.gmail", false),
//                new TestObject("user.gmail", false),
        };
    }

    public void setRepeatPasswords() {
        // TODO: Implement as many cases as possible
        repeatPassword = new TestObject[]{
                new TestObject("user.gmail", "user.gmail", true),
                new TestObject("user1@gmail.com", "user.gmail", false),
                new TestObject("user.gmail", "", false),
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