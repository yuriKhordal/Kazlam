package com.yurikh.kazlam;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.function.Function;

public class SpinnerAdapter<T> extends ArrayAdapter<SpinnerAdapter<T>.Wrapper> {
   Function<T, String> toStr;
   String noneStr;
   boolean useNoneObject;

   public SpinnerAdapter(@NonNull Context context, int resource) {
      super(context, resource);
      noneStr = context.getString(R.string.spinner_none);
      toStr = t -> {
         if (t == null)
            return noneStr;
         return t.toString();
      };
      useNoneObject = true;

      super.add(new Wrapper(null));
   }

   public void setNoneStr(String noneStr) {
      this.noneStr = noneStr;
   }

   public void setToStr(Function<T, String> toStr) {
      this.toStr = toStr;
   }

   public void setUseNoneObject(boolean useNoneObject) {
      if (this.useNoneObject == useNoneObject) return;

      this.useNoneObject = useNoneObject;
      if (useNoneObject)
         super.insert(new Wrapper(null), 0);
      else super.remove(super.getItem(0));
   }

   public class Wrapper {
      public T item;

      public Wrapper(T item) {
         this.item = item;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof SpinnerAdapter.Wrapper)) return false;
         Wrapper wrapper = (SpinnerAdapter.Wrapper)o;
         return Objects.equals(item, wrapper.item);
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(item);
      }

      @NonNull
      @Override
      public String toString() {
         return toStr.apply(item);
      }
   }
}
