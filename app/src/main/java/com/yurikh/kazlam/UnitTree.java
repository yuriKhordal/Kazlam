package com.yurikh.kazlam;

import androidx.annotation.NonNull;

import com.yurikh.kazlam.model.Soldier;
import com.yurikh.kazlam.model.Unit;
import com.yurikh.kazlam.model.UnitDao;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class UnitTree {
   public Node root;

   public UnitTree() {
      root = new Node(null);
   }

   /**
    * Build a global tree of ALL the units in the database.
    * @return a UnitTree object with the "root" unit being null and all the
    * direct(level 1) children being all the top level units(units that have
    * children but not a mother).
    */
   public static UnitTree globalTree() {
      // All the top level units as unfilled UnitTree objects.
      List<UnitTree> top = KazlamApp.getDatabase().unitsDao()
      .getTopLevelUnits().blockingGet().stream().map(unit -> {
         UnitTree node = new UnitTree();
         node.root = new Node(unit);
         return node;
      }).collect(Collectors.toList());

      // Fill all the trees in parallel.
      Completable.merge(top.stream().map(t -> t.fillAsync(t.root.parent.id))
         .collect(Collectors.toList())).blockingAwait();

      // Merge into one UnitTree:
      UnitTree tree = new UnitTree();
      tree.root = new Node(null);
      tree.root.children = top.stream().map(t -> t.root).collect(Collectors.toList());

      return tree;
   }

   public static Single<UnitTree> globalTreeAsync() {
      return Single.fromCallable(UnitTree::globalTree);
   }

   /**
    * Fills the tree with the unit structure of a unit with the given id.
    * @param id The id of the unit to construct the structure for.
    */
   public void fill(long id) {
      UnitDao dao = KazlamApp.getDatabase().unitsDao();
      root = new Node(dao.getById(id).blockingGet());

      ArrayDeque<Node> queue = new ArrayDeque<>();
      queue.add(root);

      while (!queue.isEmpty()) {
         Node node = queue.remove();
         node.children = dao.getDirectChildren(node.parent.id).blockingGet()
            .stream().map(Node::new).collect(Collectors.toList());
         queue.addAll(node.children);
      }
   }

   /**
    * Returns an object that fills the tree with the unit structure of a
    * unit with the given id.
    * @param id The id of the unit to construct the structure for.
    * @return A Completable object that constructs the structure from the
    * database.
    */
   public Completable fillAsync(long id) {
      return Completable.fromAction(() -> fill(id));
   }

   /**
    * Adds to the tree the soldiers of each unit.
    * @Note Assumes the tree has already been filled with {@link #fill(long)}
    * or {@link #fillAsync(long)}.
    */
   public void fillSoldiers() {
      fillSoldiers(root).blockingAwait();
   }

   /**
    * Returns an object that adds to the tree the soldiers of each unit.
    * @Note Assumes the tree has already been filled with {@link #fill(long)}
    * or {@link #fillAsync(long)}.
    * @return A Completable object that adds the soldiers to the tree.
    */
   public Completable fillSoldiersAsync() {
      return fillSoldiers(root);
   }

   private Completable fillSoldiers(Node node) {
      return Completable.merge(
         node.children.stream().map(this::fillSoldiers)
            .collect(Collectors.toList())
      ).mergeWith(Completable.fromAction(() -> {
         node.soldiers = KazlamApp.getDatabase().soldiersDao()
            .getByUnit(node.parent.id).blockingGet();
      }));
   }

   /**
    * Sort the units and soldiers of the tree by name.
    */
   public void sort() {
      sort(root);
   }

   private void sort(Node node) {
      node.children.forEach(this::sort);
      node.children.sort(Comparator.comparing(u -> u.parent.name));
      node.soldiers.sort(Comparator.comparing(s -> s.name));
   }

   public List<Node> flatten() {
      ArrayList<Node> list = new ArrayList<>();
      flattenInto(root, list);
      return list;
   }

   private void flattenInto(Node node, List<Node> list) {
      list.add(node);
      node.children.forEach(n -> flattenInto(n, list));
   }

   public List<Unit> flattenUnits() {
      ArrayList<Unit> list = new ArrayList<>();
      flattenUnitsInto(root, list);
      return list;
   }

   private void flattenUnitsInto(Node node, List<Unit> list) {
      list.add(node.parent);
      node.children.forEach(n -> flattenUnitsInto(n, list));
   }

   public List<Soldier> flattenSoldiers() {
      ArrayList<Soldier> list = new ArrayList<>();
      flattenSoldiersInto(root, list);
      return list;
   }

   private void flattenSoldiersInto(Node node, List<Soldier> list) {
      list.addAll(node.soldiers);
      node.children.forEach(n -> flattenSoldiersInto(n, list));
   }

   public Iterable<Node> preOrder() {
      return new PreOrder();
   }

   public Iterable<Node> postOrder() {
      return new PostOrder();
   }

   /**A tree node.*/
   public static class Node {
      public Unit parent;
      public List<Node> children;
      public List<Soldier> soldiers;

      public Node(Unit unit) {
         parent = unit;
         children = new ArrayList<>(0);
         soldiers = new ArrayList<>(0);
      }
   }

   private class PreOrder implements Iterable<Node> {
      private class InnerIterator implements Iterator<Node> {
         Stack<Node> nodeStack;

         @Override
         public boolean hasNext() {
            return !nodeStack.isEmpty();
         }

         @Override
         public Node next() {
            Node node = nodeStack.pop();
            // Opposite order because stack reverses order.
            for (int i = node.children.size() - 1; i >= 0; i--) {
               nodeStack.push(node.children.get(i));
            }
            return node;
         }
      }

      @NonNull
      @Override
      public Iterator<Node> iterator() {
         InnerIterator iter = new InnerIterator();
         iter.nodeStack = new Stack<>();
         iter.nodeStack.push(root);
         return iter;
      }

      @Override
      public void forEach(@NonNull Consumer<? super Node> action) {
         forEach(root, action);
      }

      private void forEach(Node node, @NonNull Consumer<? super Node> action) {
         action.accept(node);
         node.children.forEach(n -> forEach(n, action));
      }
   }

   private class PostOrder implements Iterable<Node> {
      private class InnerIterator implements Iterator<Node> {
         Stack<Node> nodeStack;
         @Override
         public boolean hasNext() {
            return !nodeStack.isEmpty();
         }

         @Override
         public Node next() {
            return nodeStack.pop();
         }
      }

      @NonNull
      @Override
      public Iterator<Node> iterator() {
         InnerIterator iter = new InnerIterator();
         iter.nodeStack = new Stack<>();
         fill(root, iter.nodeStack);
         return iter;
      }

      private void fill(Node node, Stack<Node> stack) {
         stack.push(node);
         node.children.forEach(n -> fill(n, stack));
      }

      @Override
      public void forEach(@NonNull Consumer<? super Node> action) {
         forEach(root, action);
      }

      private void forEach(Node node, @NonNull Consumer<? super Node> action) {
         node.children.forEach(n -> forEach(n, action));
         action.accept(node);
      }
   }
}
