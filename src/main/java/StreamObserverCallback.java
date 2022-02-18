import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;


public class StreamObserverCallback implements StreamObserver<NetworkService.HelloResponse> {

    private final ManagedChannel channel;

    public StreamObserverCallback(ManagedChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onNext(NetworkService.HelloResponse value) {}

    @Override
    public void onError(Throwable t) {}

    @Override
    public void onCompleted() {
        channel.shutdownNow();
    }

}
