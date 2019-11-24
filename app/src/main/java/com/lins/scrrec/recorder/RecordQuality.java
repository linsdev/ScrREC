package com.lins.scrrec.recorder;

import com.lins.scrrec.R;

public enum RecordQuality {
    Low    (R.id.radioBtnLow),
    Medium (R.id.radioBtnMedium),
    High   (R.id.radioBtnHigh);

    private int id;

    public int getID() {
        return id;
    }

    public static RecordQuality fromID(int id) {
        if (id == R.id.radioBtnHigh)
            return High;
        else if (id == R.id.radioBtnLow)
            return Low;
        else
            return Medium;
    }

    RecordQuality(int id) {
        this.id = id;
    }
}