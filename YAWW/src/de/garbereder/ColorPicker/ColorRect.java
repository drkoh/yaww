package de.garbereder.ColorPicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class ColorRect extends View {

	int[] mColors;
	
	public ColorRect(Context context, int vertexColor) {
		super(context);
		setColor(vertexColor);
	}
	
	public void setColor( int vertexColor )
	{
		//! @todo add comments
		/*
		 * 0xFF000000	...		0xFF000000
		 * 		.					.
		 * 		.					.
		 * 		.					.
		 * 0xFFFFFFFF	...		0xFFVERTEX
		 */
		int tl = 0xFF000000;
		int tr = 0xFF000000;
		int bl = 0xFFFFFFFF;
		int br = vertexColor;
		
		float vStepA = (((bl & 0xFF000000) >> 24) - ((tl & 0xFF000000) >> 24)) / 255.0f;
		float vStepR = (((bl & 0x00FF0000) >> 16) - ((tl & 0x00FF0000) >> 16)) / 255.0f;
		float vStepG = (((bl & 0x0000FF00) >>  8) - ((tl & 0x0000FF00) >>  8)) / 255.0f;
		float vStepB = (( bl & 0x000000FF       ) - ( tl & 0x000000FF       )) / 255.0f;
		
		float vStepA1 = (((br & 0xFF000000) >> 24) - ((tr & 0xFF000000) >> 24)) / 255.0f;
		float vStepR1 = (((br & 0x00FF0000) >> 16) - ((tr & 0x00FF0000) >> 16)) / 255.0f;
		float vStepG1 = (((br & 0x0000FF00) >>  8) - ((tr & 0x0000FF00) >>  8)) / 255.0f;
		float vStepB1 = (( br & 0x000000FF       ) - ( tr & 0x000000FF       )) / 255.0f;
		
        mColors = new int[65025]; // 255*255
        int from, to;
        float hStepA, hStepR, hStepG, hStepB; 
        for (int i = 0; i < 255; i++) {
        	// reihe
        	to   = tl+((int)(vStepA *i) << 24)+((int)(vStepR *i) << 16)+((int)(vStepG *i) << 8)+(int)(vStepB *i);
        	from = tr+((int)(vStepA1*i) << 24)+((int)(vStepR1*i) << 16)+((int)(vStepG1*i) << 8)+(int)(vStepB1*i);
    		hStepA = (((to & 0xFF000000) >> 24) - ((from & 0xFF000000) >> 24)) / 255.0f;
    		hStepR = (((to & 0x00FF0000) >> 16) - ((from & 0x00FF0000) >> 16)) / 255.0f;
    		hStepG = (((to & 0x0000FF00) >>  8) - ((from & 0x0000FF00) >>  8)) / 255.0f;
    		hStepB = (( to & 0x000000FF       ) - ( from & 0x000000FF       )) / 255.0f;
            for (int j = 0; j < 255; j++) {
            // spalte
        		mColors[65024-(i*255+j)] = from + ((int)(hStepA*j) << 24) + ((int)(hStepR*j) << 16) + ((int)(hStepG*j) << 8) + (int)(hStepB*j);
        		//mColors[i*255+j] = from;
            }
        }
        invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		Paint paint = new Paint();
		canvas.drawBitmap(mColors, 0, 255, 0, 0, getWidth(), getHeight(), true, paint);
	}

}
