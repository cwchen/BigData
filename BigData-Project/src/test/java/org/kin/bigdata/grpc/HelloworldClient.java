package org.kin.bigdata.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.kin.bigdata.grpc.demo.helloworld.GreeterGrpc;
import org.kin.bigdata.grpc.demo.helloworld.HelloReply;
import org.kin.bigdata.grpc.demo.helloworld.HelloRequest;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangjianqin on 2017/12/11.
 */
public class HelloworldClient {
    private ManagedChannel channel;
    private GreeterGrpc.GreeterBlockingStub stblockingStub;
    private GreeterGrpc.GreeterStub stub;

    public HelloworldClient(String host, int port) {
        this(ManagedChannelBuilder
                .forAddress(host, port)
                //默认使用SSL/TLS
                .usePlaintext(true)
                .build()
        );
    }

    public HelloworldClient(ManagedChannel channel) {
        this.channel = channel;
        stblockingStub = GreeterGrpc.newBlockingStub(channel);
        stub = GreeterGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void sayHello(String msg){
        System.out.println("---------------------sayHello---------------------");
        HelloRequest request = HelloRequest.newBuilder().setMessage(msg).build();
        HelloReply reply = stblockingStub.sayHello(request);
        System.out.println(reply.getMessage());
    }

    public void sayMore(String msg){
        System.out.println("---------------------sayMore---------------------");
        HelloRequest request = HelloRequest.newBuilder().setMessage(msg).build();
        Iterator<HelloReply> replys = stblockingStub.sayMore(request);
        replys.forEachRemaining(reply -> System.out.println(reply.getMessage()));
    }


    public void sayLittle(String msg) throws InterruptedException {
        System.out.println("---------------------sayLittle---------------------");
        final CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<HelloReply> response = new StreamObserver<HelloReply>() {
            @Override
            public void onNext(HelloReply helloReply) {
                System.out.println(helloReply.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        HelloRequest helloRequest = HelloRequest.newBuilder().setMessage(msg).build();
        StreamObserver<HelloRequest> request = stub.sayLittle(response);
        request.onNext(helloRequest);
        request.onNext(helloRequest);
        request.onNext(helloRequest);
        request.onNext(helloRequest);
        request.onNext(helloRequest);
        request.onCompleted();
        latch.await();
    }

    public void repeat(String msg) throws InterruptedException {
        System.out.println("---------------------repeat---------------------");
        final CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<HelloReply> response = new StreamObserver<HelloReply>() {
            @Override
            public void onNext(HelloReply helloReply) {
                System.out.println(helloReply.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        HelloRequest helloRequest = HelloRequest.newBuilder().setMessage(msg).build();
        StreamObserver<HelloRequest> request = stub.repeat(response);
        request.onNext(helloRequest);
        request.onNext(helloRequest);
        request.onNext(helloRequest);
        request.onNext(helloRequest);
        request.onNext(helloRequest);
        request.onCompleted();

        latch.await();
    }

    public static void main(String[] args) throws InterruptedException {
        HelloworldClient client = new HelloworldClient("localhost", 50001);
        client.sayHello("hjq");
        client.sayMore("hjq");
        client.sayLittle("hjq");
        client.repeat("hjq");
        client.shutdown();
    }
}
