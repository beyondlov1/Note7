package com.beyond.exception;

/**
 * @author: beyond
 * @date: 2021/9/28
 */

public class CalendarErrorException extends RuntimeException{

    public CalendarErrorException(String message) {
        super(message);
    }
}
