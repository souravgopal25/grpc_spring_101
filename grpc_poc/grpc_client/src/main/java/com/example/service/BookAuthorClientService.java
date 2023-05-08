package com.example.service;

import com.example.Author;
import com.example.AuthorId;
import com.example.Book;
import com.example.BookAuthorServiceGrpc;
import com.google.protobuf.Descriptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class BookAuthorClientService {
//    @GrpcClient("grpc-service")
//    BookAuthorServiceGrpc.BookAuthorServiceBlockingStub synchronousClient;

    //    @GrpcClient("grpc-service")
//    BookAuthorServiceGrpc.BookAuthorServiceStub asynchronousClient;
    private ManagedChannel getManagedChannel() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9000)
                .usePlaintext()
                .build();
        return channel;
    }

    private BookAuthorServiceGrpc.BookAuthorServiceStub getAsyncClient() {
        BookAuthorServiceGrpc.BookAuthorServiceStub asyncClient = BookAuthorServiceGrpc.newStub(getManagedChannel());
        return asyncClient;
    }

    public Map<Descriptors.FieldDescriptor, Object> getAuthor(int authorId) {

        BookAuthorServiceGrpc.BookAuthorServiceBlockingStub synchronousClient = BookAuthorServiceGrpc.newBlockingStub(getManagedChannel());
        AuthorId request = AuthorId.newBuilder().setAuthorId(authorId).build();
        Author response = synchronousClient.getAuthorDetails(request);
        return response.getAllFields();
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getBookByAuthorId(int authorId) throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        AuthorId request = AuthorId.newBuilder().setAuthorId(authorId).build();
        final List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();
        BookAuthorServiceGrpc.BookAuthorServiceStub asyncClient = getAsyncClient();
        asyncClient.getBookByAuthor(request, new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.add(book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
                ;
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.EMPTY_LIST;
    }
}
