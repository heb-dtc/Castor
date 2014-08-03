package com.heb.castor.app.presentations;

import android.app.Presentation;
import android.content.Context;
import android.view.Display;

public abstract class CastPresentation extends Presentation {

    private String presentationName;

    public CastPresentation(Context outerContext, Display display, String name) {
        super(outerContext, display);
        presentationName = name;
    }

    public CastPresentation(Context outerContext, Display display, String name, int theme) {
        super(outerContext, display, theme);
        presentationName = name;
    }

    protected String getName(){
        return presentationName;
    }
}
