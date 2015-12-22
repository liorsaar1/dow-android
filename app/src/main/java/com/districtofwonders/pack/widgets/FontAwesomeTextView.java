package com.districtofwonders.pack.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This textview renders a font awesome icon when provided with a know FA string
 *
 * the font codes come from the json found in
 * @see: https://gist.github.com/jabney/7720a6f92e53a830578c
 *
 * the json should refreshed when new fonts are defined using this script
 * @see: https://gist.github.com/jabney/8b395c983b70157e6492
 * <p/>
 * Created by liorsaar on 2015-10-04
 */
public class FontAwesomeTextView extends TextView {

    private static Typeface typeface;
    private static Map<String, String> unicodeMap;

    public FontAwesomeTextView(Context context) {
        super(init(context));
    }

    public FontAwesomeTextView(Context context, AttributeSet attrs) {
        super(init(context), attrs);
    }

    public FontAwesomeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(init(context), attrs, defStyle);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(getUnicode(text), type);
        setTypeface(typeface);
    }

    /**
     * can be called on launch, or lazy
     * @param context
     * @return
     */
    public static Context init(Context context) {
        try {
            // typeface
            if (typeface == null) {
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");
            }
            // unicode map
            if (unicodeMap == null) {
                unicodeMap = createUnicodeMap(context);
            }
        } catch (Throwable t) {

        }
        return context;
    }

    private static Map<String, String> createUnicodeMap(Context context) {
        JSONObject jsonObject = readJson(context, "fonts/fontawesome.json");
        Map<String, String> unicodeMap = new HashMap(jsonObject.length() + 10);
        Iterator keys = jsonObject.keys();
        while (keys.hasNext()) {
            String name = (String) keys.next();
            try {
                String value = jsonObject.getString(name);
                String code = getCode(value);
                unicodeMap.put(name, code);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return unicodeMap;
    }

    private static JSONObject readJson(Context context, String jsonName) {
        try {
            return readAsset(context, jsonName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static JSONObject readAsset(Context context, String assetName) throws IOException, JSONException {
        InputStream inputStream = context.getAssets().open(assetName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null)
            stringBuilder.append(line);
        return new JSONObject(stringBuilder.toString());
    }

    private static String getCode(String escapedHtmlString) {
        String hex = escapedHtmlString.substring(3, 7);
        int code = Integer.parseInt(hex, 16);
        StringBuffer sb = new StringBuffer();
        sb.append(Character.toChars(code));
        return sb.toString();
    }

    private static CharSequence getUnicode(CharSequence fontAwesomeName) {
        // working in the IDE
        if (unicodeMap == null) return "\uf118";

        // find the name in the json
        String name = fontAwesomeName.toString();
        if (name.startsWith("fa-")) {
            name = name.substring(3);
        }
        if (unicodeMap.containsKey(name)) {
            return unicodeMap.get(name);
        }
        // not found
        return fontAwesomeName;
    }
}