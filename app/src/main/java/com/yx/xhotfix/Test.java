package com.yx.xhotfix;

import android.content.Context;
import android.widget.Toast;

/**
 * Author by YX, Date on 2019/10/25.
 */
public class Test {

    public void add(Context context){
        int a = 10;
        int b = 0;
        Toast.makeText(context, "shit:"+(a/b), Toast.LENGTH_SHORT).show();
    }
}
