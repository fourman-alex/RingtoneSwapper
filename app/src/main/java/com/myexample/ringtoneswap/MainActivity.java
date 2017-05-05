package com.myexample.ringtoneswap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private AuthMechanismFacebookImpl authMechanismFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button authButton = (Button) findViewById(R.id.authButton);
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authMechanismFacebook = new AuthMechanismFacebookImpl(MainActivity.this);
                authMechanismFacebook.authenticate(new AuthMechanism.AuthCallback() {
                    @Override
                    public void onSuccess(String selfPhoneNumber) {

                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        authMechanismFacebook.onActivityResult(requestCode, resultCode, data);
    }
}
