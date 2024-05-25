package com.example.dometask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.ramotion.paperonboarding.PaperOnboardingFragment;
import com.ramotion.paperonboarding.PaperOnboardingPage;
import com.ramotion.paperonboarding.listeners.PaperOnboardingOnRightOutListener;

import java.util.ArrayList;

public class OnboardingActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private boolean onboardingCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Salvar no cache do dispositivo caso o usuário já tenha visto a onboarding inteira
        SharedPreferences sharedPreferences = getSharedPreferences("onboarding", MODE_PRIVATE);
        onboardingCompleted = sharedPreferences.getBoolean("onboardingCompleted", false);

        // Caso já tenha visto, ir para tela de registro
        if (onboardingCompleted) {
            startActivity(new Intent(OnboardingActivity.this, SignUpActivity.class));
            finish();
            return;
        }

        fragmentManager = getSupportFragmentManager();

        // Instância do PaperOnboarding
        final PaperOnboardingFragment paperOnboardingFragment = PaperOnboardingFragment.newInstance(getDataForOnBoarding());

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, paperOnboardingFragment);
        fragmentTransaction.commit();

        // Ao finalizar o Onboarding
        paperOnboardingFragment.setOnRightOutListener(new PaperOnboardingOnRightOutListener() {
            @Override
            public void onRightOut() {
                onboardingCompleted = true;

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("onboardingCompleted", true);
                editor.apply();

                startActivity(new Intent(OnboardingActivity.this, SignUpActivity.class));
                finish();
            }
        });
    }

    // Páginas do Onboarding
    private ArrayList<PaperOnboardingPage> getDataForOnBoarding() {
        PaperOnboardingPage src1 = new PaperOnboardingPage(
                getResources().getString(R.string.title1),
                getResources().getString(R.string.content1),
                Color.parseColor("#D2B48C"),
                R.drawable.baseline_waving_hand_24,
                R.drawable.baseline_stars_24);

        PaperOnboardingPage src2 = new PaperOnboardingPage(
                getResources().getString(R.string.title2),
                getResources().getString(R.string.content2),
                Color.parseColor("#F5DEB3"),
                R.drawable.baseline_login_24,
                R.drawable.baseline_stars_24);

        PaperOnboardingPage src3 = new PaperOnboardingPage(
                getResources().getString(R.string.title3),
                getResources().getString(R.string.content3),
                Color.parseColor("#D2B48C"),
                R.drawable.baseline_people_24,
                R.drawable.baseline_stars_24);

        // Adicionando os elementos ao ArrayList do Onboarding
        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(src1);
        elements.add(src2);
        elements.add(src3);
        return elements;
    }
}