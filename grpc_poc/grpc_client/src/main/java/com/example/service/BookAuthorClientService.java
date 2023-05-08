package com.example.service;

import com.example.*;
import com.google.protobuf.Descriptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class BookAuthorClientService {
//    @GrpcClient("grpc-service")
//    BookAuthorServiceGrpc.BookAuthorServiceBlockingStub synchronousClient;

    //    @GrpcClient("grpc-service")
//    BookAuthorServiceGrpc.BookAuthorServiceStub asynchronousClient;
    private ManagedChannel getManagedChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9000)
                .usePlaintext()
                .build();

    }

    private BookAuthorServiceGrpc.BookAuthorServiceStub getAsyncClient() {
        return BookAuthorServiceGrpc.newStub(getManagedChannel());

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

            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.EMPTY_LIST;
    }

    public Map<String, Map<Descriptors.FieldDescriptor, Object>> getExpensiveBook() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Map<String, Map<Descriptors.FieldDescriptor, Object>> response = new HashMap<>();
        BookAuthorServiceGrpc.BookAuthorServiceStub asyncClient = getAsyncClient();
        StreamObserver<Book> responseObserver = asyncClient.getExpensiveBook(new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.put("Expensive Book", book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        TempDB.getBooksFromTempDb().forEach(responseObserver::onNext);
        responseObserver.onCompleted();
        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.emptyMap();
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthorAndGender(String gender) throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();
        BookAuthorServiceGrpc.BookAuthorServiceStub asyncClient = getAsyncClient();
        StreamObserver<Book> responseObserver = asyncClient.getBooksByAuthorGender(new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.add(book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        TempDB.getAuthorsFromTempDb()
                .stream()
                .filter(author -> author.getGender().equalsIgnoreCase(gender))
                .forEach(author -> responseObserver.onNext(Book.newBuilder().setAuthorId(author.getAuthorId()).build()));
        responseObserver.onCompleted();
        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.emptyList();
    }
}
