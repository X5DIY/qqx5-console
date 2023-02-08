package qqx5;

class SetInfoException extends Exception {
    private String message;

    SetInfoException(int state) {
        switch (state) {
            case 1:
                message = "Mode is not correct.";
                break;
            case 2:
                message = "Can not find getBox type.";
                break;
            case 3:
                message = "In idol_ConvertToInt, track is not correct.";
                break;
            case 4:
                message = "In pinball_ConvertToInt, track is not correct.";
                break;
            case 5:
                message = "6 or over notes in bubble at one time.";
                break;
            case 6:
                message = "In idol_ConvertToString, track is not 0-4.";
                break;
            case 7:
                message = "In pinball_ConvertToString, track is not 0-2.";
                break;
            case 8:
                message = "6 or over notes in crescent at one time.";
                break;
        }

    }

    String warnMess() {
        return message;
    }
}
