package com.monnfamily.enlibraryapp.Contentful;

import com.contentful.vault.Space;
import com.monnfamily.enlibraryapp.Constants.Constant;

@Space(value = Constant.SPACE, models = {Book.class, Page.class, Cast.class, Category.class, MakeAppFree.class}   , locales = "en-US")
public final class ContentfulSpaceLink {
}
