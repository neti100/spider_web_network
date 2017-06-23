package lcl.android.spider.web.network.util;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * AES256 암호화 유틸
 * Created by CHS on 2017-06-21.
 */
public class FontUtil {

    public static void setGlobalFont(View view, Typeface typeface) {
        if(view != null) {
            if(view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup)view;
                int vgCnt = viewGroup.getChildCount();
                for(int i = 0; i<vgCnt; i++) {
                    View v = viewGroup.getChildAt(i);
                    if(v instanceof TextView) {
                        ((TextView) v).setTypeface(typeface);
                    }
                    setGlobalFont(v, typeface);
                }
            }
        }
    }

}
