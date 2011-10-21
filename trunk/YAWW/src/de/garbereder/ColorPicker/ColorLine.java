package de.garbereder.ColorPicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

public class ColorLine extends View {

	private Paint mPaint;
	private final int[] mColors;
	private int mColor;
	private float mMargin = 5;
	
	public ColorLine(Context context, int[] colors) {
		super(context);
		mColors = colors;
		
		Shader gradient = new LinearGradient(0,0,0,getHeight(),mColors,null,Shader.TileMode.MIRROR);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(gradient);
        mPaint.setStrokeWidth(1);
        
        mColor = 0xFFFF0000;
        
	}

    @Override 
    protected void onDraw(Canvas canvas) {
    	Paint paint = new Paint();
        paint.setColor(0xFF000000);
    	paint.setStrokeWidth(2);
    	
    	float[] pts = new float[16]; // 4 points
    	float pos = colorToNormalizedPosition(mColor);
    	pts[0] = 0;
    	pts[1] = pos*getHeight()-2.5f;
    	pts[2] = getWidth();
    	pts[3] = pos*getHeight()-2.5f;
    	
    	pts[4] = getWidth();
    	pts[5] = pos*getHeight()-2.5f;
    	pts[6] = getWidth();
    	pts[7] = pos*getHeight()+2.5f;
    	
    	pts[8] = getWidth();
    	pts[9] = pos*getHeight()+2.5f;
    	pts[10] = 0;
    	pts[11] = pos*getHeight()+2.5f;
    	
    	pts[12] = 0;
    	pts[13] = pos*getHeight()+2.5f;
    	pts[14] = 0;
    	pts[15] = pos*getHeight()-2.5f;
    	
    	canvas.drawRect(mMargin, 0, getWidth()-mMargin, getHeight(), mPaint);    	
    	canvas.drawLines(pts, paint);
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
    		int bottom) {
    	super.onLayout(changed, left, top, right, bottom);
		Shader gradient = new LinearGradient(0,0,0,getHeight(),mColors,null,Shader.TileMode.MIRROR);
        mPaint.setShader(gradient);
    }
    
    private float colorToNormalizedPosition( int color )
    {
		int i;
		boolean up = false;
		int changeingBits = 0;
    	for( i = 0; i < mColors.length-1; ++i )
    	{
    		changeingBits = mColors[i] ^ mColors[i+1];
    		if( (color & ~changeingBits) == mColors[i] && (color | changeingBits) == mColors[i+1])
    		{
    			up = true;
    			break;
    		}
    		else if((color & ~changeingBits) == mColors[i+1] && (color | changeingBits) == mColors[i] )
    		{
    			break;
    		}
    	}
    	if( changeingBits == 0xFF000000 )
    	{
    		return normalizePos( i, color, changeingBits, 24, up );
    	}
    	else if( changeingBits == 0x00FF0000 )
    	{
    		return normalizePos( i, color, changeingBits, 16, up );
    	}
    	else if( changeingBits == 0x0000FF00 )
    	{
    		return normalizePos( i, color, changeingBits, 8, up );
    	}
    	else if( changeingBits == 0x000000FF )
    	{
    		return normalizePos( i, color, changeingBits, 0, up );
    	}
    	return -1;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	switch(event.getAction())
    	{
			case MotionEvent.ACTION_UP:
    		case MotionEvent.ACTION_MOVE:
				// y position zwischen 0 und 1
				float normaPos = normalizePosition(event.getY(), getHeight());
				int color = interpolateColor(mColors, normaPos);
				setColor(color & 0x00FFFFFF + (mColor & 0xFF000000));
				invalidate();
    		break;
    	}
    	return true;
    }

    private void setColor(int color) {
    	System.out.println(ColorPickerDialog.toHex(mColor));
		mColor = color;
	}

	private float normalizePosition( float pos, float height )
    {
		float normalPos = 0;
		if( pos < mMargin )
			normalPos = 0;
		else if( pos < height )
			normalPos = pos / height;
		else if ( pos > height )
			normalPos = 1;
		return normalPos;
    }
    
    private static final int interpolateColor( int[] colors, float normalizedPosition )
    {
    	int length = colors.length;
		// letzte ueberschrittene stuetzstelle
		int fromIdx = lowerBound(normalizedPosition,length);
		// relative entferung von der letzten stuetzstelle
		// Differenz zwsichen normalisierte Position und letzter Stützstelle
		// mal geteilt durch größe der Stützstelle.
		// ursprüngliche formel ist:
		// (normPos - normIdx) / (1/length)
		// d.h. differenz relativ zur größe
		float fromRelative = (float) (normalizedPosition - fromIdx/(length-1.0))*(length-1);
		System.out.println(fromIdx);
		System.out.println(fromRelative);
		if( fromRelative > 1 ){
			fromRelative -= 1;
			fromIdx += 1;
		}
		
		if( fromIdx >= length-1 ){
			fromIdx = length-2;
			fromRelative = 1;
		}
		if( fromIdx < 0 ){
			fromIdx = 0;
			fromRelative = 0;
		}
			
		// byte finden, welches veraendert wird
		int diff = colors[fromIdx] ^ colors[fromIdx+1];
		int offset = 0;
		int color = 0;
		// wenn beim unteren index der wert auf 0 steht wird hoch gezaehlt
		if( (diff & colors[fromIdx]) == 0 )
		{
			offset = (int)(0x000000FF * (fromRelative));
			color = colors[fromIdx];
		}
		// sonst wird runtergezählt und so 1-differenz gerechnet
		else if( (diff & colors[fromIdx+1]) == 0 )
		{
			offset = (int)(0x000000FF * (1-fromRelative));
			color = colors[fromIdx+1];
		}
		// bit shift um die bytes an die richtige stelle zu schieben
		if( 0x0000FF00 == diff )
		{
			offset = offset << 8;
		}
		else if( 0x00FF0000 == diff )
		{
			offset = offset << 16;
		}
		else if( 0xFF000000 == diff )
		{
			offset = offset << 24;
		}
		return offset + color;
    }
    
    private static final int lowerBound( float normalizedPosition, int numberOfEntries )
    {
    	int i = -1;
    	float pos = 0;
    	float inc = 1.0f/(numberOfEntries-1);
    	while( normalizedPosition > pos )
    	{
    		pos += inc;
    		++i;
    	}
    	return i;
    }
    
    private float normalizePos( int idx, int color, int changeingBits, int offset, boolean up )
    {
		int x = changeingBits >> offset;
		int y = (color & changeingBits) >> offset;
		if( up )
			return 1.0f/(mColors.length-1)*idx + 1.0f/(mColors.length-1)*(((float)y/x));
		else
			return 1.0f/(mColors.length-1)*idx + 1.0f/(mColors.length-1)*(1-((float)y/x));
    	
    }
    
	public void setMargin(float mMargin) {
		this.mMargin = mMargin;
	}

	public float getMargin() {
		return mMargin;
	}

}
