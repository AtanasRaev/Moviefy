package com.moviefy.utils;

public class MediaRetrievalUtil {

    public static boolean isMediaTypeInvalid(String mediaType) {
        return !"all".equalsIgnoreCase(mediaType) && !"movies".equalsIgnoreCase(mediaType) && !"series".equalsIgnoreCase(mediaType);
    }
}
