import java.util.concurrent.atomic.AtomicLong;

class Event {
    int id;
    String data;

    public Event(int id, String data){
        this.data = data;
        this.id = id;
    }
};

public class RingBuffer {
    private final int capacity;
    private final Event[] queue;
    private final int mask;
    private final AtomicLong nextSequence = new AtomicLong();
    private volatile long publishedSequence = -1;
    private volatile long consumerSequence = -1;

    public RingBuffer(int capacity){
        
        this.capacity = capacity;
        this.mask = capacity - 1;
        queue = new Event[capacity];

        for(int i = 0;i<capacity;i++){
            Event event = new Event(0,"test");
            queue[i] = event;
        }
    }

    public void publish(int id, String data){

        while (nextSequence.get() - consumerSequence >= capacity) {
            Thread.onSpinWait();
        }

        long seq = nextSequence.getAndIncrement();

        int index = (int) seq & mask;

        Event e = queue[index];
        e.id = id;
        e.data = data;
       
        publishedSequence = seq;
    }

    public Event consume() {
        while (consumerSequence == publishedSequence) {
            Thread.onSpinWait();
        }

        consumerSequence++;

        return queue[(int)(consumerSequence & mask)];
    }
}
