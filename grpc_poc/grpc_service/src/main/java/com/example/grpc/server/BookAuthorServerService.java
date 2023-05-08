package com.example.grpc.server;

import com.example.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;

@GrpcService
public class BookAuthorServerService extends BookAuthorServiceGrpc.BookAuthorServiceImplBase {
    @Override
    public void getAuthorDetails(AuthorId request, StreamObserver<Author> responseObserver) {
        TempDB.getAuthorsFromTempDb()
                .stream()
                .filter(author -> author.getAuthorId() == request.getAuthorId())
                .findFirst()
                .ifPresent(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getBookByAuthor(AuthorId request, StreamObserver<Book> responseObserver) {
        TempDB.getBooksFromTempDb().
                stream()
                .filter(book -> book.getAuthorId() == request.getAuthorId())
                .forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Book> getExpensiveBook(StreamObserver<Book> responseObserver) {
        return new StreamObserver<Book>() {
            Book expensiveBook = null;
            float price = 0;

            @Override
            public void onNext(Book book) {
                if (book.getPrice() > price) {
                    expensiveBook = book;
                    price = expensiveBook.getPrice();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(expensiveBook);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Book> getBooksByAuthorGender(StreamObserver<Book> responseObserver) {
        return new StreamObserver<Book>() {
            final List<Book> bookList = new ArrayList<>();

            @Override
            public void onNext(Book book) {
                TempDB.getBooksFromTempDb().
                        stream()
                        .filter(bookFromDb -> book.getAuthorId() == bookFromDb.getAuthorId())
                        .forEach(bookList::add);
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                bookList.forEach(responseObserver::onNext);
                responseObserver.onCompleted();
            }
        };
    }
}
