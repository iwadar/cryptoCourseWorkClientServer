package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private int requestCode;
    private String fileName;
    private long sizeFileToSend;
    private int status;
}
