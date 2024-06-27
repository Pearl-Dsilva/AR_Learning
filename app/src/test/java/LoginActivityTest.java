//package com.example.arimagerecognizer.LoginActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.shadows.ShadowLooper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;


import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


import com.sproj.arimagerecognizer.LoginActivity;


@RunWith(RobolectricTestRunner.class)
public class LoginActivityTest {

    private LoginActivity activity;
    private ActivityController<LoginActivity> controller;

    private AutoCloseable closeable;

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private Task<AuthResult> mockAuthTask;

    @BeforeClass
    public static void setUpClass() {
        mockStatic(FirebaseAuth.class);
    }


    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);  // Initializes mocks

        // Mock FirebaseAuth and Task
        when(FirebaseAuth.getInstance()).thenReturn(mockAuth);
        mockAuthTask = mock(Task.class);

        // Stub signInWithEmailAndPassword to return the mock task
        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mockAuthTask);

        // Stub the addOnSuccessListener to simulate the successful login process
        when(mockAuthTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<AuthResult> listener = invocation.getArgument(0);
            listener.onSuccess(mock(AuthResult.class));  // Simulate a successful login
            return mockAuthTask;
        });

        // Stub the addOnFailureListener similarly if needed
        when(mockAuthTask.addOnFailureListener(any())).thenReturn(mockAuthTask);

        // Setup the LoginActivity with Robolectric
        controller = Robolectric.buildActivity(LoginActivity.class).create().start().resume().visible();
        activity = controller.get();
    }



    @After
    public void tearDown() throws Exception {
        closeable.close();  // Close resources and mocks opened by MockitoAnnotations
        Mockito.reset(mockAuth);  // Reset the specific mock

    }

//    UNIT TESTS

    @Test
    public void testUsernameValidation_validEmail() {
        assertTrue(activity.validateEmail("example@example.com"));

    }

    @Test
    public void validateUsername_invalidEmail() {
        LoginActivity activity = new LoginActivity();
        assertFalse(activity.validateEmail("example"));
        assertFalse(activity.validateEmail("example@.com"));
        assertFalse(activity.validateEmail("example.com"));
    }

    @Test
    public void validatePassword_validPassword() {
        LoginActivity activity = new LoginActivity();
        assertTrue(activity.validatePassword("password123"));
    }

    @Test
    public void validatePassword_invalidPassword() {
        LoginActivity activity = new LoginActivity();
        assertFalse(activity.validatePassword("pass")); // Too short
        assertFalse(activity.validatePassword("12345678")); // No letters
        assertFalse(activity.validatePassword("abcdefgh")); // No digits
    }

    @Test
    public void loginUser_validCredentials_SuccessfulLoginUnit() {
        Mockito.when(mockAuth.signInWithEmailAndPassword("valid@example.com", "password123"))
                .thenReturn(Tasks.forResult(Mockito.mock(AuthResult.class)));

        activity.loginUser("valid@example.com", "password123");

    }

    @Test
    public void loginUser_invalidCredentials_FailedLogin() {
        Mockito.when(mockAuth.signInWithEmailAndPassword("invalid@example.com", "badpass"))
                .thenReturn(Tasks.forException(new Exception("Login failed")));

        activity.loginUser("invalid@example.com", "badpass");

    }

//    INTEGRATION TEST

    @Test
    public void loginUser_ValidCredentials_SuccessfulLogin() {
        // Arrange
        String validEmail = "user@example.com";
        String validPassword = "password123";

        // Act
        activity.loginUser(validEmail, validPassword);

        // Assert
        verify(mockAuth).signInWithEmailAndPassword(validEmail, validPassword);
        verify(mockAuthTask).addOnSuccessListener(any());
    }
    @Test
    public void loginUser_EmptyCredentials_ShowsNoAction() {
        // Arrange
        String emptyEmail = "";
        String emptyPassword = "";

        // Act
        activity.loginUser(emptyEmail, emptyPassword);


        // Manually run the UI tasks to update the state before verification
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        // Assert
        verify(mockAuth, never()).signInWithEmailAndPassword(anyString(), anyString());
        assertEquals("Login attempt with empty username or password", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void loginUser_InvalidCredentials_ShowsFailureToast() {
        // Arrange
        String invalidEmail = "wrong@example.com";
        String invalidPassword = "wrong123";

        when(mockAuthTask.addOnFailureListener(any())).thenAnswer(invocation -> {
            OnFailureListener failureListener = invocation.getArgument(0);
            failureListener.onFailure(new Exception("Authentication failed"));
            return mockAuthTask;
        });

        // Act
        activity.loginUser(invalidEmail, invalidPassword);

        // Assert
        verify(mockAuth).signInWithEmailAndPassword(invalidEmail, invalidPassword);
        verify(mockAuthTask).addOnFailureListener(any());
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals("Login Failed: Authentication failed", ShadowToast.getTextOfLatestToast());
    }







}
