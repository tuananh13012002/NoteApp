package com.example.notesapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.Adapter.AdapterNotes;
import com.example.notesapp.Database.NotesDatabase;
import com.example.notesapp.Model.Note;
import com.example.notesapp.View.CreateNoteActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTE = 3;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 4;
    public static final int REQUEST_CODE_SELECT_IMAGE = 5;
    private TextView txtMyNotes;
    private LinearLayout layoutSearch;
    private EditText edSearch;
    private RecyclerView rvNotes;
    private LinearLayout layoutQuickAction;
    private ImageView imgAddNote;
    private ImageView imgAddImage;
    private ImageView imgAddLink;
    private ImageView imgAddNoteMain;
    private AdapterNotes adapterNotes;
    private List<Note> list;
    private AlertDialog dialogAddURL;

    private int noteClicked = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initView();
        imgAddNoteMain.setOnClickListener(view -> {
            startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE);
        });
        list = new ArrayList<>();

        adapterNotes = new AdapterNotes(list, new NotesListener() {
            @Override
            public void onNoteClicked(Note note, int position) {
                noteClicked = position;
                Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
                intent.putExtra("isViewOrUpdate", true);
                intent.putExtra("note", note);
                startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
            }
        });
        rvNotes.setAdapter(adapterNotes);
        getNotes(REQUEST_CODE_SHOW_NOTE, false);
        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (list.size() != 0) {
                    adapterNotes.search(charSequence.toString().toLowerCase());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        imgAddNote.setOnClickListener(view -> {
            startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE);
        });
        imgAddImage.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            } else {
                selectImage();
            }
        });
        imgAddLink.setOnClickListener(view -> {
            showDialogAddUrl();
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            selectImage();
        } else {
            Toast.makeText(this, "Từ chối cho phép", Toast.LENGTH_SHORT).show();
        }
    }

    private String getPathUri(Uri uri) {
        String filePath;
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);
        if (cursor == null) {
            filePath = uri.getPath();
        } else {
            cursor.moveToNext();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private void getNotes(int requestCode, final boolean isNoteDelete) {
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getDatabase(getApplicationContext()).noteDao()
                        .getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requestCode == REQUEST_CODE_SHOW_NOTE) {
                    list.addAll(notes);
                    adapterNotes.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    list.add(0, notes.get(0));
                    adapterNotes.notifyItemInserted(0);
                    rvNotes.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    list.remove(noteClicked);
                    if (isNoteDelete) {
                        adapterNotes.notifyItemRemoved(noteClicked);
                    } else {
                        list.add(noteClicked, notes.get(noteClicked));
                        adapterNotes.notifyItemChanged(noteClicked);
                    }
                }
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDelete", false));
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                       String selectPath=getPathUri(uri);
                       Intent intent=new Intent(getApplicationContext(),CreateNoteActivity.class);
                       intent.putExtra("isFromQuickActions",true);
                       intent.putExtra("quickActionType","image");
                       intent.putExtra("imagePath",selectPath);
                       startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private void showDialogAddUrl(){
        if(dialogAddURL==null){
            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
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
                    dialogAddURL.dismiss();
                    Intent intent=new Intent(getApplicationContext(),CreateNoteActivity.class);
                    intent.putExtra("isFromQuickActions",true);
                    intent.putExtra("quickActionType","URL");
                    intent.putExtra("URL",edUrl.getText().toString());
                    startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);
                }
            });

            view.findViewById(R.id.txtCancel).setOnClickListener(view1 -> {
                dialogAddURL.dismiss();
            });
        }
        dialogAddURL.show();
    }

    private void initView() {
        txtMyNotes = findViewById(R.id.txtMyNotes);
        layoutSearch = findViewById(R.id.layoutSearch);
        edSearch = findViewById(R.id.edSearch);
        rvNotes = findViewById(R.id.rvNotes);
        layoutQuickAction = findViewById(R.id.layoutQuickAction);
        imgAddNote = findViewById(R.id.imgAddNote);
        imgAddImage = findViewById(R.id.imgAddImage);
        imgAddLink = findViewById(R.id.imgAddLink);
        imgAddNoteMain = findViewById(R.id.imgAddNoteMain);
    }

}