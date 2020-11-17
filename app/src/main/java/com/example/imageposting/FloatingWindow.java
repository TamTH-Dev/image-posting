package com.example.imageposting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;

import com.txusballesteros.bubbles.BubbleLayout;
import com.txusballesteros.bubbles.BubblesManager;

public class FloatingWindow {
    private final Activity activity;
    private final Context context;
    private final BubblesManager bubblesManager;
    private final ScreenRecorder screenRecorder;

    public FloatingWindow(Activity activity, Context context, ScreenRecorder screenRecorder) {
        this.activity = activity;
        this.screenRecorder = screenRecorder;
        this.context = context;

        this.bubblesManager = new BubblesManager.Builder(context)
                .setTrashLayout(R.layout.bubble_remove).build();
        this.bubblesManager.initialize();
    }

    public void addNewBubble() {
        BubbleLayout bubbleView = (BubbleLayout) LayoutInflater.from(activity)
                .inflate(R.layout.bubble_layout, null);

        bubbleView.setOnBubbleRemoveListener(bubble -> {
            if (screenRecorder.getIsRecording()) {
                screenRecorder.stopRecord();
            }
            Intent mainScreen = new Intent(context, activity.getClass());
            activity.startActivity(mainScreen);
        });

        bubbleView.setOnBubbleClickListener(bubble -> {
            bubblesManager.removeBubble(bubbleView);
        });

        bubbleView.setShouldStickToWall(true);
        bubblesManager.addBubble(bubbleView, 60, 20);
    }

    public void destroyFloatingWindow() {
        bubblesManager.recycle();
    }
}

