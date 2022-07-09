package com.rexcola.sortingchaos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
class SortingChaosView extends SurfaceView implements SurfaceHolder.Callback {
    class SortingChaosThread extends Thread {

    	private Paint backgroundPaint;
    	private Paint foregroundPaint;
        public SortingChaosThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;

        }

        /**
         * Starts the game, setting parameters for the current difficulty.
         */
        public void doStart() {
        	doReset();
        	backgroundPaint = new Paint();
        	foregroundPaint = new Paint();
        	randomizeColors();
        		
        }

        public void doReset()
        {
        	synchronized (syncObject)
        	{
        		resetSort();
        		resetSort();
        	}
        	
        }

        private void resetSort()
        {
            // Reset the random array of values
            myRandomizer = new Random();
            randomizeArray();
            randomizeColors();

            // Reset the state
            resetQuickSort();
            highwater = mCanvasWidth -1;
            curPos = 0;
            justReset = true;
        	
        }
        private void randomizeArray()
        {
            values = new Integer[mCanvasWidth];
            for (int i= 0; i < mCanvasWidth; i++)
            {
            	values[i] = myRandomizer.nextInt(mCanvasHeight);
            }     
            linearThreshold = myRandomizer.nextInt(30)+5;
        }
        private void randomizeColors()
        {
        	backgroundPaint = new Paint();
        	foregroundPaint = new Paint();
        	backgroundPaint.setColor(Color.rgb(myRandomizer.nextInt(256),myRandomizer.nextInt(256),myRandomizer.nextInt(256)));
        	foregroundPaint.setColor(Color.rgb(myRandomizer.nextInt(256),myRandomizer.nextInt(256),myRandomizer.nextInt(256)));
        }
        @Override
        public void run() {
        	doStart();

            while (mRun) {
                Canvas c = null;
                try {
                    //c = mSurfaceHolder.lockCanvas();
                    synchronized (syncObject) {

                    	if (justReset)
                    	{
                    		c = mSurfaceHolder.lockCanvas();


                            for (int i = 0; i < mCanvasWidth; i++)
                        	{
                        		drawValue(i, c);
                        	}
                            mSurfaceHolder.unlockCanvasAndPost(c);
                            c = null;
                    		justReset = false;
                     	}
                    	quickSortPriority();
                    }
                } catch (NullPointerException ignored) {
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }



        public void setRunning(boolean b) {
            mRun = b;
        }


        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (syncObject) {
                mCanvasWidth = (width-1)/2;
                if (mCanvasWidth > 4000)
                {
                	mCanvasWidth = 4000;
                }
                mCanvasHeight = height;
                resetSort();
            }
        }


        private void swapValues(int pos1, int pos2)
        {
        	Integer temp;
        	temp = values[pos1];
        	values[pos1] = values[pos2];
        	values[pos2] = temp;
        	drawValue(pos1, null);
        	drawValue(pos2, null);

        }
        private void drawValue(int pos1, Canvas c) {
            Boolean madeCanvas = false;
            Canvas useCanvas = c;

            if (useCanvas == null) {
                Rect littleRect = new Rect(2 * pos1, 0, 2 * pos1 + 1, mCanvasHeight);
                useCanvas = mSurfaceHolder.lockCanvas(littleRect);
                madeCanvas = true;
            }

                Rect bottomRect = new Rect(2 * pos1, 0, 2 * pos1 + 1, values[pos1]);
                useCanvas.drawRect(bottomRect, backgroundPaint);
                Rect topRect = new Rect(2 * pos1, values[pos1], 2 * pos1 + 1, mCanvasHeight);
                useCanvas.drawRect(topRect, foregroundPaint);

            if (madeCanvas) {
                mSurfaceHolder.unlockCanvasAndPost(useCanvas);
            }

        }
        
        private void singleSortStep()
        {
        	if (highwater > 1)
        	{
        		if (curPos == highwater)
        		{
        			curPos = 0;
        			highwater--;
        		}
        		else
        		{
        			if (values[curPos] > values [curPos+1])
        			{
        				swapValues(curPos,curPos+1);
        			}
        			curPos++;
        		}
        	}
        }
      
        
        public class SortRangeSizeComparator implements Comparator<sortRange>
        {
        	public int compare(sortRange range1, sortRange range2)
        	{
        		if ( range1.size > range2.size)
        		{
        			return -1;
        		}
        		else
        			
        		{
        			return 1;
        		}
        	}
        }
        public class SortRangePositionComparator implements Comparator<sortRange>
        {
        	public int compare(sortRange range1, sortRange range2)
        	{
        		if ( range1.start < range2.start)
        		{
        			return -1;
        		}
        		else
        			
        		{
        			return 1;
        		}
        	}
        }
        private class sortRange{
        	public int start;
        	public int end;
        	public int size;
        	sortRange(int inStart, int inEnd)
        	{
        		start = inStart;
        		end = inEnd;
        		size = end - start;
        	}
        }
        private List<sortRange> sortList ;
        private PriorityQueue<sortRange> sortQueue;
        private PriorityQueue<sortRange> smallSortQueue;
        private boolean doingPartition;
        private int pivot;
        private int low;
        private int partitionLow;
        private int partitionHigh;
        private int high;
        int linearThreshold;
        private void resetQuickSort()
        {
        	Comparator<sortRange> rangeComparator = new SortRangeSizeComparator();
        	Comparator<sortRange> positionComparator = new SortRangePositionComparator();
            sortQueue = new PriorityQueue<sortRange>(10, rangeComparator);
            smallSortQueue = new PriorityQueue<sortRange>(10, positionComparator);
            sortList = new ArrayList<sortRange>();
                
            sortQueue.add(new sortRange(0,mCanvasWidth-1));
            doingPartition = false;
        	
        }
        private void quickSortPriority()
        {
        	if (doingPartition)
        	{
                low++;
                while ( low< partitionHigh && values[low] < pivot)
                    low++;
                high--;
                while (high>partitionLow && values[high] > pivot)
                	high--;

                if (low < high)
                {
                    swapValues(low, high);
                }
                else
                {
            		addToQueue(partitionLow,high);
            		addToQueue(high + 1,partitionHigh);
            		doingPartition = false;
                }

        	}
        	else
        	{
        		if (sortQueue.isEmpty() && smallSortQueue.isEmpty())
        		{
        			resetSort();
        		}
        		else
        		{
        			sortRange thisRange;
        			if (!sortQueue.isEmpty())
        			{
        				thisRange = sortQueue.remove();
        			}
        			else
        			{
        				thisRange = smallSortQueue.remove();
        			}
        			startPartition(thisRange.start,thisRange.end);

        		}
        	}

            
        }
        private void startPartition(int p, int r)
        {
        	pivot = values[p];
        	partitionLow = p;
        	partitionHigh = r;
        	low = p-1;
        	high = r+1;
        	doingPartition = true;
        }
        private void quickSortQueue(int p, int r)
        {
        	if (p<r)
        	{
        		int q = partition(p,r);
        		addToQueue(p,q);
        		addToQueue(q+1,r);
        	}
        }
        
        private void addToQueue(int i, int j)
        {
        	if (i < j)
        	{
        		if (j-i < linearThreshold)
        		{
        			smallSortQueue.add(new sortRange(i,j));
        		}
        		else
        		{
        			sortQueue.add(new sortRange(i,j));
        		}
        	}
        }

        private void quickSortRandom(int p, int r)
        {
        	sortList = new ArrayList<sortRange>();
        	
        	sortList.add(new sortRange(p,r));
        	while (!sortList.isEmpty())
        	{
        		int item = myRandomizer.nextInt(sortList.size());
        		sortRange thisRange = sortList.get(item);
        		sortList.remove(item);
        		quickSortRange(thisRange.start,thisRange.end);
        		
        	}
        	
        }
        private void quickSortRange(int p, int r)
        {
        	if (p<r)
        	{
        		int q = partition(p,r);
        		sortList.add(new sortRange(p,q));
        		sortList.add(new sortRange(q+1,r));
        		
        	}
        }
        private void quickSort(int p, int r)
        {
            if(p<r)
            {
                int q=partition(p,r);
                //if ( (q-p) > (r - q))
                if (myRandomizer.nextInt(1000) > 500)
                //if (p > (mCanvasWidth -r))
                //if (q > mCanvasWidth/2)
                {
                	quickSort(p,q);
                	quickSort(q+1,r);
                }
                else
                {
                	quickSort(q+1,r);
                	quickSort(p,q);
                }
            }
        }
        
        private void quickSort2(int startPos, int endPos)
        {
        	if (startPos >= endPos)
        	{
        		return;
        	}
        	
        	int pos1 = startPos;
        	int pos2 = endPos;
        	
        	while (pos1 != pos2)
        	{
        		if (values[pos1] >= values[pos2])
        		{
        			swapValues(pos1,pos2);
        			pos1++;
        		}
        		else
        		{
        			pos2--;
        		}
        		
        	}
        	quickSort2(startPos, pos1 - 1);
        	quickSort2(pos1 + 1, endPos);
        }
        
        private int partition(int p, int r) {

            int x = values[p];
            int i = p-1 ;
            int j = r+1 ;

            while (true) {
                i++;
                while ( i< r && values[i] < x)
                    i++;
                j--;
                while (j>p && values[j] > x)
                    j--;

                if (i < j)
                    swapValues(i, j);
                else
                    return j;
            }
        }
    }

            
    private Random myRandomizer;
    private Integer[] values;
    private int highwater;
    private int curPos;
    boolean justReset;

    /** The thread that actually draws the animation */
    private SortingChaosThread thread;

    private Context context;
    
    public SortingChaosView(Context inContext, AttributeSet attrs) {
        super(inContext, attrs);
    	context = inContext;

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        setFocusable(true); // make sure we get key events
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     * 
     * @return the animation thread
     */
    public SortingChaosThread getThread() {
        return thread;
    }

    public boolean onTouchEvent(MotionEvent motion)
    {
    	return true;
    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        thread.setSurfaceSize(width, height);
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {

        thread = new SortingChaosThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
            }
        });
        thread.setRunning(true);
        thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
    


    /*
     * Member (state) fields
     */

    private int mCanvasHeight = 1;
    private int mCanvasWidth = 1;
    private boolean mRun = false;
    private SurfaceHolder mSurfaceHolder;
    
    private final Integer syncObject = 1;
    

}
