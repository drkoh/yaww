package de.garbereder.ColorPicker;

import java.util.ArrayList;
import java.util.List;

import de.garbereder.ColorPicker.ColorPickerDialog.OnColorChangedListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class ColorRect extends View {

	private class Pair<T>
	{
		private T _first;
		private T _second;
		public Pair( T first, T second )
		{
			this._first = first;
			this._second = second;
		}
		public T getFirst() {
			return _first;
		}
		public void setFirst(T first) {
			this._first = first;
		}
		public T getSecond() {
			return _second;
		}
		public void setSecond(T second) {
			this._second = second;
		}
		@Override
		public String toString()
		{
			return "(" + _first + "|" + _second + ")";
		}
	}
	
	private int[] mColors;
	private boolean init = false;
	private int vertexColor;
	private Pair<Float> cursorPosition;
    private List<OnColorChangedListener> mListener;
	
	public ColorRect(Context context, int vertexColor) {
		super(context);
		this.vertexColor = vertexColor;
		this.cursorPosition = new Pair<Float>(1.0f,0.0f);
        mListener = new ArrayList<ColorPickerDialog.OnColorChangedListener>();
	}
	
    public void addOnColorChangedListener( OnColorChangedListener l )
    {
    	mListener.add(l);
    }
    
    private void invokeOnColorChanged(int color)
    {
    	for( OnColorChangedListener l : mListener )
    		l.colorChanged(color);
    }
    
	public void setVertexColor( int vertexColor )
	{
		this.vertexColor = vertexColor;
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

		int size = Math.min(getMeasuredWidth(),getMeasuredHeight());
		if(size == 0)
			return;
		
		float fSize = (float)size;
		
		float vStepA = (((bl & 0xFF000000) >> 24) - ((tl & 0xFF000000) >> 24)) / fSize;
		float vStepR = (((bl & 0x00FF0000) >> 16) - ((tl & 0x00FF0000) >> 16)) / fSize;
		float vStepG = (((bl & 0x0000FF00) >>  8) - ((tl & 0x0000FF00) >>  8)) / fSize;
		float vStepB = (( bl & 0x000000FF       ) - ( tl & 0x000000FF       )) / fSize;
		
		float vStepA1 = (((br & 0xFF000000) >> 24) - ((tr & 0xFF000000) >> 24)) / fSize;
		float vStepR1 = (((br & 0x00FF0000) >> 16) - ((tr & 0x00FF0000) >> 16)) / fSize;
		float vStepG1 = (((br & 0x0000FF00) >>  8) - ((tr & 0x0000FF00) >>  8)) / fSize;
		float vStepB1 = (( br & 0x000000FF       ) - ( tr & 0x000000FF       )) / fSize;
		
        mColors = new int[size*size];
        int from, to;
        float hStepA, hStepR, hStepG, hStepB; 
        for (int i = 0; i < size; i++) {
        	// reihe
        	to   = tl+((int)(vStepA *i) << 24)+((int)(vStepR *i) << 16)+((int)(vStepG *i) << 8)+(int)(vStepB *i);
        	from = tr+((int)(vStepA1*i) << 24)+((int)(vStepR1*i) << 16)+((int)(vStepG1*i) << 8)+(int)(vStepB1*i);
    		hStepA = (((to & 0xFF000000) >> 24) - ((from & 0xFF000000) >> 24)) / fSize;
    		hStepR = (((to & 0x00FF0000) >> 16) - ((from & 0x00FF0000) >> 16)) / fSize;
    		hStepG = (((to & 0x0000FF00) >>  8) - ((from & 0x0000FF00) >>  8)) / fSize;
    		hStepB = (( to & 0x000000FF       ) - ( from & 0x000000FF       )) / fSize;

            for (int j = 0; j < size; j++) {
            // spalte
            	//
        		mColors[(size*size-1)-(i*size+j)] = from + ((int)(hStepA*j) << 24) + ((int)(hStepR*j) << 16) + ((int)(hStepG*j) << 8) + (int)(hStepB*j);
        		//mColors[i*255+j] = from;
            }
        }
        invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		if(!init /*|| getMeasuredWidth()*getMeasuredHeight() != mColors.length*/)
		{
			setVertexColor(vertexColor);
			init = true;
		}
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		int size = Math.min(getMeasuredWidth(),getMeasuredHeight());
		canvas.drawBitmap(mColors, 0, size, 0, 0, size, size, true, paint);
		paint.setColor(0xFF000000);
		paint.setStrokeWidth(2);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawCircle(cursorPosition.getFirst()*size, cursorPosition.getSecond()*size, 5, paint);
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	super.onTouchEvent(event);
    	
    	// Only Care about the square
		int size = Math.min(getMeasuredWidth(),getMeasuredHeight());
		cursorPosition = normalizeCoords(new Pair<Float>(event.getX(),event.getY()), size, size);
    	//System.out.println("Match: " + normCoords + " to " + (int)(normCoords.getFirst()*size*size+normCoords.getSecond()*size));
    	int color = mColors[(int)(cursorPosition.getFirst()*size+cursorPosition.getSecond()*size)];
    	invokeOnColorChanged(color);
    	invalidate();
    	//System.out.println(ColorPickerDialog.toHex(color));
    	
    	return true;
    }
    
    public int getColor(){
		int size = Math.min(getMeasuredWidth(),getMeasuredHeight());
    	return mColors[(int)(cursorPosition.getFirst()*size+cursorPosition.getSecond()*size)];
    };
    
	private Pair<Float> normalizeCoords( Pair<Float> xy, float width, float height )
	{
		Pair<Float> norm = new Pair<Float>(0.0f,0.0f);
		
		if( xy.getFirst() < 0 )
			norm.setFirst(0.0f);
		else if( xy.getFirst() > width )
			norm.setFirst(1.0f);
		else 
			norm.setFirst(xy.getFirst()/width);
		
		if( xy.getSecond() < 0 )
			norm.setSecond(0.0f);
		else if( xy.getSecond() > height )
			norm.setSecond(1.0f);
		else 
			norm.setSecond(xy.getSecond()/height);
		
		return norm;
	}
	/*
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	    int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
	    int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
	    this.setMeasuredDimension(
	            parentWidth / 2, parentHeight);
	}
*/
}
