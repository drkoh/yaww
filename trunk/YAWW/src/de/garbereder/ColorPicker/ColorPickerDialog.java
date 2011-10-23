package de.garbereder.ColorPicker;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ColorPickerDialog extends Dialog {

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    private Context ctx;
    private int mInitialColor;
    private Button mColorButton;
	private ColorPickerView mCpv;
	private ColorRect cr;
	private ColorLine cl;

    private static class ColorPickerView extends View {
        private Paint mRainbowPaint;
        private Paint mChoosenPaint;
        private Paint mAlphaPaint;

        private final float mRainbowHeight;
        private final float mRainbowWidth;
        
        private int mColor = 0xFF000000;
        private float mRainbowNormalPos = 0;
        private float mAlphaNormalPos = 0;
        
        private final float mMargin;
        private final int[] mColors;
        private final int[] mAlphaColors;
        
        private List<OnColorChangedListener> mListener;
        
        ColorPickerView(Context c, int color) {
            super(c);
            
            mListener = new ArrayList<ColorPickerDialog.OnColorChangedListener>();
            
            mColors = new int[] {
            		0xFF000000,
                    0xFF0000FF,
                    0xFF00FFFF,
                    0xFF00FF00,
                    0xFFFFFF00,
                    0xFFFF0000,
                    0xFFFF00FF,
                    0xFFFFFFFF 
            };
            mAlphaColors = new int[] {
            	0xFF000000,
            	0x00000000
            };
            
            mMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
            mRainbowWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            mRainbowHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, getResources().getDisplayMetrics());
            
            setLayoutParams(new LayoutParams(-1,(int)(mRainbowHeight+mMargin)));
            
            Shader gradient = new LinearGradient(0,0,0,mRainbowHeight,mColors,null,Shader.TileMode.MIRROR);
            
            mRainbowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mRainbowPaint.setShader(gradient);
            mRainbowPaint.setStrokeWidth(5);

            Shader alphaGradient = new LinearGradient(0,0,0,mRainbowHeight,mAlphaColors,null,Shader.TileMode.MIRROR);
            mAlphaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mAlphaPaint.setShader(alphaGradient);
            mAlphaPaint.setStrokeWidth(5);
            
            mChoosenPaint = new Paint();
            mChoosenPaint.setColor(0xFF000000);
            mChoosenPaint.setStrokeWidth(5);
        }

        @Override 
        protected void onDraw(Canvas canvas) {
        	Paint paint = new Paint();
            paint.setColor(0xFF000000);
        	paint.setStrokeWidth(1);
        	canvas.drawRect(mMargin, 0, mRainbowWidth + mMargin, mRainbowHeight, mRainbowPaint);
        	canvas.drawRect(mMargin*2+mRainbowWidth, 0, mRainbowWidth + mMargin*2+mRainbowWidth, mRainbowHeight, mAlphaPaint);
        	
        	canvas.drawRect(mMargin*3+mRainbowWidth*2,0,getWidth() - mMargin,mRainbowHeight, mChoosenPaint);
        	
        	canvas.drawRect(mMargin-5, mRainbowNormalPos*mRainbowHeight-5, mMargin+mRainbowWidth+5, mRainbowNormalPos*mRainbowHeight+5, paint);
        	canvas.drawRect(mMargin*2+mRainbowWidth-5, mAlphaNormalPos*mRainbowHeight-5, mMargin*2+mRainbowWidth+mRainbowWidth+5, mAlphaNormalPos*mRainbowHeight+5, paint);
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
        	switch(event.getAction())
        	{
    			case MotionEvent.ACTION_UP:
        		case MotionEvent.ACTION_MOVE:
        			if(event.getX() < mMargin+mRainbowWidth &&
        					event.getX() > mMargin &&
        					event.getY() < mRainbowHeight*1.01
        				)
        			{
        				// y position zwischen 0 und 1
        				mRainbowNormalPos = normalizePosition(event.getY(), mRainbowHeight);
        				int color = interpolateColor(mColors, mRainbowNormalPos);
        				setColor(color & 0x00FFFFFF + (mColor & 0xFF000000));
        				mChoosenPaint.setColor(mColor);
        				mAlphaColors[0] = mColor | 0xFF000000; // set alpha to 1
        				mAlphaColors[1] = mAlphaColors[0] & 0x00FFFFFF; // set alpha to 0
        	            Shader alphaGradient = new LinearGradient(0,0,0,mRainbowHeight,mAlphaColors,null,Shader.TileMode.MIRROR);
        	            mAlphaPaint.setShader(alphaGradient);
        				invalidate();
        			}
        			// ALPHA
        			if(event.getX() < mMargin*2+2*mRainbowWidth &&
        					event.getX() > mMargin*2 + mRainbowWidth &&
        					event.getY() < mRainbowHeight*1.01 )
        			{
        				mAlphaNormalPos = normalizePosition(event.getY(), mRainbowHeight);
        				setColor(interpolateColor(mAlphaColors, mAlphaNormalPos));
        				mChoosenPaint.setColor(mColor);
        				invalidate();
        			}
        		break;
        	}
        	return true;
        }
        
        private void invokeOnColorChanged(int color)
        {
        	for( OnColorChangedListener l : mListener )
        		l.colorChanged(color);
        }
        
        public void addOnColorChangedListener( OnColorChangedListener l )
        {
        	mListener.add(l);
        }
        
        private float normalizePosition( float pos, float height )
        {
			float normalPos = 0;
			if( pos < 0 )
				normalPos = 0;
			else if( pos < mRainbowHeight )
				normalPos = pos / mRainbowHeight;
			else if ( pos < mRainbowHeight*1.01 )
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
        
        public void setColor(int color)
        {
        	mColor = color;
        	invokeOnColorChanged(color);
        }
    }

    public ColorPickerDialog(Context context,
                             int initialColor) {
        super(context);
        mInitialColor = initialColor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        
        ctx = getContext();
        LinearLayout layout = new LinearLayout(ctx);
        
        mCpv = new ColorPickerView(ctx, mInitialColor);
        mColorButton = new Button(ctx);
        mColorButton.setText(toHex(mInitialColor));
        mColorButton.setOnClickListener( new View.OnClickListener() {
			
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(ctx);

				alert.setTitle("Color");
				alert.setMessage("Choose color");

				// Set an EditText view to get user input 
				final EditText input = new EditText(ctx);
				input.setText(mColorButton.getText());
				alert.setView(input);

				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				  mCpv.setColor(new BigInteger(input.getText().toString().substring(2), 16).intValue());
				  }
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				});

				alert.show();
			}
		});

        mCpv.addOnColorChangedListener(new OnColorChangedListener() {
			public void colorChanged(int color) {
				mColorButton.setText(toHex(color));
			}
		});
        layout.setLayoutParams(new LayoutParams(-1,-1));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        //layout.addView(mCpv);

        int[] colors = new int[] {
            0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
            0xFFFFFF00, 0xFFFF0000
        };
        cl = new ColorLine(ctx, colors);
        cl.setColor(0xFFFF0000);
        cl.setMargin(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, ctx.getResources().getDisplayMetrics()));
        cl.setLayoutParams(new LayoutParams(25,-1));
        cl.addOnColorChangedListener(new OnColorChangedListener() {
			
			@Override
			public void colorChanged(int color) {
				cr.setColor(color);
			}
			
		});
        cr = new ColorRect(ctx, 0xFFFF0000);
        cr.setLayoutParams(new LayoutParams(255,255));
        layout.addView(cl);
        layout.addView(cr);
        //layout.addView(mColorButton);
        setContentView(layout);
        setTitle("Pick a Color");
    }
    
	public final static String toHex( int i )
    {
    	String s = Integer.toHexString(i);
    	while( s.length() < 8 )
    	{
    		s = "0" + s;
    	}
    	return "0x"+s.toUpperCase();
    }

	public int getColor() {
		return mCpv.mColor;
	}
}
