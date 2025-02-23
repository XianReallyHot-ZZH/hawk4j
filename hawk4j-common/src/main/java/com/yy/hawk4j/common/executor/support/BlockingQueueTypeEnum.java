package com.yy.hawk4j.common.executor.support;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * @方法描述：阻塞队列枚举类，这个类中的枚举对象表示要创建哪种类型的队列
 */
public enum BlockingQueueTypeEnum {

    /**
     * {@link java.util.concurrent.ArrayBlockingQueue}
     */
    ARRAY_BLOCKING_QUEUE(1, "ArrayBlockingQueue"),

    /**
     * {@link java.util.concurrent.LinkedBlockingQueue}
     */
    LINKED_BLOCKING_QUEUE(2, "LinkedBlockingQueue"),

    /**
     * {@link java.util.concurrent.LinkedBlockingDeque}
     */
    LINKED_BLOCKING_DEQUE(3, "LinkedBlockingDeque"),

    /**
     * {@link java.util.concurrent.SynchronousQueue}
     */
    SYNCHRONOUS_QUEUE(4, "SynchronousQueue"),

    /**
     * {@link java.util.concurrent.LinkedTransferQueue}
     */
    LINKED_TRANSFER_QUEUE(5, "LinkedTransferQueue"),

    /**
     * {@link java.util.concurrent.PriorityBlockingQueue}
     */
    PRIORITY_BLOCKING_QUEUE(6, "PriorityBlockingQueue"),

    /**
     * {@link ResizableCapacityLinkedBlockingQueue}
     */
    RESIZABLE_LINKED_BLOCKING_QUEUE(9, "ResizableCapacityLinkedBlockingQueue");


    private static final int DEFAULT_CAPACITY = 1024;

    @Getter
    private Integer type;

    @Getter
    private String name;

    BlockingQueueTypeEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static String getBlockingQueueNameByType(int type) {
        Optional<BlockingQueueTypeEnum> queueTypeEnum = Arrays.stream(BlockingQueueTypeEnum.values())
                .filter(each -> each.type == type)
                .findFirst();
        return queueTypeEnum.map(each -> each.name).orElse("");
    }

    /**
     * 根据队列名称获取队列类型枚举, 如果队列名称不存在则返回默认的队列类型枚举（LINKED_BLOCKING_QUEUE）
     *
     * @param name
     * @return
     */
    public static BlockingQueueTypeEnum getBlockingQueueTypeEnumByName(String name) {
        Optional<BlockingQueueTypeEnum> queueTypeEnum = Arrays.stream(BlockingQueueTypeEnum.values())
                .filter(each -> each.name.equals(name))
                .findFirst();
        return queueTypeEnum.orElse(LINKED_BLOCKING_QUEUE);
    }

    public static BlockingQueue<Runnable> createBlockingQueue(int type, Integer capacity) {
        BlockingQueue<Runnable> blockingQueue = null;
        if (Objects.equals(type, ARRAY_BLOCKING_QUEUE.type)) {
            blockingQueue = new ArrayBlockingQueue<>(capacity);
        } else if (Objects.equals(type, LINKED_BLOCKING_QUEUE.type)) {
            blockingQueue = new LinkedBlockingQueue<>(capacity);
        } else if (Objects.equals(type, LINKED_BLOCKING_DEQUE.type)) {
            blockingQueue = new LinkedBlockingDeque<>(capacity);
        } else if (Objects.equals(type, SYNCHRONOUS_QUEUE.type)) {
            blockingQueue = new SynchronousQueue<>();
        } else if (Objects.equals(type, LINKED_TRANSFER_QUEUE.type)) {
            blockingQueue = new LinkedTransferQueue<>();
        } else if (Objects.equals(type, PRIORITY_BLOCKING_QUEUE.type)) {
            blockingQueue = new PriorityBlockingQueue<>(capacity);
        } else if (Objects.equals(type, RESIZABLE_LINKED_BLOCKING_QUEUE.type)) {
            blockingQueue = new ResizableCapacityLinkedBlockingQueue<>(capacity);
        }
//        Collection<CustomBlockingQueue> customBlockingQueues = DynamicThreadPoolServiceLoader
//                .getSingletonServiceInstances(CustomBlockingQueue.class);
//        blockingQueue = Optional.ofNullable(blockingQueue).orElseGet(() -> customBlockingQueues.stream()
//                .filter(each -> Objects.equals(type, each.getType()))
//                .map(each -> each.generateBlockingQueue())
//                .findFirst()
//                .orElse(new LinkedBlockingQueue(capacity)));
        blockingQueue = Optional.ofNullable(blockingQueue).orElse(new LinkedBlockingQueue<>(capacity));
        return blockingQueue;
    }


}
