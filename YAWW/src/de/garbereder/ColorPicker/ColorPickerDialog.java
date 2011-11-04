package de.garbereder.ColorPicker;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
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
    private Button mOkButton;
	private ColorRect cr;
	private ColorLine cl;
	private ColorLine al;
    private List<OnColorChangedListener> mListener;
    public ColorPickerDialog cpd;

    public ColorPickerDialog(Context context,
                             int initialColor) {
        super(context);
        mInitialColor = initialColor;
        mListener = new ArrayList<ColorPickerDialog.OnColorChangedListener>();
        cpd = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        
        ctx = getContext();
        LinearLayout verticalLayout = new LinearLayout(ctx);
        LinearLayout horizontalLayout = new LinearLayout(ctx);
        LinearLayout horizontalLayout2 = new LinearLayout(ctx);
        
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

        horizontalLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        horizontalLayout2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        horizontalLayout2.setOrientation(LinearLayout.HORIZONTAL);

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
					mColorButton.setText(input.getText());
					//! @todo fixme
					//cl.setColor(new BigInteger(input.getText().toString().substring(2), 16).intValue());
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

        mOkButton = new Button(ctx);
        mOkButton.setText("Ok");
        mOkButton.setOnClickListener(new View.OnClickListener() {
        	
			@Override
			public void onClick(View v) {
				invokeOnColorChanged(getColor());
				cpd.dismiss();
			}
		});

        int[] colors = new int[] {
            0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
            0xFFFFFF00, 0xFFFF0000
        };
        int[] aColors = new int[] {
                0x00FF0000, 0xFFFF0000
            };
        cl = new ColorLine(ctx, colors);
        cl.setColor(0xFFFF0000);
        cl.setMargin(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, ctx.getResources().getDisplayMetrics()));
        cl.setLayoutParams(new LayoutParams(25,-1));
        cl.addOnColorChangedListener(new OnColorChangedListener() {
			
			@Override
			public void colorChanged(int color) {
				cr.setVertexColor(color);
				al.setColors(new int[]{color&0x00FFFFFF,color});
				al.setColor((color & 0x00FFFFFF) | (al.getColor() & 0xFF000000)); // keep alpha channel
				updateColorButton();
			}
			
		});
        al = new ColorLine(ctx, aColors);
        al.setColor(0xFFFF0000);
        al.setMargin(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, ctx.getResources().getDisplayMetrics()));
        al.setLayoutParams(new LayoutParams(25,-1));
        al.addOnColorChangedListener(new OnColorChangedListener() {
			
			@Override
			public void colorChanged(int color) {
				updateColorButton();
			}
			
		});
        cr = new ColorRect(ctx, 0xFFFF0000);
        cr.setLayoutParams(new LayoutParams(300,300));
        cr.addOnColorChangedListener(new OnColorChangedListener() {
			
			@Override
			public void colorChanged(int color) {
				updateColorButton();
			}
			
		});
        horizontalLayout.addView(cl);
        horizontalLayout.addView(al);
        horizontalLayout.addView(cr);
        horizontalLayout2.addView(mColorButton);
        horizontalLayout2.addView(mOkButton);
        verticalLayout.addView(horizontalLayout);
        verticalLayout.addView(horizontalLayout2);
        setContentView(verticalLayout);
        setTitle("Pick a Color");
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
    
    private void updateColorButton()
    {
		mColorButton.setText(ColorPickerDialog.toHex(getColor()));
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
		return (cr.getColor() & 0x00FFFFFF) | (al.getColor() & 0xFF000000);
	}
}
