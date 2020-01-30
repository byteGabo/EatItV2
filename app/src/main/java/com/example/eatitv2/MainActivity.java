package com.example.eatitv2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.eatitv2.Common.Common;
import com.example.eatitv2.Model.UserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private static  int APP_REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;

    private DatabaseReference userRef;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener!= null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).setTheme(R.style.Dialog_costum).build();
        listener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null){
                //Alredy login
                Toast.makeText(MainActivity.this, "Hola" , Toast.LENGTH_SHORT).show();
                checkUserFromFirebase(user);
            }
            else{
                //Not Login
                phoneLogin();
            }

        };
    }

    private void checkUserFromFirebase(FirebaseUser user) {
        dialog.show();
        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                        Toast.makeText(MainActivity.this,"Nos alegra tenerte de vuelta", Toast.LENGTH_SHORT).show();
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                        goToHomeActivity(userModel);
                }else{
                    showRegisterDialog(user);
                }
                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRegisterDialog(FirebaseUser user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Nuevo Registro");
        builder.setMessage("Llena tus datos Aqui");


        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        EditText edt_name = itemView.findViewById(R.id.edt_name);
        EditText edt_address = itemView.findViewById(R.id.edt_address);
        EditText edt_phone = itemView.findViewById(R.id.edt_phone);

        //Set Number user from data
        edt_phone.setText(user.getPhoneNumber());
        builder.setView(itemView);
        builder.setNegativeButton("CANCELAR", (dialog, which) -> {

        });
        builder.setPositiveButton("REGISTRARTE", (dialog, which) -> {
            if (TextUtils.isEmpty(edt_name.getText().toString())){
                Toast.makeText(this,"Por favor ingresa tu nombre", Toast.LENGTH_LONG).show();
                return;
            }
            else  if (TextUtils.isEmpty(edt_address.getText().toString())){
                Toast.makeText(this,"Por favor ingresa tu direccion", Toast.LENGTH_LONG).show();
                return;
            }
            UserModel userModel = new UserModel();
            userModel.setUid(user.getUid());
            userModel.setName(edt_name.getText().toString());
            userModel.setAddress(edt_address.getText().toString());
            userModel.setPhone(edt_phone.getText().toString());

            userRef.child(user.getUid()).setValue(userModel)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this,"Registrado!", Toast.LENGTH_SHORT).show();
                            goToHomeActivity(userModel);
                        }
                    });



        });

        //crear dialogo a mostrar
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void goToHomeActivity(UserModel userModel) {
        Common.currentUser = userModel;
        //Aqui empezara la actividad del HOME
        startActivity(new Intent(MainActivity.this,HomeActivity.class));
        finish();
    }

    private void phoneLogin() {

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers).build(), APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else {
                Toast.makeText(this,"no se pudo ingresar", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
