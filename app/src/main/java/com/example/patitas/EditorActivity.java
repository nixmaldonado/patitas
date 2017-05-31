package com.example.patitas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.concurrent.Executor;

public class EditorActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    LinearLayout cameraButton;
    ImageView imagePreview;
    EditText nameInput;
    private DatabaseReference petsDatabaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private int RC_PHOTO_PICKER = 1;
    private Uri selectedImageUri;
    private static Uri downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        cameraButton = (LinearLayout) findViewById(R.id.camera_input);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
        nameInput = (EditText) findViewById(R.id.name_input);
        imagePreview = (ImageView) findViewById(R.id.image_input);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        petsDatabaseReference = DatabaseUtils.getDatabase().getReference().child("pets");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("pet_photos");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                savePet();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void savePet() {
        if(hasRequiredInput()){
            uploadImage();
            Pet pet = new Pet(nameInput.getText().toString(), downloadUrl.toString());
            petsDatabaseReference.push().setValue(pet);
            return;
        }
    }

    private boolean hasRequiredInput() {
        if(nameInput == null) {
            Toast.makeText(EditorActivity.this, R.string.provide_name,Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(imagePreview);
        }
    }
    private void uploadImage() {
        if(hasImageUri()){
            StorageReference photoRef = storageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri).addOnSuccessListener((Executor) this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @SuppressWarnings("VisibleForTests") @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    downloadUrl = taskSnapshot.getDownloadUrl();
                }
            });
        }
    }

    private boolean hasImageUri() {
        if (selectedImageUri == null){
            return false;
        }
        return true;
    }
}

