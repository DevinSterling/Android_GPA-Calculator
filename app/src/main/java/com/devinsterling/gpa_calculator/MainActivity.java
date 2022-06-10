// Devin Sterling
// 2022-06-06
// GPA Calculator

package com.devinsterling.gpa_calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private STATE currentState = STATE.INPUT;
    private LinearLayout courses; // Possesses user input/grades
    private Button buttonCalculate;
    private TextView textViewGPA;

    /* Used for input validation */
    private enum VALIDATION {
        INVALID_EMPTY,  // Empty input
        INVALID_UNDER,  // input is under the range [0-100]
        INVALID_OVER,   // input is over the range
        VALID           // input is within the range
    }

    /* Used to determine current application state  */
    private enum STATE {
        RESET,  // The app is awaiting for a form reset or alteration
        INPUT   // The app is awaiting user input
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        courses = (LinearLayout) findViewById(R.id.courses);
        buttonCalculate = (Button) findViewById(R.id.btn_calculate);
        textViewGPA = (TextView) findViewById(R.id.textGPA);

        for (int i = 0; i < 5; i++) createCourse();
    }

    /* Adds new course to layout/UI */
    public void createCourse() {
        String courseName = getString(R.string.course_name, courses.getChildCount() + 1);

        View.inflate(this, R.layout.course_input, courses);
        LinearLayout container = (LinearLayout) courses.getChildAt(courses.getChildCount()-1);
        TextView textView = (TextView) container.getChildAt(0);
        EditText editText = (EditText) container.getChildAt(1);
        textView.setText(courseName);

        /*  Event Handling
            Detect if user input is valid  */
        editText.setOnKeyListener((view, keyCode, event) -> {
            String userInput = editText.getText().toString();

            // Request state
            requestState(STATE.INPUT);

            // Check if given string is negative and remove
            if (userInput.contains("-")) {
                userInput = userInput.replace("-", "");
                editText.setText(userInput);
            }

            // Validation check
            VALIDATION state = validateInput(userInput);

            // Change background color depending on validation
            if (state == VALIDATION.VALID) container.setBackgroundResource(R.drawable.gpa_input_box);
            else container.setBackgroundResource(R.drawable.gpa_input_box_invalid);

            return false;
        });
        /* Check if user input is empty upon losing focus (primarily checks for empty input) */
        editText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                // Validation check
                VALIDATION state = validateInput(editText.getText().toString());

                // Change background color depending on validation
                if (state != VALIDATION.VALID) container.setBackgroundResource(R.drawable.gpa_input_box_invalid);
            }
        });
    }

    /* Adds course when user chooses to do so */
    public void addCourse(View view) {
        if (courses.getChildCount() == 0) buttonCalculate.setVisibility(View.VISIBLE);
        createCourse();

        /* Request input state */
        requestState(STATE.INPUT);
    }

    /* Remove user selected course using selected view as reference */
    public void removeCourse(View view) {
        LinearLayout container = (LinearLayout) view.getParent();
        courses.removeView(container);
        renameCourses();

        /* Request input state */
        requestState(STATE.INPUT);

        /* Remove calculate button if there are no courses */
        if (courses.getChildCount() == 0) buttonCalculate.setVisibility(View.GONE);
    }

    /* Rename courses when user deletes a course from any location.
       This ensures the numbering of courses stay chronological. */
    public void renameCourses() {
        for (int i = 0; i < courses.getChildCount(); i++) {
            LinearLayout container = (LinearLayout) courses.getChildAt(i);
            TextView textView = (TextView) container.getChildAt(0);

            // Check if course name needs to be replaced
            if (!textView.getText().toString().equals(getString(R.string.course_name, i + 1))) {
                String courseName = getString(R.string.course_name, i + 1);
                textView.setText(courseName);
            }
        }
    }

    public void clearGPA(View view) {
        // Change Visibility
        textViewGPA.setVisibility(View.GONE);

        requestState(STATE.INPUT);

        // Reset User Input
        for (int i = 0; i < courses.getChildCount(); i++) {
            LinearLayout container = (LinearLayout) courses.getChildAt(i);
            EditText editText = (EditText) container.getChildAt(1);

            editText.setText("");
        }
    }

    /* Calculates the gpa from user given input.
       In addition, root background changes depending on the gpa value. */
    public void calculateGPA(View view) {
        View root = findViewById(android.R.id.content).getRootView();
        boolean badInput = false;
        String outcome = "";
        int gpa = 0;

        for (int i = 0; i < courses.getChildCount(); i++) {
            // Get user input from view group
            LinearLayout container = (LinearLayout) courses.getChildAt(i);
            EditText editText = (EditText) container.getChildAt(1);
            String userInput = editText.getText().toString();

            // Validation check
            VALIDATION state = validateInput(userInput);

            if (state == VALIDATION.VALID) gpa += Integer.parseInt(userInput);
            else {
                // Check if input is empty
                if (state == VALIDATION.INVALID_EMPTY) {
                    badInput = true;
                    outcome = getString(R.string.err_gpa_empty, i + 1);
                    break;
                }
                // Check range
                else if (state == VALIDATION.INVALID_OVER || state == VALIDATION.INVALID_UNDER) {
                    badInput = true;
                    outcome = getString(R.string.err_gpa_range, i + 1);
                    break;
                }

                editText.requestFocus();
            }
        }

        // Finish calculating GPA and change background color
        if (!badInput) {
            gpa /= courses.getChildCount();
            outcome = getString(R.string.gpa_outcome, courses.getChildCount(), gpa);

            // Change Visibility
            textViewGPA.setText(outcome);
            textViewGPA.setVisibility(View.VISIBLE);

            // Request reset State
            requestState(STATE.RESET);

            // Change root background depending on GPA value
            if (gpa < 60)  root.setBackgroundColor(Color.parseColor("#FFCCCC"));
            else if (gpa <= 79) root.setBackgroundColor(Color.parseColor("#FFFFCC"));
            else root.setBackgroundColor(Color.parseColor("#CCFFCC"));
        }
        else Toast.makeText(this, outcome, Toast.LENGTH_SHORT).show();
    }

    public VALIDATION validateInput(String userInput) {
        if (userInput.isEmpty()) return VALIDATION.INVALID_EMPTY;
        else if (userInput.contains("-"))  return VALIDATION.INVALID_UNDER;

        try {
            int grade = Integer.parseInt(userInput);
            if (grade >= 0 && grade <= 100) return VALIDATION.VALID;
            else return VALIDATION.INVALID_OVER;
        } catch (NumberFormatException e) { // Prevents app crashing on inputs greater than 999999999
            return VALIDATION.INVALID_OVER;
        }
    }

    public void requestState(STATE state) {
        // Check if requested state is current already
        if (currentState == state) return;
        else currentState = state;

        switch (state) {
            case RESET:
                // Change listener
                buttonCalculate.setText(R.string.reset_gpa);
                buttonCalculate.setOnClickListener(this::clearGPA);
                break;
            case INPUT:
                // Change Listener
                buttonCalculate.setText(R.string.calculate_gpa);
                buttonCalculate.setOnClickListener(this::calculateGPA);

                // Replace color of previous root Background
                findViewById(android.R.id.content).getRootView().setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
    }
}