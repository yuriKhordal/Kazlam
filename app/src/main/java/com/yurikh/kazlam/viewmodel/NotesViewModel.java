package com.yurikh.kazlam.viewmodel;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.yurikh.kazlam.KazlamApp;
import com.yurikh.kazlam.KazlamDb;
import com.yurikh.kazlam.UnitTree;
import com.yurikh.kazlam.model.ComplexNoteDao;
import com.yurikh.kazlam.model.ComplexNoteTagDao;
import com.yurikh.kazlam.model.Note;
import com.yurikh.kazlam.model.NoteNoteTag;
import com.yurikh.kazlam.model.NoteNoteTagDao;
import com.yurikh.kazlam.model.NoteTag;
import com.yurikh.kazlam.model.Soldier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NotesViewModel {
   // The names are the names of the columns, the values are the indicies
   // in the `notes_sortby` array
   public enum SortBy {
      soldierId(0), title(1), modifyDate(2);
      public final int val;
      SortBy(int val) { this.val = val; }
      public static SortBy fromInt(int val) {
         switch (val) {
            case 0: return soldierId;
            case 1: return title;
            case 2: return modifyDate;
            default: throw new IllegalArgumentException("Can't convert (" + val + ") to " + SortBy.class);
         }
      }
   }

   public static class NoteWrapper {
      public Note note;
      public Soldier soldier;
      public List<NoteTag> tags;

      public NoteWrapper(Note note, Soldier soldier, List<NoteTag> tags) {
         this.note = note;
         this.soldier = soldier;
         this.tags = tags;
      }
   }

   Context ctx;

   public NotesViewModel(Context ctx) {
      this.ctx = ctx;
   }

   public Maybe<Note> getNote(long id) {
      return KazlamApp.getDatabase().notesDao().getById(id)
         .subscribeOn(Schedulers.io());
   }

   public Single<List<NoteTag>> getTags(long noteId) {
      return KazlamApp.getDatabase().noteTagsDao().getByNote(noteId)
         .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public List<NoteWrapper> loadNotes(int sortBy, boolean asc) {
      List<Soldier> soldiers = KazlamApp.getDatabase().soldiersDao()
              .getAll().blockingGet();
      List<Note> notes = KazlamApp.getDatabase().notesDao()
              .getAllSortedId().blockingGet();
      List<NoteTag> tags = KazlamApp.getDatabase().noteTagsDao()
              .getAllSortedById().blockingGet();
      List<NoteNoteTag> nntgs = KazlamApp.getDatabase().noteNoteTagsDao()
              .getAllSortedByNote().blockingGet();

      Map<Long, Soldier> soldierMap = new HashMap<>();
      Map<Long, NoteTag> tagMap = new HashMap<>();
      soldiers.forEach(soldier -> soldierMap.put(soldier.id, soldier));
      tags.forEach(tag -> tagMap.put(tag.id, tag));

      List<NoteWrapper> wrappedNotes = new ArrayList<>(notes.size());
      int start = 0;
      for (Note note : notes) {
         Soldier soldier = soldierMap.get(note.soldierId);

         // Find all the tags that belong to the current note
         int end = start;
         while (end < nntgs.size() && nntgs.get(end).noteId == note.id)
            end++;

         List<NoteTag> noteTags = nntgs.subList(start, end)
                 .stream().map(nntag -> tagMap.get(nntag.noteTagId))
                 .collect(Collectors.toList());

         start = end;
         wrappedNotes.add(new NoteWrapper(note, soldier, noteTags));
      }

      sortBy(wrappedNotes, sortBy, asc);

      return wrappedNotes;
   }

   public List<NoteWrapper> loadUnitNotes(long unitId, int sortBy, boolean asc) {
      KazlamDb db = KazlamApp.getDatabase();
      UnitTree tree = new UnitTree();
      tree.fill(unitId);
      tree.fillSoldiers();

      List<Soldier> soldiers = tree.flattenSoldiers();
      List<Note> notes = db.notesDao().getBySoldiersSortedById(
              soldiers.stream().map(soldier->soldier.id).collect(Collectors.toList())
      ).blockingGet();
      List<NoteTag> tags = db.noteTagsDao().getAllSortedById().blockingGet();
      List<NoteNoteTag> nntgs = db.noteNoteTagsDao().getByNotesSortedByNote(
              notes.stream().map(note -> note.id).collect(Collectors.toList())
      ).blockingGet();

      Map<Long, Soldier> soldierMap = new HashMap<>();
      Map<Long, NoteTag> tagMap = new HashMap<>();
      soldiers.forEach(soldier -> soldierMap.put(soldier.id, soldier));
      tags.forEach(tag -> tagMap.put(tag.id, tag));

      List<NoteWrapper> wrappedNotes = new ArrayList<>(notes.size());
      int start = 0;
      for (Note note : notes) {
         Soldier soldier = soldierMap.get(note.soldierId);

         // Find all the tags that belong to the current note
         int end = start;
         while (end < nntgs.size() && nntgs.get(end).noteId == note.id)
            end++;

         List<NoteTag> noteTags = nntgs.subList(start, end)
                 .stream().map(nntag -> tagMap.get(nntag.noteTagId))
                 .collect(Collectors.toList());

         start = end;
         wrappedNotes.add(new NoteWrapper(note, soldier, noteTags));
      }

      sortBy(wrappedNotes, sortBy, asc);

      return wrappedNotes;
   }

   public List<NoteWrapper> loadSoldierNotes(long soldierId, int sortBy, boolean asc) {
      KazlamDb db = KazlamApp.getDatabase();

      Soldier soldier = getSoldier(soldierId).blockingGet();

      List<Note> notes = db.notesDao().getBySoldierSortedById(soldierId).blockingGet();
      List<NoteTag> tags = db.noteTagsDao().getAllSortedById().blockingGet();
      List<NoteNoteTag> nntgs = db.noteNoteTagsDao().getByNotesSortedByNote(
              notes.stream().map(note -> note.id).collect(Collectors.toList())
      ).blockingGet();

      Map<Long, NoteTag> tagMap = new HashMap<>();
      tags.forEach(tag -> tagMap.put(tag.id, tag));

      List<NoteWrapper> wrappedNotes = new ArrayList<>(notes.size());
      int start = 0;
      for (Note note : notes) {
         // Find all the tags that belong to the current note
         int end = start;
         while (end < nntgs.size() && nntgs.get(end).noteId == note.id)
            end++;

         List<NoteTag> noteTags = nntgs.subList(start, end)
                 .stream().map(nntag -> tagMap.get(nntag.noteTagId))
                 .collect(Collectors.toList());

         start = end;
         wrappedNotes.add(new NoteWrapper(note, soldier, noteTags));
      }

      sortBy(wrappedNotes, sortBy, asc);

      return wrappedNotes;
   }

   public Completable fillSoldiersAdapter(ArrayAdapter<Soldier> adapter) {
      return Completable.fromAction(() -> {
         adapter.addAll(KazlamApp.getDatabase().soldiersDao().getAll()
                 .subscribeOn(Schedulers.io()).blockingGet());
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public Completable fillSoldiersAdapter(long unitId, ArrayAdapter<Soldier> adapter) {
      return Completable.fromAction(() -> {
         UnitTree tree = new UnitTree();
         tree.fill(unitId);
         tree.fillSoldiers();

         List<Soldier> soldiers = tree.flattenSoldiers();
         adapter.addAll(soldiers);
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public Completable fillTagsAdapter(ArrayAdapter<NoteTag> adapter) {
      List<NoteTag> tags = KazlamApp.getDatabase().noteTagsDao().getAll()
         .subscribeOn(Schedulers.io()).blockingGet();

      return Completable.fromAction(() -> {
         adapter.addAll(KazlamApp.getDatabase().noteTagsDao().getAll()
            .subscribeOn(Schedulers.io()).blockingGet());
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public Single<Soldier> getSoldier(long id) {
      return Single.fromMaybe(KazlamApp.getDatabase().soldiersDao().getById(id))
         .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public void sortBy(List<NoteWrapper> notes, int index, boolean asc) {
      Comparator<NoteWrapper> comp;
      if (index == SortBy.soldierId.val) {
         comp = Comparator.comparing(note -> note.soldier.name);
      } else if (index == SortBy.title.val) {
         comp = Comparator.comparing(note -> note.note.title);
      } else if (index == SortBy.modifyDate.val) {
         comp = Comparator.comparing(note -> note.note.modifyDate);
      } else return;

      if (!asc) comp = comp.reversed();
      notes.sort(comp);
   }

   public Completable addNote(Soldier soldier, String title, String content, Set<String> tags) {
      return Completable.fromAction(() -> {
         Note note = new Note(soldier.id, title, content);
         List<NoteTag> noteTags = tags.stream().map(NoteTag::new)
            .collect(Collectors.toList());

         note.id = KazlamApp.getDatabase().notesDao().insert(note)
            .subscribeOn(Schedulers.io()).blockingGet();

         KazlamApp.getDatabase().noteTagsDao().addTags(noteTags)
            .subscribeOn(Schedulers.io()).blockingAwait();

         Completable.merge(noteTags.stream().map(tag ->
               KazlamApp.getDatabase().noteNoteTagsDao()
                  .insertByName(note.id, tag.name))
            .collect(Collectors.toList())
         ).subscribeOn(Schedulers.io()).blockingAwait();

      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public Completable updateNote(Note note, String title, String content, Set<String> tags) {
      return Completable.fromAction(() -> {
         note.title = title;
         note.content = content;
         List<String> originalTags = KazlamApp.getDatabase().noteTagsDao()
            .getByNote(note.id).blockingGet().stream().map(tag -> tag.name)
            .collect(Collectors.toList());

         // Subtract tags from original tags. The ones left are the ones that got removed.
         List<String> deleteTags = new ArrayList<>(originalTags);
         deleteTags.removeAll(tags);
         // Subtract original tags from tags. We're left with all the news ones.
         List<String> newTags = new ArrayList<>(tags);
         newTags.removeAll(originalTags);

         ComplexNoteDao notesDao = KazlamApp.getDatabase().notesDao();
         ComplexNoteTagDao noteTagsDao = KazlamApp.getDatabase().noteTagsDao();
         NoteNoteTagDao noteNoteTagsDao = KazlamApp.getDatabase().noteNoteTagsDao();

         noteNoteTagsDao.deleteByNames(note.id, deleteTags).blockingAwait();
         noteTagsDao.addTagNames(newTags).blockingAwait();
         Completable.merge(newTags.stream()
               .map(newTag -> noteNoteTagsDao.insertByName(note.id, newTag))
               .collect(Collectors.toList()))
            .blockingAwait();

         notesDao.update(note).blockingAwait();
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public void deleteNote(Note note) {
      KazlamApp.getDatabase().notesDao().delete(note)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe();
   }
}
