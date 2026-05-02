package com.app.toolbox;

import android.app.Activity;

import com.lib.version.checker.AbstractVersionChecker;

public class UpdateChecker extends AbstractVersionChecker {
    protected UpdateChecker(Activity activity) {
        super(activity);
    }

    @Override
    protected String NewVersionWebpageUrl() {
        return "https://github.com/iasonasTan/Toolbox/releases";
    }

    @Override
    protected String latestVersionFileWebUrl() {
        return "https://raw.githubusercontent.com/iasonasTan/Toolbox/master/latest-version.txt";
    }
}
