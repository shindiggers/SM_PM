package com.example.smmoney.views;

import android.os.Handler;

public interface HandlerActivity {
    int MSG_ANIMATEBALANCEBAR = 3;
    int MSG_ERROR = 6;
    int MSG_PROGRESS_FINISH = 5;
    int MSG_PROGRESS_UPDATE = 4;
    int MSG_REPORT_DONE = 1;
    int MSG_REPORT_TIME = 2;

    Handler getHandler();
}
