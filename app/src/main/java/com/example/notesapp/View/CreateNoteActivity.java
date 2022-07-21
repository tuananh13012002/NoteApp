package com.example.notesapp.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.notesapp.Database.NotesDatabase;
import com.example.notesapp.Model.Note;
import com.example.notesapp.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private ImageView imgBack;
    private ImageView imgSave;
    private EditText edNoteTitle;
    private EditText edNoteSubTitle;
    private EditText edNoteType;
    private TextView txtDateTime;

    private String selectedNoteColor;
    private String selectedImagePath;

    private LinearLayout viewSubtitleIndicator;
    private LinearLayout layoutColorBroad;
    private ImageView imgNote;
    private TextView txtColorBroad,textWebUrl;
    private LinearLayout layoutNoteColor,layoutWebURL;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private AlertDialog dialogAddURL,dialogDeleteNote;

    private Note alreadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_create_note);
        initView();
        imgBack.setOnClickListener(view -> {
            onBackPressed();
        });

        //   EEEE,dd MMMM yyyy HH:mm a : Saturday ,13 june 2022 21:10 PM
        txtDateTime.setText(new SimpleDateFormat("EEEE,dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));
        imgSave.setOnClickListener(view -> {
            saveNote();
        });

        selectedNoteColor = "#3E3E3E";
        selectedImagePath="";

        if(getIntent().getBooleanExtra("isViewOrUpdate",false)){
            alreadyAvailableNote=(Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
        findViewById(R.id.imgRemoveWebURL).setOnClickListener(view -> {
            textWebUrl.setText(null);
            layoutWebURL.setVisibility(View.GONE);
        });
        findViewById(R.id.imgRemoveImgNote).setOnClickListener(view -> {
            imgNote.setImageBitmap(null);
            imgNote.setVisibility(View.GONE);
            findViewById(R.id.imgRemoveImgNote).setVisibility(View.GONE);
            selectedImagePath="";
        });
        if(getIntent().getBooleanExtra("isFromQuickActions",false)){
            String type =getIntent().getStringExtra("quickActionType");
            if(type!=null){
                if(type.equals("image")){
                    selectedImagePath=getIntent().getStringExtra("imagePath");
                    imgNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    imgNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.imgRemoveImgNote).setVisibility(View.VISIBLE);
                }else if(type.equals("URL")){
                    textWebUrl.setText(getIntent().getStringExtra("URL"));
                    layoutWebURL.setVisibility(View.VISIBLE);
                }
            }
        }

        initColorBroad();
        setViewSubtitleIndicator();
    }

    private void setViewOrUpdateNote(){
        edNoteTitle.setText(alreadyAvailableNote.getTitle());
        edNoteSubTitle.setText(alreadyAvailableNote.getSubTitle());
        edNoteType.setText(alreadyAvailableNote.getNoteText());
        txtDateTime.setText(alreadyAvailableNote.getDateTime());
        if(alreadyAvailableNote.getNoteImg()!=null&&!alreadyAvailableNote.getNoteImg().trim().isEmpty()){
            imgNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getNoteImg()));
            imgNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imgRemoveImgNote).setVisibility(View.VISIBLE);
            selectedImagePath=alreadyAvailableNote.getNoteImg();
        }

        if(alreadyAvailableNote.getNoteLink()!=null&&!alreadyAvailableNote.getNoteLink().trim().isEmpty()){
            textWebUrl.setText(alreadyAvailableNote.getNoteLink());
            layoutWebURL.setVisibility(View.VISIBLE);
        }
    }

    private void saveNote() {
        if (edNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Tiêu đề ghi chú không được để trống", Toast.LENGTH_SHORT).show();
            return;
        } else if (edNoteSubTitle.getText().toString().trim().isEmpty() && edNoteType.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Ghi chú không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }
        final Note note = new Note();
        note.setTitle(edNoteTitle.getText().toString());
        note.setSubTitle(edNoteSubTitle.getText().toString());
        note.setNoteText(edNoteType.getText().toString());
        note.setDateTime(txtDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setNoteImg(selectedImagePath);

        if(layoutWebURL.getVisibility()==View.VISIBLE){
            note.setNoteLink(textWebUrl.getText().toString());
        }

        if(alreadyAvailableNote!=null){
            note.setId(alreadyAvailableNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveNoteTask().execute();
    }

    private void initColorBroad() {
        final LinearLayout linearLayout = findViewById(R.id.layoutColorBroad);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(linearLayout);
        linearLayout.findViewById(R.id.txtColorBroad).setOnClickListener(view -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                // Trang tính dưới cùng được mở rộng.
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                //Trang tính dưới cùng được thu gọn.
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        final ImageView img1 = layoutColorBroad.findViewById(R.id.imgColor1);
        final ImageView img2 = layoutColorBroad.findViewById(R.id.imgColor2);
        final ImageView img3 = layoutColorBroad.findViewById(R.id.imgColor3);
        final ImageView img4 = layoutColorBroad.findViewById(R.id.imgColor4);
        final ImageView img5 = layoutColorBroad.findViewById(R.id.imgColor5);
        layoutColorBroad.findViewById(R.id.viewColor1).setOnClickListener(view -> {
            selectedNoteColor = "#3E3E3E";
            img1.setImageResource(R.drawable.ic_baseline_check_24);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            setViewSubtitleIndicator();
        });

        layoutColorBroad.findViewById(R.id.viewColor2).setOnClickListener(view -> {
            selectedNoteColor = "#FFC107";
            img1.setImageResource(0);
            img2.setImageResource(R.drawable.ic_baseline_check_24);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            setViewSubtitleIndicator();
        });

        layoutColorBroad.findViewById(R.id.viewColor3).setOnClickListener(view -> {
            selectedNoteColor = "#F44336";
            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(R.drawable.ic_baseline_check_24);
            img4.setImageResource(0);
            img5.setImageResource(0);
            setViewSubtitleIndicator();
        });

        layoutColorBroad.findViewById(R.id.viewColor4).setOnClickListener(view -> {
            selectedNoteColor = "#2196F3";
            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(R.drawable.ic_baseline_check_24);
            img5.setImageResource(0);
            setViewSubtitleIndicator();
        });

        layoutColorBroad.findViewById(R.id.viewColor5).setOnClickListener(view -> {
            selectedNoteColor = "#4CAF50";
            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(R.drawable.ic_baseline_check_24);
            setViewSubtitleIndicator();
        });

        if(alreadyAvailableNote!=null &&alreadyAvailableNote.getColor()!=null
        && !alreadyAvailableNote.getColor().trim().isEmpty()){
            switch (alreadyAvailableNote.getColor()){
                case "#FFC107":
                    layoutColorBroad.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#F44336":
                    layoutColorBroad.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#2196F3":
                    layoutColorBroad.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#4CAF50":
                    layoutColorBroad.findViewById(R.id.viewColor5).performClick();
                    break;

            }
        }

        layoutColorBroad.findViewById(R.id.layoutAddImage).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        CreateNoteActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            } else {
                selectImage();
            }
        });

        layoutColorBroad.findViewById(R.id.layoutAddUrl).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showDialogAddUrl();
        });

        if(alreadyAvailableNote!=null){
            layoutColorBroad.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutColorBroad.findViewById(R.id.layoutDeleteNote).setOnClickListener(view -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDialogDeleteNote();
            });
        }
    }

    private void setViewSubtitleIndicator() {
        viewSubtitleIndicator.setBackgroundColor(Color.parseColor(selectedNoteColor));
    }

    private void selectImage() {
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_CODE_STORAGE_PERMISSION&&grantResults.length>0){
           selectImage();
        }else {
            Toast.makeText(this, "Từ chối cho phép", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       if(requestCode==REQUEST_CODE_SELECT_IMAGE&&resultCode==RESULT_OK){
           if(data!=null){
               Uri selectImageUri=data.getData();
               if(selectImageUri!=null){
                   try {
                       InputStream inputStream=getContentResolver().openInputStream(selectImageUri);
                       Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                       imgNote.setImageBitmap(bitmap);
                       imgNote.setVisibility(View.VISIBLE);
                       findViewById(R.id.imgRemoveImgNote).setVisibility(View.VISIBLE);
                       selectedImagePath=getPathUri(selectImageUri);
                   }catch (Exception e){
                       Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               }
           }
       }
    }
    private String getPathUri(Uri uri){
        String filePath;
        Cursor cursor=getContentResolver()
                .query(uri,null,null,null,null);
        if(cursor==null){
            filePath=uri.getPath();
        }else {
            cursor.moveToNext();
            int index=cursor.getColumnIndex("_data");
            filePath=cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
    private void showDialogAddUrl(){
        if(dialogAddURL==null){
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateNoteActivity.this);
            View view= LayoutInflater.from(this).inflate(R.layout.layout_add_url,(ViewGroup) findViewById(R.id.layoutAddUrlContainer));
            builder.setView(view);
            dialogAddURL=builder.create();
            if(dialogAddURL.getWindow()!=null){
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final  EditText edUrl=view.findViewById(R.id.edURL);
            edUrl.requestFocus();

            view.findViewById(R.id.txtAdd).setOnClickListener(view1 -> {
                if(edUrl.getText().toString().trim().isEmpty()){
                    Toast.makeText(this, "Nhập URL", Toast.LENGTH_SHORT).show();
                }else if(!Patterns.WEB_URL.matcher(edUrl.getText().toString()).matches()){
                    Toast.makeText(this, "Nhập URL hợp lệ", Toast.LENGTH_SHORT).show();
                }else {
                    textWebUrl.setText(edUrl.getText().toString());
                    layoutWebURL.setVisibility(View.VISIBLE);
                    dialogAddURL.dismiss();
                }
            });

            view.findViewById(R.id.txtCancel).setOnClickListener(view1 -> {
                dialogAddURL.dismiss();
            });
        }
        dialogAddURL.show();
    }
    private void showDialogDeleteNote(){
        if(dialogDeleteNote==null){
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateNoteActivity.this);
            View view=LayoutInflater.from(this).inflate(R.layout.layout_delete_note,(ViewGroup) findViewById(R.id.layoutDeleteContainer));
            builder.setView(view);
            dialogDeleteNote=builder.create();
            if(dialogDeleteNote.getWindow()!=null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.txtDelete).setOnClickListener(view1 -> {
                class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

                    @Override
                    protected Void doInBackground(Void... voids) {
                        NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(alreadyAvailableNote);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        Intent intent=new Intent();
                        intent.putExtra("isNoteDelete",true);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                }
                new DeleteNoteTask().execute();
            });

            view.findViewById(R.id.txtCancel).setOnClickListener(view1 -> {
                dialogDeleteNote.dismiss();
            });
        }
        dialogDeleteNote.show();
    }

    private void initView() {
        imgBack = findViewById(R.id.imgBack);
        imgSave = findViewById(R.id.imgSave);
        edNoteTitle = findViewById(R.id.edNoteTitle);
        edNoteSubTitle = findViewById(R.id.edNoteSubTitle);
        edNoteType = findViewById(R.id.edNoteType);
        txtDateTime = findViewById(R.id.txtDateTime);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        layoutColorBroad = findViewById(R.id.layoutColorBroad);
        txtColorBroad = findViewById(R.id.txtColorBroad);
        layoutNoteColor = findViewById(R.id.layoutNoteColor);
        imgNote=findViewById(R.id.imgNote);
        textWebUrl = findViewById(R.id.textWebUrl);
        layoutWebURL = findViewById(R.id.layoutWebURL);

    }
}