package com.iflytek.asr;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Slot class
 */
public class Slot implements Parcelable
{
	public final int mItemCount;
	public final int mItemIds[];
	public final String mItemTexts[];
	public Slot(int itemCount,int itemIds[],String itemTexts[]) {
		mItemCount = itemCount;
		mItemIds = itemIds;		
		mItemTexts = itemTexts;
    }
	

	public int describeContents() 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mItemCount);
		dest.writeIntArray(mItemIds);
		dest.writeStringArray(mItemTexts);	
	}		
	
	public Slot(Parcel in) 
	{
		mItemCount = in.readInt();		
		mItemIds = new int[mItemCount];
		mItemTexts = new String[mItemCount];
		in.readIntArray(mItemIds);
		in.readStringArray(mItemTexts);	
	}
	
	public static final Parcelable.Creator<Slot> CREATOR =
           new Parcelable.Creator<Slot>() {

               public Slot createFromParcel(Parcel in) {
                   return new Slot(in);
               }
       
               public Slot[] newArray(int size) {
                   return new Slot[size];
               }
	};
}