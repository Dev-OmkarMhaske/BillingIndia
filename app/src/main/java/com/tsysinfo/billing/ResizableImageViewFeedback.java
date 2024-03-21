package com.tsysinfo.billing;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ResizableImageViewFeedback extends ImageView {

	public ResizableImageViewFeedback(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int width = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(width - ( width / 3 ), width - ( width / 3 ));

	}
}