/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yy.hawk4j.common.executor.support;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于链表的阻塞队列，支持动态调整容量。它继承了 AbstractQueue 并实现了 BlockingQueue 接口
 * 主要参考了 {@linkplain java.util.concurrent.LinkedBlockingQueue}的实现，在其基础上
 * 增加了可动调整容量功能，选择基于LinkedBlockingQueue的进行增强的考虑是：1、jdk中现存的阻塞队列
 * 都没有动态调整容量的实现；2、动态线程池中对队列进行容量变更的时候，在高并发读写的场景下，
 * 基于链表的实现，读写功能性能会更好，实现也简单。因为基于数组的队列在变更容量的时候，会先创建一个新
 * 的数组队列，然后复制过去这样，这里会涉及读写的并发安全问题，这个竞态问题解决起来比较复杂，所以选择
 * 仿写、增强LinkedBlockingQueue来实现阻塞队列的动态调整容量的能力。
 * <p>
 * A clone of {@linkplain java.util.concurrent.LinkedBlockingQueue}
 * with the addition of a {@link #setCapacity(int)} method, allowing us to
 * change the capacity of the queue while it is in use.<p>
 * <p>
 * The documentation for LinkedBlockingQueue follows...<p>
 * <p>
 * An optionally-bounded {@linkplain BlockingQueue blocking queue} based on
 * linked nodes.
 * This queue orders elements FIFO (first-in-first-out).
 * The <em>head</em> of the queue is that element that has been on the
 * queue the longest time.
 * The <em>tail</em> of the queue is that element that has been on the
 * queue the shortest time. New elements
 * are inserted at the tail of the queue, and the queue retrieval
 * operations obtain elements at the head of the queue.
 * Linked queues typically have higher throughput than array-based queues but
 * less predictable performance in most concurrent applications.
 *
 * <p> The optional capacity bound constructor argument serves as a
 * way to prevent excessive queue expansion. The capacity, if unspecified,
 * is equal to {@link Integer#MAX_VALUE}.  Linked nodes are
 * dynamically created upon each insertion unless this would bring the
 * queue above capacity.
 *
 * <p>This class implements all of the <em>optional</em> methods
 * of the {@link Collection} and {@link Iterator} interfaces.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <E> the type of elements held in this collection
 * @author Doug Lea
 * @since 1.5
 **/
public class ResizableCapacityLinkedBlockingQueue<E> extends AbstractQueue<E>
        implements
            BlockingQueue<E>,
            java.io.Serializable {

    private static final long serialVersionUID = -6903933977591709194L;

    /*
     * A variant of the "two lock queue" algorithm. The putLock gates entry to put (and offer), and has an associated condition for waiting puts. Similarly for the takeLock. The "count" field that
     * they both rely on is maintained as an atomic to avoid needing to get both locks in most cases. Also, to minimize need for puts to get takeLock and vice-versa, cascading notifies are used. When
     * a put notices that it has enabled at least one take, it signals taker. That taker in turn signals others if more items have been entered since the signal. And symmetrically for takes signalling
     * puts. Operations such as remove(Object) and iterators acquire both locks.
     */

    /**
     * 内部类，Node对象就是链表中的节点，数据就包装在Node对象中
     *
     * Linked list node class
     */
    static class Node<E> {

        /**
         * 数据本身
         * The item, volatile to ensure barrier separating write and read
         */
        volatile E item;

        // 指向下一个节点
        Node<E> next;

        Node(E x) {
            item = x;
        }
    }

    /**
     * 队列的最大容量，这个成员变量是可以动态更新的
     * The capacity bound, or Integer.MAX_VALUE if none
     */
    private int capacity;

    /**
     * 当前队列存放的数据的个数
     * Current number of elements
     */
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * 链表头节点
     * Head of linked list
     */
    private transient Node<E> head;

    /**
     * 链表尾节点
     * Tail of linked list
     */
    private transient Node<E> last;

    /**
     * 数据出队的同步锁
     * Lock held by take, poll, etc
     */
    private final ReentrantLock takeLock = new ReentrantLock();

    /**
     * 出队的条件对象
     * Wait queue for waiting takes
     */
    private final Condition notEmpty = takeLock.newCondition();

    /**
     * 数据入队的同步锁
     * Lock held by put, offer, etc
     */
    private final ReentrantLock putLock = new ReentrantLock();

    /**
     * 入队的条件对象
     * Wait queue for waiting puts
     */
    private final Condition notFull = putLock.newCondition();

    /**
     * 唤醒出队线程的方法，因为当前的队列里已经有数据了
     * Signal a waiting take. Called only from put/offer (which do not
     * otherwise ordinarily lock takeLock.)
     */
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * 唤醒入队线程的方法，因为当前的队列还有剩余的容量
     * Signal a waiting put. Called only from take/poll.
     */
    private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    /**
     * 添加数据到链表中的方法，添加到队尾
     * Create a node and link it at end of queue
     *
     * @param x the item
     */
    private void insert(E x) {
        last = last.next = new Node<E>(x);
    }

    /**
     * 出队的方法。出队的时候从头节点出队
     * Remove a node from head of queue,
     *
     * @return the node
     */
    private E extract() {
        Node<E> first = head.next;
        head = first;
        E x = first.item;
        first.item = null;
        return x;
    }

    /**
     * Lock to prevent both puts and takes.
     */
    private void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    /**
     * Unlock to allow both puts and takes.
     */
    private void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }

    /**
     * 无界队列构造方法
     * Creates a <tt>LinkedBlockingQueue</tt> with a capacity of
     * {@link Integer#MAX_VALUE}.
     */
    public ResizableCapacityLinkedBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * 有界队列构造方法
     * Creates a <tt>LinkedBlockingQueue</tt> with the given (fixed) capacity.
     *
     * @param capacity the capacity of this queue.
     * @throws IllegalArgumentException if <tt>capacity</tt> is not greater
     *                                  than zero.
     */
    public ResizableCapacityLinkedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;
        last = head = new Node<E>(null);
    }

    /**
     * Creates a <tt>LinkedBlockingQueue</tt> with a capacity of
     * {@link Integer#MAX_VALUE}, initially containing the elements of the
     * given collection,
     * added in traversal order of the collection's iterator.
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if <tt>c</tt> or any element within it
     *                              is <tt>null</tt>
     */
    public ResizableCapacityLinkedBlockingQueue(Collection<? extends E> c) {
        this(Integer.MAX_VALUE);
        for (Iterator<? extends E> it = c.iterator(); it.hasNext();) {
            add(it.next());
        }
    }

    // this doc comment is overridden to remove the reference to collections
    // greater in size than Integer.MAX_VALUE

    /**
     * 得到队列存放数据个数的方法
     * Returns the number of elements in this queue.
     *
     * @return the number of elements in this queue.
     */
    @Override
    public int size() {
        return count.get();
    }

    /**
     * 核心增强的方法：动态更新capacity的方法
     *
     * Set a new capacity for the queue. Increasing the capacity can
     * cause any waiting {@link #put(Object)} invocations to succeed if the new
     * capacity is larger than the queue.
     *
     * @param capacity the new capacity for the queue
     */
    public void setCapacity(int capacity) {
        final int oldCapacity = this.capacity;
        //给capacity成员变量赋值
        this.capacity = capacity;
        final int size = count.get();
        if (capacity > size && size >= oldCapacity) {
            //因为队列扩容了，所以可以唤醒阻塞的入队线程了
            // 这里面再补充说明一点：当capacity缩小时，如果当前队列里的数据量大于capacity，
            // 那么入队的操作都会被阻塞，直到当前队列里的数据量小于capacity，才会唤醒入队线程
            signalNotFull();
        }
    }

    // this doc comment is a modified copy of the inherited doc comment,
    // without the reference to unlimited queues.

    /**
     * 得到队列剩余容量的方法
     * Returns the number of elements that this queue can ideally (in
     * the absence of memory or resource constraints) accept without
     * blocking. This is always equal to the initial capacity of this queue
     * less the current <tt>size</tt> of this queue.
     * <p>Note that you <em>cannot</em> always tell if
     * an attempt to <tt>add</tt> an element will succeed by
     * inspecting <tt>remainingCapacity</tt> because it may be the
     * case that a waiting consumer is ready to <tt>take</tt> an
     * element out of an otherwise full queue.
     */
    @Override
    public int remainingCapacity() {
        return capacity - count.get();
    }

    /**
     * Adds the specified element to the tail of this queue, waiting if
     * necessary for space to become available.
     *
     * @param o the element to add
     * @throws InterruptedException if interrupted while waiting.
     * @throws NullPointerException if the specified element is <tt>null</tt>.
     */
    @Override
    public void put(E o) throws InterruptedException {
        if (o == null) {
            throw new NullPointerException();
        }
        // Note: convention in all put/take/etc is to preset
        // local var holding count negative to indicate failure unless set.
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            /*
             * Note that count is used in wait guard even though it is not protected by lock. This works because count can only decrease at this point (all other puts are shut out by lock), and we (or
             * some other waiting put) are signalled if it ever changes from capacity. Similarly for all other uses of count in other wait guards.
             */
            try {
                while (count.get() >= capacity) {
                    notFull.await();
                }
            } catch (InterruptedException ie) {
                notFull.signal(); // propagate to a non-interrupted thread
                throw ie;
            }
            insert(o);
            c = count.getAndIncrement();
            if (c + 1 < capacity) {
                notFull.signal();
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
    }

    /**
     * 数据入队的方法
     *
     * Inserts the specified element at the tail of this queue, waiting if
     * necessary up to the specified wait time for space to become available.
     *
     * @param o       the element to add
     * @param timeout how long to wait before giving up, in units of
     *                <tt>unit</tt>
     * @param unit    a <tt>TimeUnit</tt> determining how to interpret the
     *                <tt>timeout</tt> parameter
     * @return <tt>true</tt> if successful, or <tt>false</tt> if
     * the specified waiting time elapses before space is available.
     * @throws InterruptedException if interrupted while waiting.
     * @throws NullPointerException if the specified element is <tt>null</tt>.
     */
    @Override
    public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException {

        if (o == null) {
            throw new NullPointerException();
        }
        long nanos = unit.toNanos(timeout);
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        //得到队列当前存放数据的数量
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            for (;;) {
                if (count.get() < capacity) {
                    insert(o);
                    c = count.getAndIncrement();
                    if (c + 1 < capacity) {
                        notFull.signal();
                    }
                    break;
                }
                if (nanos <= 0) {
                    return false;
                }
                try {
                    nanos = notFull.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notFull.signal(); // propagate to a non-interrupted thread
                    throw ie;
                }
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
        return true;
    }

    /**
     * 数据入队的方法
     * 往线程池提交任务的时候，线程池内部将任务提交到队列中时用的就是这个方法
     *
     * Inserts the specified element at the tail of this queue if possible,
     * returning immediately if this queue is full.
     *
     * @param o the element to add.
     * @return <tt>true</tt> if it was possible to add the element to
     * this queue, else <tt>false</tt>
     * @throws NullPointerException if the specified element is <tt>null</tt>
     */
    @Override
    public boolean offer(E o) {
        if (o == null) {
            throw new NullPointerException();
        }
        //得到队列当前存放数据的数量
        final AtomicInteger count = this.count;
        //如果存放的任务数量大于等于队列最大容量，直接返回false，这时候没办法再将数据入队
        if (count.get() >= capacity) {
            return false;
        }
        //走到这里意味着队列没有满
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        //开始执行数据入队操作，为了保证并发安全，要获得同步锁
        putLock.lock();
        try {
            //在这里再次判断一下队列是否已满,这就是一个简单的同步锁双重判断
            if (count.get() < capacity) {
                //走到这里意味着队列没有满，直接把数据添加到队列中即可
                insert(o);
                //数据添加之后，得到添加数据之前队列的容量,注意这里是getAndIncrement，先得到值然后再自增
                c = count.getAndIncrement();
                //这里判断一下当有数据入队之后，队列中是否还有容量,如果还有容量，就唤醒正在阻塞中的线程
                if (c + 1 < capacity) {
                    // put、限时的offer等方法向队列添加数据的时候，如果队列已经满了，那么当时的那个线程就会直接被阻塞，所以如果当前线程能执行到这里，有必要唤醒正在阻塞的线程
                    notFull.signal();
                }
            }
        } finally {
            //释放锁
            putLock.unlock();
        }
        //如果之前队列容量为0，就意味着想从队列中获取数据的线程会被阻塞，但现在添加了新的数据了，所以可以唤醒那些阻塞的出队线程了
        if (c == 0) {
            signalNotEmpty();
        }
        //程序走到这里会判断一下c是否不为-1，如果仍然为-1，就意味着根本没有进入try代码块中的if分支，也就意味着添加元素失败，返回false即可
        return c >= 0;
    }

    // 线程池里面对任务进行调度的时候，会有可能调用该方法获取队列中的任务
    @Override
    public E take() throws InterruptedException {
        E x;
        int c = -1;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            try {
                while (count.get() == 0) {
                    notEmpty.await();
                }
            } catch (InterruptedException ie) {
                notEmpty.signal(); // propagate to a non-interrupted thread
                throw ie;
            }

            x = extract();
            c = count.getAndDecrement();
            if (c > 1) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (c >= capacity) {
            signalNotFull();
        }
        return x;
    }

    // 线程池里面对任务进行调度的时候，会有可能调用该方法获取队列中的任务
    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E x = null;
        int c = -1;
        long nanos = unit.toNanos(timeout);
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            for (;;) {
                if (count.get() > 0) {
                    x = extract();
                    c = count.getAndDecrement();
                    if (c > 1) {
                        notEmpty.signal();
                    }
                    break;
                }
                if (nanos <= 0) {
                    return null;
                }
                try {
                    nanos = notEmpty.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notEmpty.signal(); // propagate to a non-interrupted thread
                    throw ie;
                }
            }
        } finally {
            takeLock.unlock();
        }
        if (c >= capacity) {
            signalNotFull();
        }
        return x;
    }

    @Override
    public E poll() {
        final AtomicInteger count = this.count;
        if (count.get() == 0) {
            return null;
        }
        E x = null;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            if (count.get() > 0) {
                x = extract();
                c = count.getAndDecrement();
                if (c > 1) {
                    notEmpty.signal();
                }
            }
        } finally {
            takeLock.unlock();
        }
        if (c >= capacity) {
            signalNotFull();
        }
        return x;
    }

    @Override
    public E peek() {
        if (count.get() == 0) {
            return null;
        }
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            Node<E> first = head.next;
            if (first == null) {
                return null;
            } else {
                return first.item;
            }
        } finally {
            takeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        boolean removed = false;
        fullyLock();
        try {
            Node<E> trail = head;
            Node<E> p = head.next;
            while (p != null) {
                if (o.equals(p.item)) {
                    removed = true;
                    break;
                }
                trail = p;
                p = p.next;
            }
            if (removed) {
                p.item = null;
                trail.next = p.next;
                if (count.getAndDecrement() >= capacity) {
                    notFull.signalAll();
                }
            }
        } finally {
            fullyUnlock();
        }
        return removed;
    }

    @Override
    public Object[] toArray() {
        fullyLock();
        try {
            int size = count.get();
            Object[] a = new Object[size];
            int k = 0;
            for (Node<E> p = head.next; p != null; p = p.next) {
                a[k++] = p.item;
            }
            return a;
        } finally {
            fullyUnlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        fullyLock();
        try {
            int size = count.get();
            if (a.length < size) {
                a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
            }
            int k = 0;
            for (Node<?> p = head.next; p != null; p = p.next) {
                a[k++] = (T) p.item;
            }
            return a;
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public String toString() {
        fullyLock();
        try {
            return super.toString();
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public void clear() {
        fullyLock();
        try {
            head.next = null;
            if (count.getAndSet(0) >= capacity) {
                notFull.signalAll();
            }
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        Node<E> first;
        fullyLock();
        try {
            first = head.next;
            head.next = null;
            if (count.getAndSet(0) >= capacity) {
                notFull.signalAll();
            }
        } finally {
            fullyUnlock();
        }
        // Transfer the elements outside of locks
        int n = 0;
        for (Node<E> p = first; p != null; p = p.next) {
            c.add(p.item);
            p.item = null;
            ++n;
        }
        return n;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        if (maxElements <= 0) {
            return 0;
        }
        fullyLock();
        try {
            int n = 0;
            Node<E> p = head.next;
            while (p != null && n < maxElements) {
                c.add(p.item);
                p.item = null;
                p = p.next;
                ++n;
            }
            if (n != 0) {
                head.next = p;
                if (count.getAndAdd(-n) >= capacity) {
                    notFull.signalAll();
                }
            }
            return n;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Returns an iterator over the elements in this queue in proper sequence.
     * The returned <tt>Iterator</tt> is a "weakly consistent" iterator that
     * will never throw {@link java.util.ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to)
     * reflect any modifications subsequent to construction.
     *
     * @return an iterator over the elements in this queue in proper sequence.
     */
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * Itr.
     */
    private class Itr implements Iterator<E> {

        /*
         * Basic weak-consistent iterator. At all times hold the next item to hand out so that if hasNext() reports true, we will still have it to return even if lost race with a take etc.
         */
        private Node<E> current;
        private Node<E> lastRet;
        private E currentElement;

        Itr() {
            final ReentrantLock putLock = ResizableCapacityLinkedBlockingQueue.this.putLock;
            final ReentrantLock takeLock = ResizableCapacityLinkedBlockingQueue.this.takeLock;
            putLock.lock();
            takeLock.lock();
            try {
                current = head.next;
                if (current != null) {
                    currentElement = current.item;
                }
            } finally {
                takeLock.unlock();
                putLock.unlock();
            }
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            final ReentrantLock putLock = ResizableCapacityLinkedBlockingQueue.this.putLock;
            final ReentrantLock takeLock = ResizableCapacityLinkedBlockingQueue.this.takeLock;
            putLock.lock();
            takeLock.lock();
            try {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                E x = currentElement;
                lastRet = current;
                current = current.next;
                if (current != null) {
                    currentElement = current.item;
                }
                return x;
            } finally {
                takeLock.unlock();
                putLock.unlock();
            }
        }

        @Override
        public void remove() {
            if (lastRet == null) {
                throw new IllegalStateException();
            }
            final ReentrantLock putLock = ResizableCapacityLinkedBlockingQueue.this.putLock;
            final ReentrantLock takeLock = ResizableCapacityLinkedBlockingQueue.this.takeLock;
            putLock.lock();
            takeLock.lock();
            try {
                Node<E> node = lastRet;
                lastRet = null;
                Node<E> trail = head;
                Node<E> p = head.next;
                while (p != null && p != node) {
                    trail = p;
                    p = p.next;
                }
                if (p == node) {
                    p.item = null;
                    trail.next = p.next;
                    int c = count.getAndDecrement();
                    if (c >= capacity) {
                        notFull.signalAll();
                    }
                }
            } finally {
                takeLock.unlock();
                putLock.unlock();
            }
        }
    }

    /**
     * Save the state to a stream (that is, serialize it).
     *
     * @param s the stream
     * @serialData The capacity is emitted (int), followed by all of
     * its elements (each an <tt>Object</tt>) in the proper order,
     * followed by a null
     */
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {

        fullyLock();
        try {
            // Write out any hidden stuff, plus capacity
            s.defaultWriteObject();

            // Write out all elements in the proper order.
            for (Node<E> p = head.next; p != null; p = p.next) {
                s.writeObject(p.item);
            }
            // Use trailing null as sentinel
            s.writeObject(null);
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Reconstitute this queue instance from a stream (that is,
     * deserialize it).
     *
     * @param s the stream
     */
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        // Read in capacity, and any hidden stuff
        s.defaultReadObject();

        count.set(0);
        last = head = new Node<E>(null);

        // Read in all elements and place in queue
        for (;;) {
            @SuppressWarnings("unchecked")
            E item = (E) s.readObject();
            if (item == null) {
                break;
            }
            add(item);
        }
    }
}
