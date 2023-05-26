package com.example.clientwithui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    private int requestCode;
    private String fileName;
    private long sizeFileToSend;
}