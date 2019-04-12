package com.mobithink.velo.carbon.splashscreen.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobithink.velo.carbon.R;
import com.mobithink.velo.carbon.home.ui.HomeActivity;
import com.mobithink.velo.carbon.managers.RetrofitManager;
import com.mobithink.velo.carbon.webservices.TechnicalService;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreenActivity extends AppCompatActivity {

    private int serverCallsKO=0;
    private final int SERVER_CALLS_LIMITE=5;

    @BindView(R.id.progress_wheel)
    ImageView wheel;

    @BindView(R.id.app_version_textview)
    TextView appVersionTextView;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        ButterKnife.bind(this);

        try {

            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            String versionText = getString(R.string.version_string).concat(version);
            appVersionTextView.setText(versionText);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();


        Animation rotation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);

        if(wheel!= null){
            wheel.startAnimation(rotation);
        }

        handler = new Handler();

        final Runnable runnable = new Runnable() {
            public void run() {
                checkServerStatus();

            }
        };

        handler.postDelayed(runnable, 1400);
    }

    /**
     * Verification de l'etat du serveur
     */
    private void checkServerStatus() {
        TechnicalService technicalService = RetrofitManager.build().create(TechnicalService.class);

        Call<Void> call = technicalService.checkStatus();
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                switch (response.code()) {
                    case 200:
                        Log.d(this.getClass().getName(),getString(R.string.is_up));
                        startApplicationNormally();
                        break;
                    default:
                        //Control afin d'eviter un boucle infini si le serveurs est KO
                        serverCallsKO++;
                        if (serverCallsKO < SERVER_CALLS_LIMITE){
                            checkServerStatus();
                        }
                        break;
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                checkServerStatus();
            }
        });
    }

    private void startApplicationNormally() {
        startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
    }
}