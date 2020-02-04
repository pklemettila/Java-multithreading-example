/*
	Abstract class for producer using CircularBuffer
*/
package ProducerConsumerAmbulanssi;

public abstract class Producer{
    private CircularBuffer cbBuffer;

    public Producer(CircularBuffer cb){
        cbBuffer = cb;
    }

    protected abstract Object produce();

    public synchronized void putObject(Object obj) throws InterruptedException {
        cbBuffer.put(obj);
    }
}