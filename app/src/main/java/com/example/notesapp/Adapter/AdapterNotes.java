package com.example.notesapp.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Database;

import com.example.notesapp.Database.NotesDatabase;
import com.example.notesapp.Model.Note;
import com.example.notesapp.NotesListener;
import com.example.notesapp.R;
import com.example.notesapp.View.CreateNoteActivity;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AdapterNotes extends RecyclerView.Adapter<AdapterNotes.NoteViewHolder> {
    private List<Note> list;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> listSource;
    public AdapterNotes(List<Note> list, NotesListener notesListener) {
        this.list = list;
        this.notesListener = notesListener;
        listSource=list;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notes, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = list.get(position);
        holder.txtTitle.setText(note.getTitle());
        if (note.getSubTitle().trim().isEmpty()) {
            holder.txtSubTitle.setVisibility(View.GONE);
        } else {
            holder.txtSubTitle.setText(note.getSubTitle());
        }
        holder.txtDateTime.setText(note.getDateTime());
        if (note.getColor() != null) {
            holder.layoutNote.setCardBackgroundColor(Color.parseColor(note.getColor()));
        } else {
            holder.layoutNote.setCardBackgroundColor(Color.parseColor("#3E3E3E"));
        }

        if (note.getNoteImg() != null) {
            holder.roundedImgNote.setImageBitmap(BitmapFactory.decodeFile(note.getNoteImg()));
            holder.roundedImgNote.setVisibility(View.VISIBLE);
        } else {
            holder.roundedImgNote.setVisibility(View.GONE);
        }
        holder.layoutNote.setOnClickListener(view -> {
            notesListener.onNoteClicked(list.get(position), position);
        });
//        holder.layoutNote.setOnLongClickListener(view -> {
//            Dialog dialog=new Dialog(context);
//            dialog.setContentView(R.layout.layout_delete_note);
//            TextView txtDelete=dialog.findViewById(R.id.txtDelete);
//            TextView txtCancel=dialog.findViewById(R.id.txtCancel);
//            txtDelete.setOnClickListener(view1 -> {
//                NotesDatabase.getDatabase(context).noteDao().deleteNote(note);
//                list.clear();
//                list.addAll(NotesDatabase.getDatabase(context).noteDao().getAllNotes());
//                notifyDataSetChanged();
//            });
//            txtCancel.setOnClickListener(view1 -> {
//                dialog.dismiss();
//            });
//            dialog.show();
//            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            return true;
//        });
    }
    public void search(final String key){
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(key.trim().isEmpty()){
                    list=listSource;
                }else {
                    ArrayList<Note> temp=new ArrayList<>();
                    for (Note note :listSource){
                        if(note.getTitle().toLowerCase().contains(key.toLowerCase())
                        ||note.getSubTitle().toLowerCase().contains(key.toLowerCase())
                        ||note.getNoteText().toLowerCase().contains(key.toLowerCase())){
                            temp.add(note);
                        }
                    }
                    list=temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        },500);
    }

    @Override
    public int getItemCount() {
        if (list != null)
            return list.size();
        else
            return 0;
    }


    public class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView txtTitle;
        private TextView txtSubTitle;
        private TextView txtDateTime;
        private CardView layoutNote;
        private RoundedImageView roundedImgNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtSubTitle = itemView.findViewById(R.id.txtSubTitle);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            roundedImgNote = itemView.findViewById(R.id.roundedImgNote);
        }
    }
}
