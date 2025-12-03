package com.monnfamily.enlibraryapp.Contentful;

import com.contentful.java.cda.CDAClient;
import com.monnfamily.enlibraryapp.Constants.Constant;

public final class ContentfulClient {
    private static final CDAClient CLIENT = CDAClient.builder()
            .setSpace(Constant.SPACE)
            .setToken(Constant.CDAPI)
            .build();

    public static CDAClient get() {
        return CLIENT;
    }
}
