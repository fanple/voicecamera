package com.iflytek.asr;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * RecognitionResult is a passive object that stores a single recognized query
 * and its search result.
 * 
 * TODO: Revisit and improve this class, reconciling the different types of actions and
 * the different ways they are represented. Maybe we should have a separate result object
 * for each type, and put them (type/value) in bundle?
 * {@hide}
 */
public class RecognitionResult implements Parcelable
{
	  /**
     * Status of the recognize request.
     */
    public static final int NETWORK_TIMEOUT = 1; // Network operation timed out.

    public static final int NETWORK_ERROR = 2; // Other network related errors.

    public static final int AUDIO_ERROR = 3; // Audio recording error.

    public static final int ASR_ERROR = 4; // Server sends error status.

    public static final int CLIENT_ERROR = 5; // Other client side errors.

    public static final int SPEECH_TIMEOUT = 6; // No speech input

    public static final int NO_MATCH = 7; // No recognition result matched.

    public static final int ASR_BUSY = 8; // RecognitionService busy.
    
    public static final int RESPONSE_TIMEOUT = 9; // User response timeout.

    /**
     * Type of the recognition results.
     */
    public static final int RAW_RECOGNITION_RESULT = 0;

    public static final int WEB_SEARCH_RESULT = 1;

    public static final int CONTACT_RESULT = 2;
    
    public static final int ACTION_RESULT = 3;

	
	public static final Parcelable.Creator<RecognitionResult> CREATOR =
           new Parcelable.Creator<RecognitionResult>() {

               public RecognitionResult createFromParcel(Parcel in) {
                   return new RecognitionResult(in);
               }
       
               public RecognitionResult[] newArray(int size) {
                   return new RecognitionResult[size];
               }
	};

	public RecognitionResult(Parcel in) {
		mSentenceId = in.readInt();
		mConfidence = in.readInt();
		mSlot = in.readInt();
		mSlotList = new ArrayList<Slot>();
		in.readList(mSlotList, Slot.class.getClassLoader());
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mSentenceId);
		out.writeInt(mConfidence);
		out.writeInt(mSlot);
		out.writeList(mSlotList);
	} 

	public RecognitionResult(int sentendId,
			int confidence,int slots) {
		mSentenceId = sentendId;
		mConfidence = confidence;		
		mSlot = slots;
		mSlotList = new ArrayList<Slot>();
    }
	
	/**
	 * Insert a slot to mSlotList
	 */
	public void AddSlot(int itemCount,int itemIds[],String itemTexts[]){
		mSlotList.add(  new Slot(itemCount,itemIds,itemTexts));
	}
	
	@Override
	public String toString() {
	        return "[mSentenceId=" + mSentenceId + ", mConfidence="+ mConfidence +", mSlot=" + mSlot 
	        	+ ", mSlotList=" + mSlotList.size() + "]";
	}
	/**
	 * Sentence ID
	 */
	public final int mSentenceId;
	/**
	 * Score of Confidence 1-100
	 */
	public final int mConfidence;	
	/**
	 * Slot count of this sentence
	 */
	public final int mSlot;
	/**
	 * Recognize result array of the slot list
	 */
	public final List<Slot> mSlotList;
	
	
}
