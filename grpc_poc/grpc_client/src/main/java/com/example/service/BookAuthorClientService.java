package com.example.service;

import com.example.Author;
import com.example.AuthorId;
import com.example.BookAuthorServiceGrpc;
import com.google.protobuf.Descriptors;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BookAuthorClientService {
    @GrpcClient("grpc-service")
    BookAuthorServiceGrpc.BookAuthorServiceBlockingStub synchronousClient;

//    @GrpcClient("grpc-service")
//    BookAuthorServiceGrpc.BookAuthorServiceStub asynchronousClient;

    public Map<Descriptors.FieldDescriptor,Object> getAuthor(int authorId){
        AuthorId request=AuthorId.newBuilder().setAuthorId(authorId).build();
        Author response=synchronousClient.getAuthorDetails(request);
        return response.getAllFields();
    }
}
