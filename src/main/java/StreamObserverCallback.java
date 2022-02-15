import io.grpc.stub.StreamObserver;


public class StreamObserverCallback implements StreamObserver<NetworkService.HelloResponse> {

    @Override
    public void onNext(NetworkService.HelloResponse value) {}

    @Override
    public void onError(Throwable t) {}

    @Override
    public void onCompleted() {}

}
