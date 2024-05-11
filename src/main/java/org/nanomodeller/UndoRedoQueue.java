package org.nanomodeller;

import org.nanomodeller.XMLMappingFiles.GlobalProperties;

import java.util.ArrayList;

public class UndoRedoQueue {
    public ArrayList<UndoRedoElement> queue = new ArrayList<>();
    public  UndoRedoElement currentElement;
    private static UndoRedoQueue instance;
    public int max_queue_length = 12;
    public  static UndoRedoQueue getInstance(){
        if (instance == null ){
            instance = new UndoRedoQueue();
        }
        return instance;
    }
    public void currentDown(){
        if (currentElement != null && currentElement.previous != null){
            currentElement = currentElement.previous;
        }
    }
    public void currentUp(){
        if (currentElement != null && currentElement.next != null){
            currentElement = currentElement.next;
        }
    }
    public UndoRedoElement top(){
        if (queue.size() < 1){
            return null;
        }
        return queue.get(queue.size() - 1);
    }
    public GlobalProperties next(){
        if (currentElement == null || currentElement.next == null){
            return null;
        }
        return currentElement.next.value;
    }
    public GlobalProperties prev(){
        if (top() == null || top().previous == null){
            return null;
        }
        return top().previous.value;
    }
    public void push(GlobalProperties gp){
        UndoRedoElement element = new UndoRedoElement(gp);
        if (queue.size()> max_queue_length){
            queue.remove(0);
        }

        element.previous = currentElement;
        currentElement = element;
        queue.add(element);
        if (top().previous != null){
            top().previous.next = element;
        }
        queue.get(0).previous = null;
    }
    public class UndoRedoElement {
        public GlobalProperties value;
        public UndoRedoElement previous;
        public UndoRedoElement next;

        public UndoRedoElement (GlobalProperties value){
            this.value = value;
        }
    }
}
