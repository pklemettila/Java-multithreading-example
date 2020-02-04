/* 
	An implementation of circular buffer. The size of buffer is given in the constructor.
	
	Methods:

	public synchronized void put(Object obj)

		puts a new object into buffer if space left - otherwise blocks

	public synchronized Object take()

		takes an object from buffer if any - otherwise blocks

	Synchronization uses Java's standard monitor mechanics.

*/

package ProducerConsumerAmbulanssi;

public class CircularBuffer{
    protected final Object[] buffer;
    protected int putPtr = 0;
    protected int takePtr = 0;
    protected int queuedItems = 0;

    public CircularBuffer(int size) throws IllegalArgumentException {
        // Check first that size is OK
        if( size <= 0)
            throw new IllegalArgumentException();

        buffer = new Object[size];
    }

    public int getCapacity(){
        return buffer.length;
    }

    public synchronized int getCurrentSize() {
        return queuedItems;
    }

    public synchronized void put(Object obj) throws InterruptedException{

        // Wait while buffer is full
        while(queuedItems == buffer.length)
            wait();

        // Put object in buffer
        buffer[putPtr] = obj;

        // Increase putPtr cyclically and increase current size
        putPtr = (putPtr +1) % buffer.length;
        queuedItems++;

        // Signal if put to empty buffer
        if(queuedItems == 1)
            notifyAll();

    }

    public synchronized Object take() throws InterruptedException {
        // Wait while buffer is empty

        while(queuedItems == 0)
            wait();

        Object retObj = buffer[takePtr];

        // Increase takePtr cyclically and decrease current size
        takePtr = (takePtr +1) % buffer.length;
        queuedItems--;

        // Signal if taken from full buffer
        if(queuedItems ==  (buffer.length-1) )
            notifyAll();

        return retObj;
    }
}