package expert.codinglevel.hospital_inventory.interfaces;

import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

import expert.codinglevel.hospital_inventory.view.TextValue;

public interface ICascadeResponse {
    void processFinish(
            ViewGroup viewGroup,
            HashMap<String, Integer> widgetPosition,
            HashMap<String, ArrayList<TextValue>> result
    );
}
