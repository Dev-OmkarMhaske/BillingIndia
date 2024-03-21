package com.tsysinfo.billing;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ResizableImageView extends ImageView {

	public ResizableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int width = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(width, width);

	}
}