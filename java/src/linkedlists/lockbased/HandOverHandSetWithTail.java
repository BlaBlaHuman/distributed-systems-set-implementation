package linkedlists.lockbased;

import contention.abstractions.AbstractCompositionalIntSet;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HandOverHandSetWithTail extends AbstractCompositionalIntSet {

    private Node head;
    private Node tail;

    public HandOverHandSetWithTail() {
        head = new Node(Integer.MIN_VALUE);
        tail = new Node(Integer.MAX_VALUE);
        head.next = tail;
    }
//    private final Lock headLock = new ReentrantLock();

    private int size = 0;
    private final Lock sizeLock = new ReentrantLock();

    @Override
    public boolean addInt(int x) {
        Node curr = findAndLock(x);

        curr.next.lock();

        if (curr.next.key == x) {
            curr.next.unlock();
            curr.unlock();
            return false;
        }


        Node tmp = curr.next;

//        tmp.lock(
;

        curr.next = new Node(x);
        curr.next.next = tmp;

        tmp.unlock();

        curr.unlock();

        sizeLock.lock();
        this.size++;
        sizeLock.unlock();


        return true;
    }

    private Node findAndLock(int x) {

        head.lock();
        Node pred = head;

        head.next.lock();
        Node curr = head.next;

        if(curr == tail) {
//            pred.unlock();
            curr.unlock();

            return head;
        }

        curr.next.lock();

        while (curr.next.key < x) {
            Node next = curr.next;
//            next.lock(
;
            pred.unlock();

            pred = curr;
            curr = next;

            curr.next.lock();
        }

        curr.next.unlock();

        pred.unlock();

        return curr;
    }

    @Override
    public boolean removeInt(int x) {
        Node curr = findAndLock(x);

        if (curr.next.key > x) {
            curr.unlock();
            return false;
        }

        Node toDelete = curr.next;
        toDelete.lock();

        Node successor = toDelete.next;

        successor.lock();


        curr.next = successor;

        curr.unlock();


        successor.unlock();

        sizeLock.lock();
        this.size--;
        sizeLock.unlock();
        return true;
    }

    @Override
    public boolean containsInt(int x) {
        Node curr = findAndLock(x);
        try {
            return curr.next.key == x;
        } finally {
            curr.unlock();
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        sizeLock.lock();
        head = null;
        size = 0;
    }

    private static class Node {
        Node(int item) {
            key = item;
            next = null;
            lock = new ReentrantLock();
        }

        public void lock() {
//            System.out.printf("item(%d) locked\n", this.key);
            this.lock.lock();
        }

        public void unlock() {
//            System.out.printf("item(%d) unlocked\n", this.key);
            this.lock.unlock();
        }

        public int key;
        public Node next;
        final Lock lock;
    }
}
