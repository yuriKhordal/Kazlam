package com.yurikh.kazlam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class RecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
   List<T> items;
   int resource;
   BiConsumer<ViewHolder, Integer> bindFunc;

   public RecyclerAdapter(List<T> items, int resource,
   BiConsumer<ViewHolder, Integer> bindFunc) {
      this.items = items;
      this.resource = resource;
      this.bindFunc = bindFunc;
   }

   public T getItem(int position) {
      return items.get(position);
   }

   public List<T> getItems() {
      return items;
   }

   public int getPosition(T item) {
      return items.indexOf(item);
   }

   public boolean remove(T item) {
      return items.remove(item);
   }

   public void sort(Comparator<? super T> comparator) {
      items.sort(comparator);
   }

   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      return new ViewHolder(LayoutInflater.from(parent.getContext())
         .inflate(resource, parent, false));
   }

   @Override
   public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      bindFunc.accept(holder, position);
   }

   @Override
   public int getItemCount() {
      return items.size();
   }

   public static class ViewHolder extends RecyclerView.ViewHolder {
      public ViewHolder(@NonNull View itemView) {
         super(itemView);
      }

      public <T extends View> T findViewById(int id) {
         return super.itemView.findViewById(id);
      }
   }
}
