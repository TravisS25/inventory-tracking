package expert.codinglevel.inventory_tracking.view;

import android.os.Parcel;
import android.os.Parcelable;


public class TextValue implements Parcelable {
    private String mValue;
    private String mText;

    public TextValue(String text, String value){
        mText = text;
        mValue = value;
    }

    private TextValue(Parcel in){
        mText = in.readString();
        mValue = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getText());
        dest.writeString(getValue());
    }

    public static final Parcelable.Creator<TextValue> CREATOR
            = new Parcelable.Creator<TextValue>() {
        public TextValue createFromParcel(Parcel in) {
            return new TextValue(in);
        }

        public TextValue[] newArray(int size) {
            return new TextValue[size];
        }
    };

    public String getValue(){ return mValue; }
    public String getText(){ return mText; }

    @Override
    public String toString(){
        return mText;
    }
}
