package com.cs595.uwm.chatbylocation.objModel;

import com.cs595.uwm.chatbylocation.R;

/**
 * Created by Nathan on 4/20/17.
 */

public class UserIcon {
    public static final String NONE = "none";
    public static final int NONE_RESOURCE = R.drawable.ic_default_icon;
    public static final String PHOTO = "custom";
    public static final String BEAR = "bear";
    public static final int BEAR_RESOURCE = R.drawable.ic_bear;
    public static final String DRAGON = "dragon";
    public static final int DRAGON_RESOURCE = R.drawable.ic_dragon;
    public static final String ELEPHANT = "elephant";
    public static final int ELEPHANT_RESOURCE = R.drawable.ic_elephant;
    public static final String HIPPO = "hippo";
    public static final int HIPPO_RESOURCE = R.drawable.ic_hippo;
    public static final String KOALA = "koala";
    public static final int KOALA_RESOURCE = R.drawable.ic_koala;

    public static int getIconResource(String icon) {
        if (icon == null) return NONE_RESOURCE;
        switch (icon) {
            case PHOTO:
                return 0;
            case BEAR:
                return BEAR_RESOURCE;
            case DRAGON:
                return DRAGON_RESOURCE;
            case ELEPHANT:
                return ELEPHANT_RESOURCE;
            case HIPPO:
                return HIPPO_RESOURCE;
            case KOALA:
                return KOALA_RESOURCE;
            default:
                return NONE_RESOURCE;
        }
    }
}
