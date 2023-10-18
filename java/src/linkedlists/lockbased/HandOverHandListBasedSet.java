package linkedlists.lockbased;

import contention.abstractions.AbstractCompositionalIntSet;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HandOverHandListBasedSet extends AbstractCompositionalIntSet {

    // sentinel nodes
    private Node head;
    private Node tail;

    public HandOverHandListBasedSet() {
        head = new Node(Integer.MIN_VALUE);
        tail = new Node(Integer.MAX_VALUE);
        head.next = tail;
    }

    /*
     * Insert
     *
     * @see contention.abstractions.CompositionalIntSet#addInt(int)
     */
    @Override
    public boolean addInt(int item) {
        head.lock.lock();
        Node pred = head;
        try {
            Node curr = pred.next;
            curr.lock.lock();
            while (curr.key < item) {
                pred.lock.unlock();
                pred = curr;
                curr = pred.next;
                curr.lock.lock();
            }
            if (curr.key == item) {
                return false;
            } else {
                Node node = new Node(item);
                node.next = curr;
                pred.next = node;
                return true;
            }
        } finally {
            pred.lock.unlock();
        }
    }

    /*
     * Remove
     *
     * @see contention.abstractions.CompositionalIntSet#removeInt(int)
     */
    @Override
    public boolean removeInt(int item) {
        Node pred = null;
        Node curr = null;
        head.lock.lock();
        try {
            pred = head;
            curr = head.next;
            curr.lock.lock();
            try {
                while (curr.key < item) {
                    pred.lock.unlock();
                    pred = curr;
                    curr = pred.next;
                    curr.lock.lock();
                }
                if (curr.key == item) {
                    pred.next = curr.next;
                    return true;
                } else {
                    return false;
                }
            }
            finally {
                curr.lock.unlock();
            }
        } finally {
            pred.lock.unlock();
        }
    }

    /*
     * Contains
     *
     * @see contention.abstractions.CompositionalIntSet#containsInt(int)
     */
    @Override
    public boolean containsInt(int item) {
        head.lock.lock();
        Node pred = head;
        try {
            Node curr = pred.next;
            curr.lock.lock();
            while (curr.key < item) {
                pred.lock.unlock();
                pred = curr;
                curr = pred.next;
                curr.lock.lock();
            }
            return curr.key == item;
        } finally {
            pred.lock.unlock();
        }
    }

    private class Node {
        Node(int item) {
            key = item;
            next = null;
        }

        public Lock lock = new ReentrantLock();
        public int key;
        public Node next;
    }

    @Override
    public void clear() {
        head.lock.lock();
        head = new Node(Integer.MIN_VALUE);
        head.next = new Node(Integer.MAX_VALUE);
        head.lock.unlock();
    }

    /**
     * Non atomic and thread-unsafe
     */
    @Override
    public int size() {
        int count = 0;

        Node curr = head.next;
        while (curr.key != Integer.MAX_VALUE) {
            curr = curr.next;
            count++;
        }
        return count;
    }
}
