package com.heb.castor.app.presentations;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import com.heb.castor.app.R;

public class StandByPresentation extends CastPresentation {

    private static final String STANDBY_PRESENTATION_NAME = "StandByPrez";

    public StandByPresentation(Context outerContext, Display display) {
        super(outerContext, display, STANDBY_PRESENTATION_NAME);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_stand_by);
    }
}
