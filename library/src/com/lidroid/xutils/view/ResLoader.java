package com.lidroid.xutils.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Author: wyouflf
 * Date: 13-11-9
 * Time: 下午3:12
 */
public class ResLoader {

    public static Object loadRes(ResType type, Context context, int id) {
        if (context == null || id < 1) return null;
        switch (type) {
            case Animation:
                return Animation(context, id);
            case Boolean:
                return Boolean(context, id);
            case Color:
                return Color(context, id);
            case ColorStateList:
                return ColorStateList(context, id);
            case Dimension:
                return Dimension(context, id);
            case DimensionPixelOffset:
                return DimensionPixelOffset(context, id);
            case DimensionPixelSize:
                return DimensionPixelSize(context, id);
            case Drawable:
                return Drawable(context, id);
            case Integer:
                return Integer(context, id);
            case IntArray:
                return IntArray(context, id);
            case Movie:
                return Movie(context, id);
            case String:
                return String(context, id);
            case StringArray:
                return StringArray(context, id);
            case Text:
                return Text(context, id);
            case TextArray:
                return TextArray(context, id);
            case Xml:
                return Xml(context, id);
            default:
                break;
        }

        return null;
    }

    public static Animation Animation(Context context, int id) {
        return AnimationUtils.loadAnimation(context, id);
    }

    public static boolean Boolean(Context context, int id) {
        return context.getResources().getBoolean(id);
    }

    public static int Color(Context context, int id) {
        return context.getResources().getColor(id);
    }

    public static ColorStateList ColorStateList(Context context, int id) {
        return context.getResources().getColorStateList(id);
    }

    public static float Dimension(Context context, int id) {
        return context.getResources().getDimension(id);
    }

    public static int DimensionPixelOffset(Context context, int id) {
        return context.getResources().getDimensionPixelOffset(id);
    }

    public static int DimensionPixelSize(Context context, int id) {
        return context.getResources().getDimensionPixelSize(id);
    }

    public static Drawable Drawable(Context context, int id) {
        return context.getResources().getDrawable(id);
    }

    public static int Integer(Context context, int id) {
        return context.getResources().getInteger(id);
    }

    public static int[] IntArray(Context context, int id) {
        return context.getResources().getIntArray(id);
    }

    public static Movie Movie(Context context, int id) {
        return context.getResources().getMovie(id);
    }

    public static String String(Context context, int id) {
        return context.getResources().getString(id);
    }

    public static String[] StringArray(Context context, int id) {
        return context.getResources().getStringArray(id);
    }

    public static CharSequence Text(Context context, int id) {
        return context.getResources().getText(id);
    }

    public static CharSequence[] TextArray(Context context, int id) {
        return context.getResources().getTextArray(id);
    }

    public static XmlResourceParser Xml(Context context, int id) {
        return context.getResources().getXml(id);
    }
}
