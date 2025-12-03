package com.monnfamily.enlibraryapp.Contentful;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import com.monnfamily.enlibraryapp.Constants.Constant;

@ContentType(Constant.MAKE_APP_FREE)
public class MakeAppFree extends Resource {
    @Field String makeAppFree;
    @Field Boolean isAppFree;

    public Boolean isAppFree() {
        return isAppFree;
    }
}
