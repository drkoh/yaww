package de.garbereder.ColorPicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class ColorRect extends View {

	int[] mColors;
	
	public ColorRect(Context context, int vertexColor) {
		super(context);
		
		//! @todo FIX ME please
		/*
		 * 0xFFFFFFFF	...		0xFFVERTEX
		 * 		.					.
		 * 		.					.
		 * 		.					.
		 * 0xFF000000	...		0xFF000000
		 */
		int a = (vertexColor & 0xFF000000) >> 24;
		int r = (vertexColor & 0x00FF0000) >> 16;
		int g = (vertexColor & 0x0000FF00) >> 8;
		int b = vertexColor & 0x000000FF;
		int aStep = a / 0x000000FF;
		int rStep = r / 0x000000FF;
		int gStep = g / 0x000000FF;
		int bStep = b / 0x000000FF;
        mColors = new int[65025]; // 255*255
        for (int i = 0; i < 255; i++) {
            for (int j = 0; j < 255; j++) {
            	mColors[i*255 + j] = (0xFFFFFFFF - (a+aStep*i << 24)) | (0xFFFFFFFF - (r+rStep*i << 16)) | (0xFFFFFFFF - (g+gStep*i << 8)) | (0xFFFFFFFF - b+bStep*i);
            	//colors[y * 255 + x] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		Paint paint = new Paint();
		canvas.drawBitmap(mColors, 0, 255, 0, 0, 255, 255, true, paint);
	}

}
