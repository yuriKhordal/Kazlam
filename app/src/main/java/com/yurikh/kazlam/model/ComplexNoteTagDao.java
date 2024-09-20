package com.yurikh.kazlam.model;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Dao
public abstract class ComplexNoteTagDao implements NoteTagDao {

   public Completable addTags(List<NoteTag> tags) {
      return Completable.merge(
         tags.stream().map(this::addTagIfNotExist).collect(Collectors.toList())
      );
   }

   public Completable addTagNames(List<String> tagNames) {
      return Completable.merge(
         tagNames.stream().map(this::addTagIfNotExist).collect(Collectors.toList())
      );
   }

   private Completable addTagIfNotExist(NoteTag tag) {
      return Completable.fromAction(() -> {
         if (getByName(tag.name) == null)
            insert(tag).subscribeOn(Schedulers.io()).blockingSubscribe();
      });
   }

   private Completable addTagIfNotExist(String tagName) {
      return Completable.fromAction(() -> {
         if (getByName(tagName) == null)
            insert(new NoteTag(tagName)).subscribeOn(Schedulers.io()).blockingSubscribe();
      });
   }

   @Query("SELECT * FROM NoteTags WHERE name=:name")
   protected abstract NoteTag getByName(String name);
}
