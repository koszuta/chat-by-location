package com.cs595.uwm.chatbylocation.objModel;

import com.cs595.uwm.chatbylocation.R;

/**
 * Created by Nathan on 4/20/17.
 */

public class UserIcon {
    public static final String NONE = "none";
    public static final String PHOTO = "custom";
    public static final String BEAR = "bear";
    public static final String DRAGON = "dragon";
    public static final String ELEPHANT = "elephant";
    public static final String HIPPO = "hippo";
    public static final String KOALA = "koala";

    public static int getIconResource(String icon) {
        int res = R.drawable.ic_default_icon;
        if (icon == null) return res;
        switch (icon) {
            case PHOTO:
                res = 0;
                break;
            case BEAR:
                res = R.drawable.ic_bear;
                break;
            case DRAGON:
                res = R.drawable.ic_dragon;
                break;
            case ELEPHANT:
                res = R.drawable.ic_elephant;
                break;
            case HIPPO:
                res = R.drawable.ic_hippo;
                break;
            case KOALA:
                res = R.drawable.ic_koala;
                break;
        }
        return res;
    }
}
