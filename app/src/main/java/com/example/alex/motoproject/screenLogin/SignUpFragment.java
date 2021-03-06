package com.example.alex.motoproject.screenLogin;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.alex.motoproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static com.example.alex.motoproject.util.ArgKeys.EMAIL;
import static com.example.alex.motoproject.util.ArgKeys.PASSWORD;
import static com.example.alex.motoproject.util.ArgKeys.REPEAT_PASSWORD;


public class SignUpFragment extends Fragment {

    private static final String TAG = "log";
    private EditText mEmail, mPassword, mRepeatPassword;
    private FirebaseAuth mFireBaseAuth;
    private View v;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        mEmail.setText(savedInstanceState.getString(EMAIL));
        mPassword.setText(savedInstanceState.getString(PASSWORD));
        mRepeatPassword.setText(savedInstanceState.getString(REPEAT_PASSWORD));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EMAIL, mEmail.getText().toString());
        outState.putString(PASSWORD, mPassword.getText().toString());
        outState.putString(REPEAT_PASSWORD, mRepeatPassword.getText().toString());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFireBaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmail = (EditText) view.findViewById(R.id.sign_up_email);
        mPassword = (EditText) view.findViewById(R.id.sign_up_pass);
        mRepeatPassword = (EditText) view.findViewById(R.id.sign_up_repeat_pass);
        v = view;
        Button mButtonSubmit = (Button) view.findViewById(R.id.sign_up_btn_ok);

        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                if (mEmail.getText().length() == 0) {
                    mEmail.setError(getString(R.string.email_is_empty));

                }
                if (mPassword.getText().length() < 5) {

                    mPassword.setText("");
                    mPassword.setError(getString(R.string.less_6_chars));
                }
                if (mRepeatPassword.getText().length() < 5) {

                    mRepeatPassword.setText("");
                    mRepeatPassword.setError(getString(R.string.hint_repeat_pass));
                }
                if (!mPassword.getText().toString().equals(mRepeatPassword.getText().toString())) {

                    mPassword.setText("");
                    mPassword.setError(getString(R.string.hint_repeat_pass));
                    mRepeatPassword.setText("");
                    mRepeatPassword.setError(getString(R.string.pass_not_match));
                } else if (mPassword.getText().toString().equals(mRepeatPassword.getText().toString())
                        & mEmail.getText().length() > 0
                        & mPassword.getText().length() > 5) {

                    addNewUserToFireBase(mEmail.getText().toString(), mPassword.getText().toString());
                }
            }
        });
    }

    //Method for add new firebaseAuthCurrentUser into FireBase Auth, SignUp
    public void addNewUserToFireBase(String email, String password) {
        mFireBaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((getActivity()), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the firebaseUser. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in firebaseUser can be handled in the listener.
                        if (mFireBaseAuth.getCurrentUser() != null) {
                            mFireBaseAuth.getCurrentUser().sendEmailVerification();
                            mFireBaseAuth.signOut();
                            Log.d(TAG, "onComplete: all done");
                            Toast.makeText(getContext(), "Успішно!", Toast.LENGTH_LONG).show();

                        } else {
                            Log.d(TAG, "onComplete: addNewFirebase User: current user is null");
                            Toast.makeText(getContext(), "Активуйте обліковий запис", Toast.LENGTH_LONG).show();
                        }

                        if (!task.isSuccessful()) {
                            Log.d(TAG, "onComplete: ");
                            Toast.makeText(getContext(), "Спробуйте інший і-мейл", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFireBaseAuth = null;
    }
}
